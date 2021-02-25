package io.getstream.chat.android.ui.message.list.adapter.viewholder.decorator.internal

import androidx.annotation.ColorInt
import com.getstream.sdk.chat.adapter.MessageListItem
import io.getstream.chat.android.ui.message.list.adapter.viewholder.internal.GiphyViewHolder
import io.getstream.chat.android.ui.message.list.adapter.viewholder.internal.MessagePlainTextViewHolder
import io.getstream.chat.android.ui.message.list.adapter.viewholder.internal.TextAndAttachmentsViewHolder
import io.getstream.chat.android.ui.message.list.internal.MessageListItemStyle

internal class TextDecorator(val style: MessageListItemStyle) : BaseDecorator() {
    override fun decorateTextAndAttachmentsMessage(
        viewHolder: TextAndAttachmentsViewHolder,
        data: MessageListItem.MessageItem,
    ) {
        viewHolder.binding.apply {
            getStyleTextColor(data.isMine)?.let { textColor ->
                messageText.setTextColor(textColor)
            }

            getStyleLinkTextColor(data.isMine)?.let { linkTextColor ->
                messageText.setLinkTextColor(linkTextColor)
            }
        }
    }

    override fun decoratePlainTextMessage(
        viewHolder: MessagePlainTextViewHolder,
        data: MessageListItem.MessageItem,
    ) {
        viewHolder.binding.apply {
            getStyleTextColor(data.isMine)?.let { textColor ->
                messageText.setTextColor(textColor)
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
