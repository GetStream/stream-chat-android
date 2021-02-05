@file:JvmName("ChannelHeaderViewModelBinding")

package io.getstream.chat.android.ui.messages.header

import androidx.lifecycle.LifecycleOwner
import com.getstream.sdk.chat.utils.extensions.getUsers
import com.getstream.sdk.chat.viewmodel.ChannelHeaderViewModel
import io.getstream.chat.android.ui.utils.extensions.getDisplayName
import io.getstream.chat.android.ui.utils.extensions.getOnlineStateSubtitle

/**
 * Binds [MessagesHeaderView] with [ChannelHeaderViewModel], updating the view's state
 * based on data provided by the ViewModel.
 */
@JvmName("bind")
public fun ChannelHeaderViewModel.bindView(view: MessagesHeaderView, lifecycle: LifecycleOwner) {
    channelState.observe(lifecycle) {
        view.setTitle(it.getDisplayName(view.context))
        view.setOnlineStateSubtitle(it.getOnlineStateSubtitle(view.context))

        val otherUsers = it.getUsers()
        if (otherUsers.size == 1) {
            view.setAvatar(user = otherUsers.first())
        } else {
            view.setAvatar(channel = it)
        }
    }
    online.observe(lifecycle) { isOnline ->
        if (isOnline) {
            view.showOnlineStateSubtitle()
        } else {
            view.showSearchingForNetworkLabel()
        }
    }
    typingUsers.observe(lifecycle, view::showTypingStateLabel)

    activeThread.observe(lifecycle) { message ->
        if (message != null) {
            view.setThreadMode()
        } else {
            view.setNormalMode()
        }
    }
}
