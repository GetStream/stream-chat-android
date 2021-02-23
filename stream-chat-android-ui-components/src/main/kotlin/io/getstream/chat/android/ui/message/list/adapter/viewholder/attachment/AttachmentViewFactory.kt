package io.getstream.chat.android.ui.message.list.adapter.viewholder.attachment

import android.content.Context
import android.view.View
import io.getstream.chat.android.client.models.Attachment

/**
 * Factory for creating content views for links attachments and other types of attachments.
 */
public interface AttachmentViewFactory {

    /**
     * Creates a content view for the link attachment type.
     *
     * @param linkAttachment Attachment representing some link.
     * @param context Context related to parent view.
     */
    public fun createLinkAttachmentView(linkAttachment: Attachment, context: Context): View

    /**
     * Creates a content view for general attachments.
     *
     * @param attachments List of attachments. Resulting view should represents this list.
     * @param context Context related to parent view.
     */
    public fun createAttachmentsView(attachments: List<Attachment>, context: Context): View
}
