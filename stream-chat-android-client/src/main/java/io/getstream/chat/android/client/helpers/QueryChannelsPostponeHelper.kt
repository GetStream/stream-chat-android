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

package io.getstream.chat.android.client.helpers

import io.getstream.chat.android.client.api.ErrorCall
import io.getstream.chat.android.client.call.Call
import io.getstream.chat.android.client.call.CoroutineCall
import io.getstream.chat.android.client.call.await
import io.getstream.chat.android.client.clientstate.SocketState
import io.getstream.chat.android.client.clientstate.SocketStateService
import io.getstream.chat.android.client.errors.ChatError
import io.getstream.chat.android.client.models.Channel
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.delay
import java.util.concurrent.TimeUnit

/**
 * Class responsible for postponing query channels request until the socket connection is established.
 * The request will be retried [attemptsCount] times with a [delayDuration] ms delay between each request.
 *
 * @param socketStateService Service responsible for providing current socket state
 * @param coroutineScope Coroutine scope where the call should be run.
 * @param delayDuration The delay duration between each query channels request. Default: [DELAY_DURATION].
 * @param attemptsCount Maximum number of attempts to be performed. Default: [MAX_ATTEMPTS_COUNT].
 */
internal class QueryChannelsPostponeHelper(
    private val socketStateService: SocketStateService,
    private val coroutineScope: CoroutineScope,
    private val delayDuration: Long = DELAY_DURATION,
    private val attemptsCount: Int = MAX_ATTEMPTS_COUNT,
) {

    /**
     * Postpones query channels call.
     *
     * @param queryChannelsCall A query channels call to be run when the socket connection is established.
     *
     * @return Executable async [Call] responsible for querying channels
     */
    internal fun postponeQueryChannels(queryChannelsCall: () -> Call<List<Channel>>): Call<List<Channel>> {
        return CoroutineCall(coroutineScope) {
            doSafeJob { queryChannelsCall() }.await()
        }
    }

    @Suppress("TooGenericExceptionCaught")
    private suspend fun <T : Any> doSafeJob(job: () -> Call<T>): Call<T> =
        try {
            doJob(attemptsCount, job)
        } catch (e: Exception) {
            ErrorCall(ChatError(e.message, e))
        }

    private tailrec suspend fun <T> doJob(attemptCount: Int = attemptsCount, job: () -> T): T {
        check(attemptCount > 0) {
            "Failed to perform job. Waiting for set user completion was too long. Limit of attempts was reached."
        }
        return when (socketStateService.state) {
            is SocketState.Connected -> job()
            is SocketState.Idle, SocketState.Pending, SocketState.Disconnected -> {
                delay(delayDuration)
                doJob(attemptCount - 1, job)
            }
        }
    }

    companion object {
        private const val DELAY_DURATION = 200L
        private val MAX_DURATION = TimeUnit.SECONDS.toMillis(5)
        private val MAX_ATTEMPTS_COUNT = (MAX_DURATION / DELAY_DURATION).toInt()
    }
}
