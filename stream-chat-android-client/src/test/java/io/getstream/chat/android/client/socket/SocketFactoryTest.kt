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

package io.getstream.chat.android.client.socket

import io.getstream.chat.android.client.parser.ChatParser
import io.getstream.chat.android.client.parser2.ParserFactory
import io.getstream.chat.android.client.token.FakeTokenManager
import io.getstream.chat.android.client.utils.HeadersUtil
import io.getstream.chat.android.models.User
import io.getstream.chat.android.randomString
import io.getstream.chat.android.randomUser
import okhttp3.OkHttpClient
import okhttp3.WebSocketListener
import org.amshove.kluent.`should be equal to`
import org.junit.jupiter.params.ParameterizedTest
import org.junit.jupiter.params.provider.Arguments
import org.junit.jupiter.params.provider.MethodSource
import org.mockito.kotlin.any
import org.mockito.kotlin.doReturn
import org.mockito.kotlin.mock
import org.mockito.kotlin.only
import org.mockito.kotlin.verify
import org.mockito.kotlin.whenever
import java.net.URLEncoder
import java.nio.charset.StandardCharsets
import java.util.Locale

internal class SocketFactoryTest {

    private val httpClient: OkHttpClient = mock<OkHttpClient>().apply {
        whenever(this.newWebSocket(any(), any())) doReturn mock()
    }

    private val socketFactory = SocketFactory(
        chatParser,
        FakeTokenManager(token, loadSyncToken),
        headersUtil,
        httpClient,
    )

    /** [arguments] */
    @ParameterizedTest
    @MethodSource("arguments")
    internal fun testCreateSocket(connectionConf: SocketFactory.ConnectionConf, expectedUrl: String) {
        socketFactory.createSocket(connectionConf)

        verify(httpClient, only()).newWebSocket(
            org.mockito.kotlin.check {
                it.url.toString() `should be equal to` expectedUrl
            },
            any<WebSocketListener>(),
        )
    }

    companion object {
        private val chatParser: ChatParser = ParserFactory.createMoshiChatParser()
        private val endpoint = "https://${randomString().lowercase(Locale.getDefault())}/"
        private val apiKey = randomString()
        private val token = randomString()
        private val loadSyncToken = randomString()
        private val headersUtil: HeadersUtil = mock<HeadersUtil>().apply {
            whenever(this.buildSdkTrackingHeaders()) doReturn "mocked-header-value"
        }

        @JvmStatic
        @Suppress("MaxLineLength")
        fun arguments() = listOf(
            randomUser(image = randomString(), name = randomString(), language = randomString()).let {
                Arguments.of(
                    SocketFactory.ConnectionConf.UserConnectionConf(endpoint, apiKey, it),
                    "${endpoint}connect?json=${buildFullUserJson(it, it.id)}&api_key=$apiKey&authorization=$token&stream-auth-type=jwt",
                )
            },
            randomUser().let {
                Arguments.of(
                    SocketFactory.ConnectionConf.UserConnectionConf(endpoint, apiKey, it).asReconnectionConf(),
                    "${endpoint}connect?json=${buildMinimumUserJson(it.id)}&api_key=$apiKey&authorization=$loadSyncToken&stream-auth-type=jwt",
                )
            },
            User("anon").let {
                Arguments.of(
                    SocketFactory.ConnectionConf.AnonymousConnectionConf(endpoint, apiKey, it).asReconnectionConf(),
                    "${endpoint}connect?json=${buildMinimumUserJson(it.id)}&api_key=$apiKey&stream-auth-type=anonymous",
                )
            },
            User("!anon").let {
                Arguments.of(
                    SocketFactory.ConnectionConf.AnonymousConnectionConf(endpoint, apiKey, it).asReconnectionConf(),
                    "${endpoint}connect?json=${buildMinimumUserJson("anon")}&api_key=$apiKey&stream-auth-type=anonymous",
                )
            },
        )

        private fun buildMinimumUserJson(userId: String): String = encode(
            defaultMap(userId, mapOf("id" to userId)),
        )

        private fun buildFullUserJson(user: User, userId: String): String = encode(
            defaultMap(
                userId,
                mapOf(
                    "id" to userId,
                    "role" to user.role,
                    "banned" to user.isBanned,
                    "invisible" to user.isInvisible,
                    "language" to user.language,
                    "image" to user.image,
                    "name" to user.name,
                ) + user.extraData,
            ),
        )

        private fun defaultMap(userId: String, userDetails: Map<String, Any>): Map<String, Any> =
            mapOf(
                "user_details" to userDetails,
                "user_id" to userId,
                "server_determines_connection_id" to true,
                "X-Stream-Client" to headersUtil.buildSdkTrackingHeaders(),
            )

        private fun encode(map: Map<String, Any>): String =
            URLEncoder.encode(
                chatParser.toJson(map),
                StandardCharsets.UTF_8.name(),
            )
    }
}
