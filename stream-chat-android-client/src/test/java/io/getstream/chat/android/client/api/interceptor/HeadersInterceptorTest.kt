/*
 * Copyright (c) 2014-2025 Stream.io Inc. All rights reserved.
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

import io.getstream.chat.android.client.api.FakeChain
import io.getstream.chat.android.client.api.FakeResponse
import io.getstream.chat.android.client.utils.HeadersUtil
import org.amshove.kluent.`should be equal to`
import org.junit.Test
import org.mockito.kotlin.doReturn
import org.mockito.kotlin.mock
import org.mockito.kotlin.whenever

internal class HeadersInterceptorTest {

    @Test
    fun testAnonymousUserHeaders() {
        // given
        val isAnonymous = { true }
        val headersUtil = mock<HeadersUtil>()
        whenever(headersUtil.buildSdkTrackingHeaders()).doReturn("sdkTrackingHeaders")
        whenever(headersUtil.buildUserAgent()).doReturn("userAgent")
        val interceptor = HeadersInterceptor(isAnonymous, headersUtil)
        // when
        val response = interceptor.intercept(FakeChain(FakeResponse(200)))
        // then
        response.request.header("User-Agent") `should be equal to` "userAgent"
        response.request.header("Content-Type") `should be equal to` "application/json"
        response.request.header("stream-auth-type") `should be equal to` "anonymous"
        response.request.header("X-Stream-Client") `should be equal to` "sdkTrackingHeaders"
        response.request.header("Cache-Control") `should be equal to` "no-cache"
    }

    @Test
    fun testAuthenticatedUserHeaders() {
        // given
        val isAnonymous = { false }
        val headersUtil = mock<HeadersUtil>()
        whenever(headersUtil.buildSdkTrackingHeaders()).doReturn("sdkTrackingHeaders")
        whenever(headersUtil.buildUserAgent()).doReturn("userAgent")
        val interceptor = HeadersInterceptor(isAnonymous, headersUtil)
        // when
        val response = interceptor.intercept(FakeChain(FakeResponse(200)))
        // then
        response.request.header("User-Agent") `should be equal to` "userAgent"
        response.request.header("Content-Type") `should be equal to` "application/json"
        response.request.header("stream-auth-type") `should be equal to` "jwt"
        response.request.header("X-Stream-Client") `should be equal to` "sdkTrackingHeaders"
        response.request.header("Cache-Control") `should be equal to` "no-cache"
    }
}
