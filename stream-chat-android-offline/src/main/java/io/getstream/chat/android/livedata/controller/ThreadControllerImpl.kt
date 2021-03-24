package io.getstream.chat.android.livedata.controller

import androidx.lifecycle.LiveData
import androidx.lifecycle.asLiveData
import io.getstream.chat.android.client.models.Message
import io.getstream.chat.android.client.utils.Result
import io.getstream.chat.android.livedata.ChatDomainImpl
import io.getstream.chat.android.offline.channel.ChannelController
import io.getstream.chat.android.offline.thread.ThreadController as ThreadControllerStateFlow

/**
 * The ThreadControllerImpl sets up a few convenient livedata objects for working with threads
 *
 * * messages: a list of sorted thread messages
 * * loadingOlderMessages: if we are currently loading older messages
 * * endOfOlderMessages: if we've reached the earliest point in this thread
 */
internal class ThreadControllerImpl(
    override val threadId: String,
    val channelControllerImpl: ChannelController,
    val domain: ChatDomainImpl,
) : ThreadController {

    private val threadControllerStateFlow = ThreadControllerStateFlow(threadId, channelControllerImpl, domain)

    /** the sorted list of messages for this thread */
    override val messages = threadControllerStateFlow.messages.asLiveData()

    /** if we are currently loading older messages */
    override val loadingOlderMessages: LiveData<Boolean> = threadControllerStateFlow.loadingOlderMessages.asLiveData()

    /** if we've reached the earliest point in this thread */
    override val endOfOlderMessages: LiveData<Boolean> = threadControllerStateFlow.endOfOlderMessages.asLiveData()

    override fun getMessagesSorted(): List<Message> = threadControllerStateFlow.getMessagesSorted()

    suspend fun loadOlderMessages(limit: Int = 30): Result<List<Message>> =
        threadControllerStateFlow.loadOlderMessages(limit)
}
