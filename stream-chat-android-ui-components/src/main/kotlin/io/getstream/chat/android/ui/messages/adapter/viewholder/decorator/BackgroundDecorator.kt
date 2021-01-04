package io.getstream.chat.android.ui.messages.adapter.viewholder.decorator

import android.graphics.Paint
import android.view.View
import androidx.core.content.ContextCompat
import com.getstream.sdk.chat.adapter.MessageListItem
import com.getstream.sdk.chat.utils.extensions.isBottomPosition
import com.google.android.material.shape.MaterialShapeDrawable
import com.google.android.material.shape.ShapeAppearanceModel
import io.getstream.chat.android.client.models.Attachment
import io.getstream.chat.android.ui.R
import io.getstream.chat.android.ui.messages.adapter.viewholder.GiphyViewHolder
import io.getstream.chat.android.ui.messages.adapter.viewholder.MessageDeletedViewHolder
import io.getstream.chat.android.ui.messages.adapter.viewholder.MessagePlainTextViewHolder
import io.getstream.chat.android.ui.messages.adapter.viewholder.OnlyFileAttachmentsViewHolder
import io.getstream.chat.android.ui.messages.adapter.viewholder.OnlyMediaAttachmentsViewHolder
import io.getstream.chat.android.ui.messages.adapter.viewholder.PlainTextWithFileAttachmentsViewHolder
import io.getstream.chat.android.ui.messages.adapter.viewholder.PlainTextWithMediaAttachmentsViewHolder
import io.getstream.chat.android.ui.utils.extensions.dpToPxPrecise
import io.getstream.chat.android.ui.utils.extensions.hasLink
import io.getstream.chat.android.ui.utils.extensions.withReply
import io.getstream.chat.android.ui.utils.extensions.withText

internal class BackgroundDecorator : BaseDecorator() {

    override fun decorateDeletedMessage(viewHolder: MessageDeletedViewHolder, data: MessageListItem.MessageItem) {
        val bottomRightCorner = if (data.isBottomPosition()) 0f else DEFAULT_CORNER_RADIUS
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
        setDefaultBackgroundDrawable(viewHolder.binding.messageContainer, data)
    }

    override fun decorateOnlyMediaAttachmentsMessage(
        viewHolder: OnlyMediaAttachmentsViewHolder,
        data: MessageListItem.MessageItem
    ) = decorateAttachmentsAndBackground(
        viewHolder.binding.backgroundView,
        viewHolder.binding.mediaAttachmentsGroupView,
        data
    )

    override fun decoratePlainTextWithMediaAttachmentsMessage(
        viewHolder: PlainTextWithMediaAttachmentsViewHolder,
        data: MessageListItem.MessageItem
    ) = decorateAttachmentsAndBackground(
        viewHolder.binding.backgroundView,
        viewHolder.binding.mediaAttachmentsGroupView,
        data
    )

    override fun decorateOnlyFileAttachmentsMessage(
        viewHolder: OnlyFileAttachmentsViewHolder,
        data: MessageListItem.MessageItem
    ) = decorateAttachmentsAndBackground(
        viewHolder.binding.backgroundView,
        viewHolder.binding.fileAttachmentsView,
        data
    )

    override fun decoratePlainTextWithFileAttachmentsMessage(
        viewHolder: PlainTextWithFileAttachmentsViewHolder,
        data: MessageListItem.MessageItem
    ) = decorateAttachmentsAndBackground(
        viewHolder.binding.backgroundView,
        viewHolder.binding.fileAttachmentsView,
        data
    )

    override fun decorateGiphyMessage(viewHolder: GiphyViewHolder, data: MessageListItem.MessageItem) {
        viewHolder.binding.cardView.shapeAppearanceModel = ShapeAppearanceModel.builder()
            .setAllCornerSizes(DEFAULT_CORNER_RADIUS)
            .setBottomRightCornerSize(SMALL_CARD_VIEW_CORNER_RADIUS)
            .build()
        viewHolder.binding.mediaAttachmentView.setImageShapeByCorners(
            IMAGE_VIEW_CORNER_RADIUS,
            IMAGE_VIEW_CORNER_RADIUS,
            0f,
            0f
        )
    }

