package io.getstream.chat.android.offline.usecase

import androidx.annotation.CheckResult
import io.getstream.chat.android.client.call.Call
import io.getstream.chat.android.client.call.CoroutineCall
import io.getstream.chat.android.client.call.await
import io.getstream.chat.android.client.utils.Result
import io.getstream.chat.android.core.internal.coroutines.DispatcherProvider
import io.getstream.chat.android.offline.ChatDomainImpl
import kotlinx.coroutines.async
import kotlinx.coroutines.awaitAll

internal class MarkAllRead(private val domain: ChatDomainImpl) {
    @CheckResult
    fun invoke(): Call<Boolean> = CoroutineCall(domain.scope) {
        // update the UI first
        domain.allActiveChannels().map { channel ->
            domain.scope.async(DispatcherProvider.Main) {
                channel.markRead()
            }
        }.awaitAll() // wait for the UI updates to avoid races

        // then update via remote API
        domain.client.markAllRead().await()
        Result(true)
    }
}
