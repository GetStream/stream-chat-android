package io.getstream.chat.android.ui.messages.adapter.viewholder.decorator

import androidx.core.view.isVisible
import com.getstream.sdk.chat.adapter.MessageListItem
import io.getstream.chat.android.client.models.Message
import io.getstream.chat.android.ui.messages.adapter.view.LinkAttachmentView
import io.getstream.chat.android.ui.messages.adapter.viewholder.GiphyViewHolder
import io.getstream.chat.android.ui.messages.adapter.viewholder.MessagePlainTextViewHolder
import io.getstream.chat.android.ui.messages.adapter.viewholder.OnlyFileAttachmentsViewHolder
import io.getstream.chat.android.ui.messages.adapter.viewholder.OnlyMediaAttachmentsViewHolder
import io.getstream.chat.android.ui.messages.adapter.viewholder.PlainTextWithFileAttachmentsViewHolder
import io.getstream.chat.android.ui.messages.adapter.viewholder.PlainTextWithMediaAttachmentsViewHolder
import io.getstream.chat.android.ui.utils.extensions.hasLink

internal class LinkAttachmentDecorator : BaseDecorator() {
    override fun decoratePlainTextWithFileAttachmentsMessage(
        viewHolder: PlainTextWithFileAttachmentsViewHolder,
        data: MessageListItem.MessageItem,
        isThread: Boolean
    ) {
        decorate(viewHolder.binding.linkAttachmentView, data.message)
    }

    override fun decorateOnlyFileAttachmentsMessage(
        viewHolder: OnlyFileAttachmentsViewHolder,
        data: MessageListItem.MessageItem,
        isThread: Boolean
    ) = Unit

    override fun decoratePlainTextWithMediaAttachmentsMessage(
        viewHolder: PlainTextWithMediaAttachmentsViewHolder,
        data: MessageListItem.MessageItem,
        isThread: Boolean
    ) {
        decorate(viewHolder.binding.linkAttachmentView, data.message)
    }

    override fun decorateOnlyMediaAttachmentsMessage(
        viewHolder: OnlyMediaAttachmentsViewHolder,
        data: MessageListItem.MessageItem,
        isThread: Boolean
    ) = Unit

    override fun decoratePlainTextMessage(
        viewHolder: MessagePlainTextViewHolder,
        data: MessageListItem.MessageItem,
        isThread: Boolean
    ) {
        decorate(viewHolder.binding.linkAttachmentView, data.message)
    }

    override fun decorateGiphyMessage(viewHolder: GiphyViewHolder, data: MessageListItem.MessageItem, isThread: Boolean) = Unit

    private fun decorate(linkAttachmentView: LinkAttachmentView, message: Message) {
        val linkAttachment = message.attachments.firstOrNull { it.hasLink() }
        if (linkAttachment != null) {
            linkAttachmentView.isVisible = true
            linkAttachmentView.showLinkAttachment(linkAttachment)
        } else {
            linkAttachmentView.isVisible = false
        }
    }
}
