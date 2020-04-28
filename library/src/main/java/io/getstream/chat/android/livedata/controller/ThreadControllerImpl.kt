package io.getstream.chat.android.livedata.controller

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.Transformations
import io.getstream.chat.android.client.api.models.Pagination
import io.getstream.chat.android.client.errors.ChatError
import io.getstream.chat.android.client.logger.ChatLogger
import io.getstream.chat.android.client.models.Message
import io.getstream.chat.android.client.utils.Result

class ThreadControllerImpl(
    override var threadId: String,
    var channelControllerImpl: ChannelControllerImpl
) :
    ThreadController {
    private val logger = ChatLogger.get("ThreadController")
    private val _messages = channelControllerImpl.getThreadMessages(threadId)
    override val messages = Transformations.map(_messages) { it.values.sortedBy { m -> m.createdAt }.filter { channelControllerImpl.hideMessagesBefore == null || it.createdAt!! > channelControllerImpl.hideMessagesBefore } }

    private val _loadingOlderMessages = MutableLiveData<Boolean>(false)
    override val loadingOlderMessages: LiveData<Boolean> = _loadingOlderMessages

    override fun getMessagesSorted(): List<Message> {
        val messageMap = _messages.value ?: mutableMapOf()
        return messageMap.values.sortedBy { m -> m.createdAt }
    }

    private val _endOfOlderMessages = MutableLiveData<Boolean>(false)
    override val endOfOlderMessages: LiveData<Boolean> = _endOfOlderMessages

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
