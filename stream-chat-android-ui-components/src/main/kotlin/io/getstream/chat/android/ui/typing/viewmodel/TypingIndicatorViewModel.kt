package io.getstream.chat.android.ui.typing.viewmodel

import androidx.lifecycle.LiveData
import androidx.lifecycle.MediatorLiveData
import androidx.lifecycle.ViewModel
import io.getstream.chat.android.client.models.User
import io.getstream.chat.android.livedata.ChatDomain

public class TypingIndicatorViewModel(
    cid: String,
    chatDomain: ChatDomain = ChatDomain.instance(),
) : ViewModel() {

    private val _typingUsers = MediatorLiveData<List<User>>()

    public val typingUsers: LiveData<List<User>> = _typingUsers

    init {
        _typingUsers.value = emptyList()

        chatDomain.useCases.watchChannel(cid, 0).enqueue { channelControllerResult ->
            if (channelControllerResult.isSuccess) {
                val channelController = channelControllerResult.data()
                _typingUsers.addSource(channelController.typing) { typingEvent ->
                    _typingUsers.value = typingEvent.users
                }
            }
        }
    }
}
