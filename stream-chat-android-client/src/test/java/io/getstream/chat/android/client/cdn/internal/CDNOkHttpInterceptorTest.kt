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

package io.getstream.chat.android.client.cdn.internal

import io.getstream.chat.android.client.api.FakeChain
import io.getstream.chat.android.client.api.FakeResponse
import io.getstream.chat.android.client.cdn.CDN
import io.getstream.chat.android.client.cdn.CDNRequest
import okhttp3.Request
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Test

internal class CDNOkHttpInterceptorTest {

    @Test
    fun `intercept rewrites URL when CDN returns different URL`() {
        val cdn = object : CDN {
            override suspend fun fileRequest(url: String) =
                CDNRequest("https://cdn.example.com/rewritten")
        }
        val interceptor = CDNOkHttpInterceptor(cdn)
        val chain = FakeChain(
            FakeResponse(200),
            request = Request.Builder().url("https://original.com/file.mp4").build(),
        )

        val response = interceptor.intercept(chain)

        assertEquals("https://cdn.example.com/rewritten", response.request.url.toString())
    }

    @Test
    fun `intercept adds CDN headers to the request`() {
        val cdn = object : CDN {
            override suspend fun fileRequest(url: String) =
                CDNRequest(url, headers = mapOf("Authorization" to "Bearer token123", "X-Custom" to "value"))
        }
        val interceptor = CDNOkHttpInterceptor(cdn)
        val chain = FakeChain(
            FakeResponse(200),
            request = Request.Builder().url("https://original.com/file.mp4").build(),
        )

        val response = interceptor.intercept(chain)

        assertEquals("Bearer token123", response.request.header("Authorization"))
        assertEquals("value", response.request.header("X-Custom"))
    }

    @Test
    fun `intercept adds CDN headers without removing existing ones`() {
        val cdn = object : CDN {
            override suspend fun fileRequest(url: String) =
                CDNRequest(url, headers = mapOf("X-CDN" to "cdn-value"))
        }
        val interceptor = CDNOkHttpInterceptor(cdn)
        val originalRequest = Request.Builder()
            .url("https://original.com/file.mp4")
            .addHeader("X-Existing", "existing-value")
            .build()
        val chain = FakeChain(FakeResponse(200), request = originalRequest)

        val response = interceptor.intercept(chain)

        assertEquals("existing-value", response.request.header("X-Existing"))
        assertEquals("cdn-value", response.request.header("X-CDN"))
    }

    @Test
    @Suppress("TooGenericExceptionThrown")
    fun `intercept falls back to original request when CDN throws`() {
        val cdn = object : CDN {
            override suspend fun fileRequest(url: String): CDNRequest {
                throw RuntimeException("CDN unavailable")
            }
        }
        val interceptor = CDNOkHttpInterceptor(cdn)
        val chain = FakeChain(
            FakeResponse(200),
            request = Request.Builder().url("https://original.com/file.mp4").build(),
        )

        val response = interceptor.intercept(chain)

        assertEquals("https://original.com/file.mp4", response.request.url.toString())
        assertNull(response.request.header("Authorization"))
    }

    @Test
    fun `intercept passes through unchanged when CDN returns original URL and null headers`() {
        val cdn = object : CDN {
            override suspend fun fileRequest(url: String) = CDNRequest(url, headers = null)
        }
        val interceptor = CDNOkHttpInterceptor(cdn)
        val chain = FakeChain(
            FakeResponse(200),
            request = Request.Builder().url("https://original.com/file.mp4").build(),
        )

        val response = interceptor.intercept(chain)

        assertEquals("https://original.com/file.mp4", response.request.url.toString())
    }
}
