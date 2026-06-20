package com.baer.memolio.core.server

import androidx.room.Room
import androidx.test.core.app.ApplicationProvider
import app.cash.turbine.test
import com.baer.memolio.core.data.AlbumRepository
import com.baer.memolio.core.data.AlbumRepositoryImpl
import com.baer.memolio.core.data.PhotoRepository
import com.baer.memolio.core.data.PhotoRepositoryImpl
import com.baer.memolio.core.database.MemolioDatabase
import com.baer.memolio.core.media.MediaImporter
import com.baer.memolio.core.media.Transcoder
import com.baer.memolio.core.media.Transcoder.Decoded
import com.baer.memolio.core.model.Album
import com.baer.memolio.core.storage.FileStorage
import com.google.common.truth.Truth.assertThat
import io.ktor.client.request.forms.MultiPartFormDataContent
import io.ktor.client.request.forms.formData
import io.ktor.client.request.get
import io.ktor.client.request.post
import io.ktor.client.request.setBody
import io.ktor.client.statement.bodyAsText
import io.ktor.http.Headers
import io.ktor.http.HttpHeaders
import io.ktor.http.HttpStatusCode
import io.ktor.server.testing.ApplicationTestBuilder
import io.ktor.server.testing.testApplication
import kotlinx.coroutines.test.UnconfinedTestDispatcher
import kotlinx.coroutines.test.runTest
import org.junit.After
import org.junit.Before
import org.junit.Rule
import org.junit.Test
import org.junit.rules.TemporaryFolder
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import java.io.File
import java.io.IOException

@RunWith(RobolectricTestRunner::class)
class FrameServerTest {
    @get:Rule val tmp = TemporaryFolder()

    private lateinit var db: MemolioDatabase
    private lateinit var photos: PhotoRepository
    private lateinit var albums: AlbumRepository
    private lateinit var storage: FileStorage
    private lateinit var importer: MediaImporter

    private val token = "secret123"

    private class FakeTranscoder : Transcoder {
        override fun readBounds(source: File): Decoded? = Decoded(2000, 1500)
        override fun writeDownscaledJpeg(source: File, dest: File, maxEdge: Int, quality: Int): Decoded {
            dest.parentFile?.mkdirs(); dest.writeBytes(byteArrayOf(0xFF.toByte(), 0xD8.toByte()))
            return Decoded(maxEdge, (maxEdge * 0.75).toInt())
        }
    }

    @Before
    fun setup() = runTest {
        db = Room.inMemoryDatabaseBuilder(
            ApplicationProvider.getApplicationContext(),
            MemolioDatabase::class.java
        ).allowMainThreadQueries().build()
        val dispatcher = UnconfinedTestDispatcher()
        photos = PhotoRepositoryImpl(db.photoDao(), dispatcher)
        albums = AlbumRepositoryImpl(db.albumDao(), dispatcher)
        storage = FileStorage(tmp.root)
        importer = MediaImporter(FakeTranscoder(), storage, photos, dispatcher)
        albums.upsert(Album("a1", "All", null, 0L, 0))
    }

    @After
    fun teardown() = db.close()

    private fun ApplicationTestBuilder.installRoutes(
        importerOverride: MediaImporter = importer,
        currentToken: () -> String = { token },
        events: UploadEventBus = UploadEventBus()
    ) = application {
        frameRoutes(
            FrameServerDeps(
                tokenProvider = TokenProvider { currentToken() },
                importer = importerOverride,
                albums = albums,
                assetLoader = { "<!DOCTYPE html><html><body>upload</body></html>".byteInputStream() },
                uploadEvents = events
            )
        )
    }

    private fun jpegPart(name: String, bytes: ByteArray) = MultiPartFormDataContent(
        formData {
            append("album", "a1")
            append("caption", "trip")
            append(
                "photo", bytes,
                Headers.build {
                    append(HttpHeaders.ContentType, "image/jpeg")
                    append(HttpHeaders.ContentDisposition, "filename=\"$name\"")
                }
            )
        }
    )

    @Test
    fun rootWithoutTokenIs403() = testApplication {
        installRoutes()
        val res = client.get("/")
        assertThat(res.status).isEqualTo(HttpStatusCode.Forbidden)
    }

    @Test
    fun rootWithTokenServesUploadPage() = testApplication {
        installRoutes()
        val res = client.get("/?t=$token")
        assertThat(res.status).isEqualTo(HttpStatusCode.OK)
        assertThat(res.bodyAsText()).contains("upload")
    }

    @Test
    fun uploadWithoutTokenIs403() = testApplication {
        installRoutes()
        val res = client.post("/upload") { setBody(jpegPart("a.jpg", byteArrayOf(1, 2, 3))) }
        assertThat(res.status).isEqualTo(HttpStatusCode.Forbidden)
    }

    @Test
    fun uploadWithWrongTokenIs403() = testApplication {
        installRoutes()
        val res = client.post("/upload?t=nope") { setBody(jpegPart("a.jpg", byteArrayOf(1, 2, 3))) }
        assertThat(res.status).isEqualTo(HttpStatusCode.Forbidden)
    }

    @Test
    fun uploadWithTokenCreatesPhotoRow() = testApplication {
        installRoutes()
        val res = client.post("/upload?t=$token") { setBody(jpegPart("a.jpg", "img-bytes".toByteArray())) }
        assertThat(res.status).isEqualTo(HttpStatusCode.OK)
        assertThat(res.bodyAsText()).contains("added")
        photos.observePhotos("a1").test {
            assertThat(awaitItem()).hasSize(1)
        }
    }

