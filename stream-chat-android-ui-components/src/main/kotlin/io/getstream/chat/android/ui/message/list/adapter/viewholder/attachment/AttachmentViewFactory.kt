package io.getstream.chat.android.ui.message.list.adapter.viewholder.attachment

import android.content.Context
import android.view.View
import android.view.ViewGroup
import io.getstream.chat.android.client.models.Attachment
import io.getstream.chat.android.ui.common.extensions.internal.hasLink
import io.getstream.chat.android.ui.common.extensions.internal.isMedia
import io.getstream.chat.android.ui.message.list.adapter.view.internal.FileAttachmentsView
import io.getstream.chat.android.ui.message.list.adapter.view.internal.LinkAttachmentView
import io.getstream.chat.android.ui.message.list.adapter.view.internal.MediaAttachmentsGroupView

/**
 * Factory for creating content views for links attachments and other types of attachments.
 */
public open class AttachmentViewFactory {
    /**
     * Creates a content view for the link attachment type.
     *
     * @param linkAttachment Attachment representing some link.
     * @param context Context related to parent view.
     */
    public open fun createLinkAttachmentView(linkAttachment: Attachment, context: Context): View {
        require(linkAttachment.hasLink()) { "Can create link view only for attachments with link" }
        return LinkAttachmentView(context).apply {
            layoutParams = DEFAULT_LAYOUT_PARAMS
        }
    }

    /**
     * Creates a content view for general attachments.
     *
     * @param attachments List of attachments. Resulting view should represents this list.
     * @param context Context related to parent view.
     */
    public open fun createAttachmentsView(attachments: List<Attachment>, context: Context): View {
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

        private fun Collection<Attachment>.isMedia(): Boolean =
            isNotEmpty() && all { it.isMedia() && it.hasLink().not() }

        private val DEFAULT_LAYOUT_PARAMS =
            ViewGroup.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT)
    }
}
