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
import io.getstream.chat.android.client.clientstate.SocketState
import io.getstream.chat.android.client.clientstate.SocketStateService
import io.getstream.chat.android.client.errors.ChatError
import io.getstream.chat.android.client.helpers.CallPostponeHelper.Companion.DELAY_DURATION
import io.getstream.chat.android.client.helpers.CallPostponeHelper.Companion.MAX_ATTEMPTS_COUNT
import io.getstream.chat.android.client.scope.UserScope
import io.getstream.chat.android.client.utils.internal.toggle.ToggleService
import kotlinx.coroutines.delay
import java.util.concurrent.TimeUnit
import io.getstream.chat.android.client.socket.experimental.ChatSocket as ChatSocketExperimental

/**
 * Class responsible for postponing call until the socket connection is established.
 * The request will be retried [attemptsCount] times with a [delayDuration] ms delay between each request.
 *
 * @param socketStateService Service responsible for providing current socket state
 * @param userScope Coroutine scope where the call should be run.
 * @param delayDuration The delay duration between each query channels request. Default: [DELAY_DURATION].
 * @param attemptsCount Maximum number of attempts to be performed. Default: [MAX_ATTEMPTS_COUNT].
 */
internal class CallPostponeHelper(
    private val socketStateService: SocketStateService,
    private val userScope: UserScope,
    private val delayDuration: Long = DELAY_DURATION,
    private val attemptsCount: Int = MAX_ATTEMPTS_COUNT,
    private val chatSocketExperimental: ChatSocketExperimental? = null,
) {

    /**
     * Postpones or immediately executes the call based on [shouldPostpone] parameter.
     *
     * @param shouldPostpone Whether the call should be postponed
     * @param call A call to be run when the socket connection is established.
     *
     * @return Executable async [Call] responsible for querying channels
     */
    internal fun <T : Any> postponeCallIfNeeded(shouldPostpone: Boolean, call: () -> Call<T>): Call<T> {
        return if (shouldPostpone) {
            postponeCall(call)
        } else {
            call()
        }
    }

    /**
     * Postpones call.
     *
     * @param call A call to be run when the socket connection is established.
     *
     * @return Executable async [Call] responsible for querying channels
     */
    internal fun <T : Any> postponeCall(call: () -> Call<T>): Call<T> {
        return CoroutineCall(userScope) {
            doSafeJob {
                call()
            }.await()
        }
    }

    @Suppress("TooGenericExceptionCaught")
    private suspend fun <T : Any> doSafeJob(job: () -> Call<T>): Call<T> =
        try {
            doJob(attemptsCount, job)
        } catch (e: Exception) {
            ErrorCall(userScope, ChatError(e.message, e))
        }

    private tailrec suspend fun <T> doJob(attemptCount: Int = attemptsCount, job: () -> T): T {
        check(attemptCount > 0) {
            "Failed to perform job. Waiting for set user completion was too long. Limit of attempts was reached."
        }

        return if (ToggleService.isSocketExperimental()) {
            when (chatSocketExperimental!!.isConnected()) {
                true -> job()
                false -> {
                    delay(delayDuration)
                    doJob(attemptCount - 1, job)
                }
            }
        } else {
            when (socketStateService.state) {
                is SocketState.Connected -> job()
                is SocketState.Idle, SocketState.Pending, SocketState.Disconnected -> {
                    delay(delayDuration)
                    doJob(attemptCount - 1, job)
                }
            }
        }
    }

    companion object {
        private const val DELAY_DURATION = 200L
        private val MAX_DURATION = TimeUnit.SECONDS.toMillis(5)
        private val MAX_ATTEMPTS_COUNT = (MAX_DURATION / DELAY_DURATION).toInt()
    }
}
