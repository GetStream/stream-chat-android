package io.getstream.chat.android.ui.messages.adapter.viewholder.decorator

import android.widget.TextView
import androidx.core.content.ContextCompat
import com.getstream.sdk.chat.adapter.MessageListItem
import io.getstream.chat.android.ui.R
import io.getstream.chat.android.ui.messages.adapter.viewholder.GiphyViewHolder
import io.getstream.chat.android.ui.messages.adapter.viewholder.MessagePlainTextViewHolder
import io.getstream.chat.android.ui.messages.adapter.viewholder.OnlyFileAttachmentsViewHolder
import io.getstream.chat.android.ui.messages.adapter.viewholder.OnlyMediaAttachmentsViewHolder
import io.getstream.chat.android.ui.messages.adapter.viewholder.PlainTextWithFileAttachmentsViewHolder
import io.getstream.chat.android.ui.messages.adapter.viewholder.PlainTextWithMediaAttachmentsViewHolder
import io.getstream.chat.android.ui.messages.view.MessageListItemStyle

internal class TextDecorator(val style: MessageListItemStyle) : BaseDecorator() {

    private fun decorateMessageText(
        textView: TextView,
        isMine: Boolean,
    ) {
        val styleTextColor = if (isMine) style.messageTextColorMine else style.messageTextColorTheirs
        val styleLinkTextColor = if (isMine) style.messageLinkTextColorMine else style.messageLinkTextColorTheirs

        val textColor = styleTextColor ?: ContextCompat.getColor(textView.context, R.color.stream_ui_black)
        val linkTextColor =
            styleLinkTextColor ?: ContextCompat.getColor(textView.context, R.color.stream_ui_accent_blue)

        textView.apply {
            setTextColor(textColor)
            setLinkTextColor(linkTextColor)
        }
    }

    override fun decoratePlainTextWithFileAttachmentsMessage(
        viewHolder: PlainTextWithFileAttachmentsViewHolder,
        data: MessageListItem.MessageItem,
    ) {
        decorateMessageText(viewHolder.binding.messageText, data.isMine)
    }

    override fun decorateOnlyFileAttachmentsMessage(
        viewHolder: OnlyFileAttachmentsViewHolder,
        data: MessageListItem.MessageItem,
    ) {
        decorateMessageText(viewHolder.binding.sentFiles, data.isMine)
    }

    override fun decoratePlainTextWithMediaAttachmentsMessage(
        viewHolder: PlainTextWithMediaAttachmentsViewHolder,
        data: MessageListItem.MessageItem,
    ) {
        decorateMessageText(viewHolder.binding.messageText, data.isMine)
    }

    override fun decorateOnlyMediaAttachmentsMessage(
        viewHolder: OnlyMediaAttachmentsViewHolder,
        data: MessageListItem.MessageItem,
    ) = Unit

    override fun decoratePlainTextMessage(
        viewHolder: MessagePlainTextViewHolder,
        data: MessageListItem.MessageItem,
    ) {
        decorateMessageText(viewHolder.binding.messageText, data.isMine)
    }

    override fun decorateGiphyMessage(
        viewHolder: GiphyViewHolder,
        data: MessageListItem.MessageItem,
    ) = Unit
}
