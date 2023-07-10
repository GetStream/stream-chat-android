package io.getstream.chat.android.ui.channel.list.adapter.viewholder

import com.getstream.sdk.chat.utils.ListenerDelegate
import io.getstream.chat.android.ui.channel.list.ChannelListView.ChannelOptionVisibilityPredicate

internal class ChannelListVisibilityContainerImpl(
    isMoreOptionsVisible: ChannelOptionVisibilityPredicate = ChannelOptionVisibilityPredicate.DEFAULT,
    isDeleteOptionVisible: ChannelOptionVisibilityPredicate = ChannelOptionVisibilityPredicate.DEFAULT,
) : ChannelListVisibilityContainer {

    override var isMoreOptionsVisible: ChannelOptionVisibilityPredicate by ListenerDelegate(isMoreOptionsVisible) { realPredicate ->
        ChannelOptionVisibilityPredicate { channel ->
            realPredicate().invoke(channel)
        }
    }

    override var isDeleteOptionVisible: ChannelOptionVisibilityPredicate by ListenerDelegate(isDeleteOptionVisible) { realPredicate ->
        ChannelOptionVisibilityPredicate { channel ->
            realPredicate().invoke(channel)
        }
    }
}