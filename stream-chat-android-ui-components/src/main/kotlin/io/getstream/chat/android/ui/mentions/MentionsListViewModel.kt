package io.getstream.chat.android.ui.mentions

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import io.getstream.chat.android.client.ChatClient
import io.getstream.chat.android.client.api.models.SearchMessagesRequest
import io.getstream.chat.android.client.models.Filters
import io.getstream.chat.android.client.models.Message
import io.getstream.chat.android.client.models.name

public class MentionsListViewModel : ViewModel() {

    private val _mentions: MutableLiveData<List<Message>> = MutableLiveData()
    public val mentions: LiveData<List<Message>> = _mentions

    init {
        // A temporary dummy implementation using the search API
        fetchFromServer()
    }

    private fun fetchFromServer() {
        // TODO replace with usages of the mentions API
        val currentUser = requireNotNull(ChatClient.instance().getCurrentUser())
        val channelFilter = Filters.`in`("members", listOf(currentUser.id))
        val messageFilter = Filters.autocomplete("text", "@${currentUser.name}")

        // TODO add paging
        val request = SearchMessagesRequest(
            offset = 0,
            limit = 30,
            channelFilter = channelFilter,
            messageFilter = messageFilter,
        )
        ChatClient.instance()
            .searchMessages(request)
            .enqueue {
                if (it.isSuccess) {
                    _mentions.value = it.data()
                } else {
                    it.error().message
                }
            }
    }
}
