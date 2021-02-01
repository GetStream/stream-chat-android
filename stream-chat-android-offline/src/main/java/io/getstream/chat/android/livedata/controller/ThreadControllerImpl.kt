package io.getstream.chat.android.livedata.controller

import androidx.lifecycle.LiveData
import androidx.lifecycle.asLiveData
import io.getstream.chat.android.client.ChatClient
import io.getstream.chat.android.client.errors.ChatError
import io.getstream.chat.android.client.logger.ChatLogger
import io.getstream.chat.android.client.models.Message
import io.getstream.chat.android.client.utils.Result
import io.getstream.chat.android.livedata.ChatDomainImpl
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.map
import wasCreatedAfterOrAt

/**
 * The ThreadControllerImpl sets up a few convenient livedata objects for working with threads
 *
 * * messages: a list of sorted thread messages
 * * loadingOlderMessages: if we are currently loading older messages
 * * endOfOlderMessages: if we've reached the earliest point in this thread
 */
internal class ThreadControllerImpl(
    override val threadId: String,
    val channelControllerImpl: ChannelControllerImpl,
    val client: ChatClient,
    val domain: ChatDomainImpl,
) : ThreadController {

    private val _loadingOlderMessages = MutableStateFlow(false)
    private val _endOfOlderMessages = MutableStateFlow(false)
    private var firstMessage: Message? = null
    private val logger = ChatLogger.get("ThreadController")

    private val threadMessages: Flow<List<Message>> = channelControllerImpl.unfilteredMessages.map { messageList -> messageList.filter { it.id == threadId || it.parentId == threadId } }

    private val sortedVisibleMessages: Flow<List<Message>> = threadMessages.map {
        it.sortedBy { m -> m.createdAt ?: m.createdLocallyAt }
            .filter { channelControllerImpl.hideMessagesBefore == null || it.wasCreatedAfterOrAt(channelControllerImpl.hideMessagesBefore) }
    }

    /** the sorted list of messages for this thread */
    override val messages = sortedVisibleMessages.asLiveData()

    /** if we are currently loading older messages */
    override val loadingOlderMessages: LiveData<Boolean> = _loadingOlderMessages.asLiveData()

    /** if we've reached the earliest point in this thread */
    override val endOfOlderMessages: LiveData<Boolean> = _endOfOlderMessages.asLiveData()

    override fun getMessagesSorted(): List<Message> = messages.value ?: listOf()

    suspend fun loadOlderMessages(limit: Int = 30): Result<List<Message>> {
        // TODO: offline storage for thread load more
        if (_loadingOlderMessages.value) {
            val errorMsg = "already loading messages for this thread, ignoring the load more requests."
            logger.logI(errorMsg)
            return Result(ChatError(errorMsg))
        }
        _loadingOlderMessages.value = true
        val result = channelControllerImpl.loadOlderThreadMessages(threadId, limit, firstMessage)
        if (result.isSuccess) {
            _endOfOlderMessages.value = result.data().size < limit
            firstMessage = result.data().sortedBy { it.createdAt }.firstOrNull() ?: firstMessage
        }

        _loadingOlderMessages.value = false
        return result
    }
}
