package io.getstream.chat.android.ui.message.list.adapter.viewholder.decorator.internal

import android.widget.TextView
import com.getstream.sdk.chat.adapter.MessageListItem
import io.getstream.chat.android.ui.message.list.MessageListItemStyle
import io.getstream.chat.android.ui.message.list.adapter.viewholder.internal.GiphyAttachmentViewHolder
import io.getstream.chat.android.ui.message.list.adapter.viewholder.internal.GiphyViewHolder
import io.getstream.chat.android.ui.message.list.adapter.viewholder.internal.MessagePlainTextViewHolder
import io.getstream.chat.android.ui.message.list.adapter.viewholder.internal.TextAndAttachmentsViewHolder

internal class TextDecorator(private val style: MessageListItemStyle) : BaseDecorator() {
    override fun decorateTextAndAttachmentsMessage(
        viewHolder: TextAndAttachmentsViewHolder,
        data: MessageListItem.MessageItem,
    ) = setupTextView(viewHolder.binding.messageText, data)

    /**
     * Decorates the text of the Giphy attachment.
     *
     * @param viewHolder The holder to decorate.
     * @param data The item that holds all the information.
     */
    override fun decorateGiphyAttachmentMessage(
        viewHolder: GiphyAttachmentViewHolder,
        data: MessageListItem.MessageItem,
    ) = setupTextView(viewHolder.binding.messageText, data)

    override fun decoratePlainTextMessage(
        viewHolder: MessagePlainTextViewHolder,
        data: MessageListItem.MessageItem,
    ) = setupTextView(viewHolder.binding.messageText, data)

    override fun decorateGiphyMessage(
        viewHolder: GiphyViewHolder,
        data: MessageListItem.MessageItem,
    ) = Unit

    private fun setupTextView(textView: TextView, data: MessageListItem.MessageItem) {
        if (data.isMine) {
            style.textStyleMine.apply(textView)
        } else {
            style.textStyleTheirs.apply(textView)
        }

        style.getStyleLinkTextColor(data.isMine)?.let { linkTextColor ->
            textView.setLinkTextColor(linkTextColor)
        }
    }
}
