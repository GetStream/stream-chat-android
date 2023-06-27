package io.getstream.chat.android.client.user

import androidx.core.net.toUri
import io.getstream.chat.android.client.ChatClient
import io.getstream.chat.android.client.api.ChatClientConfig
import io.getstream.chat.android.client.errors.ChatError
import io.getstream.chat.android.client.errors.ChatErrorCode
import io.getstream.chat.android.client.errors.ChatNetworkError
import io.getstream.chat.android.client.events.ChatEvent
import io.getstream.chat.android.client.events.ConnectedEvent
import io.getstream.chat.android.client.models.User
import io.getstream.chat.android.client.network.NetworkStateProvider
import io.getstream.chat.android.client.parser.ChatParser
import io.getstream.chat.android.client.scope.UserScope
import io.getstream.chat.android.client.socket.EventsParser.Companion.CODE_CLOSE_SOCKET_FROM_CLIENT
import io.getstream.chat.android.client.socket.SocketErrorMessage
import io.getstream.chat.android.client.token.TokenManager
import io.getstream.chat.android.client.utils.Result
import io.getstream.logging.StreamLog
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch
import kotlinx.coroutines.withTimeoutOrNull
import okhttp3.OkHttpClient
import okhttp3.Request
import okhttp3.Response
import okhttp3.WebSocket
import okhttp3.WebSocketListener
import java.net.URLEncoder
import java.nio.charset.StandardCharsets

internal interface CurrentUserFetcher {
    suspend fun fetch(): Result<User>
}

internal class CurrentUserFetcherImpl(
    private val getCurrentUserId: () -> String?,
    private val userScope: UserScope,
    private val httpClient: OkHttpClient,
    private val chatParser: ChatParser,
    private val tokenManager: TokenManager,
    private val networkStateProvider: NetworkStateProvider,
    private val config: ChatClientConfig,
) : CurrentUserFetcher, WebSocketListener() {

    private val logger = StreamLog.getLogger("Chat:CurrentUserFetcher")

    private val resultFlow = MutableSharedFlow<Result<User>>()

    override suspend fun fetch(): Result<User> {
        logger.d { "[fetch] no args" }
        if (!networkStateProvider.isConnected()) {
            logger.w { "[fetch] rejected (no internet connection)" }
            return Result.error(ChatNetworkError.create(ChatErrorCode.NETWORK_FAILED))
        }

        val url = buildUrl()
        logger.v { "[fetch] url: $url" }
        val request = Request.Builder()
            .url(url)
            .build()
        val ws = httpClient.newWebSocket(request, this)

        //TODO magic number
        try {
            val result = withTimeoutOrNull(10_000) {
                resultFlow.first()
            } ?: Result.error(ChatError("Timeout while fetching current user"))
            logger.v { "[fetch] completed: $result" }
            return result
        } catch (e: Exception) {
            logger.e { "[fetch] failed: $e" }
            return Result.error(ChatError(e.message, e))
        } finally {
            try {
                ws.close(CLOSE_SOCKET_CODE, CLOSE_SOCKET_REASON)
            } catch (_: Exception) {
                // no-op
            }
        }
    }

    private fun buildUrl(): String {
        val userId = getCurrentUserId()
        val payload = mapOf(
            "user_details" to mapOf(
                "id" to userId,
            ),
            "user_id" to userId,
            "server_determines_connection_id" to true,
            "X-Stream-Client" to ChatClient.buildSdkTrackingHeaders(),
        )
        val jsonPayload = chatParser.toJson(payload)

        return config.wssUrl.toUri().buildUpon()
            .apply {
                appendPath("connect")
                appendQueryParameter("json", jsonPayload)
                appendQueryParameter("api_key", config.apiKey)
                if (userId == ChatClient.ANONYMOUS_USER_ID) {
                    appendQueryParameter("stream-auth-type", "anonymous")
                } else {
                    appendQueryParameter("stream-auth-type", "jwt")
                    appendQueryParameter("authorization", tokenManager.getToken())
                }
            }
            .build()
            .toString()
    }

    override fun onOpen(webSocket: WebSocket, response: Response) {
        logger.i { "[onOpen] response: $response" }
    }

    override fun onFailure(webSocket: WebSocket, t: Throwable, response: Response?) {
        logger.e { "[onFailure] response: $response; failure: $t" }

        if (response?.code == CODE_CLOSE_SOCKET_FROM_CLIENT) {
            logger.i { "[onFailure] socket closed by client" }
            return
        }
        resultFlow.tryEmit(
            Result.error(ChatNetworkError.create(ChatErrorCode.SOCKET_CLOSED))
        )
    }

    override fun onMessage(webSocket: WebSocket, text: String) {
        userScope.launch {
            try {
                logger.i { "[onMessage] text: $text" }
                val errorResult = chatParser.fromJsonOrError(text, SocketErrorMessage::class.java)
                if (errorResult.isSuccess && errorResult.data().error != null) {
                    val error = errorResult.data().error!!
                    resultFlow.emit(
                        Result.error(
                            ChatNetworkError.create(error.code, error.message, error.statusCode)
                        )
                    )
                    return@launch
                }
                val eventResult = chatParser.fromJsonOrError(text, ChatEvent::class.java)
                if (eventResult.isError) {
                    val error = eventResult.error()
                    logger.e { "[onMessage] failed to parse event: $error" }
                    resultFlow.emit(
                        Result.error(ChatError("Failed to parse event: $error"))
                    )
                    return@launch
                }
                if (eventResult.isSuccess) {
                    val event = eventResult.data()
                    if (event is ConnectedEvent) {
                        logger.i { "[onMessage] connected event received" }
                        resultFlow.emit(Result.success(event.me))
                    }
                }
            } catch (t: Throwable) {
                logger.e(t) { "[onMessage] failed: $t" }
                resultFlow.emit(
                    Result.error(ChatError(message = t.message, cause = t))
                )
            }
        }
    }

    companion object {
        private const val CLOSE_SOCKET_CODE = 1000
        private const val CLOSE_SOCKET_REASON = "Connection close by client"
    }
}