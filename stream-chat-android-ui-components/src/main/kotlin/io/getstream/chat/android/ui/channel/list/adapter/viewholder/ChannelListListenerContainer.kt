package io.getstream.chat.android.ui.channel.list.adapter.viewholder

import com.getstream.sdk.chat.utils.ListenerDelegate
import io.getstream.chat.android.ui.channel.list.ChannelListView

public class ChannelListListenerContainer(
    channelClickListener: ChannelListView.ChannelClickListener = ChannelListView.ChannelClickListener.DEFAULT,
    channelLongClickListener: ChannelListView.ChannelClickListener = ChannelListView.ChannelClickListener.DEFAULT,
    deleteClickListener: ChannelListView.ChannelClickListener = ChannelListView.ChannelClickListener.DEFAULT,
    moreOptionsClickListener: ChannelListView.ChannelClickListener = ChannelListView.ChannelClickListener.DEFAULT,
    userClickListener: ChannelListView.UserClickListener = ChannelListView.UserClickListener.DEFAULT,
    swipeListener: ChannelListView.SwipeListener = ChannelListView.SwipeListener.DEFAULT,
) {
    public var channelClickListener: ChannelListView.ChannelClickListener by ListenerDelegate(channelClickListener) { getListener ->
        ChannelListView.ChannelClickListener { channel ->
            getListener().onClick(channel)
        }
    }

    public var channelLongClickListener: ChannelListView.ChannelClickListener by ListenerDelegate(
        channelLongClickListener
    ) { getListener ->
        ChannelListView.ChannelClickListener { channel ->
            getListener().onClick(channel)
        }
    }

    public var deleteClickListener: ChannelListView.ChannelClickListener by ListenerDelegate(deleteClickListener) { getListener ->
        ChannelListView.ChannelClickListener { channel ->
            getListener().onClick(channel)
        }
    }

    public var moreOptionsClickListener: ChannelListView.ChannelClickListener by ListenerDelegate(
        moreOptionsClickListener
    ) { getListener ->
        ChannelListView.ChannelClickListener { channel ->
            getListener().onClick(channel)
        }
    }

    public var userClickListener: ChannelListView.UserClickListener by ListenerDelegate(userClickListener) { getListener ->
        ChannelListView.UserClickListener { user ->
            getListener().onUserClick(user)
        }
    }

    public var swipeListener: ChannelListView.SwipeListener by ListenerDelegate(swipeListener) { getDelegate ->
        object : ChannelListView.SwipeListener {
            override fun onSwipeStarted(viewHolder: SwipeViewHolder, adapterPosition: Int, x: Float?, y: Float?) {
                getDelegate().onSwipeStarted(viewHolder, adapterPosition, x, y)
            }

            override fun onSwipeChanged(
                viewHolder: SwipeViewHolder,
                adapterPosition: Int,
                dX: Float,
                totalDeltaX: Float
            ) {
                getDelegate().onSwipeChanged(viewHolder, adapterPosition, dX, totalDeltaX)
            }

            override fun onSwipeCompleted(
                viewHolder: SwipeViewHolder,
                adapterPosition: Int,
                x: Float?,
                y: Float?
            ) {
                getDelegate().onSwipeCompleted(viewHolder, adapterPosition, x, y)
            }

            override fun onSwipeCanceled(
                viewHolder: SwipeViewHolder,
                adapterPosition: Int,
                x: Float?,
                y: Float?
            ) {
                getDelegate().onSwipeCanceled(viewHolder, adapterPosition, x, y)
            }

            override fun onRestoreSwipePosition(viewHolder: SwipeViewHolder, adapterPosition: Int) {
                getDelegate().onRestoreSwipePosition(viewHolder, adapterPosition)
            }
        }
    }
}
