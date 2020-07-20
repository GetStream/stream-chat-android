package io.getstream.chat.android.client.socket.okhttp

import io.getstream.chat.android.client.ChatClient
import io.getstream.chat.android.client.events.ChatEvent
import io.getstream.chat.android.client.logger.ChatLogger
import io.getstream.chat.android.client.models.User
import io.getstream.chat.android.client.parser.ChatParser
import io.getstream.chat.android.client.socket.EventsParser
import io.getstream.chat.android.client.socket.Socket
import io.getstream.chat.android.client.socket.SocketFactory
import io.getstream.chat.android.client.token.TokenManager
import okhttp3.OkHttpClient
import okhttp3.Request
import okhttp3.WebSocket
import java.io.UnsupportedEncodingException
import java.net.URLEncoder
import java.nio.charset.StandardCharsets

internal class OkHttpSocketFactory(
    private val eventsParser: EventsParser,
    private val parser: ChatParser,
    private val tokenManager: TokenManager
) : SocketFactory {

    private val logger = ChatLogger.get(SocketFactory::class.java.simpleName)
    private val httpClient = OkHttpClient()

    override fun create(endpoint: String, apiKey: String, user: User?): Socket {

        val url = buildUrl(endpoint, apiKey, user)
        val request = Request.Builder().url(url).build()
        val newWebSocket = httpClient.newWebSocket(request, eventsParser)

        logger.logI("new web socket: $url")

        return OkHttpSocket(newWebSocket, parser)
    }

    private fun buildUrl(endpoint: String, apiKey: String, user: User?): String {
        var json = buildUserDetailJson(user)
        return try {
            json = URLEncoder.encode(json, StandardCharsets.UTF_8.name())
            val baseWsUrl: String =
                endpoint + "connect?json=" + json + "&api_key=" + apiKey
            if (user == null) {
                "$baseWsUrl&stream-auth-type=anonymous"
            } else {
                val token = tokenManager.getToken()
                "$baseWsUrl&authorization=$token&stream-auth-type=jwt"
            }
        } catch (throwable: Throwable) {
            throw UnsupportedEncodingException("Unable to encode user details json: $json")
        }
    }

    private fun buildUserDetailJson(user: User?): String {
        val data = mutableMapOf<String, Any>()
        user?.let {
            data["user_details"] = user
            data["user_id"] = user.id
        }
        data["server_determines_connection_id"] = true
        data["X-STREAM-CLIENT"] = ChatClient.instance().getVersion()
        return parser.toJson(data)
    }

    class OkHttpSocket(val socket: WebSocket, val parser: ChatParser) : Socket {

        override fun send(event: ChatEvent) {
            socket.send(parser.toJson(event))
        }

        override fun cancel() {
            socket.cancel()
        }

        override fun close(code: Int, reason: String) {
            socket.close(code, reason)
        }
    }
}
