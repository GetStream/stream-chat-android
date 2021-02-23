package io.getstream.chat.android.ui.message.list.adapter.viewholder.attachment.internal

import android.content.Context
import android.view.View
import android.view.ViewGroup
import io.getstream.chat.android.client.models.Attachment
import io.getstream.chat.android.core.internal.InternalStreamChatApi
import io.getstream.chat.android.ui.common.extensions.internal.hasLink
import io.getstream.chat.android.ui.message.list.adapter.internal.MessageListItemViewTypeMapper.isMedia
import io.getstream.chat.android.ui.message.list.adapter.view.internal.FileAttachmentsView
import io.getstream.chat.android.ui.message.list.adapter.view.internal.LinkAttachmentView
import io.getstream.chat.android.ui.message.list.adapter.view.internal.MediaAttachmentsGroupView
import io.getstream.chat.android.ui.message.list.adapter.viewholder.attachment.AttachmentViewFactory

@InternalStreamChatApi
internal class StreamAttachmentViewFactory : AttachmentViewFactory {
    override fun createLinkAttachmentView(linkAttachment: Attachment, context: Context): View {
        require(linkAttachment.hasLink()) { "Can create link view only for attachments with link" }
        return LinkAttachmentView(context).apply {
            layoutParams = DEFAULT_LAYOUT_PARAMS
        }
    }

    override fun createAttachmentsView(attachments: List<Attachment>, context: Context): View {
        return when {
            attachments.isMedia() -> MediaAttachmentsGroupView(context).apply {
                layoutParams = DEFAULT_LAYOUT_PARAMS
            }
            attachments.isNotEmpty() -> FileAttachmentsView(context).apply {
                layoutParams = DEFAULT_LAYOUT_PARAMS
            }
            else -> error("Unsupported case for attachment view factory!")
        }
    }

    private companion object {
        private val DEFAULT_LAYOUT_PARAMS =
            ViewGroup.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT)
    }
}
