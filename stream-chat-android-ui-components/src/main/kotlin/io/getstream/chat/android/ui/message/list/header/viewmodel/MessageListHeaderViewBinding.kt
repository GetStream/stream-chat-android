@file:JvmName("MessageListHeaderViewModelBinding")

package io.getstream.chat.android.ui.message.list.header.viewmodel

import androidx.lifecycle.LifecycleOwner
import io.getstream.chat.android.ui.common.extensions.getDisplayName
import io.getstream.chat.android.ui.common.extensions.internal.getOnlineStateSubtitle
import io.getstream.chat.android.ui.message.list.header.MessageListHeaderView

/**
 * Binds [MessageListHeaderView] with [MessageListHeaderViewModel], updating the view's state
 * based on data provided by the ViewModel.
 */
@JvmName("bind")
public fun MessageListHeaderViewModel.bindView(view: MessageListHeaderView, lifecycle: LifecycleOwner) {
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
