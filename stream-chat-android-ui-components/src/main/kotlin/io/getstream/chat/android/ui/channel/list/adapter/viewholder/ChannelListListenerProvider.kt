package io.getstream.chat.android.ui.channel.list.adapter.viewholder

import io.getstream.chat.android.ui.channel.list.ChannelListView

public class ChannelListListenerProvider(
    channelClickListener: ChannelListView.ChannelClickListener = ChannelListView.ChannelClickListener.DEFAULT,
    channelLongClickListener: ChannelListView.ChannelClickListener = ChannelListView.ChannelClickListener.DEFAULT,
    deleteClickListener: ChannelListView.ChannelClickListener = ChannelListView.ChannelClickListener.DEFAULT,
    moreOptionsClickListener: ChannelListView.ChannelClickListener = ChannelListView.ChannelClickListener.DEFAULT,
    userClickListener: ChannelListView.UserClickListener = ChannelListView.UserClickListener.DEFAULT,
    swipeEventListener: ChannelListView.SwipeEventListener = ChannelListView.SwipeEventListener.DEFAULT
) {
    public var channelClickListener: ChannelListView.ChannelClickListener by Provider(channelClickListener) { getListener ->
        ChannelListView.ChannelClickListener { channel ->
            getListener().onClick(channel)
        }
    }

    public var channelLongClickListener: ChannelListView.ChannelClickListener by Provider(channelLongClickListener) { getListener ->
        ChannelListView.ChannelClickListener { channel ->
            getListener().onClick(channel)
        }
    }

    public var deleteClickListener: ChannelListView.ChannelClickListener by Provider(deleteClickListener) { getListener ->
        ChannelListView.ChannelClickListener { channel ->
            getListener().onClick(channel)
        }
    }

    public var moreOptionsClickListener: ChannelListView.ChannelClickListener by Provider(moreOptionsClickListener) { getListener ->
        ChannelListView.ChannelClickListener { channel ->
            getListener().onClick(channel)
        }
    }

    public var userClickListener: ChannelListView.UserClickListener by Provider(userClickListener) { getListener ->
        ChannelListView.UserClickListener { user ->
            getListener().onUserClick(user)
        }
    }

    public var swipeEventListener: ChannelListView.SwipeEventListener by Provider(swipeEventListener) { getListener ->
        ChannelListView.SwipeEventListener { event ->
            getListener().onSwipeEvent(event)
        }
    }
}
