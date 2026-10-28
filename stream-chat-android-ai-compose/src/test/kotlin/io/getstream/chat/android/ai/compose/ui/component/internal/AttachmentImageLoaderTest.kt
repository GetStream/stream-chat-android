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

package io.getstream.chat.android.ai.compose.ui.component.internal

import android.content.Context
import android.graphics.Bitmap
import android.graphics.Color
import android.net.Uri
import androidx.core.net.toUri
import androidx.test.core.app.ApplicationProvider
import androidx.test.ext.junit.runners.AndroidJUnit4
import kotlinx.coroutines.test.runTest
import org.junit.After
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertNull
import org.junit.Assert.assertSame
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.annotation.GraphicsMode
import java.io.ByteArrayOutputStream
import java.io.File
import java.io.IOException
import java.io.OutputStream
import java.net.InetAddress
import java.net.ServerSocket
import java.net.Socket
import java.util.concurrent.atomic.AtomicInteger
import kotlin.concurrent.thread

@RunWith(AndroidJUnit4::class)
@GraphicsMode(GraphicsMode.Mode.NATIVE)
internal class AttachmentImageLoaderTest {

    private val context = ApplicationProvider.getApplicationContext<Context>()
    private lateinit var server: LocalHttpServer

    @Before
    fun setUp() {
        server = LocalHttpServer { path, out ->
            when (path) {
                "/photo.png" -> out.respond(200, png(width = 400, height = 200))
                "/missing.png" -> out.respond(404, "Not found".toByteArray())
                "/not-an-image.png" -> out.respond(200, "<html>Sign in</html>".toByteArray())
                // Says it's larger than the limit and sends nothing: the loader must not read it.
                "/declared-too-large.png" -> out.head(200, "Content-Length: ${MaxDownloadBytes + 1}")
                // No length is declared: the loader stops once it reads past the limit. Decoders
                // ignore what follows an image, so only the limit keeps this one from showing.
                "/streamed-too-large.png" -> {
                    out.head(200, "Connection: close")
                    val image = png(width = 400, height = 200)
                    out.write(image)
                    val chunk = ByteArray(64 * 1024)
                    var sent = image.size
                    while (sent <= MaxDownloadBytes) {
                        out.write(chunk)
                        sent += chunk.size
                    }
                }
            }
        }
    }

    @After
    fun tearDown() {
        server.close()
    }

    @Test
    fun `an image on the web is downloaded and downsampled to the size it shows at`() = runTest {
        val bitmap = AttachmentImageLoader.load(context, url("/photo.png"), targetSizePx = 100)

        assertNotNull(bitmap)
        assertEquals(200, bitmap!!.width)
        assertEquals(100, bitmap.height)
        assertEquals(Color.RED, bitmap.getPixel(10, 10))
    }

    @Test
    fun `a downloaded image is kept in memory and not downloaded again`() = runTest {
        val uri = url("/photo.png")
        assertNull(AttachmentImageLoader.cached(uri, targetSizePx = 50))

        val first = AttachmentImageLoader.load(context, uri, targetSizePx = 50)
        val second = AttachmentImageLoader.load(context, uri, targetSizePx = 50)

        assertNotNull(first)
        assertSame(first, second)
        assertSame(first, AttachmentImageLoader.cached(uri, targetSizePx = 50))
        assertEquals(1, server.requests.get())
        assertNull("another size is another entry", AttachmentImageLoader.cached(uri, targetSizePx = 60))
    }

    @Test
    fun `a page that isn't found shows no image`() = runTest {
        val uri = url("/missing.png")

        assertNull(AttachmentImageLoader.load(context, uri, targetSizePx = 100))
        assertNull("a failure isn't kept", AttachmentImageLoader.cached(uri, targetSizePx = 100))
    }

    @Test
    fun `a response that isn't an image shows no image`() = runTest {
        assertNull(AttachmentImageLoader.load(context, url("/not-an-image.png"), targetSizePx = 100))
    }

    @Test
    fun `a download that says it is too large isn't read`() = runTest {
        assertNull(AttachmentImageLoader.load(context, url("/declared-too-large.png"), targetSizePx = 100))
        assertEquals(1, server.requests.get())
    }

    @Test
    fun `a download that turns out too large is dropped`() = runTest {
        val padded = png(width = 400, height = 200) + ByteArray(64 * 1024)
        assertNotNull("an image followed by padding decodes", decodeSampledBitmap(padded, targetSizePx = 100))

        assertNull(AttachmentImageLoader.load(context, url("/streamed-too-large.png"), targetSizePx = 100))
        assertEquals(1, server.requests.get())
    }

    @Test
    fun `a server that can't be reached shows no image`() = runTest {
        // A path no other test loads: the loader keeps what it decoded by address, and a later
        // server can be given the port an earlier test's was, which would serve this from memory.
        val uri = url("/unreachable.png")
        server.close()

        assertNull(AttachmentImageLoader.load(context, uri, targetSizePx = 100))
    }

    @Test
    fun `an image on this device is read through the content resolver`() = runTest {
        val file = File.createTempFile("photo", ".png", context.cacheDir)
        file.writeBytes(png(width = 80, height = 40))

        val bitmap = AttachmentImageLoader.load(context, Uri.fromFile(file), targetSizePx = 100)

        assertNotNull(bitmap)
        assertEquals(80, bitmap!!.width)
        assertEquals(40, bitmap.height)
    }

    private fun url(path: String): Uri = "http://127.0.0.1:${server.port}$path".toUri()

    private fun OutputStream.head(code: Int, vararg headers: String) {
        val lines = listOf("HTTP/1.1 $code Status") + headers
        write((lines.joinToString("\r\n") + "\r\n\r\n").toByteArray())
    }

    private fun OutputStream.respond(code: Int, body: ByteArray) {
        head(code, "Content-Length: ${body.size}", "Connection: close")
        write(body)
    }

    // An image, red all over.
    private fun png(width: Int, height: Int): ByteArray {
        val bitmap = Bitmap.createBitmap(width, height, Bitmap.Config.ARGB_8888).apply { eraseColor(Color.RED) }
        return ByteArrayOutputStream().also { bitmap.compress(Bitmap.CompressFormat.PNG, 100, it) }.toByteArray()
    }

    private companion object {
        // The loader's limit on a download.
        const val MaxDownloadBytes = 20 * 1024 * 1024
    }
}

/** A server on this device that answers each request on its own connection, for a loader to download from. */
private class LocalHttpServer(private val handle: (path: String, out: OutputStream) -> Unit) : AutoCloseable {
    private val socket = ServerSocket(0, 0, InetAddress.getLoopbackAddress())
    val port: Int = socket.localPort
    val requests = AtomicInteger()

    init {
        thread(isDaemon = true) {
            while (!socket.isClosed) {
                val connection = try {
                    socket.accept()
                } catch (_: IOException) {
                    break
                }
                thread(isDaemon = true) { serve(connection) }
            }
        }
    }

    private fun serve(connection: Socket) = connection.use {
        val reader = it.getInputStream().bufferedReader()
        val path = reader.readLine()?.split(' ')?.getOrNull(1) ?: return@use
        while (!reader.readLine().isNullOrEmpty()) Unit
        requests.incrementAndGet()
        try {
            handle(path, it.getOutputStream())
            it.getOutputStream().flush()
        } catch (_: IOException) {
            // The loader hangs up on what it won't read.
        }
    }

    override fun close() = socket.close()
}
