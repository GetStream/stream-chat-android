package io.getstream.chat.android.ui.message.list.adapter.viewholder.decorator.internal

import com.getstream.sdk.chat.adapter.MessageListItem
import io.getstream.chat.android.ui.common.extensions.internal.dpToPxPrecise
import io.getstream.chat.android.ui.message.list.adapter.viewholder.internal.CustomAttachmentsViewHolder
import io.getstream.chat.android.ui.message.list.adapter.viewholder.internal.FileAttachmentsViewHolder
import io.getstream.chat.android.ui.message.list.adapter.viewholder.internal.GiphyAttachmentViewHolder
import io.getstream.chat.android.ui.message.list.adapter.viewholder.internal.GiphyViewHolder
import io.getstream.chat.android.ui.message.list.adapter.viewholder.internal.ImageAttachmentViewHolder
import io.getstream.chat.android.ui.message.list.adapter.viewholder.internal.LinkAttachmentsViewHolder
import io.getstream.chat.android.ui.message.list.adapter.viewholder.internal.MessageDeletedViewHolder
import io.getstream.chat.android.ui.message.list.adapter.viewholder.internal.MessagePlainTextViewHolder
import io.getstream.chat.android.ui.message.list.background.MessageBackgroundFactory

internal class BackgroundDecorator(
    private val messageBackgroundFactory: MessageBackgroundFactory,
) : BaseDecorator() {

    /**
     * Decorates the background of the custom attachments message.
     *
     * @param viewHolder The holder to decorate.
     * @param data The item that holds all the information.
     */
    override fun decorateCustomAttachmentsMessage(
        viewHolder: CustomAttachmentsViewHolder,
        data: MessageListItem.MessageItem,
    ) {
        viewHolder.binding.messageContainer.background =
            messageBackgroundFactory.textAndAttachmentMessageBackground(
                viewHolder.binding.messageContainer.context,
                data
            )
    }

    /**
     * Decorates the background of the Giphy attachment.
     *
     * @param viewHolder The holder to decorate.
     * @param data The item that holds all the information.
     */
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

    /**
     * Decorates the background of the file attachments message.
     *
     * @param viewHolder The holder to decorate.
     * @param data The item that holds all the information.
     */
    override fun decorateFileAttachmentsMessage(
        viewHolder: FileAttachmentsViewHolder,
        data: MessageListItem.MessageItem,
    ) {
        viewHolder.binding.messageContainer.background =
            messageBackgroundFactory.fileAttachmentsMessageBackground(
                viewHolder.binding.messageContainer.context,
                data
            )
    }

    /**
     * Decorates the background of the image attachments message.
     *
     * @param viewHolder The holder to decorate.
     * @param data The item that holds all the information.
     */
    override fun decorateImageAttachmentsMessage(
        viewHolder: ImageAttachmentViewHolder,
        data: MessageListItem.MessageItem,
    ) {
        viewHolder.binding.messageContainer.background =
            messageBackgroundFactory.imageAttachmentMessageBackground(
                viewHolder.binding.messageContainer.context,
                data
            )
    }

    /**
     * Decorates the background of the deleted message.
     *
     * @param viewHolder The holder to decorate.
     * @param data The item that holds all the information.
     */
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

    /**
     * Decorates the background of the plain text message.
     *
     * @param viewHolder The holder to decorate.
     * @param data The item that holds all the information.
     */
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

    /**
     * Decorates the background of the ephemeral Giphy message.
     *
     * @param viewHolder The holder to decorate.
     * @param data The item that holds all the information.
     */
    override fun decorateGiphyMessage(
        viewHolder: GiphyViewHolder,
        data: MessageListItem.MessageItem,
    ) {
        viewHolder.binding.cardView.background =
            messageBackgroundFactory.giphyAppearanceModel(viewHolder.binding.cardView.context)
    }

    /**
     * Decorates the background of the message container.
     *
     * @param viewHolder The holder to decorate.
     * @param data The item that holds all the information.
     */
    override fun decorateLinkAttachmentsMessage(
        viewHolder: LinkAttachmentsViewHolder,
        data: MessageListItem.MessageItem,
    ) {
        viewHolder.binding.messageContainer.background =
            messageBackgroundFactory.linkAttachmentMessageBackground(
                viewHolder.binding.messageContainer.context,
                data
            )
    }

    companion object {
        internal val DEFAULT_CORNER_RADIUS = 16.dpToPxPrecise()
    }
}
