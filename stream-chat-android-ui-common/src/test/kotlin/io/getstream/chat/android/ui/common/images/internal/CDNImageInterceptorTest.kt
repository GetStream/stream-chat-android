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

package io.getstream.chat.android.ui.common.images.internal

import android.content.Context
import androidx.test.ext.junit.runners.AndroidJUnit4
import coil3.intercept.Interceptor
import coil3.network.NetworkHeaders
import coil3.network.httpHeaders
import coil3.request.ImageRequest
import coil3.request.ImageResult
import coil3.request.SuccessResult
import coil3.size.Size
import io.getstream.chat.android.client.cdn.CDN
import io.getstream.chat.android.client.cdn.CDNRequest
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test
import org.junit.runner.RunWith
import org.mockito.kotlin.mock
import org.robolectric.RuntimeEnvironment
import org.robolectric.annotation.Config

@RunWith(AndroidJUnit4::class)
@Config(sdk = [33])
internal class CDNImageInterceptorTest {

    private val context: Context get() = RuntimeEnvironment.getApplication()

    @Test
    fun `intercept rewrites URL when CDN returns different URL`() = runTest {
        val cdn = object : CDN {
            override suspend fun imageRequest(url: String) =
                CDNRequest("https://cdn.example.com/image.jpg")
        }
        val interceptor = CDNImageInterceptor(cdn)
        val request = ImageRequest.Builder(context)
            .data("https://original.com/image.jpg")
            .build()
        val chain = FakeCoilChain(request)

        interceptor.intercept(chain)

        val proceededRequest = chain.proceededRequest!!
        assertEquals("https://cdn.example.com/image.jpg", proceededRequest.data.toString())
    }

    @Test
    fun `intercept adds CDN headers to request`() = runTest {
        val cdn = object : CDN {
            override suspend fun imageRequest(url: String) =
                CDNRequest(url, mapOf("Authorization" to "Bearer token", "X-Custom" to "value"))
        }
        val interceptor = CDNImageInterceptor(cdn)
        val request = ImageRequest.Builder(context)
            .data("https://original.com/image.jpg")
            .build()
        val chain = FakeCoilChain(request)

        interceptor.intercept(chain)

        val headers = chain.proceededRequest!!.httpHeaders
        assertEquals("Bearer token", headers["Authorization"])
        assertEquals("value", headers["X-Custom"])
    }

    @Test
    fun `intercept CDN headers override existing headers for same key`() = runTest {
        val cdn = object : CDN {
            override suspend fun imageRequest(url: String) =
                CDNRequest(url, mapOf("Authorization" to "CDN-token"))
        }
        val interceptor = CDNImageInterceptor(cdn)
        val existingHeaders = NetworkHeaders.Builder()
            .add("Authorization", "Original-token")
            .add("X-Existing", "keep-me")
            .build()
        val request = ImageRequest.Builder(context)
            .data("https://original.com/image.jpg")
            .httpHeaders(existingHeaders)
            .build()
        val chain = FakeCoilChain(request)

        interceptor.intercept(chain)

        val headers = chain.proceededRequest!!.httpHeaders
        assertEquals("CDN-token", headers["Authorization"])
        assertEquals("keep-me", headers["X-Existing"])
    }

    @Test
    fun `intercept skips non-HTTP URLs`() = runTest {
        var cdnCalled = false
        val cdn = object : CDN {
            override suspend fun imageRequest(url: String): CDNRequest {
                cdnCalled = true
                return CDNRequest("https://should-not-be-used.com")
            }
        }
        val interceptor = CDNImageInterceptor(cdn)
        val request = ImageRequest.Builder(context)
            .data("content://media/image.jpg")
            .build()
        val chain = FakeCoilChain(request)

        interceptor.intercept(chain)

        assertTrue("CDN should not be called for content:// URLs", !cdnCalled)
        assertTrue("Request should pass through unchanged", chain.proceededRequest == null || chain.directProceed)
    }

    @Test
    @Suppress("TooGenericExceptionThrown")
    fun `intercept falls back to original request when CDN throws`() = runTest {
        val cdn = object : CDN {
            override suspend fun imageRequest(url: String): CDNRequest {
                throw RuntimeException("CDN unavailable")
            }
        }
        val interceptor = CDNImageInterceptor(cdn)
        val request = ImageRequest.Builder(context)
            .data("https://original.com/image.jpg")
            .build()
        val chain = FakeCoilChain(request)

        interceptor.intercept(chain)

        assertTrue("Should fall back to direct proceed on CDN error", chain.directProceed)
    }

    @Suppress("EmptyFunctionBlock")
    private class FakeCoilChain(
        override val request: ImageRequest,
    ) : Interceptor.Chain {
        var proceededRequest: ImageRequest? = null
        var directProceed: Boolean = false

        override val size: Size get() = Size.ORIGINAL

        override suspend fun proceed(): ImageResult {
            directProceed = true
            return mock<SuccessResult>()
        }

        override fun withRequest(request: ImageRequest): Interceptor.Chain {
            return FakeCoilChainWithRequest(request, this)
        }

        override fun withSize(size: Size): Interceptor.Chain = this
    }

    @Suppress("EmptyFunctionBlock")
    private class FakeCoilChainWithRequest(
        override val request: ImageRequest,
        private val parent: FakeCoilChain,
    ) : Interceptor.Chain {
        override val size: Size get() = Size.ORIGINAL

        override suspend fun proceed(): ImageResult {
            parent.proceededRequest = request
            return mock<SuccessResult>()
        }

        override fun withRequest(request: ImageRequest): Interceptor.Chain {
            return FakeCoilChainWithRequest(request, parent)
        }

        override fun withSize(size: Size): Interceptor.Chain = this
    }
}
