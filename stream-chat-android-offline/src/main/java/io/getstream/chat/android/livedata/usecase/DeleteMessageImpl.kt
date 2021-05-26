package io.getstream.chat.android.livedata.usecase

import androidx.annotation.CheckResult
import io.getstream.chat.android.client.call.Call
import io.getstream.chat.android.client.models.Message
import io.getstream.chat.android.livedata.ChatDomain

public sealed interface DeleteMessage {
    /**
     * Deletes the specified message, request is retried according to the retry policy specified on the chatDomain
     * @param message the message to mark as deleted
     * @see io.getstream.chat.android.livedata.utils.RetryPolicy
     */
    @CheckResult
    public operator fun invoke(message: Message): Call<Message>
}

internal class DeleteMessageImpl(private val chatDomain: ChatDomain) : DeleteMessage {
    override operator fun invoke(message: Message): Call<Message> = chatDomain.deleteMessage(message)
}
