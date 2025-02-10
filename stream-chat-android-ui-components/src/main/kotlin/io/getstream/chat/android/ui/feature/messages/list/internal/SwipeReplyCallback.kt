package io.getstream.chat.android.ui.feature.messages.list.internal

import android.graphics.Canvas
import android.graphics.Rect
import android.graphics.drawable.Drawable
import android.view.View
import androidx.recyclerview.widget.ItemTouchHelper
import androidx.recyclerview.widget.ItemTouchHelper.ACTION_STATE_IDLE
import androidx.recyclerview.widget.ItemTouchHelper.RIGHT
import androidx.recyclerview.widget.RecyclerView
import io.getstream.chat.android.models.Message
import io.getstream.chat.android.ui.feature.messages.list.adapter.BaseMessageItemViewHolder
import io.getstream.chat.android.ui.feature.messages.list.adapter.MessageListItem

internal class SwipeReplyCallback(
        val replyDrawable: Drawable,
        val canReply: (Message?) -> Boolean,
    ) : ItemTouchHelper.Callback() {

        private val RecyclerView.ViewHolder.message: Message?
            get() = asBaseMessageItemViewHolder()?.messageItem?.message

        private val BaseMessageItemViewHolder<*>.messageItem: MessageListItem.MessageItem?
            get() = data as? MessageListItem.MessageItem

        private fun RecyclerView.ViewHolder.asBaseMessageItemViewHolder(): BaseMessageItemViewHolder<*>? =
            this as? BaseMessageItemViewHolder<*>

        var onReply: (message: Message) -> Unit = {}

        override fun getMovementFlags(recyclerView: RecyclerView, viewHolder: RecyclerView.ViewHolder): Int =
            when (canReply(viewHolder.message)) {
                true -> makeMovementFlags(ACTION_STATE_IDLE, RIGHT)
                false -> 0
            }

        override fun onMove(
            recyclerView: RecyclerView,
            viewHolder: RecyclerView.ViewHolder,
            target: RecyclerView.ViewHolder,
        ): Boolean = false

        override fun getSwipeThreshold(viewHolder: RecyclerView.ViewHolder): Float = 0.3f

        override fun onSwiped(viewHolder: RecyclerView.ViewHolder, direction: Int) {
            viewHolder.message?.let { onReply(it) }
        }

        override fun onChildDraw(
            c: Canvas,
            recyclerView: RecyclerView,
            viewHolder: RecyclerView.ViewHolder,
            dX: Float,
            dY: Float,
            actionState: Int,
            isCurrentlyActive: Boolean
        ) {
            val rightAlignment = minOf(dX.toInt(), replyDrawable.intrinsicWidth*3)
            val topAlignment = viewHolder.itemView.middleVerticalPoint - replyDrawable.intrinsicHeight/2
            replyDrawable.bounds = Rect(
                rightAlignment - replyDrawable.intrinsicWidth,
                topAlignment,
                rightAlignment,
                topAlignment + replyDrawable.intrinsicHeight,
            )
            super.onChildDraw(c, recyclerView, viewHolder, minOf(dX, (recyclerView.width/2).toFloat()), dY, actionState, isCurrentlyActive)
            replyDrawable.draw(c)
        }

        private val View.middleVerticalPoint: Int
            get() = (top + bottom) / 2
    }