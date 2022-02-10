package io.getstream.chat.android.ui.message.list.adapter.viewholder.decorator.internal

import com.getstream.sdk.chat.adapter.MessageListItem
import io.getstream.chat.android.ui.common.extensions.internal.dpToPxPrecise
import io.getstream.chat.android.ui.message.list.adapter.viewholder.internal.GiphyAttachmentViewHolder
import io.getstream.chat.android.ui.message.list.adapter.viewholder.internal.GiphyViewHolder
import io.getstream.chat.android.ui.message.list.adapter.viewholder.internal.ImageAttachmentViewHolder
import io.getstream.chat.android.ui.message.list.adapter.viewholder.internal.MessageDeletedViewHolder
import io.getstream.chat.android.ui.message.list.adapter.viewholder.internal.MessagePlainTextViewHolder
import io.getstream.chat.android.ui.message.list.adapter.viewholder.internal.TextAndAttachmentsViewHolder
import io.getstream.chat.android.ui.message.list.background.MessageBackgroundFactory

internal class BackgroundDecorator(private val messageBackgroundFactory: MessageBackgroundFactory) : BaseDecorator() {

    override fun decorateTextAndAttachmentsMessage(
        viewHolder: TextAndAttachmentsViewHolder,
        data: MessageListItem.MessageItem,
    ) {
        viewHolder.binding.messageContainer.background =
            messageBackgroundFactory.textAndAttachmentMessageBackground(
                viewHolder.binding.messageContainer.context,
                data
            )
    }

    override fun decorateGiphyAttachmentMessage(
        viewHolder: GiphyAttachmentViewHolder,
        data: MessageListItem.MessageItem,
    ) {
        viewHolder.binding.messageContainer.background =
            messageBackgroundFactory.textAndAttachmentMessageBackground(
                viewHolder.binding.messageContainer.context,
                data
            )
    }

    override fun decorateImageAttachmentMessage(
        viewHolder: ImageAttachmentViewHolder,
        data: MessageListItem.MessageItem,
    ) {
        viewHolder.binding.messageContainer.background =
            messageBackgroundFactory.textAndAttachmentMessageBackground(
                viewHolder.binding.messageContainer.context,
                data
            )
    }

    override fun decorateDeletedMessage(
        viewHolder: MessageDeletedViewHolder,
        data: MessageListItem.MessageItem,
    ) {
        viewHolder.binding.messageContainer.background =
            messageBackgroundFactory.deletedMessageBackground(
                viewHolder.binding.messageContainer.context,
                data
            )
    }

    override fun decoratePlainTextMessage(
        viewHolder: MessagePlainTextViewHolder,
        data: MessageListItem.MessageItem,
    ) {
        viewHolder.binding.messageContainer.background =
            messageBackgroundFactory.plainTextMessageBackground(
                viewHolder.binding.messageContainer.context,
                data
            )
    }

    override fun decorateGiphyMessage(
        viewHolder: GiphyViewHolder,
        data: MessageListItem.MessageItem,
    ) {
        viewHolder.binding.cardView.background =
            messageBackgroundFactory.giphyAppearanceModel(viewHolder.binding.cardView.context)
    }

    companion object {
        private val SMALL_CARD_VIEW_CORNER_RADIUS = 2.dpToPxPrecise()

        internal val DEFAULT_CORNER_RADIUS = 16.dpToPxPrecise()
    }
}
