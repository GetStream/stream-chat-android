package io.getstream.chat.android.livedata.controller

import androidx.lifecycle.LiveData
import androidx.lifecycle.MediatorLiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.Transformations
import io.getstream.chat.android.client.ChatClient
import io.getstream.chat.android.client.call.Call
import io.getstream.chat.android.client.errors.ChatError
import io.getstream.chat.android.client.logger.ChatLogger
import io.getstream.chat.android.client.models.Message
import io.getstream.chat.android.client.utils.Result
import io.getstream.chat.android.livedata.ChatDomainImpl
import kotlinx.coroutines.withContext
import wasCreatedAfterOrAt

internal class ThreadControllerImpl(
    override val threadId: String,
    val channelControllerImpl: ChannelControllerImpl,
    val client: ChatClient,
    val domain: ChatDomainImpl,
) : ThreadController {
    private val logger = ChatLogger.get("ThreadController")
    private val threadMessages: MutableLiveData<Map<String, Message>> = MutableLiveData(mapOf())

    private val channelMessages: LiveData<Map<String, Message>> = Transformations.map(channelControllerImpl.unfilteredMessages) { messages ->
        messages?.asSequence()?.filter { it.id == threadId || it.parentId == threadId }?.associateBy { it.id }
    }
    private val mediatorLiveData: MediatorLiveData<Map<String, Message>> = MediatorLiveData<Map<String, Message>>().apply {
        addSource(threadMessages) { postValue(value.plus(it)) }
        addSource(channelMessages) { postValue(value.plus(it)) }
    }
    override val messages =
        Transformations.map(mediatorLiveData) {
            it.values.sortedBy { m -> m.createdAt ?: m.createdLocallyAt }
                .filter { channelControllerImpl.hideMessagesBefore == null || it.wasCreatedAfterOrAt(channelControllerImpl.hideMessagesBefore) }
        }

    private val _loadingOlderMessages = MutableLiveData(false)
    override val loadingOlderMessages: LiveData<Boolean> = _loadingOlderMessages

    override fun getMessagesSorted(): List<Message> = messages.value ?: listOf()

    private val _endOfOlderMessages = MutableLiveData(false)
    override val endOfOlderMessages: LiveData<Boolean> = _endOfOlderMessages

    suspend fun watch(limit: Int = 30): Result<List<Message>> = loadMessages(client.getReplies(threadId, limit), limit)

    // TODO: offline storage for thread load more

    private suspend fun loadMessages(call: Call<List<Message>>, limit: Int): Result<List<Message>> =
        withContext(domain.dispatcherIO) {
            call.execute().apply {
                val data = this.takeIf { it.isSuccess }
                    ?.data()
                val newMessages = data
                    ?.associateBy { it.id }
                    ?: mapOf()
                threadMessages.postValue(threadMessages.value.plus(newMessages))
                _endOfOlderMessages.postValue((data?.size ?: limit) < limit)
            }
        }

    suspend fun loadOlderMessages(limit: Int = 30): Result<List<Message>> {
        if (_loadingOlderMessages.value == true) {
            val errorMsg = "already loading messages for this thread, ignoring the load more requests."
            logger.logI(errorMsg)
            return Result(null, ChatError(errorMsg))
        }
        _loadingOlderMessages.postValue(true)
        return (
            mediatorLiveData.value
                ?.values
                ?.asSequence()
                ?.filter { it.parentId == threadId }
                ?.sortedBy { it.createdAt ?: it.createdLocallyAt }
                ?.firstOrNull()
                ?.let { loadMessages(client.getRepliesMore(threadId, it.id, limit), limit) }
                ?: watch(limit)
            ).also { _loadingOlderMessages.postValue(false) }
    }
}

private fun Map<String, Message>?.plus(map: Map<String, Message>): Map<String, Message> = (this ?: mapOf()) + map
