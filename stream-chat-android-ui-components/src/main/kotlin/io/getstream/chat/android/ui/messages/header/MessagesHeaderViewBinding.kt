package io.getstream.chat.android.ui.messages.header

import androidx.lifecycle.LifecycleOwner
import com.getstream.sdk.chat.viewmodel.ChannelHeaderViewModel
import io.getstream.chat.android.ui.utils.extensions.getDisplayName

@JvmName("bind")
public fun ChannelHeaderViewModel.bindView(view: MessagesHeaderView, lifecycle: LifecycleOwner) {
    channelState.observe(lifecycle) {
        view.setTitle(it.getDisplayName())
        view.setAvatar(channel = it)
    }
}
