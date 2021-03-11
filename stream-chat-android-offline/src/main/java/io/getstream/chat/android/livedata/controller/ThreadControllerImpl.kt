package io.getstream.chat.android.livedata.controller

import androidx.lifecycle.LiveData
import androidx.lifecycle.asLiveData
import io.getstream.chat.android.client.models.Message
import io.getstream.chat.android.client.utils.Result
import io.getstream.chat.android.offline.ThreadControllerImpl as NewThreadControllerImpl

/**
 * The ThreadControllerImpl sets up a few convenient livedata objects for working with threads
 *
 * * messages: a list of sorted thread messages
 * * loadingOlderMessages: if we are currently loading older messages
 * * endOfOlderMessages: if we've reached the earliest point in this thread
 */
internal class ThreadControllerImpl(val delegate: NewThreadControllerImpl) : ThreadController {

    override val threadId: String
        get() = delegate.threadId

    /** the sorted list of messages for this thread */
    override val messages = delegate.messages.asLiveData()

    /** if we are currently loading older messages */
    override val loadingOlderMessages: LiveData<Boolean> = delegate.loadingOlderMessages.asLiveData()

    /** if we've reached the earliest point in this thread */
    override val endOfOlderMessages: LiveData<Boolean> = delegate.endOfOlderMessages.asLiveData()

    override fun getMessagesSorted(): List<Message> = delegate.getMessagesSorted()

    suspend fun loadOlderMessages(limit: Int = 30): Result<List<Message>> = delegate.loadOlderMessages(limit)
}
