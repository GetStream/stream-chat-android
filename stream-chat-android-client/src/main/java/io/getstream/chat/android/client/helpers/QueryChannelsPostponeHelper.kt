package io.getstream.chat.android.client.helpers

import io.getstream.chat.android.client.api.ChatApi
import io.getstream.chat.android.client.api.ErrorCall
import io.getstream.chat.android.client.api.models.QueryChannelRequest
import io.getstream.chat.android.client.api.models.QueryChannelsRequest
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

internal class QueryChannelsPostponeHelper(
    private val api: ChatApi,
    private val socketStateService: SocketStateService,
    private val coroutineScope: CoroutineScope,
    private val delayDuration: Long = DELAY_DURATION,
    private val attemptsCount: Int = MAX_ATTEMPTS_COUNT,
) {

    internal fun queryChannel(
        channelType: String,
        channelId: String,
        request: QueryChannelRequest,
    ): Call<Channel> = CoroutineCall(coroutineScope) {
        doSafeJob { api.queryChannel(channelType, channelId, request) }.await()
    }

    internal fun queryChannels(request: QueryChannelsRequest): Call<List<Channel>> = CoroutineCall(coroutineScope) {
        doSafeJob { api.queryChannels(request) }.await()
    }

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
            is SocketState.Idle -> error("Socket connection must be established before querying channels")
            is SocketState.Connected, -> job()
            is SocketState.Pending, SocketState.Disconnected -> {
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
