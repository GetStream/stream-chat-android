package io.getstream.chat.android.ui.message.list.adapter.viewholder.attachment

import android.content.Context
import android.view.View
import io.getstream.chat.android.client.models.Attachment

public interface AttachmentViewFactory {
    public fun createLinkAttachmentView(linkAttachment: Attachment, context: Context): View
    public fun createAttachmentsView(attachments: List<Attachment>, context: Context): View
}
