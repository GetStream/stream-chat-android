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
import io.getstream.chat.android.randomString
import org.amshove.kluent.shouldBeEqualTo
import org.junit.Test

internal class ApiKeyInterceptorTest {

    @Test
    fun testApiKeyIsAddedAsHeader() {
        // given
        val apiKey = randomString()
        val interceptor = ApiKeyInterceptor(apiKey)
        // when
        val response = interceptor.intercept(FakeChain(FakeResponse(200)))
        // then
        val apiKeyQueryParam = response.request.url.queryParameter("api_key")
        apiKeyQueryParam shouldBeEqualTo apiKey
    }
}
