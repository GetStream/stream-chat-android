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

import io.getstream.chat.android.client.ChatClient
import io.getstream.chat.android.client.parser.ChatParser
import io.getstream.chat.android.client.token.TokenManager
import io.getstream.chat.android.models.User
import io.getstream.log.taggedLogger
import okhttp3.OkHttpClient
import okhttp3.Request
import java.io.UnsupportedEncodingException
import java.net.URLEncoder
import java.nio.charset.StandardCharsets

internal class SocketFactory(
    private val parser: ChatParser,
    private val tokenManager: TokenManager,
    private val httpClient: OkHttpClient = OkHttpClient(),
) {
    private val logger by taggedLogger("Chat:SocketFactory")

    @Throws(UnsupportedEncodingException::class)
    fun createSocket(connectionConf: ConnectionConf): StreamWebSocket {
        val request = buildRequest(connectionConf)
        logger.i { "new web socket: ${request.url}" }
        return StreamWebSocket(parser) { httpClient.newWebSocket(request, it) }
    }

    @Throws(UnsupportedEncodingException::class)
    private fun buildRequest(connectionConf: ConnectionConf): Request =
        Request.Builder()
            .url(buildUrl(connectionConf))
            .build()

    @Suppress("TooGenericExceptionCaught")
    @Throws(UnsupportedEncodingException::class)
    private fun buildUrl(connectionConf: ConnectionConf): String {
        var json = buildUserDetailJson(connectionConf)
        return try {
            json = URLEncoder.encode(json, StandardCharsets.UTF_8.name())
            val baseWsUrl = "${connectionConf.endpoint}connect?json=$json&api_key=${connectionConf.apiKey}"
            when (connectionConf) {
                is ConnectionConf.AnonymousConnectionConf -> "$baseWsUrl&stream-auth-type=anonymous"
                is ConnectionConf.UserConnectionConf -> {
                    val token = tokenManager.getToken()
                    "$baseWsUrl&authorization=$token&stream-auth-type=jwt"
                }
            }
        } catch (_: Throwable) {
            throw UnsupportedEncodingException("Unable to encode user details json: $json")
        }
    }

    private fun buildUserDetailJson(connectionConf: ConnectionConf): String {
        val data = mapOf(
            "user_details" to connectionConf.reduceUserDetails(),
            "user_id" to connectionConf.id,
            "server_determines_connection_id" to true,
            "X-Stream-Client" to ChatClient.buildSdkTrackingHeaders(),
        )
        return parser.toJson(data)
    }

    /**
     * Converts the [User] object to a map of properties updated while connecting the user.
     * [User.name] and [User.image] will only be included if they are not blank.
     *
     * @return A map of User's properties to update.
     */
    private fun ConnectionConf.reduceUserDetails(): Map<String, Any> = mutableMapOf<String, Any>("id" to id)
        .apply {
            if (!isReconnection) {
                put("role", user.role)
                put("banned", user.banned)
                put("invisible", user.invisible)
                put("teams", user.teams)
                if (user.language.isNotBlank()) put("language", user.language)
                if (user.image.isNotBlank()) put("image", user.image)
                if (user.name.isNotBlank()) put("name", user.name)
                putAll(user.extraData)
            }
        }

    internal sealed class ConnectionConf {
        var isReconnection: Boolean = false
            private set
        abstract val endpoint: String
        abstract val apiKey: String
        abstract val user: User

        data class AnonymousConnectionConf(
            override val endpoint: String,
            override val apiKey: String,
            override val user: User,
        ) : ConnectionConf()

        data class UserConnectionConf(
            override val endpoint: String,
            override val apiKey: String,
            override val user: User,
        ) : ConnectionConf()

        internal fun asReconnectionConf(): ConnectionConf = this.also { isReconnection = true }

        internal val id: String
            get() = when (this) {
                is AnonymousConnectionConf -> user.id.replace("!", "")
                is UserConnectionConf -> user.id
            }
    }
}
