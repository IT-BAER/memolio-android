package com.baer.memolio.core.server

import java.net.Inet4Address
import java.net.NetworkInterface

/**
 * Resolves the device's LAN IPv4. The candidate selection is pure (unit-tested);
 * [currentIpv4] walks Android's [NetworkInterface] list and feeds it through the
 * same selector.
 */
object NetworkAddress {

    /**
     * Picks the best LAN IPv4 from [candidates] (string form). Prefers RFC-1918 private
     * addresses; otherwise the first non-loopback IPv4; null if none qualify. IPv6 and
     * loopback are skipped.
     */
    fun selectLanIpv4(candidates: List<String>): String? {
        val ipv4 = candidates.filter { it.isIpv4() && !it.isLoopbackV4() }
        return ipv4.firstOrNull { it.isPrivateV4() } ?: ipv4.firstOrNull()
    }

    /** Live lookup of the device's current LAN IPv4, or null if offline. */
    fun currentIpv4(): String? {
        val addresses = buildList {
            for (iface in NetworkInterface.getNetworkInterfaces()) {
                if (!iface.isUp || iface.isLoopback) continue
                for (addr in iface.inetAddresses) {
                    if (addr is Inet4Address && !addr.isLoopbackAddress) {
                        add(addr.hostAddress ?: continue)
                    }
                }
            }
        }
        return selectLanIpv4(addresses)
    }

    private fun String.isIpv4(): Boolean {
        val parts = split(".")
        if (parts.size != 4) return false
        return parts.all { p -> p.toIntOrNull()?.let { it in 0..255 } == true }
    }

    private fun String.isLoopbackV4(): Boolean = startsWith("127.")

    private fun String.isPrivateV4(): Boolean {
        val o = split(".").mapNotNull { it.toIntOrNull() }
        if (o.size != 4) return false
        return when {
            o[0] == 10 -> true
            o[0] == 172 && o[1] in 16..31 -> true
            o[0] == 192 && o[1] == 168 -> true
            else -> false
        }
    }
}
