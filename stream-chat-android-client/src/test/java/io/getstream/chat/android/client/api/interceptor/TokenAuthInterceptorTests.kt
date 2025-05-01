/*
 * Copyright (c) 2014-2022 Stream.io Inc. All rights reserved.
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
import io.getstream.chat.android.client.api.FakeResponse.Body
import io.getstream.chat.android.client.errors.ChatErrorCode
import io.getstream.chat.android.client.errors.ChatRequestError
import io.getstream.chat.android.client.parser2.ParserFactory
import io.getstream.chat.android.client.token.CacheableTokenProvider
import io.getstream.chat.android.client.token.FakeTokenManager
import io.getstream.chat.android.client.token.FakeTokenProvider
import io.getstream.chat.android.client.token.TokenManagerImpl
import okhttp3.Request
import org.amshove.kluent.invoking
import org.amshove.kluent.shouldBeEqualTo
import org.amshove.kluent.shouldThrow
import org.junit.Test
import org.junit.jupiter.api.Assertions
import org.mockito.Mockito.verify
import org.mockito.kotlin.argumentCaptor
import org.mockito.kotlin.spy
import org.mockito.kotlin.times

internal class TokenAuthInterceptorTests {

    val token = "token"
    val parser = ParserFactory.createMoshiChatParser()

    @Test
    fun undefinedToken() {
        val tm = TokenManagerImpl()
        val interceptor = TokenAuthInterceptor(tm, parser) { false }

        val exceptionResult = invoking {
            interceptor.intercept(FakeChain(FakeResponse(200)))
        } shouldThrow (ChatRequestError::class)

        exceptionResult.exception.streamCode shouldBeEqualTo ChatErrorCode.UNDEFINED_TOKEN.code
    }

    @Test
    fun error500() {
        val tm = FakeTokenManager("token")
        val interceptor = TokenAuthInterceptor(tm, parser) { false }

        val exceptionResult = invoking {
            interceptor.intercept(FakeChain(FakeResponse(500)))
        } shouldThrow (ChatRequestError::class)

        exceptionResult.exception.statusCode shouldBeEqualTo 500
    }

    @Test
    fun validTokenAttachment() {
        val tm = FakeTokenManager(token)
        val interceptor = TokenAuthInterceptor(tm, parser) { false }

        val response = interceptor.intercept(FakeChain(FakeResponse(200)))

        val headerValue = response.request.headers[TokenAuthInterceptor.AUTH_HEADER]

        headerValue shouldBeEqualTo token
    }

    @Test
    fun invalidTokenAttachment() {
        val invalidHeader = "🤢"

        val tm = FakeTokenManager(invalidHeader)
        val interceptor = TokenAuthInterceptor(tm, parser) { false }

        val exceptionResult = invoking {
            interceptor.intercept(FakeChain(FakeResponse(200)))
        } shouldThrow (ChatRequestError::class)

        exceptionResult.exception.streamCode shouldBeEqualTo ChatErrorCode.INVALID_TOKEN.code
    }

    @Test
    fun expiredToken() {
        val tm = TokenManagerImpl()
        val interceptor = TokenAuthInterceptor(tm, parser) { false }

        tm.setTokenProvider(CacheableTokenProvider(FakeTokenProvider("token-a", "token-b")))

        val chain = FakeChain(
            FakeResponse(444, Body("""{ "code": 40 }""")),
            FakeResponse(200, Body("""{}""")),
        )
        interceptor.intercept(chain)
        chain.processChain()
        interceptor.intercept(chain)
    }

    @Test
    fun expiredTokenIsReplacedWithFreshOne() {
        val tokenManager = FakeTokenManager(
            token = "expired-token",
            loadSyncToken = "fresh-token",
        )
        val interceptor = TokenAuthInterceptor(tokenManager, parser) { false }

        val request = Mother.randomGetRequest()
        val response = FakeResponse(401, Body("""{ "code": 40 }""")) // Token expired
        val chain = spy(FakeChain(response, request = request))

        interceptor.intercept(chain)

        // Use argument captor to verify the actual requests passed to proceed
        val requestCaptor = argumentCaptor<Request>()
        verify(chain, times(2)).proceed(requestCaptor.capture())

        // Extract the captured requests
        val capturedRequests = requestCaptor.allValues

        // Verify the first request has the expired token
        Assertions.assertEquals("expired-token", capturedRequests[0].header(TokenAuthInterceptor.AUTH_HEADER))
        // Verify the second request has the fresh token
        Assertions.assertEquals("fresh-token", capturedRequests[1].header(TokenAuthInterceptor.AUTH_HEADER))
    }
}
