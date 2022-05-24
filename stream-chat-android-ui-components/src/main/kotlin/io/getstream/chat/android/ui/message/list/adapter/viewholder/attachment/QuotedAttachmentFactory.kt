package io.getstream.chat.android.ui.message.list.adapter.viewholder.attachment

import android.view.View
import android.view.ViewGroup
import io.getstream.chat.android.client.models.Message

/**
 * Represents a handler that can handle quoted attachments of certain type and create views for them.
 */
public interface QuotedAttachmentFactory {

    /**
     * Checks if this [QuotedAttachmentFactory] can consume quoted attachments from the given message.
     *
     * @param message The message containing custom quoted attachments that we are going to render.
     * @return True if the factory can handle the  quoted attachments from this quoted message.
     */
    public fun canHandle(message: Message): Boolean

    /**
     * Create a view for the quoted attachments.
     *
     * @param message The message containing attachments that we are going to render.
     * @param parent The parent View where the attachment content view is supposed to be placed.
     * @return A quoted attachment view to be placed inside the quoted message.
     */
    public fun generateQuotedAttachmentView(
        message: Message,
        parent: ViewGroup,
    ): View
}