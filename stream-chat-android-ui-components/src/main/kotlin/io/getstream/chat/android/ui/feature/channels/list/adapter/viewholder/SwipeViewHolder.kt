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

package io.getstream.chat.android.ui.feature.channels.list.adapter.viewholder

import android.annotation.SuppressLint
import android.view.MotionEvent
import android.view.View
import io.getstream.chat.android.ui.feature.channels.list.ChannelListView
import io.getstream.chat.android.ui.utils.extensions.dpToPxPrecise
import kotlin.math.abs
import kotlin.math.absoluteValue

public abstract class SwipeViewHolder(itemView: View) : BaseChannelListItemViewHolder(itemView) {

    /**
     * The view that will be swiped.
     */
    public abstract fun getSwipeView(): View

    /**
     * The X position where the swipe view is considered to be opened.
     */
    public abstract fun getOpenedX(): Float

    /**
     * The X position where the swipe view is considered to be closed.
     */
    public abstract fun getClosedX(): Float

    /**
     * The range which the swipe view can slide
     */
    public abstract fun getSwipeDeltaRange(): ClosedFloatingPointRange<Float>

    /**
     * If swipe is enabled or disabled
     */
    public abstract fun isSwipeEnabled(): Boolean

    /**
     * If the swipe view is swiped of not. When true, swipe view is completely swiped, when false it is in the default state
     */
    public abstract fun isSwiped(): Boolean

    protected var listener: ChannelListView.SwipeListener? = null
    protected var swiping: Boolean = false

    @SuppressLint("ClickableViewAccessibility")
    public fun setSwipeListener(view: View, swipeListener: ChannelListView.SwipeListener?) {
        var startX = 0f
        var startY = 0f
        var prevX = 0f
        var wasSwiping = false
        listener = swipeListener

        view.setOnTouchListener { _, event ->
            if (!isSwipeEnabled()) {
                return@setOnTouchListener false
            }

            val rawX = event.rawX
            val rawY = event.rawY

            when (event.action) {
                MotionEvent.ACTION_DOWN -> {
                    // store the starting x & y so we can calculate total deltas
                    startX = rawX
                    startY = rawY
                    // initialize the previous x to the start values
                    prevX = startX
                    // don't know if it's a swipe yet; assume it's not
                    swiping = false
                    // don't consume
                    swiping
                }

                MotionEvent.ACTION_MOVE -> {
                    // calculate the total delta for both axes
                    val totalDeltaX = rawX - startX
                    val totalDeltaY = rawY - startY
                    // calculate the delta from the last event to this one
                    val lastMoveDeltaX = rawX - prevX
                    // now that we've calculated, update the previous x value with this event's x
                    prevX = rawX
                    // store the old swiping value so we can determine if we were ever swiping
                    wasSwiping = swiping
                    // determine if it's a swipe by comparing total axis delta magnitude
                    swiping = totalDeltaX.absoluteValue > totalDeltaY.absoluteValue

                    when {
                        // we've started swiping
                        !wasSwiping && swiping -> {
                            swipeListener?.onSwipeStarted(this, absoluteAdapterPosition, rawX, rawY)
                        }
                        // signal swipe movement
                        swiping -> {
                            swipeListener?.onSwipeChanged(this, absoluteAdapterPosition, lastMoveDeltaX, totalDeltaX)
                        }
                        // axis magnitude measurement has dictated we are no longer swiping
                        wasSwiping && !swiping -> {
                            swipeListener?.onSwipeCanceled(this, absoluteAdapterPosition, rawX, rawY)
                        }
                    }
                    // consume if we are swiping
                    swiping
                }

                MotionEvent.ACTION_UP -> {
                    // we should consume if we were swiping
                    var shouldConsume = false
                    if (wasSwiping) {
                        // no longer swiping
                        swiping = false
                        wasSwiping = false
                        // we should consume if we were swiping, and past threshold
                        shouldConsume = abs(rawX - startX) > SWIPE_THRESHOLD
                        // signal end of swipe
                        swipeListener?.onSwipeCompleted(this, absoluteAdapterPosition, rawX, rawY)
                    }

                    // consume if swipe distance is bigger than threshold
                    shouldConsume
                }

                MotionEvent.ACTION_CANCEL -> {
                    // take action if we were swiping, otherwise leave it alone
                    if (wasSwiping) {
                        // no longer swiping...
                        swiping = false
                        wasSwiping = false
                        // signal cancellation
                        swipeListener?.onSwipeCanceled(this, absoluteAdapterPosition, rawX, rawY)
                    }

                    wasSwiping
                }

                else -> false
            }
        }
    }

    private companion object {
        private val SWIPE_THRESHOLD = 16.dpToPxPrecise()
    }
}
