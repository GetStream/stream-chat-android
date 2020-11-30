package io.getstream.chat.android.ui.mentions

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import io.getstream.chat.android.client.ChatClient
import io.getstream.chat.android.client.api.models.SearchMessagesRequest
import io.getstream.chat.android.client.models.Filters
import io.getstream.chat.android.client.models.Message
import io.getstream.chat.android.client.models.User
import io.getstream.chat.android.client.models.image
import io.getstream.chat.android.client.models.name
import java.util.Date

public class MentionsListViewModel : ViewModel() {

    private val _mentions: MutableLiveData<List<Message>> = MutableLiveData()
    public val mentions: LiveData<List<Message>> = _mentions

    init {
        fetchDummyData()
        // fetchFromServer()
    }

    private fun fetchDummyData() {
        _mentions.value = List(20) {
            Message(
                id = "dummy-id-$it",
                user = User().apply {
                    name = "Jane Doe"
                    image = "https://randomuser.me/api/portraits/women/0.jpg"
                },
                createdAt = Date(2020, 7, 15, 10, 22),
                text = "Hello world, how are you doing?",
            )
        }
    }

    private fun fetchFromServer() {
        // TODO update these filters
        val channelFilter = Filters.`in`("members", listOf(ChatClient.instance().getCurrentUser()!!.id))
        val messageFilter = Filters.autocomplete("text", "hi")

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
