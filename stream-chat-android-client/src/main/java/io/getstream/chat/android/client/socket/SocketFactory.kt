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

package io.getstream.chat.android.client.socket

import io.getstream.chat.android.PrivacySettings
import io.getstream.chat.android.client.api2.mapping.USER_DEVICES_CUSTOM_KEY
import io.getstream.chat.android.client.parser.ChatParser
import io.getstream.chat.android.client.parser2.V2EventParser
import io.getstream.chat.android.client.token.TokenManager
import io.getstream.chat.android.client.utils.HeadersUtil
import io.getstream.chat.android.models.User
import io.getstream.log.taggedLogger
import okhttp3.OkHttpClient
import okhttp3.Request
import java.io.UnsupportedEncodingException
import java.net.URLEncoder
import java.nio.charset.StandardCharsets

/**
 * @param v2EventParser Connects through the v2 endpoint and parses its events with it, or null for v1.
 */
internal class SocketFactory(
    private val parser: ChatParser,
    private val tokenManager: TokenManager,
    private val headersUtil: HeadersUtil,
    private val httpClient: OkHttpClient = OkHttpClient(),
    private val v2EventParser: V2EventParser? = null,
) {
    private val logger by taggedLogger("Chat:SocketFactory")

    @Throws(UnsupportedEncodingException::class)
    fun createSocket(connectionConf: ConnectionConf): StreamWebSocket {
        if (v2EventParser != null) return createV2Socket(connectionConf, v2EventParser)
        val request = buildRequest(connectionConf)
        logger.i { "new web socket: ${request.url}" }
        return StreamWebSocket(parser) { httpClient.newWebSocket(request, it) }
    }

    /**
     * The v2 socket authenticates with a frame sent once open, which must arrive within the server's handshake
     * deadline, so the token is resolved before the upgrade. The request carries no Authorization header: the server
     * prefers it over the frame's token.
     */
    private fun createV2Socket(connectionConf: ConnectionConf, eventParser: V2EventParser): StreamWebSocket {
        val authType = when (connectionConf) {
            is ConnectionConf.AnonymousConnectionConf -> "anonymous"
            is ConnectionConf.UserConnectionConf -> "jwt"
        }
        val request = Request.Builder()
            .url(
                "${connectionConf.endpoint}api/v2/connect?api_key=${connectionConf.apiKey}" +
                    "&stream-auth-type=$authType" +
                    "&$X_STREAM_CLIENT=${encodedSdkTrackingHeaders()}",
            )
            .addHeader(X_STREAM_CLIENT, headersUtil.buildSdkTrackingHeaders())
            .build()
        logger.i { "new v2 web socket: ${request.url}" }
        val protocol = V2SocketProtocol(buildAuthFrame(connectionConf), eventParser)
        return StreamWebSocket(parser, protocol) { httpClient.newWebSocket(request, it) }
    }

    private fun encodedSdkTrackingHeaders(): String =
        URLEncoder.encode(headersUtil.buildSdkTrackingHeaders(), StandardCharsets.UTF_8.name())

    private fun buildAuthFrame(connectionConf: ConnectionConf): String {
        val token = when (connectionConf) {
            // iOS sends this placeholder until anonymous connections with an empty token are deployed everywhere.
            is ConnectionConf.AnonymousConnectionConf -> ANONYMOUS_TOKEN
            is ConnectionConf.UserConnectionConf -> {
                tokenManager.ensureTokenLoaded()
                tokenManager.getToken()
            }
        }
        val frame = mapOf(
            "token" to token,
            "user_details" to connectionConf.reduceV2UserDetails(),
            "products" to listOf("chat"),
        )
        return V2SocketProtocol.frameAdapter.toJson(frame)
    }

    /**
     * The user details the v2 connect reads, with custom data nested under `custom`; it ignores any other field.
     * Reconnections send the id only, as on v1.
     */
    private fun ConnectionConf.reduceV2UserDetails(): Map<String, Any> = mutableMapOf<String, Any>("id" to id)
        .apply {
            if (!isReconnection) {
                if (user.name.isNotBlank()) put("name", user.name)
                if (user.image.isNotBlank()) put("image", user.image)
                if (user.language.isNotBlank()) put("language", user.language)
                user.invisible?.also { put("invisible", it) }
                user.privacySettings?.also { put("privacy_settings", it.reducePrivacySettings()) }
                val custom = user.extraData - USER_DEVICES_CUSTOM_KEY
                if (custom.isNotEmpty()) put("custom", custom)
            }
        }

    @Throws(UnsupportedEncodingException::class)
    private fun buildRequest(connectionConf: ConnectionConf): Request =
        Request.Builder()
            .url(buildUrl(connectionConf))
            .addHeader(X_STREAM_CLIENT, headersUtil.buildSdkTrackingHeaders())
            .build()

    @Suppress("TooGenericExceptionCaught")
    @Throws(UnsupportedEncodingException::class)
    private fun buildUrl(connectionConf: ConnectionConf): String {
        var json = buildUserDetailJson(connectionConf)
        return try {
            json = URLEncoder.encode(json, StandardCharsets.UTF_8.name())
            val sdkTrackingHeaders = encodedSdkTrackingHeaders()
            val baseWsUrl = "${connectionConf.endpoint}connect?" +
                "json=$json" +
                "&api_key=${connectionConf.apiKey}" +
                "&$X_STREAM_CLIENT=$sdkTrackingHeaders"
            when (connectionConf) {
                is ConnectionConf.AnonymousConnectionConf -> "$baseWsUrl&stream-auth-type=anonymous"
                is ConnectionConf.UserConnectionConf -> {
                    tokenManager.ensureTokenLoaded()
                    val token = tokenManager.getToken()
                    "$baseWsUrl&authorization=$token&stream-auth-type=jwt"
                }
            }
        } catch (_: UnsupportedEncodingException) {
            throw UnsupportedEncodingException("Unable to encode user details json: $json")
        }
    }

    private fun buildUserDetailJson(connectionConf: ConnectionConf): String {
        val data = mapOf(
            "user_details" to connectionConf.reduceUserDetails(),
            "user_id" to connectionConf.id,
            "server_determines_connection_id" to true,
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
                if (user.role.isNotBlank()) put("role", user.role)
                user.banned?.also { put("banned", it) }
                user.invisible?.also { put("invisible", it) }
                user.privacySettings?.also { put("privacy_settings", it.reducePrivacySettings()) }
                if (user.teams.isNotEmpty()) put("teams", user.teams)
                if (user.language.isNotBlank()) put("language", user.language)
                if (user.image.isNotBlank()) put("image", user.image)
                if (user.name.isNotBlank()) put("name", user.name)
                putAll(user.extraData - USER_DEVICES_CUSTOM_KEY)
            }
        }

    private fun PrivacySettings.reducePrivacySettings(): Map<String, Any> = mutableMapOf<String, Any>()
        .apply {
            typingIndicators?.let {
                put("typing_indicators", mapOf<String, Any>("enabled" to it.enabled))
            }
            deliveryReceipts?.let {
                put("delivery_receipts", mapOf<String, Any>("enabled" to it.enabled))
            }
            readReceipts?.let {
                put("read_receipts", mapOf<String, Any>("enabled" to it.enabled))
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

    companion object {
        private const val X_STREAM_CLIENT = "X-Stream-Client"
        private const val ANONYMOUS_TOKEN = "anon"
    }
}
