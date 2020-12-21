package io.getstream.chat.android.ui.messages.adapter.viewholder.decorator

import android.graphics.Paint
import android.view.View
import androidx.core.content.ContextCompat
import com.getstream.sdk.chat.adapter.MessageListItem
import com.google.android.material.shape.MaterialShapeDrawable
import com.google.android.material.shape.ShapeAppearanceModel
import io.getstream.chat.android.ui.R
import io.getstream.chat.android.ui.messages.adapter.viewholder.MessageDeletedViewHolder
import io.getstream.chat.android.ui.messages.adapter.viewholder.MessagePlainTextViewHolder
import io.getstream.chat.android.ui.messages.adapter.viewholder.OnlyFileAttachmentsViewHolder
import io.getstream.chat.android.ui.messages.adapter.viewholder.OnlyMediaAttachmentsViewHolder
import io.getstream.chat.android.ui.messages.adapter.viewholder.PlainTextWithFileAttachmentsViewHolder
import io.getstream.chat.android.ui.messages.adapter.viewholder.PlainTextWithMediaAttachmentsViewHolder
import io.getstream.chat.android.ui.utils.extensions.dpToPxPrecise

internal class BackgroundDecorator : BaseDecorator() {

    override fun decorateDeletedMessage(viewHolder: MessageDeletedViewHolder, data: MessageListItem.MessageItem) {
        val bottomRightCorner =
            if (data.positions.contains(MessageListItem.Position.BOTTOM)) 0f else DEFAULT_CORNER_RADIUS
        val shapeAppearanceModel = ShapeAppearanceModel.builder().setAllCornerSizes(DEFAULT_CORNER_RADIUS)
            .setBottomRightCornerSize(bottomRightCorner).build()
        viewHolder.binding.deleteLabel.background = MaterialShapeDrawable(shapeAppearanceModel).apply {
            setTint(ContextCompat.getColor(viewHolder.itemView.context, MESSAGE_DELETED_BACKGROUND))
        }
    }

    override fun decoratePlainTextMessage(
        viewHolder: MessagePlainTextViewHolder,
        data: MessageListItem.MessageItem
    ) {
        setDefaultBackgroundDrawable(viewHolder.binding.messageText, data)
    }

    private fun setDefaultBackgroundDrawable(view: View, data: MessageListItem.MessageItem) {
        val radius = DEFAULT_CORNER_RADIUS
        val bottomRightCorner =
            if (data.isMine && data.positions.contains(MessageListItem.Position.BOTTOM)) 0f else radius
        val bottomLeftCorner =
            if (data.isMine.not() && data.positions.contains(MessageListItem.Position.BOTTOM)) 0f else radius
        val shapeAppearanceModel =
            ShapeAppearanceModel.builder().setAllCornerSizes(radius).setBottomLeftCornerSize(bottomLeftCorner)
                .setBottomRightCornerSize(bottomRightCorner).build()
        view.background = MaterialShapeDrawable(shapeAppearanceModel).apply {
            if (data.isMine) {
                paintStyle = Paint.Style.FILL
                setTint(ContextCompat.getColor(view.context, MESSAGE_CURRENT_USER_BACKGROUND))
            } else {
                paintStyle = Paint.Style.FILL_AND_STROKE
                setStrokeTint(ContextCompat.getColor(view.context, MESSAGE_OTHER_STROKE_COLOR))
                strokeWidth = DEFAULT_STROKE_WIDTH
                setTint(ContextCompat.getColor(view.context, MESSAGE_OTHER_USER_BACKGROUND))
            }
        }
    }

    override fun decorateOnlyMediaAttachmentsMessage(
        viewHolder: OnlyMediaAttachmentsViewHolder,
        data: MessageListItem.MessageItem
    ) {
        setDefaultBackgroundDrawable(viewHolder.binding.mediaAttachmentsGroupView, data)
    }

    override fun decoratePlainTextWithMediaAttachmentsMessage(
        viewHolder: PlainTextWithMediaAttachmentsViewHolder,
        data: MessageListItem.MessageItem
    ) {
        setDefaultBackgroundDrawable(viewHolder.binding.backgroundView, data)
        val shapeAppearanceModel = ShapeAppearanceModel.builder()
            .setTopLeftCornerSize(DEFAULT_CORNER_RADIUS)
            .setTopRightCornerSize(DEFAULT_CORNER_RADIUS)
            .build()
        viewHolder.binding.mediaAttachmentsGroupView.background = MaterialShapeDrawable(shapeAppearanceModel).apply {
            setTint(ContextCompat.getColor(viewHolder.itemView.context, R.color.stream_ui_transparent))
        }
    }

    override fun decorateOnlyFileAttachmentsMessage(
        viewHolder: OnlyFileAttachmentsViewHolder,
        data: MessageListItem.MessageItem
    ) {
        setDefaultBackgroundDrawable(viewHolder.binding.fileAttachmentsView, data)
    }

    override fun decoratePlainTextWithFileAttachmentsMessage(
        viewHolder: PlainTextWithFileAttachmentsViewHolder,
        data: MessageListItem.MessageItem
    ) {
        setDefaultBackgroundDrawable(viewHolder.binding.backgroundView, data)
    }

    companion object {
        private val MESSAGE_DELETED_BACKGROUND = R.color.stream_ui_grey_light_opacity_50
        private val MESSAGE_OTHER_STROKE_COLOR = R.color.stream_ui_border_stroke
        private val MESSAGE_OTHER_USER_BACKGROUND = R.color.stream_ui_white
        private val MESSAGE_CURRENT_USER_BACKGROUND = R.color.stream_ui_grey_90
        private val DEFAULT_CORNER_RADIUS = 16.dpToPxPrecise()
        private val DEFAULT_STROKE_WIDTH = 1.dpToPxPrecise()
    }
}
