@file:JvmName("ChannelListHeaderViewModelBinding")

package io.getstream.chat.android.ui.channel.list.header.viewmodel

import androidx.lifecycle.LifecycleOwner
import io.getstream.chat.android.ui.channel.list.header.ChannelListHeaderView

/**
 * Binds [ChannelListHeaderView] with [ChannelListHeaderViewModel], updating the view's state
 * based on data provided by the ViewModel.
 */
@JvmName("bind")
public fun ChannelListHeaderViewModel.bindView(view: ChannelListHeaderView, lifecycleOwner: LifecycleOwner) {
    with(view) {
        currentUser.observe(lifecycleOwner) { user ->
            setUser(user)
        }
        online.observe(lifecycleOwner) { isOnline ->
            if (isOnline) {
                showOnlineTitle()
            } else {
                showOfflineTitle()
            }
        }
    }
}
