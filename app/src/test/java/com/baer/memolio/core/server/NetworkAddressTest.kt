package com.baer.memolio.core.server

import com.google.common.truth.Truth.assertThat
import org.junit.Test

class NetworkAddressTest {

    @Test
    fun picksFirstSiteLocalIpv4() {
        val picked = NetworkAddress.selectLanIpv4(
            listOf("127.0.0.1", "fe80::1", "192.168.1.42", "10.0.0.5")
        )
        assertThat(picked).isEqualTo("192.168.1.42")
    }

    @Test
    fun ignoresLoopbackAndIpv6() {
        val picked = NetworkAddress.selectLanIpv4(listOf("127.0.0.1", "::1", "fe80::abcd"))
        assertThat(picked).isNull()
    }

    @Test
    fun acceptsAllPrivateRanges() {
        assertThat(NetworkAddress.selectLanIpv4(listOf("172.16.5.9"))).isEqualTo("172.16.5.9")
        assertThat(NetworkAddress.selectLanIpv4(listOf("10.1.2.3"))).isEqualTo("10.1.2.3")
        assertThat(NetworkAddress.selectLanIpv4(listOf("192.168.0.1"))).isEqualTo("192.168.0.1")
    }

    @Test
    fun fallsBackToAnyNonLoopbackIpv4WhenNoPrivateMatch() {
        // Not in a private block, but a valid routable IPv4 -> still usable on some LANs.
        assertThat(NetworkAddress.selectLanIpv4(listOf("100.64.0.1"))).isEqualTo("100.64.0.1")
    }

    @Test
    fun returnsNullForEmpty() {
        assertThat(NetworkAddress.selectLanIpv4(emptyList())).isNull()
    }
}
