package io.getstream.chat.android.ui.message.list.adapter.viewholder.decorator.internal

import com.getstream.sdk.chat.adapter.MessageListItem
import com.google.android.material.shape.ShapeAppearanceModel
import io.getstream.chat.android.ui.common.extensions.internal.dpToPxPrecise
import io.getstream.chat.android.ui.message.list.adapter.viewholder.internal.GiphyViewHolder
import io.getstream.chat.android.ui.message.list.adapter.viewholder.internal.MessageDeletedViewHolder
import io.getstream.chat.android.ui.message.list.adapter.viewholder.internal.MessagePlainTextViewHolder
import io.getstream.chat.android.ui.message.list.adapter.viewholder.internal.TextAndAttachmentsViewHolder
import io.getstream.chat.android.ui.message.list.background.BackgroundDrawer

internal class BackgroundDecorator(private val backgroundDrawer: BackgroundDrawer) : BaseDecorator() {

    override fun decorateTextAndAttachmentsMessage(
        viewHolder: TextAndAttachmentsViewHolder,
        data: MessageListItem.MessageItem,
    ) {
        viewHolder.binding.messageContainer.background =
            backgroundDrawer.textAndAttachmentMessageBackground(
                viewHolder.binding.messageContainer.context,
                data
            )
    }

    override fun decorateDeletedMessage(
        viewHolder: MessageDeletedViewHolder,
        data: MessageListItem.MessageItem,
    ) {
        viewHolder.binding.messageContainer.background =
            backgroundDrawer.deletedMessageBackground(
                viewHolder.binding.messageContainer.context,
                data
            )
    }

    override fun decoratePlainTextMessage(
        viewHolder: MessagePlainTextViewHolder,
        data: MessageListItem.MessageItem,
    ) {
        viewHolder.binding.messageContainer.background =
            backgroundDrawer.plainTextMessageBackground(
                viewHolder.binding.messageContainer.context,
                data
            )
    }

    override fun decorateGiphyMessage(
        viewHolder: GiphyViewHolder,
        data: MessageListItem.MessageItem,
    ) {
        viewHolder.binding.cardView.shapeAppearanceModel = ShapeAppearanceModel.builder()
            .setAllCornerSizes(DEFAULT_CORNER_RADIUS)
            .setBottomRightCornerSize(SMALL_CARD_VIEW_CORNER_RADIUS)
            .build()
    }

    companion object {
        private val SMALL_CARD_VIEW_CORNER_RADIUS = 2.dpToPxPrecise()

        internal val DEFAULT_CORNER_RADIUS = 16.dpToPxPrecise()
    }
}
