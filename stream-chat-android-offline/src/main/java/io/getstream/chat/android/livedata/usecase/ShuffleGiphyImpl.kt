package io.getstream.chat.android.livedata.usecase

import androidx.annotation.CheckResult
import io.getstream.chat.android.client.call.Call
import io.getstream.chat.android.client.models.Message
import io.getstream.chat.android.livedata.ChatDomain

public sealed interface ShuffleGiphy {
    /**
     * Performs giphy shuffle operation. Removes the original "ephemeral" message from local storage.
     * Returns new "ephemeral" message with new giphy url.
     * API call to remove the message is retried according to the retry policy specified on the chatDomain
     * @param message the message to send
     * @see io.getstream.chat.android.livedata.utils.RetryPolicy
     */
    @CheckResult
    public operator fun invoke(message: Message): Call<Message>
}

internal class ShuffleGiphyImpl(private val chatDomain: ChatDomain) : ShuffleGiphy {
    override operator fun invoke(message: Message): Call<Message> = chatDomain.shuffleGiphy(message)
}