    @Test
    fun uploadWithoutAlbumPartDefaultsToAllAlbum() = testApplication {
        // Default free pool album "all" (shared-contract addendum F) must exist for the
        // FK on Photo.albumId; the route falls back to it when no album part is sent.
        albums.upsert(Album("all", "All Photos", null, 0L, 0))
        val noAlbumPart = MultiPartFormDataContent(
            formData {
                append(
                    "photo", "no-album-bytes".toByteArray(),
                    Headers.build {
                        append(HttpHeaders.ContentType, "image/jpeg")
                        append(HttpHeaders.ContentDisposition, "filename=\"b.jpg\"")
                    }
                )
            }
        )
        installRoutes()
        val res = client.post("/upload?t=$token") { setBody(noAlbumPart) }
        assertThat(res.status).isEqualTo(HttpStatusCode.OK)
        photos.observePhotos("all").test {
            assertThat(awaitItem()).hasSize(1)
        }
    }

    @Test
    fun successfulUploadPublishesAddedEvent() = testApplication {
        val bus = UploadEventBus()
        installRoutes(events = bus)
        bus.events.test {
            val res = client.post("/upload?t=$token") { setBody(jpegPart("a.jpg", "ev-bytes".toByteArray())) }
            assertThat(res.status).isEqualTo(HttpStatusCode.OK)
            assertThat(awaitItem()).isEqualTo(UploadOutcome.ADDED)
        }
    }

    @Test
    fun duplicateUploadPublishesDuplicateEvent() = testApplication {
        val bus = UploadEventBus()
        installRoutes(events = bus)
        val bytes = "dup-bytes".toByteArray()
        client.post("/upload?t=$token") { setBody(jpegPart("a.jpg", bytes)) }
        bus.events.test {
            client.post("/upload?t=$token") { setBody(jpegPart("a.jpg", bytes)) }
            assertThat(awaitItem()).isEqualTo(UploadOutcome.DUPLICATE)
        }
    }

    @Test
    fun duplicateUploadSkipsWithFriendlyMessage() = testApplication {
        installRoutes()
        val bytes = "same-bytes".toByteArray()
        val first = client.post("/upload?t=$token") { setBody(jpegPart("a.jpg", bytes)) }
        val second = client.post("/upload?t=$token") { setBody(jpegPart("a.jpg", bytes)) }
        assertThat(first.status).isEqualTo(HttpStatusCode.OK)
        assertThat(second.status).isEqualTo(HttpStatusCode.OK)
        assertThat(second.bodyAsText()).contains("duplicate")
    }

    @Test
    fun unsupportedTypeIsRejected() = testApplication {
        val rejecting = MediaImporter(
            object : Transcoder {
                override fun readBounds(source: File): Decoded? = null
                override fun writeDownscaledJpeg(source: File, dest: File, maxEdge: Int, quality: Int) =
                    error("unused")
            },
            storage, photos, UnconfinedTestDispatcher()
        )
        installRoutes(importerOverride = rejecting)
        val res = client.post("/upload?t=$token") {
            setBody(jpegPart("notes.txt", "plain text".toByteArray()))
        }
        assertThat(res.status).isEqualTo(HttpStatusCode.UnsupportedMediaType)
        // Stable machine code the browser page maps to a localized string.
        assertThat(res.bodyAsText()).contains("unsupported")
    }

    @Test
    fun diskFullYields507() = testApplication {
        // Transcoder that fails like a full disk during cache write; the IOException
        // propagates out of MediaImporter.import and the route maps it to 507.
        val diskFull = MediaImporter(
            object : Transcoder {
                override fun readBounds(source: File): Decoded? = Decoded(2000, 1500)
                override fun writeDownscaledJpeg(source: File, dest: File, maxEdge: Int, quality: Int): Decoded =
                    throw IOException("No space left on device")
            },
            storage, photos, UnconfinedTestDispatcher()
        )
        installRoutes(importerOverride = diskFull)
        val res = client.post("/upload?t=$token") { setBody(jpegPart("a.jpg", "x".toByteArray())) }
        assertThat(res.status).isEqualTo(HttpStatusCode.InsufficientStorage)
        assertThat(res.bodyAsText()).contains("storage_full")
    }

    @Test
    fun albumListWithTokenReturnsJson() = testApplication {
        installRoutes()
        val res = client.get("/album-list?t=$token")
        assertThat(res.status).isEqualTo(HttpStatusCode.OK)
        assertThat(res.bodyAsText()).contains("\"All\"")
    }

    @Test
    fun albumListWithoutTokenIs403() = testApplication {
        installRoutes()
        assertThat(client.get("/album-list").status).isEqualTo(HttpStatusCode.Forbidden)
    }

    @Test
    fun rotatedTokenInvalidatesOldLinkOnNextRequest() = testApplication {
        var live = "tok-1"
        installRoutes(currentToken = { live })
        // Old token works.
        assertThat(client.get("/?t=tok-1").status).isEqualTo(HttpStatusCode.OK)
        // Rotate: the provider now reports a new token; old link is rejected immediately.
        live = "tok-2"
        assertThat(client.get("/?t=tok-1").status).isEqualTo(HttpStatusCode.Forbidden)
        assertThat(client.get("/?t=tok-2").status).isEqualTo(HttpStatusCode.OK)
    }
}
