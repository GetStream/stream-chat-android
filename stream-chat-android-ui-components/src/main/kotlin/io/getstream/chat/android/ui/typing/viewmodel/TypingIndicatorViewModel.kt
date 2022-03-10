package io.getstream.chat.android.ui.typing.viewmodel

import androidx.lifecycle.LiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.asLiveData
import androidx.lifecycle.viewModelScope
import io.getstream.chat.android.client.ChatClient
import io.getstream.chat.android.client.models.User
import io.getstream.chat.android.offline.experimental.channel.state.ChannelState
import io.getstream.chat.android.offline.experimental.extensions.asReferenced
import kotlinx.coroutines.flow.map

/**
 * ViewModel used by [io.getstream.chat.android.ui.typing.TypingIndicatorView].
 * It is responsible for updating the state of users who are currently typing.
 *
 * @param cid The full channel id, i.e. "messaging:123".
 * @param chatClient The main entry point for all low-level chat operations.
 */
public class TypingIndicatorViewModel(
    cid: String,
    chatClient: ChatClient = ChatClient.instance(),
) : ViewModel() {

    /**
     * Holds information about the current channel and is actively updated.
     */
    public val channelState: ChannelState =
        chatClient.asReferenced().watchChannel(cid).asState(viewModelScope)

    /**
     * A list of users who are currently typing.
     */
    public val typingUsers: LiveData<List<User>> =
        channelState.typing.map { typingEvent ->
            typingEvent.users
        }.asLiveData()
}
