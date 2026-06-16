package com.baer.memolio.core.media

import com.google.common.truth.Truth.assertThat
import org.junit.Test

class HasherTest {
    @Test
    fun sameBytesProduceSameHash() {
        val bytes = "hello".toByteArray()
        assertThat(Hasher.sha256(bytes)).isEqualTo(Hasher.sha256("hello".toByteArray()))
    }

    @Test
    fun differentBytesProduceDifferentHash() {
        assertThat(Hasher.sha256("a".toByteArray()))
            .isNotEqualTo(Hasher.sha256("b".toByteArray()))
    }

    @Test
    fun hashIsHexLowercase64Chars() {
        val hash = Hasher.sha256("x".toByteArray())
        assertThat(hash).matches("[0-9a-f]{64}")
    }
}
