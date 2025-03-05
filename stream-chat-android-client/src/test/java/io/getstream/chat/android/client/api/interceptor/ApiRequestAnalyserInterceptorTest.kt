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

import io.getstream.chat.android.client.Mother
import io.getstream.chat.android.client.api.FakeChain
import io.getstream.chat.android.client.api.FakeResponse
import io.getstream.chat.android.client.plugins.requests.ApiRequestsAnalyser
import okhttp3.Request
import org.junit.jupiter.params.ParameterizedTest
import org.junit.jupiter.params.provider.Arguments
import org.junit.jupiter.params.provider.MethodSource
import org.mockito.kotlin.any
import org.mockito.kotlin.doNothing
import org.mockito.kotlin.mock
import org.mockito.kotlin.times
import org.mockito.kotlin.verify
import org.mockito.kotlin.whenever

internal class ApiRequestAnalyserInterceptorTest {

    @ParameterizedTest
    @MethodSource("apiRequestAnalyzerInterceptorInput")
    fun testApiRequestAnalyserInterceptor(request: Request, body: String) {
        // given
        val analyser = mock<ApiRequestsAnalyser>()
        doNothing().whenever(analyser).registerRequest(any(), any())
        val interceptor = ApiRequestAnalyserInterceptor(analyser)
        // when
        val chain = FakeChain(
            response = arrayOf(FakeResponse(200)),
            request = request,
        )
        interceptor.intercept(chain)
        // then
        verify(analyser, times(1)).registerRequest(
            requestName = chain.request().url.toString(),
            data = mapOf("body" to body),
        )
    }

    companion object {

        @JvmStatic
        fun apiRequestAnalyzerInterceptorInput() = listOf(
            Arguments.of(Mother.randomGetRequest(), "no_body"),
            Arguments.of(Mother.randomPostRequest(body = "body"), "body"),
        )
    }
}
