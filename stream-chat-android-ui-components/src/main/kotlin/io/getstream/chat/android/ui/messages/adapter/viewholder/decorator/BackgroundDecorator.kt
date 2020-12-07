package io.getstream.chat.android.ui.messages.adapter.viewholder.decorator

import androidx.core.content.ContextCompat
import com.getstream.sdk.chat.adapter.MessageListItem
import io.getstream.chat.android.ui.R
import io.getstream.chat.android.ui.messages.adapter.viewholder.MessageDeletedViewHolder
import io.getstream.chat.android.ui.messages.adapter.viewholder.MessagePlainTextViewHolder
import io.getstream.chat.android.ui.utils.extensions.dpToPxPrecise

internal class BackgroundDecorator : BaseDecorator() {

    override fun decorateDeletedMessage(viewHolder: MessageDeletedViewHolder, data: MessageListItem.MessageItem) {
        val radius = DEFAULT_CORNER_RADIUS_DP.dpToPxPrecise()
        viewHolder.binding.deleteLabel.background = BackgroundDrawable(
            color = ContextCompat.getColor(viewHolder.itemView.context, MESSAGE_DELETED_BACKGROUND),
            topLeftCornerPx = radius,
            topRightCornerPx = radius,
            bottomRightCornerPx = if (data.positions.contains(MessageListItem.Position.BOTTOM)) 0f else radius,
            bottomLeftCornerPx = radius
        )
    }

    override fun decoratePlainTextMessage(
        viewHolder: MessagePlainTextViewHolder,
        data: MessageListItem.MessageItem
    ) {
        val radius = DEFAULT_CORNER_RADIUS_DP.dpToPxPrecise()
        viewHolder.binding.messageText.background = if (data.isMine) {
            BackgroundDrawable(
                color = ContextCompat.getColor(viewHolder.itemView.context, MESSAGE_CURRENT_USER_BACKGROUND),
                topLeftCornerPx = radius,
                topRightCornerPx = radius,
                bottomRightCornerPx = if (data.positions.contains(MessageListItem.Position.BOTTOM)) 0f else radius,
                bottomLeftCornerPx = radius
            )
        } else {
            BackgroundStrokeDrawable(
                color = ContextCompat.getColor(viewHolder.itemView.context, MESSAGE_OTHER_USER_BACKGROUND),
                strokeColor = ContextCompat.getColor(viewHolder.itemView.context, MESSAGE_OTHER_STROKE_COLOR),
                strokeWidth = DEFAULT_STROKE_WIDTH_DP.dpToPxPrecise(),
                topLeftCornerPx = radius,
                topRightCornerPx = radius,
                bottomRightCornerPx = radius,
                bottomLeftCornerPx = if (data.positions.contains(MessageListItem.Position.BOTTOM)) 0f else radius
            )
        }
    }

    companion object {
        private val MESSAGE_DELETED_BACKGROUND = R.color.stream_ui_grey_light_opacity_50
        private val MESSAGE_OTHER_STROKE_COLOR = R.color.stream_ui_border_stroke
        private val MESSAGE_OTHER_USER_BACKGROUND = R.color.stream_ui_white
        private val MESSAGE_CURRENT_USER_BACKGROUND = R.color.stream_ui_grey_90
        private const val DEFAULT_CORNER_RADIUS_DP = 16
        private const val DEFAULT_STROKE_WIDTH_DP = 1
    }
}
