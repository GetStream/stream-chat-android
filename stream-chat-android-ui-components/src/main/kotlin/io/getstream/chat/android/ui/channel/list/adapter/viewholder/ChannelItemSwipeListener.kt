package io.getstream.chat.android.ui.channel.list.adapter.viewholder

import android.content.Context
import android.view.View
import androidx.recyclerview.widget.RecyclerView
import io.getstream.chat.android.ui.R
import io.getstream.chat.android.ui.channel.list.ChannelListView
import io.getstream.chat.android.ui.channel.list.ScrollPauseLinearLayoutManager
import io.getstream.chat.android.ui.utils.extensions.getDimension
import io.getstream.chat.android.ui.utils.extensions.safeCast

internal class ChannelItemSwipeListener(
    context: Context,
    val recyclerView: RecyclerView,
    val layoutManager: ScrollPauseLinearLayoutManager
) : ChannelListView.SwipeListener {

    private val menuItemWidth = context.getDimension(R.dimen.stream_ui_channel_list_item_option_icon_width).toFloat()
    private val optionsMenuWidth = menuItemWidth * ChannelViewHolder.OPTIONS_COUNT
    private val openValue = -optionsMenuWidth
    private val closedValue = 0f
    private val swipeRange = openValue..closedValue
    private val swipeStateByPosition = mutableMapOf<Int, SwipeState>()
    var multiSwipeEnabled: Boolean = false

    // Represents the internal swipe state of the ViewHolder
    sealed class SwipeState {
        internal object Open : SwipeState()
        internal object Closed : SwipeState()
    }

    override fun onSwipeStarted(viewHolder: RecyclerView.ViewHolder, adapterPosition: Int, x: Float, y: Float) {
        // pause scrolling
        layoutManager.verticalScrollEnabled = false
    }

    override fun onSwipeChanged(viewHolder: RecyclerView.ViewHolder, adapterPosition: Int, dX: Float) {
        viewHolder.safeCast<ChannelViewHolder>()?.let { channelViewHolder ->
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

    override fun onSwipeCompleted(viewHolder: RecyclerView.ViewHolder, adapterPosition: Int, x: Float, y: Float) {
        viewHolder.safeCast<ChannelViewHolder>()?.let { channelViewHolder ->
            val itemViewForeground = channelViewHolder.getItemViewForeground()
            // determine snap value
            val snapValue = when {
                itemViewForeground.x <= openValue / 2 -> openValue
                else -> closedValue
            }
            // animate to snap
            itemViewForeground.animateSwipeToValue(snapValue)
            // determine swipe state
            val swipeState = when {
                snapValue < 0 -> SwipeState.Open
                else -> SwipeState.Closed
            }
            // persist swipe state for the current item
            swipeStateByPosition[adapterPosition] = swipeState
            // potentially reset all other items
            if (!multiSwipeEnabled && swipeState == SwipeState.Open) {
                closeAllOtherPositions(adapterPosition)
            }
            // re-enable scrolling
            layoutManager.verticalScrollEnabled = true
        }
    }

    override fun onSwipeCanceled(viewHolder: RecyclerView.ViewHolder, adapterPosition: Int, x: Float, y: Float) {
        viewHolder.safeCast<ChannelViewHolder>()?.let { channelViewHolder ->
            // animate closed
            channelViewHolder
                .getItemViewForeground()
                .animateSwipeToValue(closedValue)
            // persist swipe state
            swipeStateByPosition[adapterPosition] = SwipeState.Closed
        }

        // re-enable scrolling
        layoutManager.verticalScrollEnabled = true
    }

    override fun onRestoreSwipePosition(viewHolder: RecyclerView.ViewHolder, adapterPosition: Int) {
        viewHolder.safeCast<ChannelViewHolder>()?.let { channelViewHolder ->
            channelViewHolder.getItemViewForeground().x = when (swipeStateByPosition[adapterPosition]) {
                SwipeState.Open -> -optionsMenuWidth
                else -> 0f
            }
        }
    }

    private fun View.animateSwipeToValue(value: Float) {
        animate()
            .x(value)
            .setStartDelay(0)
            .setDuration(100)
            .start()
    }

    // closes all swipe menus other than the position passed in
    private fun closeAllOtherPositions(adapterPosition: Int) {
        swipeStateByPosition
            .asSequence()
            .filter { it.key != adapterPosition }
            .filter { it.value == SwipeState.Open }
            .forEach { swipeStateEntry ->
                // persist the new state
                swipeStateByPosition[swipeStateEntry.key] = SwipeState.Closed
                // if the view holder currently visible, animate it closed
                recyclerView.findViewHolderForAdapterPosition(swipeStateEntry.key)
                    ?.safeCast<ChannelViewHolder>()
                    ?.let { viewHolder ->
                        val viewCompletelyVisible =
                            layoutManager.isViewPartiallyVisible(viewHolder.itemView, true, false)
                        val viewPartiallyVisible =
                            layoutManager.isViewPartiallyVisible(viewHolder.itemView, false, false)
                        val onScreen = viewCompletelyVisible || viewPartiallyVisible
                        if (onScreen) {
                            viewHolder
                                .getItemViewForeground()
                                .animateSwipeToValue(closedValue)
                        }
                    }
            }
    }
}
