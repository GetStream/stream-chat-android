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

import org.junit.Assert.assertArrayEquals
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Test
import java.io.ByteArrayInputStream
import java.io.InputStream
import java.net.HttpURLConnection
import java.net.Inet6Address
import java.net.InetAddress
import java.net.URL
import java.net.UnknownHostException

internal class ImageDownloaderTest {

    private val image = byteArrayOf(1, 2, 3)
    private val opened = mutableListOf<String>()
    private val connections = mutableListOf<FakeConnection>()
    private val responses = mutableMapOf<String, Response>()

    private val downloader = downloader(publicHostsOnly = true)

    private fun downloader(publicHostsOnly: Boolean) = ImageDownloader(
        publicHostsOnly = publicHostsOnly,
        resolve = { host ->
            when (host) {
                "cdn.example.com", "other.example.com" -> listOf(InetAddress.getByName("93.184.216.34"))
                "localhost" -> listOf(InetAddress.getByName("127.0.0.1"), InetAddress.getByName("::1"))
                "router.example.com" -> listOf(InetAddress.getByName("192.168.1.1"))
                "split.example.com" -> listOf(
                    InetAddress.getByName("93.184.216.34"),
                    InetAddress.getByName("10.0.0.7"),
                )
                // Literal addresses resolve without a lookup.
                else -> if (host.any { it.isLetter() } && !host.contains(':')) {
                    throw UnknownHostException(host)
                } else {
                    InetAddress.getAllByName(host).toList()
                }
            }
        },
        open = { url ->
            opened += url.toString()
            val response = responses[url.toString()] ?: Response(HttpURLConnection.HTTP_NOT_FOUND)
            FakeConnection(url, response).also { connections += it }
        },
    )

    @Test
    fun `public addresses are public`() {
        listOf("8.8.8.8", "93.184.216.34", "2606:4700::1111", "::ffff:8.8.8.8").forEach {
            assertTrue(it, ImageDownloader.isPublicAddress(InetAddress.getByName(it)))
        }
    }

    @Test
    fun `local, private and special addresses are not public`() {
        listOf(
            "127.0.0.1", "127.1.2.3", "::1", "0.0.0.0", "0.1.2.3", "::",
            "10.0.0.2", "172.16.4.1", "172.31.255.255",
            "192.168.1.1", "169.254.169.254", "fe80::1", "fc00::1", "fd12:3456::1", "224.0.0.1", "ff02::1",
            "::ffff:127.0.0.1", "::ffff:192.168.1.1", "::ffff:169.254.169.254",
        ).forEach {
            assertFalse(it, ImageDownloader.isPublicAddress(InetAddress.getByName(it)))
        }
    }

    @Test
    fun `IPv4 addresses kept in IPv6 form are checked as IPv4`() {
        fun ipv6(vararg tail: Int): InetAddress {
            val bytes = ByteArray(16)
            tail.forEachIndexed { i, b -> bytes[16 - tail.size + i] = b.toByte() }
            return Inet6Address.getByAddress(null, bytes, null)
        }
        assertFalse(ImageDownloader.isPublicAddress(ipv6(0xFF, 0xFF, 127, 0, 0, 1)))
        assertFalse(ImageDownloader.isPublicAddress(ipv6(0xFF, 0xFF, 10, 0, 0, 1)))
        assertFalse(ImageDownloader.isPublicAddress(ipv6(127, 0, 0, 1)))
        assertFalse(ImageDownloader.isPublicAddress(ipv6(192, 168, 0, 1)))
        assertTrue(ImageDownloader.isPublicAddress(ipv6(0xFF, 0xFF, 8, 8, 8, 8)))
    }

    @Test
    fun `hosts that are local or don't resolve are not public`() {
        assertFalse(downloader.isPublicHost("127.0.0.1"))
        assertFalse(downloader.isPublicHost("[::1]"))
        assertFalse(downloader.isPublicHost("router.example.com"))
        assertFalse(downloader.isPublicHost("split.example.com"))
        assertFalse(downloader.isPublicHost("missing.example.com"))
        assertFalse(downloader.isPublicHost(""))
        assertFalse(downloader.isPublicHost(null))
        assertTrue(downloader.isPublicHost("cdn.example.com"))
        assertTrue(downloader.isPublicHost("8.8.8.8"))
    }

    @Test
    fun `links to local and private hosts are not opened`() {
        listOf(
            "http://127.0.0.1/a.png",
            "http://localhost:8080/a.png",
            "http://[::1]/a.png",
            "http://[::ffff:127.0.0.1]/a.png",
            "http://[::ffff:10.0.0.1]/a.png",
            "http://10.0.0.1/a.png",
            "http://172.16.0.1/a.png",
            "https://192.168.0.10/a.png",
            "http://169.254.169.254/latest/meta-data/",
            "http://[fe80::1]/a.png",
            "http://[fd00::1]/a.png",
            "http://0.0.0.0/a.png",
            "http://router.example.com/a.png",
            "http://split.example.com/a.png",
        ).forEach { assertNull(it, downloader.download(it, maxBytes = 1024)) }
        assertEquals(emptyList<String>(), opened)
    }

    @Test
    fun `only web links are opened`() {
        listOf("file:///data/data/app/secret", "ftp://cdn.example.com/a.png", "content://media/1", "a.png", "")
            .forEach { assertNull(it, downloader.download(it, maxBytes = 1024)) }
        assertEquals(emptyList<String>(), opened)
    }

