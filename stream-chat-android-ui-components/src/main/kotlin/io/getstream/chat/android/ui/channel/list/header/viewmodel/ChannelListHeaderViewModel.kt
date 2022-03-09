package io.getstream.chat.android.ui.channel.list.header.viewmodel

import androidx.lifecycle.LiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.asLiveData
import io.getstream.chat.android.client.ChatClient
import io.getstream.chat.android.client.models.User
import io.getstream.chat.android.offline.experimental.extensions.globalState
import io.getstream.chat.android.offline.model.ConnectionState

/**
 * ViewModel class for [io.getstream.chat.android.ui.channel.list.header.ChannelListHeaderView].
 * Responsible for updating current user information.
 * Can be bound to the view using [ChannelListHeaderViewModel.bindView] function.
 *
 * @param chatDomain Entry point for offline operations.
 */
// TODO update kdocs
public class ChannelListHeaderViewModel @JvmOverloads constructor(
    private val chatClient: ChatClient = ChatClient.instance(),
) : ViewModel() {
    public val currentUser: LiveData<User?> = chatClient.globalState.user.asLiveData()
    public val connectionState: LiveData<ConnectionState> = chatClient.globalState.connectionState.asLiveData()
}