    private fun decorateAttachmentsAndBackground(
        background: View,
        attachmentView: View,
        data: MessageListItem.MessageItem
    ) {
        setDefaultBackgroundDrawable(background, data)

        val topLeftCorner = if (data.message.withReply()) 0f else DEFAULT_CORNER_RADIUS
        val topRightCorner = if (data.message.withReply()) 0f else DEFAULT_CORNER_RADIUS
        val bottomRightCorner =
            if (data.message.withText() || (data.isMine && data.isBottomPosition())) 0f else DEFAULT_CORNER_RADIUS
        val bottomLeftCorner =
            if (data.message.withText() || (data.isTheirs && data.isBottomPosition())) 0f else DEFAULT_CORNER_RADIUS

        attachmentView.background = ShapeAppearanceModel.builder()
            .setTopLeftCornerSize(topLeftCorner)
            .setTopRightCornerSize(topRightCorner)
            .setBottomRightCornerSize(bottomRightCorner)
            .setBottomLeftCornerSize(bottomLeftCorner)
            .build()
            .let(::MaterialShapeDrawable)
            .apply { setTint(ContextCompat.getColor(attachmentView.context, R.color.stream_ui_transparent)) }
    }

    private fun setDefaultBackgroundDrawable(view: View, data: MessageListItem.MessageItem) {
        val radius = DEFAULT_CORNER_RADIUS
        val bottomRightCorner = if (data.isMine && data.isBottomPosition()) 0f else radius
        val bottomLeftCorner = if (data.isMine.not() && data.isBottomPosition()) 0f else radius
        val shapeAppearanceModel =
            ShapeAppearanceModel.builder().setAllCornerSizes(radius).setBottomLeftCornerSize(bottomLeftCorner)
                .setBottomRightCornerSize(bottomRightCorner).build()
        view.background = MaterialShapeDrawable(shapeAppearanceModel).apply {
            val hasLink = data.message.attachments.any(Attachment::hasLink)
            if (data.isMine) {
                paintStyle = Paint.Style.FILL
                setTint(
                    ContextCompat.getColor(
                        view.context,
                        if (hasLink) MESSAGE_LINK_BACKGROUND else MESSAGE_CURRENT_USER_BACKGROUND
                    )
                )
            } else {
                paintStyle = Paint.Style.FILL_AND_STROKE
                setStrokeTint(ContextCompat.getColor(view.context, MESSAGE_OTHER_STROKE_COLOR))
                strokeWidth = DEFAULT_STROKE_WIDTH
                setTint(
                    ContextCompat.getColor(
                        view.context,
                        if (hasLink) MESSAGE_LINK_BACKGROUND else MESSAGE_OTHER_USER_BACKGROUND
                    )
                )
            }
        }
    }

    companion object {
        private val MESSAGE_DELETED_BACKGROUND = R.color.stream_ui_grey_light_opacity_50
        private val MESSAGE_OTHER_STROKE_COLOR = R.color.stream_ui_border_stroke
        private val MESSAGE_OTHER_USER_BACKGROUND = R.color.stream_ui_white
        private val MESSAGE_CURRENT_USER_BACKGROUND = R.color.stream_ui_grey_90
        private val MESSAGE_LINK_BACKGROUND = R.color.stream_ui_blue_alice
        private val DEFAULT_CORNER_RADIUS = 16.dpToPxPrecise()
        private val DEFAULT_STROKE_WIDTH = 1.dpToPxPrecise()
        private val SMALL_CARD_VIEW_CORNER_RADIUS = 2.dpToPxPrecise()
        private val IMAGE_VIEW_CORNER_RADIUS = 8.dpToPxPrecise()
    }
}
