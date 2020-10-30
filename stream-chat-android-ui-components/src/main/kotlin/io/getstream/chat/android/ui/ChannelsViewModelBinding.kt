@file:JvmName("ChannelsViewModelBinding")

package io.getstream.chat.android.ui

import androidx.lifecycle.LifecycleOwner
import com.getstream.sdk.chat.viewmodel.channels.ChannelsViewModel

@JvmName("bind")
public fun ChannelsViewModel.bindView(
    view: ChannelsView,
    lifecycle: LifecycleOwner
) {
    state.observe(lifecycle) {
        if (it is ChannelsViewModel.State.Result) {
            view.setChannels(it.channels)
        }
    }
}
