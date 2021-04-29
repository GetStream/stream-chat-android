package io.getstream.chat.android.client.socket

import io.getstream.chat.android.client.ChatClient
import io.getstream.chat.android.client.logger.ChatLogger
import io.getstream.chat.android.client.models.User
import io.getstream.chat.android.client.parser.ChatParser
import io.getstream.chat.android.client.token.TokenManager
import okhttp3.OkHttpClient
import okhttp3.Request
import java.io.UnsupportedEncodingException
import java.net.URLEncoder
import java.nio.charset.StandardCharsets

internal class SocketFactory(
    private val parser: ChatParser,
    private val tokenManager: TokenManager,
) {

    private val logger = ChatLogger.get(SocketFactory::class.java.simpleName)
    private val httpClient = OkHttpClient()

    fun createAnonymousSocket(eventsParser: EventsParser, endpoint: String, apiKey: String): Socket =
        create(eventsParser, endpoint, apiKey, User(ANONYMOUS_USER_ID), true)

    fun createNormalSocket(eventsParser: EventsParser, endpoint: String, apiKey: String, user: User): Socket =
        create(eventsParser, endpoint, apiKey, user, false)

    private fun create(eventsParser: EventsParser, endpoint: String, apiKey: String, user: User, isAnonymous: Boolean): Socket {
        val url = buildUrl(endpoint, apiKey, user, isAnonymous)
        val request = Request.Builder().url(url).build()
        val newWebSocket = httpClient.newWebSocket(request, eventsParser)

        logger.logI("new web socket: $url")

        return Socket(newWebSocket, parser)
    }

    private fun buildUrl(endpoint: String, apiKey: String, user: User, isAnonymous: Boolean): String {
        var json = buildUserDetailJson(user)
        return try {
            json = URLEncoder.encode(json, StandardCharsets.UTF_8.name())
            val baseWsUrl: String =
                endpoint + "connect?json=" + json + "&api_key=" + apiKey
            if (isAnonymous) {
                "$baseWsUrl&stream-auth-type=anonymous"
            } else {
                val token = tokenManager.getToken()
                "$baseWsUrl&authorization=$token&stream-auth-type=jwt"
            }
        } catch (throwable: Throwable) {
            throw UnsupportedEncodingException("Unable to encode user details json: $json")
        }
    }

    private fun buildUserDetailJson(user: User): String {
        val data = mapOf(
            "user_details" to user.reduceUserDetails(),
            "user_id" to user.id,
            "server_determines_connection_id" to true,
            "X-Stream-Client" to ChatClient.instance().getVersion()
        )
        return parser.toJson(data)
    }

    companion object {
        /**
         *  It doesn't matter what user id we send to the server for anonymous user
         *  as the server will always return the user with "!anon" user id
         */
        private const val ANONYMOUS_USER_ID = "anon"
    }

    private fun User.reduceUserDetails(): Map<String, Any> {
        val details = mutableMapOf(
            "id" to id,
            "role" to role,
            "banned" to banned,
            "invisible" to invisible,
            "teams" to teams,
        )
        details.putAll(extraData)
        return details
    }
}
