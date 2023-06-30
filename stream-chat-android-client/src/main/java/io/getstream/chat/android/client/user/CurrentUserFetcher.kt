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

import androidx.core.net.toUri
import io.getstream.chat.android.client.ChatClient
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
import io.getstream.chat.android.client.utils.Result
import io.getstream.logging.StreamLog
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.SharedFlow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch
import kotlinx.coroutines.withTimeoutOrNull
import okhttp3.OkHttpClient
import okhttp3.Request
import okhttp3.Response
import okhttp3.WebSocket
import okhttp3.WebSocketListener

/**
 * Fetches the current user from the backend.
 */
internal interface CurrentUserFetcher {
    suspend fun fetch(): Result<User>
}

/**
 * Builds url for fetching the current user.
 */
internal interface CurrentUserUrlBuilder {
    fun buildUrl(): String
}

/**
 * Fetches the current user from the backend.
 */
internal class CurrentUserUrlBuilderImpl(
    private val getCurrentUserId: () -> String?,
    private val getBaseUrl: () -> String,
    private val getApiKey: () -> String,
    private val getToken: () -> String,
    private val chatParser: ChatParser,
) : CurrentUserUrlBuilder {
    override fun buildUrl(): String {
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

        return getBaseUrl().toUri().buildUpon()
            .apply {
                appendPath("connect")
                appendQueryParameter("json", jsonPayload)
                appendQueryParameter("api_key", getApiKey())
                if (userId == ChatClient.ANONYMOUS_USER_ID) {
                    appendQueryParameter("stream-auth-type", "anonymous")
                } else {
                    appendQueryParameter("stream-auth-type", "jwt")
                    appendQueryParameter("authorization", getToken())
                }
            }
            .build()
            .toString()
    }
}

/**
 * Builds url for fetching the current user.
 */
internal class CurrentUserFetcherImpl(
    private val userScope: UserScope,
    private val httpClient: OkHttpClient,
    private val chatParser: ChatParser,
    private val networkStateProvider: NetworkStateProvider,
    private val urlBuilder: CurrentUserUrlBuilder,
) : CurrentUserFetcher, WebSocketListener() {

    private val logger = StreamLog.getLogger("Chat:CurrentUserFetcher")

    private val resultFlow = MutableSharedFlow<Result<User>>()

    override suspend fun fetch(): Result<User> {
        logger.d { "[fetch] no args" }
        if (!networkStateProvider.isConnected()) {
            logger.w { "[fetch] rejected (no internet connection)" }
            return Result.error(ChatNetworkError.create(ChatErrorCode.NETWORK_FAILED))
        }
        var ws: WebSocket? = null
        try {
            val url = urlBuilder.buildUrl()
            logger.v { "[fetch] url: $url" }
            val request = Request.Builder()
                .url(url)
                .build()
            ws = httpClient.newWebSocket(request, this)
            return resultFlow.firstWithTimeout(TIMEOUT_MS).also {
                logger.v { "[fetch] completed: $it" }
            }
        } catch (e: Throwable) {
            logger.e { "[fetch] failed: $e" }
            return Result.error(ChatError(e.message, e))
        } finally {
            try {
                ws?.close(CLOSE_SOCKET_CODE, CLOSE_SOCKET_REASON)
            } catch (_: Exception) {
                // no-op
            }
        }
    }

    private suspend fun SharedFlow<Result<User>>.firstWithTimeout(
        timeMillis: Long,
    ): Result<User> = withTimeoutOrNull(timeMillis) {
        first()
    } ?: Result.error(ChatError("Timeout while fetching current user"))

    override fun onOpen(webSocket: WebSocket, response: Response) {
        logger.i { "[onOpen] response: $response" }
    }

    override fun onFailure(webSocket: WebSocket, t: Throwable, response: Response?) {
        userScope.launch {
            logger.e { "[onFailure] response: $response; failure: $t" }

            if (response?.code == CODE_CLOSE_SOCKET_FROM_CLIENT) {
                logger.i { "[onFailure] socket closed by client" }
                return@launch
            }
            resultFlow.emit(
                Result.error(ChatNetworkError.create(ChatErrorCode.SOCKET_CLOSED))
            )
        }
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

    private companion object {
        private const val TIMEOUT_MS = 15_000L
        private const val CLOSE_SOCKET_CODE = 1000
        private const val CLOSE_SOCKET_REASON = "Connection close by client"
    }
}
