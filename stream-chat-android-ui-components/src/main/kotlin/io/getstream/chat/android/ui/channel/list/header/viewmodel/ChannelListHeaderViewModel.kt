package io.getstream.chat.android.ui.channel.list.header.viewmodel

import androidx.lifecycle.LiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.asLiveData
import io.getstream.chat.android.client.ChatClient
import io.getstream.chat.android.client.models.User
import io.getstream.chat.android.offline.experimental.extensions.globalState
import io.getstream.chat.android.offline.experimental.global.GlobalState
import io.getstream.chat.android.offline.model.ConnectionState

/**
 * ViewModel class for [io.getstream.chat.android.ui.channel.list.header.ChannelListHeaderView].
 * Responsible for updating current user information.
 * Can be bound to the view using [ChannelListHeaderViewModel.bindView] function.
 *
 * @param globalState Global state of OfflinePlugin. Contains information
 * such as the current user, connection state, unread counts etc.
 */
public class ChannelListHeaderViewModel @JvmOverloads constructor(
    globalState: GlobalState = ChatClient.instance().globalState,
) : ViewModel() {

    /**
     * The user who is currently logged in.
     */
    public val currentUser: LiveData<User?> = globalState.user.asLiveData()

    /**
     * The state of the connection for the user currently logged in.
     */
    public val connectionState: LiveData<ConnectionState> = globalState.connectionState.asLiveData()
}
