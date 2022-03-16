@file:JvmName("ChannelListHeaderViewModelBinding")

package io.getstream.chat.android.ui.channel.list.header.viewmodel

import androidx.lifecycle.LifecycleOwner
import io.getstream.chat.android.offline.model.connection.ConnectionState
import io.getstream.chat.android.ui.channel.list.header.ChannelListHeaderView

/**
 * Binds [ChannelListHeaderView] with [ChannelListHeaderViewModel], updating the view's state
 * based on data provided by the ViewModel, and propagating view events to the ViewModel as needed.
 *
 * This function sets listeners on the view and ViewModel. Call this method
 * before setting any additional listeners on these objects yourself.
 */
@JvmName("bind")
public fun ChannelListHeaderViewModel.bindView(view: ChannelListHeaderView, lifecycleOwner: LifecycleOwner) {
    with(view) {
        currentUser.observe(lifecycleOwner) { user ->
            user?.let(::setUser)
        }
        connectionState.observe(lifecycleOwner) { connectionState ->
            when (connectionState) {
                ConnectionState.CONNECTED -> showOnlineTitle()
                ConnectionState.CONNECTING -> showConnectingTitle()
                ConnectionState.OFFLINE -> showOfflineTitle()
            }
        }
    }
}
