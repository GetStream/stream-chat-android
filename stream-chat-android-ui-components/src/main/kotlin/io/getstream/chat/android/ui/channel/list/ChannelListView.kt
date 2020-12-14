package io.getstream.chat.android.ui.channel.list

import android.content.Context
import android.graphics.Color
import android.graphics.drawable.ColorDrawable
import android.util.AttributeSet
import android.view.View
import androidx.annotation.DrawableRes
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import io.getstream.chat.android.client.logger.ChatLogger
import io.getstream.chat.android.client.models.Channel
import io.getstream.chat.android.client.models.User
import io.getstream.chat.android.ui.R
import io.getstream.chat.android.ui.channel.list.ChannelListView.ChannelClickListener
import io.getstream.chat.android.ui.channel.list.ChannelListView.UserClickListener
import io.getstream.chat.android.ui.channel.list.adapter.ChannelListItemAdapter
import io.getstream.chat.android.ui.channel.list.adapter.viewholder.ChannelItemViewHolder
import io.getstream.chat.android.ui.channel.list.adapter.viewholder.ChannelListItemViewHolderFactory
import io.getstream.chat.android.ui.utils.extensions.cast
import io.getstream.chat.android.ui.utils.extensions.getDimension
import io.getstream.chat.android.ui.utils.extensions.safeCast

