package io.getstream.chat.android.ui.channel.list.adapter.viewholder

import android.annotation.SuppressLint
import android.view.MotionEvent
import android.view.View
import io.getstream.chat.android.ui.channel.list.ChannelListView
import kotlin.math.absoluteValue

public abstract class SwipeViewHolder(itemView: View) : BaseChannelListItemViewHolder(itemView) {
    @SuppressLint("ClickableViewAccessibility")
    public fun setSwipeListener(view: View, swipeListener: ChannelListView.SwipeListener) {
        var startX = 0f
        var startY = 0f
        var prevX = 0f
        var swiping = false
        var wasSwiping = false
        val position = absoluteAdapterPosition

        // restore the view's last state
        swipeListener.onRestoreSwipePosition(this, absoluteAdapterPosition)

        view.setOnTouchListener { _, event ->
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
                            swipeListener.onSwipeStarted(this, position, rawX, rawY)
                        }
                        // signal swipe movement
                        swiping -> {
                            swipeListener.onSwipeChanged(this, position, lastMoveDeltaX)
                        }
                        // axis magnitude measurement has dictated we are no longer swiping
                        wasSwiping && !swiping -> {
                            swipeListener.onSwipeCanceled(this, position, rawX, rawY)
                        }
                    }
                    // consume if we are swiping
                    swiping
                }

                MotionEvent.ACTION_UP -> {
                    // signal end of swipe
                    swipeListener.onSwipeCompleted(this, position, rawX, rawY)
                    wasSwiping = false
                    // consume if we were swiping
                    swiping
                }

                MotionEvent.ACTION_CANCEL -> {
                    // take action if we were swiping, otherwise leave it alone
                    if (wasSwiping) {
                        // no longer swiping...
                        swiping = false
                        wasSwiping = false
                        // signal cancellation
                        swipeListener.onSwipeCanceled(this, position, rawX, rawY)
                    }

                    wasSwiping
                }

                else -> false
            }
        }
    }
}
