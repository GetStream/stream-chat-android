/*
 * Copyright (c) 2014-2026 Stream.io Inc. All rights reserved.
 *
 * Licensed under the Stream License;
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *    https://github.com/GetStream/stream-chat-android/blob/main/LICENSE
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package io.getstream.chat.android.ai.compose.util.internal

import java.net.HttpURLConnection
import java.net.Inet4Address
import java.net.Inet6Address
import java.net.InetAddress
import java.net.URL
import java.util.Locale

/**
 * Downloads the bytes of images whose links the model wrote, so only from public hosts: every
 * address a host resolves to must be public, and redirects are followed here, up to
 * [MaxRedirects], with each target checked the same way. Resolves host names, so call it off the
 * main thread.
 *
 * @param resolve The addresses a host name resolves to.
 * @param open Opens a connection to a link.
 */
internal class ImageDownloader(
    private val resolve: (String) -> List<InetAddress> = { host -> InetAddress.getAllByName(host).toList() },
    private val open: (URL) -> HttpURLConnection = { url -> url.openConnection() as HttpURLConnection },
) {

    /**
     * The bytes at [url], or `null` when it isn't a web link on a public host, redirects to one
     * that isn't, redirects more than [MaxRedirects] times, fails, or is larger than [maxBytes].
     *
     * @param url The image's link.
     * @param maxBytes The most bytes it downloads.
     */
    fun download(url: String, maxBytes: Int): ByteArray? {
        var target = webUrl(url) ?: return null
        repeat(MaxRedirects + 1) {
            if (!isPublicHost(target.host)) return null
            val connection = open(target)
            try {
                connection.connectTimeout = TimeoutMs
                connection.readTimeout = TimeoutMs
                // Redirects are followed here, so each target's host is checked too.
                connection.instanceFollowRedirects = false
                val status = connection.responseCode
                if (status in HttpURLConnection.HTTP_MULT_CHOICE until HttpURLConnection.HTTP_BAD_REQUEST) {
                    val location = connection.getHeaderField("Location") ?: return null
                    target = runCatching { URL(target, location) }.getOrNull()?.takeIf(::isWeb) ?: return null
                } else {
                    if (status !in HttpURLConnection.HTTP_OK until HttpURLConnection.HTTP_MULT_CHOICE) return null
                    val length = connection.getHeaderField("Content-Length")?.trim()?.toLongOrNull()
                    if (length != null && length > maxBytes) return null
                    return connection.inputStream.use { it.readAtMost(maxBytes) }
                }
            } finally {
                connection.disconnect()
            }
        }
        return null
    }

    /**
     * Whether every address [host] resolves to is public, so a link the model wrote can't reach
     * the device itself or the local network.
     */
    fun isPublicHost(host: String?): Boolean {
        if (host.isNullOrEmpty()) return false
        val addresses = runCatching { resolve(host) }.getOrNull()
        return !addresses.isNullOrEmpty() && addresses.all(::isPublicAddress)
    }

    internal companion object {
        /** The most redirects a download follows. */
        const val MaxRedirects = 5

        private const val TimeoutMs = 15_000
        private const val UniqueLocalMask = 0xFE
        private const val UniqueLocal = 0xFC
        private const val MappedPrefixBytes = 10
        private const val Ipv4Bytes = 4
        private const val Ipv6Bytes = 16
        private const val FirstOctetOfZeroNetwork = 0
        private const val MappedMarker: Byte = -1 // 0xFF

        /** [url] as a link, when it is a web link. */
        fun webUrl(url: String): URL? = runCatching { URL(url) }.getOrNull()?.takeIf(::isWeb)

        private fun isWeb(url: URL): Boolean {
            val protocol = url.protocol.lowercase(Locale.ROOT)
            return protocol == "https" || protocol == "http"
        }

        /**
         * Whether [address] is neither loopback, private, link-local, multicast, unspecified nor
         * unique-local, including IPv4 addresses written in IPv6 form.
         */
        fun isPublicAddress(address: InetAddress): Boolean {
            embeddedIpv4(address)?.let { return isPublicAddress(it) }
            val local = address.isAnyLocalAddress ||
                address.isLoopbackAddress ||
                address.isLinkLocalAddress ||
                address.isSiteLocalAddress ||
                address.isMulticastAddress
            // 0.0.0.0/8 means this network, which a connection can reach the device itself through.
            val zeroNetwork = address is Inet4Address && address.address[0].toInt() == FirstOctetOfZeroNetwork
            // IPv6 unique local addresses (fc00::/7) are the private range Java doesn't flag.
            val uniqueLocal = address is Inet6Address &&
                (address.address[0].toInt() and UniqueLocalMask) == UniqueLocal
            return !local && !zeroNetwork && !uniqueLocal
        }

        /** The IPv4 address an IPv4-mapped (::ffff:a.b.c.d) or IPv4-compatible (::a.b.c.d) one carries. */
        private fun embeddedIpv4(address: InetAddress): InetAddress? {
            if (address !is Inet6Address) return null
            val bytes = address.address
            if (bytes.size != Ipv6Bytes || (0 until MappedPrefixBytes).any { bytes[it].toInt() != 0 }) return null
            val marker = bytes.copyOfRange(MappedPrefixBytes, Ipv6Bytes - Ipv4Bytes)
            val mapped = marker.all { it == MappedMarker }
            val compatible = marker.all { it.toInt() == 0 } &&
                !address.isLoopbackAddress &&
                !address.isAnyLocalAddress
            if (!mapped && !compatible) return null
            return InetAddress.getByAddress(bytes.copyOfRange(Ipv6Bytes - Ipv4Bytes, Ipv6Bytes))
        }
    }
}
