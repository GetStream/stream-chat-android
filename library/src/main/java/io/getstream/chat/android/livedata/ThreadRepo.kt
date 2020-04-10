package io.getstream.chat.android.livedata

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import io.getstream.chat.android.client.api.models.Pagination
import io.getstream.chat.android.client.logger.ChatLogger
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.launch


class ThreadRepo(var threadId: String, var channelRepo: ChannelRepo) {
    private val logger = ChatLogger.get("ThreadRepo")
    val messages = channelRepo.getThreadMessages(threadId)

    private val _loadingOlderMessages = MutableLiveData<Boolean>(false)
    val loadingOlderMessages : LiveData<Boolean> = _loadingOlderMessages


    private val _endOfOlderMessages = MutableLiveData<Boolean>(false)
    val endOfOlderMessages : LiveData<Boolean> = _endOfOlderMessages

    fun loadOlderMessages(limit: Int=30) {
        GlobalScope.launch(Dispatchers.IO) {
            _loadOlderMessages(limit)
        }
    }

    suspend fun _loadOlderMessages(limit: Int=30) {
        if (_loadingOlderMessages.value == true) {
            logger.logI("already loading messages for this thread, ignoring the load more requests.")
            return
        }
        _loadingOlderMessages.postValue(true)
        val response = channelRepo.loadMoreThreadMessages(threadId, limit, Pagination.LESS_THAN)
        if (response.isSuccess) {
            if(response.data().size < limit) {
                _endOfOlderMessages.postValue(true)
            }
        }
        _loadingOlderMessages.postValue(false)
    }

}