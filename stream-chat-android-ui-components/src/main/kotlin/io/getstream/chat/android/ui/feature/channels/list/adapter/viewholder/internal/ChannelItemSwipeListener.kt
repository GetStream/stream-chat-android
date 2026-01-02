/*
 * Copyright (c) 2014-2026 Stream.io Inc. All rights reserved.
 *
 * Licensed under the Stream License;
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *    https://github.com/GetStream/stream-chat-android/blob/main/LICENSE
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package io.getstream.chat.android.ui.feature.channels.list.adapter.viewholder.internal

import android.view.View
import androidx.recyclerview.widget.RecyclerView
import io.getstream.chat.android.ui.common.extensions.internal.safeCast
import io.getstream.chat.android.ui.feature.channels.list.ChannelListView
import io.getstream.chat.android.ui.feature.channels.list.adapter.viewholder.SwipeViewHolder
import io.getstream.chat.android.ui.widgets.internal.ScrollPauseLinearLayoutManager

internal class ChannelItemSwipeListener @JvmOverloads constructor(
    val recyclerView: RecyclerView,
    val layoutManager: ScrollPauseLinearLayoutManager,
    private val swipeStateByPosition: MutableMap<Int, SwipeState> = mutableMapOf(),
    private var multiSwipeEnabled: Boolean = false,
) : ChannelListView.SwipeListener {

    // Represents the internal swipe state of the ViewHolder
    sealed class SwipeState {
        internal object Open : SwipeState() {
            override fun toString(): String = "Open"
        }
        internal object Closed : SwipeState() {
            override fun toString(): String = "Closed"
        }
    }

    override fun onSwipeStarted(viewHolder: SwipeViewHolder, adapterPosition: Int, x: Float?, y: Float?) {
        // disable scrolling
        layoutManager.verticalScrollEnabled = false
    }

    override fun onSwipeChanged(viewHolder: SwipeViewHolder, adapterPosition: Int, dX: Float, totalDeltaX: Float) {
        // our edge starts at 0, so our x can always be clamped into our delta range
        val projectedX = viewHolder.getSwipeView().x + dX
        // clamp it and animate if necessary
        projectedX.coerceIn(viewHolder.getSwipeDeltaRange()).let { clampedX ->
            // set the new x if it's different
            val swipeView = viewHolder.getSwipeView()
            if (swipeView.x != clampedX) {
                swipeView.x = clampedX
            }
        }
        // cancel ripple animation
        viewHolder.itemView.isPressed = false
    }

    /**
     * Called when swipe is completed.
     *
     * @param viewHolder [SwipeViewHolder].
     * @param adapterPosition
     * @param x position in the X axis.
     * @param y position in the Y axis.
     */
    override fun onSwipeCompleted(viewHolder: SwipeViewHolder, adapterPosition: Int, x: Float?, y: Float?) {
        // determine snap value
        val snapValue = if (viewHolder.isSwiped()) {
            viewHolder.getOpenedX()
        } else {
            viewHolder.getClosedX()
        }

        // animate to snap
        viewHolder.getSwipeView().animateSwipeTo(snapValue)

        val swipeState = if (viewHolder.isSwiped()) {
            SwipeState.Open
        } else {
            SwipeState.Closed
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

    override fun onSwipeCanceled(viewHolder: SwipeViewHolder, adapterPosition: Int, x: Float?, y: Float?) {
        // animate closed
        viewHolder.getSwipeView().animateSwipeTo(viewHolder.getClosedX())
        // persist swipe state
        swipeStateByPosition[adapterPosition] = SwipeState.Closed
        // re-enable scrolling
        layoutManager.verticalScrollEnabled = true
    }

    override fun onRestoreSwipePosition(viewHolder: SwipeViewHolder, adapterPosition: Int) {
        viewHolder.apply {
            getSwipeView().x = when (swipeStateByPosition[adapterPosition]) {
                SwipeState.Open -> getOpenedX()
                else -> getClosedX()
            }
        }
    }

    private fun View.animateSwipeTo(value: Float) {
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
                            viewHolder.getSwipeView().animateSwipeTo(viewHolder.getClosedX())
                        }
                    }
            }
    }
}
