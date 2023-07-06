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

import io.getstream.chat.android.client.api.ChatClientConfig
import io.getstream.chat.android.client.errors.ChatError
import io.getstream.chat.android.client.errors.ChatErrorCode
import io.getstream.chat.android.client.errors.ChatNetworkError
import io.getstream.chat.android.client.events.ConnectedEvent
import io.getstream.chat.android.client.models.User
import io.getstream.chat.android.client.network.NetworkStateProvider
import io.getstream.chat.android.client.socket.SocketFactory
import io.getstream.chat.android.client.socket.SocketFactory.ConnectionConf
import io.getstream.chat.android.client.socket.SocketFactory.ConnectionConf.AnonymousConnectionConf
import io.getstream.chat.android.client.socket.SocketFactory.ConnectionConf.UserConnectionConf
import io.getstream.chat.android.client.socket.experimental.ws.StreamWebSocket
import io.getstream.chat.android.client.socket.experimental.ws.StreamWebSocketEvent
import io.getstream.chat.android.client.utils.Result
import io.getstream.chat.android.client.utils.toResultError
import io.getstream.logging.StreamLog
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.mapNotNull
import kotlinx.coroutines.withTimeoutOrNull
import java.io.UnsupportedEncodingException

/**
 * Fetches the current user from the backend.
 */
internal class CurrentUserFetcher(
    private val networkStateProvider: NetworkStateProvider,
    private val socketFactory: SocketFactory,
    private val config: ChatClientConfig,
) {

    private val logger = StreamLog.getLogger("Chat:CurrentUserFetcher")

    suspend fun fetch(currentUser: User): Result<User> {
        logger.d { "[fetch] no args" }
        if (!networkStateProvider.isConnected()) {
            logger.w { "[fetch] rejected (no internet connection)" }
            return Result.error(ChatNetworkError.create(ChatErrorCode.NETWORK_FAILED))
        }
        var ws: StreamWebSocket? = null
        return try {
            ws = socketFactory.createSocket(currentUser.toConnectionConf(config))
            ws.listen().firstUserWithTimeout(TIMEOUT_MS).also {
                logger.v { "[fetch] completed: $it" }
            }
        } catch (e: UnsupportedEncodingException) {
            logger.e { "[fetch] failed: $e" }
            Result.error(ChatError(e.message, e))
        } finally {
            try {
                ws?.close()
            } catch (_: IllegalArgumentException) {
                // no-op
            }
        }
    }

    private fun User.toConnectionConf(config: ChatClientConfig): ConnectionConf = when (config.isAnonymous) {
        true -> AnonymousConnectionConf(config.wssUrl, config.apiKey, this)
        false -> UserConnectionConf(config.wssUrl, config.apiKey, this)
    }.asReconnectionConf()

    private suspend fun Flow<StreamWebSocketEvent>.firstUserWithTimeout(
        timeMillis: Long,
    ): Result<User> = withTimeoutOrNull(timeMillis) {
        mapNotNull {
            when (it) {
                is StreamWebSocketEvent.Error -> it.chatError.toResultError()
                is StreamWebSocketEvent.Message -> (it.chatEvent as? ConnectedEvent)?.let { Result.success(it.me) }
            }
        }
            .first()
    } ?: Result.error(ChatError("Timeout while fetching current user"))

    private companion object {
        private const val TIMEOUT_MS = 15_000L
    }
}