public class ChannelListView @JvmOverloads constructor(
    context: Context,
    attrs: AttributeSet? = null,
    defStyle: Int = 0
) : RecyclerView(context, attrs, defStyle) {

    private var endReachedListener: EndReachedListener? = null
    private val layoutManager: ScrollPauseLinearLayoutManager
    private val scrollListener: EndReachedScrollListener = EndReachedScrollListener()
    private val dividerDecoration: SimpleVerticalListDivider = SimpleVerticalListDivider()

    init {
        setHasFixedSize(true)
        layoutManager = ScrollPauseLinearLayoutManager(context)
        setLayoutManager(layoutManager)
        adapter = createAdapter()
        parseStyleAttributes(context, attrs)
        addItemDecoration(dividerDecoration)
    }

    private val menuItemWidth = context.getDimension(R.dimen.stream_ui_channel_list_item_option_icon_width).toFloat()
    private val optionsMenuWidth = menuItemWidth * ChannelItemViewHolder.OPTIONS_COUNT
    private val openValue = -optionsMenuWidth
    private val closedValue = 0f
    private val swipeRange = openValue..closedValue
    private val swipeStateByPosition = mutableMapOf<Int, SwipeState>()
    public val multiSwipeEnabled: Boolean = false

    public sealed class SwipeState {
        internal object Open : SwipeState()
        internal object Closed : SwipeState()
    }

    private fun createAdapter(): ChannelListItemAdapter {
        return ChannelListItemAdapter().apply {
            // set the default swipe event listener - pauses scrolling while swiping
            swipeDelegate = object : ViewHolderSwipeDelegate {
                override fun onSwipeStarted(viewHolder: ViewHolder, adapterPosition: Int, x: Float, y: Float) {
                    layoutManager.verticalScrollEnabled = false
                }

                override fun onSwipeChanged(viewHolder: ViewHolder, adapterPosition: Int, dX: Float) {
                    viewHolder.safeCast<ChannelItemViewHolder>()?.let { channelViewHolder ->
                        val itemViewForeground = channelViewHolder.getItemViewForeground()
                        // determine the new x value by adding the delta calculated from the move
                        val projectedX = itemViewForeground.x + dX
                        // clamp it and animate if necessary
                        projectedX.coerceIn(swipeRange).let { clampedX ->
                            // set the new x if it's different
                            if (itemViewForeground.x != clampedX) {
                                itemViewForeground.x = clampedX
                            }
                        }
                    }
                }

                override fun onSwipeEnded(viewHolder: ViewHolder, adapterPosition: Int, x: Float, y: Float) {
                    viewHolder.safeCast<ChannelItemViewHolder>()?.let { channelViewHolder ->
                        val itemViewForeground = channelViewHolder.getItemViewForeground()
                        // determine snap value
                        val snapValue = when {
                            itemViewForeground.x <= openValue / 2 -> openValue
                            else -> closedValue
                        }
                        // animate to snap
                        itemViewForeground
                            .animate()
                            .x(snapValue)
                            .setStartDelay(0)
                            .setDuration(100)
                            .start()
                        // determine swipe state
                        val swipeState = when {
                            snapValue < 0 -> SwipeState.Open
                            else -> SwipeState.Closed
                        }
                        // persist swipe state for the current item
                        swipeStateByPosition[adapterPosition] = swipeState
                        // potentially reset all other items
                        if (!multiSwipeEnabled && swipeState == SwipeState.Open) {
                            swipeStateByPosition
                                .asSequence()
                                .filter { it.key != adapterPosition }
                                .filter { it.value == SwipeState.Open }
                                .forEach { swipeStateEntry ->
                                    // persist the new state
                                    swipeStateByPosition[swipeStateEntry.key] = SwipeState.Closed
                                    // if the view holder currently visible, animate it closed
                                    findViewHolderForAdapterPosition(swipeStateEntry.key)?.cast<ChannelItemViewHolder>()
                                        ?.let { viewHolder ->
                                            val viewCompletelyVisible =
                                                layoutManager.isViewPartiallyVisible(viewHolder.itemView, true, false)
                                            val viewPartiallyVisible =
                                                layoutManager.isViewPartiallyVisible(viewHolder.itemView, false, false)
                                            val onScreen = viewCompletelyVisible || viewPartiallyVisible
                                            if (onScreen) {
                                                viewHolder
                                                    .getItemViewForeground()
                                                    .animate()
                                                    .x(closedValue)
                                                    .setDuration(100)
                                                    .setStartDelay(0)
                                                    .start()
                                            }
                                        }
                                }
                        }

                        layoutManager.verticalScrollEnabled = true
                    }
                }

                override fun onSwipeCanceled(viewHolder: ViewHolder, adapterPosition: Int, x: Float, y: Float) {
                    viewHolder.safeCast<ChannelItemViewHolder>()?.let { channelViewHolder ->
                        channelViewHolder
                            .getItemViewForeground()
                            .animate()
                            .x(closedValue)
                            .setStartDelay(0)
                            .setDuration(100)
                            .start()

                        // persist channel item's menu state as closed
                        swipeStateByPosition[adapterPosition] = SwipeState.Closed
                    }
                    layoutManager.verticalScrollEnabled = true
                }

                // invert this?
                override fun onRestoreSwipePosition(viewHolder: ViewHolder, adapterPosition: Int) {
                    viewHolder.safeCast<ChannelItemViewHolder>()?.let { channelViewHolder ->
                        channelViewHolder.getItemViewForeground().x = when (swipeStateByPosition[adapterPosition]) {
                            SwipeState.Open -> -optionsMenuWidth
                            else -> 0f
                        }
                    }
                }
            }
        }
    }

    private fun parseStyleAttributes(context: Context, attrs: AttributeSet?) {
        // parse the attributes
        requireAdapter().style = ChannelListViewStyle(context, attrs).apply {
            // use the background color as a default for the avatar border
            if (avatarBorderColor == -1) {
                background.let { channelViewBackground ->
                    avatarBorderColor = when (channelViewBackground) {
                        is ColorDrawable -> channelViewBackground.color
                        else -> Color.WHITE
                    }
                }
            }
        }
    }

    private fun requireAdapter(): ChannelListItemAdapter {
        val logger = ChatLogger.get("ChannelListView::requireAdapter")
        val channelAdapter = adapter

        require(channelAdapter != null) {
            logger.logE("Required adapter was null")
        }

        require(channelAdapter is ChannelListItemAdapter) {
            logger.logE("Adapter must be an instance of ChannelListItemAdapter")
        }

        return channelAdapter.cast()
    }

    private fun canScrollUpForChannelEvent(): Boolean = layoutManager.findFirstVisibleItemPosition() < 3

    public fun setViewHolderFactory(factory: ChannelListItemViewHolderFactory) {
        requireAdapter().viewHolderFactory = factory
    }

    public fun setChannelClickListener(listener: ChannelClickListener?) {
        requireAdapter().listenerProvider.channelClickListener = listener ?: ChannelClickListener.DEFAULT
    }

    public fun setChannelLongClickListener(listener: ChannelClickListener?) {
        requireAdapter().listenerProvider.channelLongClickListener = listener ?: ChannelClickListener.DEFAULT
    }

    public fun setUserClickListener(listener: UserClickListener?) {
        requireAdapter().listenerProvider.userClickListener = listener ?: UserClickListener.DEFAULT
    }

    public fun setChannelDeleteClickListener(listener: ChannelClickListener?) {
        requireAdapter().listenerProvider.deleteClickListener = listener ?: ChannelClickListener.DEFAULT
    }

    public fun setMoreOptionsClickListener(listener: ChannelClickListener?) {
        requireAdapter().listenerProvider.moreOptionsClickListener = listener ?: ChannelClickListener.DEFAULT
    }

    public fun setSwipeDelegate(listener: ViewHolderSwipeDelegate?) {
        requireAdapter().swipeDelegate = listener ?: ViewHolderSwipeDelegate.DEFAULT
    }

    public fun setItemSeparator(@DrawableRes drawableResource: Int) {
        dividerDecoration.drawableResource = drawableResource
    }

    public fun setItemSeparatorHeight(height: Int) {
        dividerDecoration.drawableHeight = height
    }

    public fun setOnEndReachedListener(listener: EndReachedListener?) {
        endReachedListener = listener
        observeListEndRegion()
    }

    private fun observeListEndRegion() {
        addOnScrollListener(scrollListener)
    }

    public fun setPaginationEnabled(enabled: Boolean) {
        scrollListener.setPaginationEnabled(enabled)
    }

    public fun reachedEndOfChannels(endReached: Boolean) {
        requireAdapter().endReached = endReached
    }

    public fun setChannels(channels: List<Channel>) {
        requireAdapter().submitList(channels)
    }

    public override fun onVisibilityChanged(view: View, visibility: Int) {
        super.onVisibilityChanged(view, visibility)
        if (visibility == 0 && adapter != null) requireAdapter().notifyDataSetChanged()
    }

    public fun interface UserClickListener {
        public companion object {
            public val DEFAULT: UserClickListener = UserClickListener {}
        }

        public fun onUserClick(user: User)
    }

    public fun interface ChannelClickListener {
        public companion object {
            public val DEFAULT: ChannelClickListener = ChannelClickListener {}
        }

        public fun onClick(channel: Channel)
    }

    public interface ViewHolderSwipeDelegate {
        public companion object {

            public val DEFAULT: ViewHolderSwipeDelegate = object : ViewHolderSwipeDelegate {
                override fun onSwipeStarted(viewHolder: ViewHolder, adapterPosition: Int, x: Float, y: Float) {
                    // no-op
                }

                override fun onSwipeChanged(viewHolder: ViewHolder, adapterPosition: Int, dX: Float) {
                    // no-op
                }

                override fun onSwipeEnded(viewHolder: ViewHolder, adapterPosition: Int, x: Float, y: Float) {
                    // no-op
                }

                override fun onSwipeCanceled(viewHolder: ViewHolder, adapterPosition: Int, x: Float, y: Float) {
                    // no-op
                }

                override fun onRestoreSwipePosition(viewHolder: ViewHolder, adapterPosition: Int) {
                    // no-op
                }
            }
        }

        public fun onSwipeStarted(viewHolder: ViewHolder, adapterPosition: Int, x: Float, y: Float)
        public fun onSwipeChanged(viewHolder: ViewHolder, adapterPosition: Int, dX: Float)
        public fun onSwipeEnded(viewHolder: ViewHolder, adapterPosition: Int, x: Float, y: Float)
        public fun onSwipeCanceled(viewHolder: ViewHolder, adapterPosition: Int, x: Float, y: Float)
        public fun onRestoreSwipePosition(viewHolder: ViewHolder, adapterPosition: Int)
    }

    public fun interface EndReachedListener {
        public fun onEndReached()
    }

    private inner class EndReachedScrollListener : OnScrollListener() {
        private var enabled = false
        override fun onScrollStateChanged(recyclerView: RecyclerView, newState: Int) {
            if (SCROLL_STATE_IDLE == newState) {
                val linearLayoutManager = getLayoutManager()?.cast<LinearLayoutManager>()
                val lastVisiblePosition = linearLayoutManager?.findLastVisibleItemPosition()
                val reachedTheEnd = requireAdapter().itemCount - 1 == lastVisiblePosition
                if (reachedTheEnd && enabled) {
                    endReachedListener?.onEndReached()
                }
            }
        }

        fun setPaginationEnabled(enabled: Boolean) {
            this.enabled = enabled
        }
    }
}
