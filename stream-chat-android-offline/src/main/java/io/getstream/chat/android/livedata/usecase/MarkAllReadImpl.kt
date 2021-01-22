package io.getstream.chat.android.livedata.usecase

import androidx.annotation.CheckResult
import io.getstream.chat.android.client.call.Call
import io.getstream.chat.android.client.call.CoroutineCall
import io.getstream.chat.android.client.utils.Result
import io.getstream.chat.android.core.internal.coroutines.DispatcherProvider
import io.getstream.chat.android.livedata.ChatDomainImpl
import kotlinx.coroutines.async
import kotlinx.coroutines.awaitAll

public interface MarkAllRead {
    @CheckResult
    public operator fun invoke(): Call<Boolean>
}

internal data class MarkAllReadImpl(private val domain: ChatDomainImpl) : MarkAllRead {
    override fun invoke(): Call<Boolean> = CoroutineCall(domain.scope) {
        // update the UI first
        domain.allActiveChannels().map { channel ->
            domain.scope.async(DispatcherProvider.Main) {
                channel.markRead()
            }
        }.awaitAll() // wait for the UI updates to avoid races

        // then update via remote API
        domain.client.markAllRead().execute()
        Result(true)
    }
}
