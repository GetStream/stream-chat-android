package io.getstream.chat.android.ui.messages.header

import androidx.lifecycle.LifecycleOwner
import com.getstream.sdk.chat.viewmodel.ChannelHeaderViewModel
import io.getstream.chat.android.ui.utils.extensions.getDisplayName
import io.getstream.chat.android.ui.utils.extensions.getOnlineStateSubtitle

@JvmName("bind")
public fun ChannelHeaderViewModel.bindView(view: MessagesHeaderView, lifecycle: LifecycleOwner) {
    channelState.observe(lifecycle) {
        view.setTitle(it.getDisplayName())
        view.setOnlineStateSubtitle(it.getOnlineStateSubtitle(view.context))
        view.setAvatar(channel = it)
    }
    online.observe(lifecycle) { isOnline ->
        if (isOnline) {
            view.showOnlineStateSubtitle()
        } else {
            view.showSearchingForNetworkLabel()
        }
    }
}
