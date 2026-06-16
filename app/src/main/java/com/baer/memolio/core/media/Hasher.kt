package com.baer.memolio.core.media

import java.io.InputStream
import java.security.MessageDigest

object Hasher {
    fun sha256(bytes: ByteArray): String = MessageDigest.getInstance("SHA-256")
        .digest(bytes)
        .joinToString("") { "%02x".format(it) }

    fun sha256(stream: InputStream): String {
        val digest = MessageDigest.getInstance("SHA-256")
        val buffer = ByteArray(8192)
        var read = stream.read(buffer)
        while (read != -1) {
            digest.update(buffer, 0, read)
            read = stream.read(buffer)
        }
        return digest.digest().joinToString("") { "%02x".format(it) }
    }
}
