package io.getstream.chat.android.ui.channel.list.adapter.viewholder

import com.getstream.sdk.chat.utils.ListenerDelegate
import io.getstream.chat.android.ui.channel.list.ChannelListView

internal class ChannelListListenerContainer(
    channelClickListener: ChannelListView.ChannelClickListener = ChannelListView.ChannelClickListener.DEFAULT,
    channelLongClickListener: ChannelListView.ChannelClickListener = ChannelListView.ChannelClickListener.DEFAULT,
    deleteClickListener: ChannelListView.ChannelClickListener = ChannelListView.ChannelClickListener.DEFAULT,
    moreOptionsClickListener: ChannelListView.ChannelClickListener = ChannelListView.ChannelClickListener.DEFAULT,
    userClickListener: ChannelListView.UserClickListener = ChannelListView.UserClickListener.DEFAULT,
    swipeListener: ChannelListView.SwipeListener = ChannelListView.SwipeListener.DEFAULT,
) {
    var channelClickListener: ChannelListView.ChannelClickListener by ListenerDelegate(channelClickListener) { realListener ->
        ChannelListView.ChannelClickListener { channel ->
            realListener().onClick(channel)
        }
    }

    var channelLongClickListener: ChannelListView.ChannelClickListener by ListenerDelegate(
        channelLongClickListener
    ) { realListener ->
        ChannelListView.ChannelClickListener { channel ->
            realListener().onClick(channel)
        }
    }

    var deleteClickListener: ChannelListView.ChannelClickListener by ListenerDelegate(deleteClickListener) { realListener ->
        ChannelListView.ChannelClickListener { channel ->
            realListener().onClick(channel)
        }
    }

    var moreOptionsClickListener: ChannelListView.ChannelClickListener by ListenerDelegate(
        moreOptionsClickListener
    ) { realListener ->
        ChannelListView.ChannelClickListener { channel ->
            realListener().onClick(channel)
        }
    }

    var userClickListener: ChannelListView.UserClickListener by ListenerDelegate(userClickListener) { realListener ->
        ChannelListView.UserClickListener { user ->
            realListener().onClick(user)
        }
    }

    var swipeListener: ChannelListView.SwipeListener by ListenerDelegate(swipeListener) { realListener ->
        object : ChannelListView.SwipeListener {
            override fun onSwipeStarted(viewHolder: SwipeViewHolder, adapterPosition: Int, x: Float?, y: Float?) {
                realListener().onSwipeStarted(viewHolder, adapterPosition, x, y)
            }

            override fun onSwipeChanged(
                viewHolder: SwipeViewHolder,
                adapterPosition: Int,
                dX: Float,
                totalDeltaX: Float
            ) {
                realListener().onSwipeChanged(viewHolder, adapterPosition, dX, totalDeltaX)
            }

            override fun onSwipeCompleted(
                viewHolder: SwipeViewHolder,
                adapterPosition: Int,
                x: Float?,
                y: Float?
            ) {
                realListener().onSwipeCompleted(viewHolder, adapterPosition, x, y)
            }

            override fun onSwipeCanceled(
                viewHolder: SwipeViewHolder,
                adapterPosition: Int,
                x: Float?,
                y: Float?
            ) {
                realListener().onSwipeCanceled(viewHolder, adapterPosition, x, y)
            }

            override fun onRestoreSwipePosition(viewHolder: SwipeViewHolder, adapterPosition: Int) {
                realListener().onRestoreSwipePosition(viewHolder, adapterPosition)
            }
        }
    }
}
