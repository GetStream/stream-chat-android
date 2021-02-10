@file:JvmName("ChannelHeaderViewModelBinding")

package io.getstream.chat.android.ui.messages.header

import androidx.lifecycle.LifecycleOwner
import com.getstream.sdk.chat.viewmodel.ChannelHeaderViewModel
import io.getstream.chat.android.ui.utils.extensions.getDisplayName
import io.getstream.chat.android.ui.utils.extensions.getOnlineStateSubtitle

/**
 * Binds [MessageListHeaderView] with [ChannelHeaderViewModel], updating the view's state
 * based on data provided by the ViewModel.
 */
@JvmName("bind")
public fun ChannelHeaderViewModel.bindView(view: MessageListHeaderView, lifecycle: LifecycleOwner) {
    channelState.observe(lifecycle) {
        view.setTitle(it.getDisplayName(view.context))
        view.setOnlineStateSubtitle(it.getOnlineStateSubtitle(view.context))
        view.setAvatar(it)
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
