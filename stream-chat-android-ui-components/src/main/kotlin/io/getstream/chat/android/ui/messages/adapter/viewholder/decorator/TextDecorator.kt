package io.getstream.chat.android.ui.messages.adapter.viewholder.decorator

import androidx.annotation.ColorInt
import com.getstream.sdk.chat.adapter.MessageListItem
import io.getstream.chat.android.ui.messages.adapter.viewholder.GiphyViewHolder
import io.getstream.chat.android.ui.messages.adapter.viewholder.MessagePlainTextViewHolder
import io.getstream.chat.android.ui.messages.adapter.viewholder.OnlyFileAttachmentsViewHolder
import io.getstream.chat.android.ui.messages.adapter.viewholder.OnlyMediaAttachmentsViewHolder
import io.getstream.chat.android.ui.messages.adapter.viewholder.PlainTextWithFileAttachmentsViewHolder
import io.getstream.chat.android.ui.messages.adapter.viewholder.PlainTextWithMediaAttachmentsViewHolder
import io.getstream.chat.android.ui.messages.view.MessageListItemStyle

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
