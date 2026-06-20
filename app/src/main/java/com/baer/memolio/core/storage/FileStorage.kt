package com.baer.memolio.core.storage

import java.io.File
import java.io.OutputStream

/**
 * Owns the app-scoped media directory layout. Pass the app's filesDir (or a chosen
 * external app-specific dir) as [root]. No Android imports so it is unit-testable.
 */
class FileStorage(private val root: File) {

    private val photosDir get() = File(root, "photos")
    private val displayDir get() = File(root, "cache/display")
    private val thumbDir get() = File(root, "cache/thumb")

    fun originalFile(id: String, ext: String) = File(photosDir, "$id.$ext")
    fun displayCacheFile(id: String) = File(displayDir, "$id.jpg")
    fun thumbFile(id: String) = File(thumbDir, "$id.jpg")

    fun writeOriginal(id: String, ext: String, write: (OutputStream) -> Unit): File {
        val file = originalFile(id, ext)
        file.parentFile?.mkdirs()
        file.outputStream().use(write)
        return file
    }

    fun deleteAll(id: String, ext: String) {
        originalFile(id, ext).delete()
        displayCacheFile(id).delete()
        thumbFile(id).delete()
    }

    /** Sum of bytes used by all media files. */
    fun usedBytes(): Long =
        listOf(photosDir, displayDir, thumbDir)
            .flatMap { it.walkTopDown().filter(File::isFile).toList() }
            .sumOf { it.length() }

    /** Total bytes of the partition the media dir lives on (drives the storage meter). */
    fun totalBytes(): Long = root.totalSpace
}
