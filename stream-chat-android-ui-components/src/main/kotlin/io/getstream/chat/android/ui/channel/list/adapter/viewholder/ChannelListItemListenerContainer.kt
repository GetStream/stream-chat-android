package io.getstream.chat.android.ui.channel.list.adapter.viewholder

import io.getstream.chat.android.ui.channel.list.ChannelListView

public class ChannelListItemListenerContainer(
    channelClickListener: ChannelListView.ChannelClickListener = ChannelListView.ChannelClickListener.DEFAULT,
    channelLongClickListener: ChannelListView.ChannelClickListener = ChannelListView.ChannelClickListener.DEFAULT,
    deleteClickListener: ChannelListView.ChannelClickListener = ChannelListView.ChannelClickListener.DEFAULT,
    userClickListener: ChannelListView.UserClickListener = ChannelListView.UserClickListener.DEFAULT,
) {
    public var channelClickListener: ChannelListView.ChannelClickListener by ListenerDelegate(channelClickListener) { listenerProvider ->
        ChannelListView.ChannelClickListener { channel ->
            listenerProvider().onClick(channel)
        }
    }

    public var channelLongClickListener: ChannelListView.ChannelClickListener by ListenerDelegate(
        channelLongClickListener
    ) { listenerProvider ->
        ChannelListView.ChannelClickListener { channel ->
            listenerProvider().onClick(channel)
        }
    }

    public var deleteClickListener: ChannelListView.ChannelClickListener by ListenerDelegate(deleteClickListener) { listenerProvider ->
        ChannelListView.ChannelClickListener { channel ->
            listenerProvider().onClick(channel)
        }
    }

    public var userClickListener: ChannelListView.UserClickListener by ListenerDelegate(userClickListener) { listenerProvider ->
        ChannelListView.UserClickListener { user ->
            listenerProvider().onUserClick(user)
        }
    }
}
