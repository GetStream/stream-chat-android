package io.getstream.chat.android.ui.search

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

public class SearchViewModel : ViewModel() {

    private val _results: MutableLiveData<List<Message>> = MutableLiveData()
    public val results: LiveData<List<Message>> = _results

    public fun setQuery(query: String) {
        fetchDummyResults(query)
    }

    private fun fetchDummyResults(query: String) {
        _results.value = List(5) {
            Message(
                id = "dummy-id-$it",
                cid = "messaging:placeholder",
                user = User().apply {
                    name = "John Doe"
                    image = "https://randomuser.me/api/portraits/men/0.jpg"
                },
                createdAt = Date(2020, 3, 15, 18, 11),
                text = "Message with \"$query\" in it",
            )
        }
    }

    private fun fetchServerResults(query: String) {
        // TODO update these filters
        val channelFilter = Filters.`in`("members", listOf(ChatClient.instance().getCurrentUser()!!.id))
        val messageFilter = Filters.autocomplete("text", query)

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
                    _results.value = it.data()
                } else {
                    it.error().message
                }
            }
    }
}