    @Test
    fun `an image on a public host downloads, following no redirect on its own`() {
        responses["https://cdn.example.com/a.png"] = Response(HttpURLConnection.HTTP_OK, body = image)

        assertArrayEquals(image, downloader.download("https://cdn.example.com/a.png", maxBytes = 1024))
        assertFalse(connections.single().instanceFollowRedirects)
        assertTrue(connections.single().disconnected)
    }

    @Test
    fun `a redirect to a private address is refused`() {
        responses["https://cdn.example.com/a.png"] = redirect("http://10.0.0.1/a.png")
        responses["http://10.0.0.1/a.png"] = Response(HttpURLConnection.HTTP_OK, body = image)

        assertNull(downloader.download("https://cdn.example.com/a.png", maxBytes = 1024))
        assertEquals(listOf("https://cdn.example.com/a.png"), opened)
    }

    @Test
    fun `a redirect to a host that resolves to a private address is refused`() {
        responses["https://cdn.example.com/a.png"] = redirect("http://router.example.com/admin")

        assertNull(downloader.download("https://cdn.example.com/a.png", maxBytes = 1024))
        assertEquals(listOf("https://cdn.example.com/a.png"), opened)
    }

    @Test
    fun `a redirect to a link that isn't on the web is refused`() {
        responses["https://cdn.example.com/a.png"] = redirect("file:///etc/hosts")

        assertNull(downloader.download("https://cdn.example.com/a.png", maxBytes = 1024))
        assertEquals(listOf("https://cdn.example.com/a.png"), opened)
    }

    @Test
    fun `redirects to public hosts are followed, relative ones too`() {
        responses["https://cdn.example.com/a.png"] = redirect("https://other.example.com/b.png")
        responses["https://other.example.com/b.png"] = redirect("/c.png")
        responses["https://other.example.com/c.png"] = Response(HttpURLConnection.HTTP_OK, body = image)

        assertArrayEquals(image, downloader.download("https://cdn.example.com/a.png", maxBytes = 1024))
        assertEquals(3, opened.size)
    }

    @Test
    fun `up to the redirect limit is followed, one more is refused`() {
        val limit = ImageDownloader.MaxRedirects
        (0 until limit + 1).forEach { responses[hop(it)] = redirect(hop(it + 1)) }

        responses[hop(limit)] = Response(HttpURLConnection.HTTP_OK, body = image)
        assertArrayEquals(image, downloader.download(hop(0), maxBytes = 1024))

        opened.clear()
        responses[hop(limit)] = redirect(hop(limit + 1))
        responses[hop(limit + 1)] = Response(HttpURLConnection.HTTP_OK, body = image)
        assertNull(downloader.download(hop(0), maxBytes = 1024))
        assertEquals(limit + 1, opened.size)
    }

    @Test
    fun `a redirect without a location is refused`() {
        responses["https://cdn.example.com/a.png"] = Response(HttpURLConnection.HTTP_MOVED_TEMP)

        assertNull(downloader.download("https://cdn.example.com/a.png", maxBytes = 1024))
    }

    @Test
    fun `an image larger than the byte cap is refused`() {
        responses["https://cdn.example.com/big.png"] = Response(HttpURLConnection.HTTP_OK, body = ByteArray(2048))
        responses["https://cdn.example.com/declared.png"] =
            Response(HttpURLConnection.HTTP_OK, headers = mapOf("Content-Length" to "4096"), body = image)

        assertNull(downloader.download("https://cdn.example.com/big.png", maxBytes = 1024))
        assertNull(downloader.download("https://cdn.example.com/declared.png", maxBytes = 1024))
        assertFalse(connections.last().bodyRead)
    }

    @Test
    fun `an error response is refused`() {
        responses["https://cdn.example.com/a.png"] = Response(HttpURLConnection.HTTP_INTERNAL_ERROR, body = image)

        assertNull(downloader.download("https://cdn.example.com/a.png", maxBytes = 1024))
    }

    @Test
    fun `without the host check, private hosts download and redirects stay capped`() {
        val anyHost = downloader(publicHostsOnly = false)
        responses["http://10.0.2.2:8080/a.png"] = redirect("http://router.example.com/b.png")
        responses["http://router.example.com/b.png"] = Response(HttpURLConnection.HTTP_OK, body = image)

        assertArrayEquals(image, anyHost.download("http://10.0.2.2:8080/a.png", maxBytes = 1024))
        assertFalse(connections.last().instanceFollowRedirects)

        opened.clear()
        (0..ImageDownloader.MaxRedirects).forEach { responses[hop(it)] = redirect(hop(it + 1)) }
        assertNull(anyHost.download(hop(0), maxBytes = 1024))
        assertEquals(ImageDownloader.MaxRedirects + 1, opened.size)
        assertNull(anyHost.download("file:///etc/hosts", maxBytes = 1024))
    }

    private fun hop(index: Int) = "https://cdn.example.com/hop/$index.png"

    private fun redirect(location: String) =
        Response(HttpURLConnection.HTTP_MOVED_TEMP, headers = mapOf("Location" to location))

    private class Response(
        val status: Int,
        val headers: Map<String, String> = emptyMap(),
        val body: ByteArray = ByteArray(0),
    )

    private class FakeConnection(url: URL, private val response: Response) : HttpURLConnection(url) {
        var disconnected = false
        var bodyRead = false

        override fun connect() = Unit
        override fun disconnect() {
            disconnected = true
        }
        override fun usingProxy(): Boolean = false
        override fun getResponseCode(): Int = response.status
        override fun getHeaderField(name: String?): String? =
            response.headers.entries.firstOrNull { it.key.equals(name, ignoreCase = true) }?.value
        override fun getInputStream(): InputStream {
            bodyRead = true
            return ByteArrayInputStream(response.body)
        }
    }
}
