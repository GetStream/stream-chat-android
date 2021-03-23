package io.getstream.chat.android.client.helpers

import io.getstream.chat.android.client.api.ChatApi
import io.getstream.chat.android.client.api.ErrorCall
import io.getstream.chat.android.client.api.models.QueryChannelRequest
import io.getstream.chat.android.client.api.models.QueryChannelsRequest
import io.getstream.chat.android.client.call.Call
import io.getstream.chat.android.client.call.CoroutineCall
import io.getstream.chat.android.client.clientstate.ClientState
import io.getstream.chat.android.client.clientstate.ClientStateService
import io.getstream.chat.android.client.errors.ChatError
import io.getstream.chat.android.client.models.Channel
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.delay
import java.util.concurrent.TimeUnit

internal class QueryChannelsPostponeHelper(
    private val api: ChatApi,
    private val clientStateService: ClientStateService,
    private val coroutineScope: CoroutineScope = GlobalScope,
    private val delayDuration: Long = DELAY_DURATION,
    private val attemptsCount: Int = MAX_ATTEMPTS_COUNT
) {

    internal fun queryChannel(
        channelType: String,
        channelId: String,
        request: QueryChannelRequest
    ): Call<Channel> = CoroutineCall(coroutineScope) {
        doSafeJob { api.queryChannel(channelType, channelId, request) }.execute()
    }

    internal fun queryChannels(request: QueryChannelsRequest): Call<List<Channel>> = CoroutineCall(coroutineScope) {
        doSafeJob { api.queryChannels(request) }.execute()
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
        return when (clientStateService.state) {
            is ClientState.Idle -> error("User must be set before querying channels")
            is ClientState.User.Authorized,
            is ClientState.Anonymous.Authorized -> job()
            is ClientState.User.Pending,
            is ClientState.Anonymous.Pending -> {
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
