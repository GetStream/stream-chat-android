package io.getstream.chat.android.livedata.controller

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import io.getstream.chat.android.client.api.models.Pagination
import io.getstream.chat.android.client.errors.ChatError
import io.getstream.chat.android.client.logger.ChatLogger
import io.getstream.chat.android.client.models.Message
import io.getstream.chat.android.client.utils.Result
import io.getstream.chat.android.livedata.controller.ChannelController
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.launch


class ThreadController(var threadId: String, var channelController: ChannelController) {
    private val logger = ChatLogger.get("ThreadRepo")
    val messages = channelController.getThreadMessages(threadId)

    private val _loadingOlderMessages = MutableLiveData<Boolean>(false)
    val loadingOlderMessages: LiveData<Boolean> = _loadingOlderMessages


    private val _endOfOlderMessages = MutableLiveData<Boolean>(false)
    val endOfOlderMessages: LiveData<Boolean> = _endOfOlderMessages

    suspend fun loadOlderMessages(limit: Int = 30): Result<List<Message>> {
        if (_loadingOlderMessages.value == true) {
            logger.logI("already loading messages for this thread, ignoring the load more requests.")
            return Result(null, ChatError("already loading messages for this thread, ignoring the load more requests."))
        }
        _loadingOlderMessages.postValue(true)
        val response = channelController.loadMoreThreadMessages(threadId, limit, Pagination.LESS_THAN)
        if (response.isSuccess) {
            if (response.data().size < limit) {
                _endOfOlderMessages.postValue(true)
            }
        }
        _loadingOlderMessages.postValue(false)

        return response
    }

}