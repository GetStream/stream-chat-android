package io.getstream.chat.android.ui.message.list.adapter.viewholder.decorator.internal

import androidx.annotation.ColorInt
import com.getstream.sdk.chat.adapter.MessageListItem
import io.getstream.chat.android.ui.message.list.adapter.viewholder.internal.GiphyViewHolder
import io.getstream.chat.android.ui.message.list.adapter.viewholder.internal.MessagePlainTextViewHolder
import io.getstream.chat.android.ui.message.list.adapter.viewholder.internal.OnlyFileAttachmentsViewHolder
import io.getstream.chat.android.ui.message.list.adapter.viewholder.internal.OnlyMediaAttachmentsViewHolder
import io.getstream.chat.android.ui.message.list.adapter.viewholder.internal.PlainTextWithFileAttachmentsViewHolder
import io.getstream.chat.android.ui.message.list.adapter.viewholder.internal.PlainTextWithMediaAttachmentsViewHolder
import io.getstream.chat.android.ui.message.list.internal.MessageListItemStyle

internal class TextDecorator(val style: MessageListItemStyle) : BaseDecorator() {

    override fun decoratePlainTextWithFileAttachmentsMessage(
        viewHolder: PlainTextWithFileAttachmentsViewHolder,
        data: MessageListItem.MessageItem,
    ) {
        getStyleTextColor(data.isMine)?.let(viewHolder.binding.messageText::setTextColor)
    }

    override fun decorateOnlyFileAttachmentsMessage(
        viewHolder: OnlyFileAttachmentsViewHolder,
        data: MessageListItem.MessageItem,
    ) {
        getStyleTextColor(data.isMine)?.let(viewHolder.binding.sentFiles::setTextColor)
    }

    override fun decoratePlainTextWithMediaAttachmentsMessage(
        viewHolder: PlainTextWithMediaAttachmentsViewHolder,
        data: MessageListItem.MessageItem,
    ) {
        getStyleTextColor(data.isMine)?.let(viewHolder.binding.messageText::setTextColor)
    }

    override fun decorateOnlyMediaAttachmentsMessage(
        viewHolder: OnlyMediaAttachmentsViewHolder,
        data: MessageListItem.MessageItem,
    ) = Unit

    override fun decoratePlainTextMessage(
        viewHolder: MessagePlainTextViewHolder,
        data: MessageListItem.MessageItem,
    ) {
        viewHolder.binding.apply {
            getStyleTextColor(data.isMine)?.let { textColor ->
                messageText.setTextColor(textColor)
                // set title & description colors as well
                linkAttachmentView.binding.apply {
                    descriptionTextView.setTextColor(textColor)
                    titleTextView.setTextColor(textColor)
                }
            }

            getStyleLinkTextColor(data.isMine)?.let { linkTextColor ->
                messageText.setLinkTextColor(linkTextColor)
            }
        }
    }

    override fun decorateGiphyMessage(
        viewHolder: GiphyViewHolder,
        data: MessageListItem.MessageItem,
    ) = Unit

    @ColorInt
    private fun getStyleTextColor(isMine: Boolean): Int? {
        return if (isMine) style.messageTextColorMine else style.messageTextColorTheirs
    }

    @ColorInt
    private fun getStyleLinkTextColor(isMine: Boolean): Int? {
        return if (isMine) style.messageLinkTextColorMine else style.messageLinkTextColorTheirs
    }
}
