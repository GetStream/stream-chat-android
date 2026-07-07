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

package io.getstream.chat.android.client.api.interceptor

import io.getstream.chat.android.client.Mother
import io.getstream.chat.android.client.api.FakeChain
import io.getstream.chat.android.client.api.FakeResponse
import io.getstream.chat.android.randomString
import io.getstream.log.SilentStreamLogger
import io.getstream.log.StreamLog
import okhttp3.Interceptor
import okhttp3.MediaType
import okhttp3.MediaType.Companion.toMediaType
import okhttp3.Protocol
import okhttp3.Request
import okhttp3.RequestBody
import okhttp3.RequestBody.Companion.toRequestBody
import okhttp3.Response
import okhttp3.ResponseBody
import okhttp3.ResponseBody.Companion.toResponseBody
import okio.Buffer
import okio.BufferedSink
import okio.GzipSink
import okio.GzipSource
import okio.buffer
import org.amshove.kluent.invoking
import org.amshove.kluent.shouldBeEqualTo
import org.amshove.kluent.shouldThrow
import org.junit.After
import org.junit.Before
import org.junit.Test
import java.io.IOException

internal class HttpLoggingInterceptorTest {

    private val interceptor = HttpLoggingInterceptor()

    @Before
    fun setUp() {
        StreamLog.install(SilentStreamLogger)
    }

    @After
    fun tearDown() {
        StreamLog.unInstall()
    }

    @Test
    fun `Should proceed without logging when logger is not installed`() {
        StreamLog.unInstall()
        val chain = FakeChain(FakeResponse(200))

        val response = interceptor.intercept(chain)

        response.code shouldBeEqualTo 200
    }

    @Test
    fun `Should proceed request without body`() {
        val chain = FakeChain(FakeResponse(200, FakeResponse.Body("{}")))

        val response = interceptor.intercept(chain)

        response.code shouldBeEqualTo 200
        response.body!!.string() shouldBeEqualTo "{}"
    }

    @Test
    fun `Should proceed request with plain text body`() {
        val chain = FakeChain(
            FakeResponse(200, FakeResponse.Body("{}")),
            request = Mother.randomPostRequest(),
        )

        val response = interceptor.intercept(chain)

        response.code shouldBeEqualTo 200
    }

    @Test
    fun `Should proceed request with unknown encoded body`() {
        val request = Request.Builder()
            .url("https://hello.url")
            .header("Content-Encoding", "br")
            .post(randomString().toRequestBody())
            .build()
        val chain = FakeChain(FakeResponse(200, FakeResponse.Body("{}")), request = request)

        val response = interceptor.intercept(chain)

        response.code shouldBeEqualTo 200
    }

    @Test
    fun `Should proceed request with binary body`() {
        val request = Request.Builder()
            .url("https://hello.url")
            .post(BINARY_CONTENT.toRequestBody())
            .build()
        val chain = FakeChain(FakeResponse(200, FakeResponse.Body("{}")), request = request)

        val response = interceptor.intercept(chain)

        response.code shouldBeEqualTo 200
    }

    @Test
    fun `Should proceed request with duplex body`() {
        val request = Request.Builder()
            .url("https://hello.url")
            .post(FakeRequestBody(duplex = true))
            .build()
        val chain = FakeChain(FakeResponse(200, FakeResponse.Body("{}")), request = request)

        val response = interceptor.intercept(chain)

        response.code shouldBeEqualTo 200
    }

    @Test
    fun `Should proceed request with one-shot body`() {
        val request = Request.Builder()
            .url("https://hello.url")
            .post(FakeRequestBody(oneShot = true))
            .build()
        val chain = FakeChain(FakeResponse(200, FakeResponse.Body("{}")), request = request)

        val response = interceptor.intercept(chain)

        response.code shouldBeEqualTo 200
    }

    @Test
    fun `Should proceed response without promised body`() {
        val chain = FakeChain(FakeResponse(204, FakeResponse.Body("")))

        val response = interceptor.intercept(chain)

        response.code shouldBeEqualTo 204
    }

    @Test
    fun `Should proceed response with binary body`() {
        val chain = FakeChain(FakeResponse(200, FakeResponse.Body(BINARY_CONTENT)))

        val response = interceptor.intercept(chain)

        response.code shouldBeEqualTo 200
    }

    @Test
    fun `Should proceed response with unknown encoded body`() {
        val chain = FixedResponseChain(
            headers = mapOf("Content-Encoding" to "br"),
            body = "{}".toResponseBody("application/json".toMediaType()),
        )

        val response = interceptor.intercept(chain)

        response.code shouldBeEqualTo 200
        requireNotNull(response.body).string() shouldBeEqualTo "{}"
    }

    @Test
    fun `Should proceed response with gzip encoded body`() {
        val gzipped = Buffer()
        GzipSink(gzipped).buffer().use { it.writeUtf8("""{"key":"value"}""") }
        val chain = FixedResponseChain(
            headers = mapOf("Content-Encoding" to "gzip"),
            body = gzipped.readByteString().toResponseBody("application/json".toMediaType()),
        )

        val response = interceptor.intercept(chain)

        response.code shouldBeEqualTo 200
        val body = requireNotNull(response.body)
        GzipSource(body.source()).buffer().readUtf8() shouldBeEqualTo """{"key":"value"}"""
    }

    @Test
    fun `Should rethrow failure from the chain`() {
        val chain = ThrowingChain()

        invoking { interceptor.intercept(chain) } shouldThrow IOException::class
    }

    private companion object {
        /**
         * Non-whitespace ISO control characters which make the interceptor treat the body as binary.
         */
        const val BINARY_CONTENT: String = "\u0000\u0001\u0002"
    }

    private class FakeRequestBody(
        private val duplex: Boolean = false,
        private val oneShot: Boolean = false,
    ) : RequestBody() {
        override fun contentType(): MediaType? = null
        override fun isDuplex(): Boolean = duplex
        override fun isOneShot(): Boolean = oneShot
        override fun writeTo(sink: BufferedSink) { /* No-Op */ }
    }

    private class FixedResponseChain(
        private val headers: Map<String, String>,
        private val body: ResponseBody,
        private val delegate: FakeChain = FakeChain(FakeResponse(200)),
    ) : Interceptor.Chain by delegate {
        override fun proceed(request: Request): Response =
            Response.Builder()
                .code(200)
                .request(request)
                .protocol(Protocol.HTTP_2)
                .message("ok")
                .apply { headers.forEach { (name, value) -> header(name, value) } }
                .body(body)
                .build()
    }

    private class ThrowingChain(
        private val delegate: FakeChain = FakeChain(FakeResponse(200)),
    ) : Interceptor.Chain by delegate {
        override fun proceed(request: Request): Response = throw IOException("failure")
    }
}
