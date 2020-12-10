package io.getstream.chat.android.ui.channel.list.adapter.viewholder

import io.getstream.chat.android.ui.channel.list.ChannelListView

public class ChannelListItemListenerContainer(
    channelClickListener: ChannelListView.ChannelClickListener = ChannelListView.ChannelClickListener.DEFAULT,
    channelLongClickListener: ChannelListView.ChannelClickListener = ChannelListView.ChannelClickListener.DEFAULT,
    deleteClickListener: ChannelListView.ChannelClickListener = ChannelListView.ChannelClickListener.DEFAULT,
    moreOptionsClickListener: ChannelListView.ChannelClickListener = ChannelListView.ChannelClickListener.DEFAULT,
    userClickListener: ChannelListView.UserClickListener = ChannelListView.UserClickListener.DEFAULT,
) {
    public var channelClickListener: ChannelListView.ChannelClickListener by Provider(channelClickListener) { getClickListener ->
        ChannelListView.ChannelClickListener { channel ->
            getClickListener().onClick(channel)
        }
    }

    public var channelLongClickListener: ChannelListView.ChannelClickListener by Provider(channelLongClickListener) { getClickListener ->
        ChannelListView.ChannelClickListener { channel ->
            getClickListener().onClick(channel)
        }
    }

    public var deleteClickListener: ChannelListView.ChannelClickListener by Provider(deleteClickListener) { getClickListener ->
        ChannelListView.ChannelClickListener { channel ->
            getClickListener().onClick(channel)
        }
    }

    public var moreOptionsClickListener: ChannelListView.ChannelClickListener by Provider(moreOptionsClickListener) { getClickListener ->
        ChannelListView.ChannelClickListener { channel ->
            getClickListener().onClick(channel)
        }
    }

    public var userClickListener: ChannelListView.UserClickListener by Provider(userClickListener) { getClickListener ->
        ChannelListView.UserClickListener { user ->
            getClickListener().onUserClick(user)
        }
    }
}
