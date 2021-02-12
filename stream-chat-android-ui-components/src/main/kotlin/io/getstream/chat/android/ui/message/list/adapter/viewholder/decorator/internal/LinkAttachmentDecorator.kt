package io.getstream.chat.android.ui.message.list.adapter.viewholder.decorator.internal

import android.view.View
import androidx.constraintlayout.widget.ConstraintLayout
import androidx.constraintlayout.widget.ConstraintSet
import androidx.core.view.isVisible
import com.getstream.sdk.chat.adapter.MessageListItem
import com.getstream.sdk.chat.utils.extensions.updateConstraints
import io.getstream.chat.android.client.models.Message
import io.getstream.chat.android.ui.common.extensions.internal.hasLink
import io.getstream.chat.android.ui.message.list.adapter.view.internal.LinkAttachmentView
import io.getstream.chat.android.ui.message.list.adapter.viewholder.internal.GiphyViewHolder
import io.getstream.chat.android.ui.message.list.adapter.viewholder.internal.MessagePlainTextViewHolder
import io.getstream.chat.android.ui.message.list.adapter.viewholder.internal.OnlyFileAttachmentsViewHolder
import io.getstream.chat.android.ui.message.list.adapter.viewholder.internal.OnlyMediaAttachmentsViewHolder
import io.getstream.chat.android.ui.message.list.adapter.viewholder.internal.PlainTextWithFileAttachmentsViewHolder
import io.getstream.chat.android.ui.message.list.adapter.viewholder.internal.PlainTextWithMediaAttachmentsViewHolder

internal class LinkAttachmentDecorator : BaseDecorator() {
    override fun decoratePlainTextWithFileAttachmentsMessage(
        viewHolder: PlainTextWithFileAttachmentsViewHolder,
        data: MessageListItem.MessageItem,
    ) {
        decorate(viewHolder.binding.linkAttachmentView, data.message)
    }

    override fun decorateOnlyFileAttachmentsMessage(
        viewHolder: OnlyFileAttachmentsViewHolder,
        data: MessageListItem.MessageItem,
    ) = Unit

    override fun decoratePlainTextWithMediaAttachmentsMessage(
        viewHolder: PlainTextWithMediaAttachmentsViewHolder,
        data: MessageListItem.MessageItem,
    ) {
        decorate(viewHolder.binding.linkAttachmentView, data.message)
    }

    override fun decorateOnlyMediaAttachmentsMessage(
        viewHolder: OnlyMediaAttachmentsViewHolder,
        data: MessageListItem.MessageItem,
    ) = Unit

    override fun decoratePlainTextMessage(
        viewHolder: MessagePlainTextViewHolder,
        data: MessageListItem.MessageItem,
    ) {
        applyMaxPossibleWidth(viewHolder.binding.root, viewHolder.binding.messageContainer, data.message)
        decorate(viewHolder.binding.linkAttachmentView, data.message)
    }

    override fun decorateGiphyMessage(
        viewHolder: GiphyViewHolder,
        data: MessageListItem.MessageItem,
    ) = Unit

    private fun decorate(linkAttachmentView: LinkAttachmentView, message: Message) {
        val linkAttachment = message.attachments.firstOrNull { it.hasLink() }
        if (linkAttachment != null) {
            linkAttachmentView.isVisible = true
            linkAttachmentView.showLinkAttachment(linkAttachment)
        } else {
            linkAttachmentView.isVisible = false
        }
    }

    private fun applyMaxPossibleWidth(root: ConstraintLayout, messageContainer: View, message: Message) {
        val hasLink = message.attachments.any { it.hasLink() }
        val layoutWidth = messageContainer.layoutParams.width
        if (hasLink && layoutWidth == ConstraintSet.WRAP_CONTENT) {
            root.updateConstraints {
                constrainWidth(messageContainer.id, ConstraintSet.MATCH_CONSTRAINT)
                constrainDefaultWidth(messageContainer.id, ConstraintSet.MATCH_CONSTRAINT_SPREAD)
            }
        } else if (!hasLink && layoutWidth == ConstraintSet.MATCH_CONSTRAINT) {
            root.updateConstraints {
                constrainWidth(messageContainer.id, ConstraintSet.WRAP_CONTENT)
                constrainDefaultWidth(messageContainer.id, ConstraintSet.MATCH_CONSTRAINT_WRAP)
            }
        }
    }
}
