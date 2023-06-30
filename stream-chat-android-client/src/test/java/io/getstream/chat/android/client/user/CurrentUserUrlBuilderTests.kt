/*
 * Copyright (c) 2014-2023 Stream.io Inc. All rights reserved.
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

package io.getstream.chat.android.client.user

import androidx.test.ext.junit.runners.AndroidJUnit4
import io.getstream.chat.android.client.parser.ChatParser
import org.amshove.kluent.`should be equal to`
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import org.mockito.kotlin.any
import org.mockito.kotlin.doReturn
import org.mockito.kotlin.mock
import org.mockito.kotlin.whenever
import java.net.URLEncoder
import java.nio.charset.StandardCharsets

@RunWith(AndroidJUnit4::class)
internal class CurrentUserUrlBuilderTests {

    private val baseUrl = "wss://chat-us-east-1.stream-io-api.com"
    private val apiKey = "qk4nn7rpcn75"
    private val token =
        "eyJ0eXAiOiJKV1QiLCJhbGciOiJIUzI1NiJ9.eyJ1c2VyX2lkIjoiYmVuZGVyIn0.3KYJIoYvSPgTURznP8nWvsA2Yj2-vLqrm-ubqAeOlcQ"
    private val userId = "bender"

    private lateinit var chatParser: ChatParser
    private lateinit var urlBuilder: CurrentUserUrlBuilder

    @Before
    fun before() {
        chatParser = mock()
        urlBuilder = CurrentUserUrlBuilderImpl(
            getCurrentUserId = { userId },
            getApiKey = { apiKey },
            getToken = { token },
            getBaseUrl = { "wss://chat-us-east-1.stream-io-api.com" },
            chatParser = chatParser
        )
    }

    @Test
    fun testBuildUrl() {
        /* Given */
        val jsonPayload = "{\"user_id\":\"$userId\"}"
        val jsonEncoded = URLEncoder.encode(jsonPayload, StandardCharsets.UTF_8.name())
        whenever(chatParser.toJson(any())) doReturn jsonPayload

        /* When */
        val url = urlBuilder.buildUrl()

        /* Then */
        url `should be equal to` "$baseUrl/connect?json=$jsonEncoded" +
            "&api_key=$apiKey" +
            "&stream-auth-type=jwt" +
            "&authorization=$token"
    }
}
