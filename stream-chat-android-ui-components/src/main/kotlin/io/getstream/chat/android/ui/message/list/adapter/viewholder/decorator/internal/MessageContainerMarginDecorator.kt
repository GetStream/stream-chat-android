package io.getstream.chat.android.ui.message.list.adapter.viewholder.decorator.internal

import android.view.View
import android.view.ViewGroup
import androidx.core.view.updateLayoutParams
import com.getstream.sdk.chat.adapter.MessageListItem
import io.getstream.chat.android.ui.message.list.MessageListItemStyle
import io.getstream.chat.android.ui.message.list.adapter.viewholder.internal.GiphyViewHolder
import io.getstream.chat.android.ui.message.list.adapter.viewholder.internal.LinkAttachmentsViewHolder
import io.getstream.chat.android.ui.message.list.adapter.viewholder.internal.MessageDeletedViewHolder
import io.getstream.chat.android.ui.message.list.adapter.viewholder.internal.MessagePlainTextViewHolder
import io.getstream.chat.android.ui.message.list.adapter.viewholder.internal.TextAndAttachmentsViewHolder

internal class MessageContainerMarginDecorator(private val style: MessageListItemStyle) : BaseDecorator() {

    override fun decorateTextAndAttachmentsMessage(
        viewHolder: TextAndAttachmentsViewHolder,
        data: MessageListItem.MessageItem,
    ) {
        viewHolder.binding.run { configMargins(messageContainer, footnote, style) }
    }

    override fun decoratePlainTextMessage(viewHolder: MessagePlainTextViewHolder, data: MessageListItem.MessageItem) {
        viewHolder.binding.run { configMargins(messageContainer, footnote, style) }
    }

    override fun decorateDeletedMessage(viewHolder: MessageDeletedViewHolder, data: MessageListItem.MessageItem) {
        viewHolder.binding.run { configMargins(messageContainer, footnote, style) }
    }

    override fun decorateGiphyMessage(viewHolder: GiphyViewHolder, data: MessageListItem.MessageItem) = Unit

    /**
     * Decorates the message container of the link attachment.
     *
     * @param viewHolder The holder to decorate.
     * @param data The item that holds all the information.
     */
    override fun decorateLinkAttachmentsMessage(
        viewHolder: LinkAttachmentsViewHolder,
        data: MessageListItem.MessageItem,
    ) {
        viewHolder.binding.run { configMargins(messageContainer, footnote, style) }
    }

    private fun configMargins(messageContainer: View, footnote: View, style: MessageListItemStyle) {
        messageContainer.updateLayoutParams<ViewGroup.MarginLayoutParams> {
            marginStart = style.messageStartMargin
            marginEnd = style.messageEndMargin
        }

        footnote.updateLayoutParams<ViewGroup.MarginLayoutParams> {
            marginStart = style.messageStartMargin
            marginEnd = style.messageEndMargin
        }
    }
}
