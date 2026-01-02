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

package io.getstream.chat.android.client.user

import io.getstream.chat.android.client.api.ChatClientConfig
import io.getstream.chat.android.client.errors.ChatErrorCode
import io.getstream.chat.android.client.events.ConnectedEvent
import io.getstream.chat.android.client.network.NetworkStateProvider
import io.getstream.chat.android.client.socket.SocketFactory
import io.getstream.chat.android.client.socket.SocketFactory.ConnectionConf
import io.getstream.chat.android.client.socket.SocketFactory.ConnectionConf.AnonymousConnectionConf
import io.getstream.chat.android.client.socket.SocketFactory.ConnectionConf.UserConnectionConf
import io.getstream.chat.android.client.socket.StreamWebSocket
import io.getstream.chat.android.client.socket.StreamWebSocketEvent
import io.getstream.chat.android.models.User
import io.getstream.log.taggedLogger
import io.getstream.result.Error
import io.getstream.result.Result
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

    private val logger by taggedLogger("Chat:CurrentUserFetcher")

    suspend fun fetch(currentUser: User): Result<User> {
        logger.d { "[fetch] no args" }
        if (!networkStateProvider.isConnected()) {
            logger.w { "[fetch] rejected (no internet connection)" }
            return Result.Failure(ChatErrorCode.NETWORK_FAILED.toNetworkError())
        }
        var ws: StreamWebSocket? = null
        return try {
            ws = socketFactory.createSocket(currentUser.toConnectionConf(config))
            ws.listen().firstUserWithTimeout(TIMEOUT_MS).also {
                logger.v { "[fetch] completed: $it" }
            }
        } catch (e: UnsupportedEncodingException) {
            logger.e { "[fetch] failed: $e" }
            Result.Failure(Error.ThrowableError(e.message.orEmpty(), e))
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
                is StreamWebSocketEvent.Error -> Result.Failure(it.streamError)
                is StreamWebSocketEvent.Message -> (it.chatEvent as? ConnectedEvent)?.let { Result.Success(it.me) }
            }
        }
            .first()
    } ?: Result.Failure(Error.GenericError("Timeout while fetching current user"))

    private fun ChatErrorCode.toNetworkError() = Error.NetworkError(
        message = description,
        serverErrorCode = code,
    )

    private companion object {
        private const val TIMEOUT_MS = 15_000L
    }
}
