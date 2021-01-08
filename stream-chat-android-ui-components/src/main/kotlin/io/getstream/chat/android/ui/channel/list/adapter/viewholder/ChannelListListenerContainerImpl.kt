package io.getstream.chat.android.ui.channel.list.adapter.viewholder

import com.getstream.sdk.chat.utils.ListenerDelegate
import io.getstream.chat.android.ui.channel.list.ChannelListView.ChannelClickListener
import io.getstream.chat.android.ui.channel.list.ChannelListView.ChannelLongClickListener
import io.getstream.chat.android.ui.channel.list.ChannelListView.SwipeListener
import io.getstream.chat.android.ui.channel.list.ChannelListView.UserClickListener

internal class ChannelListListenerContainerImpl(
    channelClickListener: ChannelClickListener = ChannelClickListener.DEFAULT,
    channelLongClickListener: ChannelLongClickListener = ChannelLongClickListener.DEFAULT,
    deleteClickListener: ChannelClickListener = ChannelClickListener.DEFAULT,
    moreOptionsClickListener: ChannelClickListener = ChannelClickListener.DEFAULT,
    userClickListener: UserClickListener = UserClickListener.DEFAULT,
    swipeListener: SwipeListener = SwipeListener.DEFAULT,
) : ChannelListListenerContainer {

    override var channelClickListener: ChannelClickListener by ListenerDelegate(channelClickListener) { realListener ->
        ChannelClickListener { channel ->
            realListener().onClick(channel)
        }
    }

    override var channelLongClickListener: ChannelLongClickListener by ListenerDelegate(
        channelLongClickListener
    ) { realListener ->
        ChannelLongClickListener { channel ->
            realListener().onLongClick(channel)
        }
    }

    override var deleteClickListener: ChannelClickListener by ListenerDelegate(deleteClickListener) { realListener ->
        ChannelClickListener { channel ->
            realListener().onClick(channel)
        }
    }

    override var moreOptionsClickListener: ChannelClickListener by ListenerDelegate(
        moreOptionsClickListener
    ) { realListener ->
        ChannelClickListener { channel ->
            realListener().onClick(channel)
        }
    }

    override var userClickListener: UserClickListener by ListenerDelegate(userClickListener) { realListener ->
        UserClickListener { user ->
            realListener().onClick(user)
        }
    }

    override var swipeListener: SwipeListener by ListenerDelegate(swipeListener) { realListener ->
        object : SwipeListener {
            override fun onSwipeStarted(viewHolder: SwipeViewHolder, adapterPosition: Int, x: Float?, y: Float?) {
                realListener().onSwipeStarted(viewHolder, adapterPosition, x, y)
            }

            override fun onSwipeChanged(
                viewHolder: SwipeViewHolder,
                adapterPosition: Int,
                dX: Float,
                totalDeltaX: Float,
            ) {
                realListener().onSwipeChanged(viewHolder, adapterPosition, dX, totalDeltaX)
            }

            override fun onSwipeCompleted(
                viewHolder: SwipeViewHolder,
                adapterPosition: Int,
                x: Float?,
                y: Float?,
            ) {
                realListener().onSwipeCompleted(viewHolder, adapterPosition, x, y)
            }

            override fun onSwipeCanceled(
                viewHolder: SwipeViewHolder,
                adapterPosition: Int,
                x: Float?,
                y: Float?,
            ) {
                realListener().onSwipeCanceled(viewHolder, adapterPosition, x, y)
            }

            override fun onRestoreSwipePosition(viewHolder: SwipeViewHolder, adapterPosition: Int) {
                realListener().onRestoreSwipePosition(viewHolder, adapterPosition)
            }
        }
    }
}
