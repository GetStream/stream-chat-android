package io.getstream.chat.android.ui.messages.adapter.viewholder.decorator

import android.view.View
import androidx.core.content.ContextCompat
import com.getstream.sdk.chat.adapter.MessageListItem
import io.getstream.chat.android.ui.R
import io.getstream.chat.android.ui.messages.adapter.viewholder.MessageDeletedViewHolder
import io.getstream.chat.android.ui.messages.adapter.viewholder.MessagePlainTextViewHolder
import io.getstream.chat.android.ui.messages.adapter.viewholder.OnlyMediaAttachmentsViewHolder
import io.getstream.chat.android.ui.utils.extensions.dpToPxPrecise

internal class BackgroundDecorator : BaseDecorator() {

    override fun decorateDeletedMessage(viewHolder: MessageDeletedViewHolder, data: MessageListItem.MessageItem) {
        viewHolder.binding.deleteLabel.background = BackgroundDrawable(
            color = ContextCompat.getColor(viewHolder.itemView.context, MESSAGE_DELETED_BACKGROUND),
            topLeftCornerPx = DEFAULT_STROKE_WIDTH_DP,
            topRightCornerPx = DEFAULT_STROKE_WIDTH_DP,
            bottomRightCornerPx = if (data.positions.contains(MessageListItem.Position.BOTTOM)) 0f else DEFAULT_STROKE_WIDTH_DP,
            bottomLeftCornerPx = DEFAULT_STROKE_WIDTH_DP
        )
    }

    override fun decoratePlainTextMessage(
        viewHolder: MessagePlainTextViewHolder,
        data: MessageListItem.MessageItem
    ) {
        setDefaultBackgroundDrawable(viewHolder.binding.messageText, data)
    }

    private fun setDefaultBackgroundDrawable(view: View, data: MessageListItem.MessageItem) {
        view.background = if (data.isMine) {
            BackgroundDrawable(
                color = ContextCompat.getColor(view.context, MESSAGE_CURRENT_USER_BACKGROUND),
                topLeftCornerPx = DEFAULT_STROKE_WIDTH_DP,
                topRightCornerPx = DEFAULT_STROKE_WIDTH_DP,
                bottomRightCornerPx = if (data.positions.contains(MessageListItem.Position.BOTTOM)) 0f else DEFAULT_STROKE_WIDTH_DP,
                bottomLeftCornerPx = DEFAULT_STROKE_WIDTH_DP
            )
        } else {
            BackgroundStrokeDrawable(
                color = ContextCompat.getColor(view.context, MESSAGE_OTHER_USER_BACKGROUND),
                strokeColor = ContextCompat.getColor(view.context, MESSAGE_OTHER_STROKE_COLOR),
                strokeWidth = DEFAULT_STROKE_WIDTH_DP,
                topLeftCornerPx = DEFAULT_CORNER_RADIUS_DP,
                topRightCornerPx = DEFAULT_CORNER_RADIUS_DP,
                bottomRightCornerPx = DEFAULT_CORNER_RADIUS_DP,
                bottomLeftCornerPx = if (data.positions.contains(MessageListItem.Position.BOTTOM)) 0f else DEFAULT_STROKE_WIDTH_DP
            )
        }
    }

    override fun decorateOnlyMediaAttachmentsMessage(
        viewHolder: OnlyMediaAttachmentsViewHolder,
        data: MessageListItem.MessageItem
    ) {
        viewHolder.binding.imageView.clipToOutline = true
        viewHolder.binding.imageView.background = BackgroundDrawable(
            color = ContextCompat.getColor(viewHolder.itemView.context, android.R.color.transparent),
            topLeftCornerPx = DEFAULT_CORNER_RADIUS_DP,
            topRightCornerPx = DEFAULT_CORNER_RADIUS_DP,
            bottomRightCornerPx = if (data.isMine && data.positions.contains(MessageListItem.Position.BOTTOM)) 0f else DEFAULT_CORNER_RADIUS_DP,
            bottomLeftCornerPx = if (data.isMine.not() && data.positions.contains(MessageListItem.Position.BOTTOM)) 0f else DEFAULT_CORNER_RADIUS_DP
        )
        setDefaultBackgroundDrawable(viewHolder.binding.backgroundView, data)
    }

    companion object {
        private val MESSAGE_DELETED_BACKGROUND = R.color.stream_grey_light_opacity_50
        private val MESSAGE_OTHER_STROKE_COLOR = R.color.stream_border_stroke
        private val MESSAGE_OTHER_USER_BACKGROUND = R.color.stream_white
        private val MESSAGE_CURRENT_USER_BACKGROUND = R.color.stream_grey_90
        private val DEFAULT_CORNER_RADIUS_DP = 16.dpToPxPrecise()
        private val DEFAULT_STROKE_WIDTH_DP = 1.dpToPxPrecise()
    }
}
