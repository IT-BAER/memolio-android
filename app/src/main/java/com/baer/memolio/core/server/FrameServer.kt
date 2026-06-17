package com.baer.memolio.core.server

import com.baer.memolio.core.data.AlbumRepository
import com.baer.memolio.core.media.MediaImporter
import io.ktor.http.ContentType
import io.ktor.http.HttpStatusCode
import io.ktor.http.content.PartData
import io.ktor.http.content.forEachPart
import io.ktor.serialization.kotlinx.json.json
import io.ktor.server.application.Application
import io.ktor.server.application.call
import io.ktor.server.application.install
import io.ktor.server.cio.CIO
import io.ktor.server.engine.EmbeddedServer
import io.ktor.server.engine.embeddedServer
import io.ktor.server.plugins.contentnegotiation.ContentNegotiation
import io.ktor.server.request.receiveMultipart
import io.ktor.server.response.respond
import io.ktor.server.response.respondBytes
import io.ktor.server.response.respondText
import io.ktor.server.routing.get
import io.ktor.server.routing.post
import io.ktor.server.routing.routing
import io.ktor.utils.io.jvm.javaio.toInputStream
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.first
import kotlinx.serialization.Serializable
import java.io.IOException
import java.io.InputStream

/** Loads a bundled asset (e.g. upload.html) as a stream. */
fun interface AssetLoader {
    fun open(path: String): InputStream
}

/**
 * Returns the currently valid upload token. Per shared-contract addendum C the production
 * implementation must reflect [SettingsRepository.rotateToken] immediately, so it caches a
 * `@Volatile` value synced from `appSettings` (not a one-shot read). See [SettingsTokenProvider].
 */
fun interface TokenProvider {
    fun current(): String
}

/** Everything the routes need, injected so they are testable without the CIO host. */
class FrameServerDeps(
    val tokenProvider: TokenProvider,
    val importer: MediaImporter,
    val albums: AlbumRepository,
    val assetLoader: AssetLoader
)

@Serializable
private data class AlbumDto(val id: String, val name: String)

@Serializable
private data class UploadResponse(val status: String, val message: String, val id: String? = null)

/** Free pool / default album id (shared-contract addendum F). */
private const val DEFAULT_ALBUM_ID = "all"

private val ALLOWED_EXTS = setOf("jpg", "jpeg", "png", "heic", "heif", "webp")

/** Installs all Memolio routes on this application. Shared by the CIO host and tests. */
fun Application.frameRoutes(deps: FrameServerDeps) {
    install(ContentNegotiation) { json() }

    routing {
        get("/") {
            if (!validToken(call.request.queryParameters["t"], deps)) {
                call.respondText("Forbidden", status = HttpStatusCode.Forbidden); return@get
            }
            val html = deps.assetLoader.open("upload.html").use { it.readBytes() }
            call.respondBytes(html, ContentType.Text.Html, HttpStatusCode.OK)
        }

        get("/album-list") {
            if (!validToken(call.request.queryParameters["t"], deps)) {
                call.respondText("Forbidden", status = HttpStatusCode.Forbidden); return@get
            }
            val list = deps.albums.observeAlbums().first().map { AlbumDto(it.id, it.name) }
            call.respond(list)
        }

        post("/upload") {
            if (!validToken(call.request.queryParameters["t"], deps)) {
                call.respondText("Forbidden", status = HttpStatusCode.Forbidden); return@post
            }
            var album = ""
            var caption: String? = null
            var fileBytes: ByteArray? = null
            var fileName = "upload"

            call.receiveMultipart().forEachPart { part ->
                when (part) {
                    is PartData.FormItem -> when (part.name) {
                        "album" -> album = part.value
                        "caption" -> caption = part.value.ifBlank { null }
                    }
                    is PartData.FileItem -> {
                        fileName = part.originalFileName ?: fileName
                        fileBytes = part.provider().toInputStream().use { it.readBytes() }
                    }
                    else -> {}
                }
                part.release()
            }

            val bytes = fileBytes
            if (bytes == null || bytes.isEmpty()) {
                call.respond(HttpStatusCode.BadRequest, UploadResponse("error", "No file received"))
                return@post
            }
            val ext = fileName.substringAfterLast('.', "").lowercase()
            if (ext.isEmpty() || ext !in ALLOWED_EXTS) {
                call.respond(
                    HttpStatusCode.UnsupportedMediaType,
                    UploadResponse("error", "Unsupported file type")
                )
                return@post
            }
            val albumId = album.ifBlank { DEFAULT_ALBUM_ID }

            try {
                val result = deps.importer.import(
                    bytes = bytes,
                    ext = ext,
                    albumId = albumId,
                    caption = caption,
                    sourceDevice = null,
                    now = System.currentTimeMillis()
                )
                when (result) {
                    is MediaImporter.ImportResult.Added ->
                        call.respond(HttpStatusCode.OK, UploadResponse("added", "Added", result.id))
                    MediaImporter.ImportResult.Duplicate ->
                        call.respond(
                            HttpStatusCode.OK,
                            UploadResponse("duplicate", "Already on the frame — skipped")
                        )
                    is MediaImporter.ImportResult.Rejected ->
                        call.respond(
                            HttpStatusCode.UnsupportedMediaType,
                            UploadResponse("error", result.reason)
                        )
                }
            } catch (_: IOException) {
                call.respond(
                    HttpStatusCode.InsufficientStorage,
                    UploadResponse("error", "Frame storage full")
                )
            }
        }
    }
}

private fun validToken(provided: String?, deps: FrameServerDeps): Boolean {
    val expected = deps.tokenProvider.current()
    return expected.isNotEmpty() && provided == expected
}

/**
 * Thin CIO host. Owns lifecycle + the resolved LAN [address]; routing is delegated to
 * [frameRoutes] so it is shared with tests. Disk-full (IOException) -> 507 is handled in
 * the routes. Binds `0.0.0.0:<port>` so any LAN client can reach it.
 */
class FrameServer(
    private val deps: FrameServerDeps,
    private val ipResolver: () -> String? = NetworkAddress::currentIpv4
) {
    private var engine: EmbeddedServer<*, *>? = null

    private val _address = MutableStateFlow<String?>(null)
    val address: StateFlow<String?> = _address.asStateFlow()

    fun start(port: Int) {
        if (engine != null) return
        engine = embeddedServer(CIO, port = port, host = "0.0.0.0") {
            frameRoutes(deps)
        }.also { it.start(wait = false) }
        _address.value = ipResolver()?.let { "$it:$port" }
    }

    fun stop() {
        engine?.stop(gracePeriodMillis = 200, timeoutMillis = 1000)
        engine = null
        _address.value = null
    }
}
