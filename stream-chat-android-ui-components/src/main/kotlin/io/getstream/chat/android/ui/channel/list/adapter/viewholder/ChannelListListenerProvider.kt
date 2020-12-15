package io.getstream.chat.android.ui.channel.list.adapter.viewholder

import androidx.recyclerview.widget.RecyclerView
import io.getstream.chat.android.ui.channel.list.ChannelListView

public class ChannelListListenerProvider(
    channelClickListener: ChannelListView.ChannelClickListener = ChannelListView.ChannelClickListener.DEFAULT,
    channelLongClickListener: ChannelListView.ChannelClickListener = ChannelListView.ChannelClickListener.DEFAULT,
    deleteClickListener: ChannelListView.ChannelClickListener = ChannelListView.ChannelClickListener.DEFAULT,
    moreOptionsClickListener: ChannelListView.ChannelClickListener = ChannelListView.ChannelClickListener.DEFAULT,
    userClickListener: ChannelListView.UserClickListener = ChannelListView.UserClickListener.DEFAULT,
    swipeListener: ChannelListView.SwipeListener = ChannelListView.SwipeListener.DEFAULT
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

    public var swipeListener: ChannelListView.SwipeListener by Provider(swipeListener) { getDelegate ->
        object : ChannelListView.SwipeListener {
            override fun onSwipeStarted(viewHolder: RecyclerView.ViewHolder, adapterPosition: Int, x: Float, y: Float) {
                getDelegate().onSwipeStarted(viewHolder, adapterPosition, x, y)
            }

            override fun onSwipeChanged(viewHolder: RecyclerView.ViewHolder, adapterPosition: Int, dX: Float) {
                getDelegate().onSwipeChanged(viewHolder, adapterPosition, dX)
            }

            override fun onSwipeCompleted(
                viewHolder: RecyclerView.ViewHolder,
                adapterPosition: Int,
                x: Float,
                y: Float
            ) {
                getDelegate().onSwipeCompleted(viewHolder, adapterPosition, x, y)
            }

            override fun onSwipeCanceled(
                viewHolder: RecyclerView.ViewHolder,
                adapterPosition: Int,
                x: Float,
                y: Float
            ) {
                getDelegate().onSwipeCanceled(viewHolder, adapterPosition, x, y)
            }

            override fun onRestoreSwipePosition(viewHolder: RecyclerView.ViewHolder, adapterPosition: Int) {
                getDelegate().onRestoreSwipePosition(viewHolder, adapterPosition)
            }
        }
    }
}
