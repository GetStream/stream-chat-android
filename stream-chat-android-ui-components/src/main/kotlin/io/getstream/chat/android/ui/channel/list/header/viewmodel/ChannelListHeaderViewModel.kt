package io.getstream.chat.android.ui.channel.list.header.viewmodel

import androidx.lifecycle.LiveData
import androidx.lifecycle.ViewModel
import io.getstream.chat.android.client.models.User
import io.getstream.chat.android.livedata.ChatDomain
import io.getstream.chat.android.offline.model.ConnectionState

/**
 * ViewModel class for [io.getstream.chat.android.ui.channel.list.header.ChannelListHeaderView].
 * Responsible for updating current user information.
 * Can be bound to the view using [ChannelListHeaderViewModel.bindView] function.
 *
 * @param chatDomain Entry point for offline operations.
 */
public class ChannelListHeaderViewModel @JvmOverloads constructor(
    private val chatDomain: ChatDomain = ChatDomain.instance(),
) : ViewModel() {
    public val currentUser: LiveData<User?> = chatDomain.user
    public val connectionState: LiveData<ConnectionState> = chatDomain.connectionState
    @Deprecated(
        message = "Use connectionState instead",
        level = DeprecationLevel.ERROR
    )
    @Suppress("DEPRECATION_ERROR")
    public val online: LiveData<Boolean> = chatDomain.online
}
