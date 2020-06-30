package io.getstream.chat.android.livedata.controller

import androidx.lifecycle.LiveData
import androidx.lifecycle.MediatorLiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.Transformations
import io.getstream.chat.android.client.ChatClient
import io.getstream.chat.android.client.api.models.Pagination
import io.getstream.chat.android.client.errors.ChatError
import io.getstream.chat.android.client.logger.ChatLogger
import io.getstream.chat.android.client.models.Message
import io.getstream.chat.android.client.utils.Result
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

class ThreadControllerImpl(
    override val threadId: String,
    val channelControllerImpl: ChannelControllerImpl,
    val client: ChatClient
) :
    ThreadController {
    private val logger = ChatLogger.get("ThreadController")
    private val threadMessages: MutableLiveData<Map<String, Message>> = MutableLiveData(mapOf())

    private val channelMessages: LiveData<Map<String, Message>> = Transformations.map(channelControllerImpl.messages) {
        it.asSequence()
            .filter { it.id == threadId || it.parentId == threadId }
            .associateBy { it.id }
    }
    private val mediatorLiveData: MediatorLiveData<Map<String, Message>> = MediatorLiveData<Map<String, Message>>().apply {
        addSource(threadMessages) { postValue(value.plus(it)) }
        addSource(channelMessages) { postValue(value.plus(it)) }
    }
    override val messages = Transformations.map(mediatorLiveData) { it.values.sortedBy { m -> m.createdAt }.filter { channelControllerImpl.hideMessagesBefore == null || it.createdAt!! > channelControllerImpl.hideMessagesBefore } }

    private val _loadingOlderMessages = MutableLiveData<Boolean>(false)
    override val loadingOlderMessages: LiveData<Boolean> = _loadingOlderMessages

    override fun getMessagesSorted(): List<Message> = messages.value ?: listOf()

    private val _endOfOlderMessages = MutableLiveData<Boolean>(false)
    override val endOfOlderMessages: LiveData<Boolean> = _endOfOlderMessages

    suspend fun watch(limit: Int = 30) {
        withContext(Dispatchers.IO) {
            val newMessages = client.getReplies(threadId, limit).execute()
                .takeIf { it.isSuccess }
                ?.data()
                ?.associateBy { it.id }
                ?: mapOf()
            threadMessages.postValue(threadMessages.value.plus(newMessages))
        }
    }

    suspend fun loadOlderMessages(limit: Int = 30): Result<List<Message>> {
        if (_loadingOlderMessages.value == true) {
            val errorMsg = "already loading messages for this thread, ignoring the load more requests."
            logger.logI(errorMsg)
            return Result(null, ChatError(errorMsg))
        }
        _loadingOlderMessages.postValue(true)
        val response = channelControllerImpl.loadMoreThreadMessages(threadId, limit, Pagination.LESS_THAN)
        if (response.isSuccess) {
            if (response.data().size < limit) {
                _endOfOlderMessages.postValue(true)
            }
        }
        _loadingOlderMessages.postValue(false)

        return response
    }
}

private fun Map<String, Message>?.plus(map: Map<String, Message>): Map<String, Message> = (this ?: mapOf()) + map