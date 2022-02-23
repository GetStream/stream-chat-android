package io.getstream.chat.android.ui.message.list.adapter.viewholder.attachment

import android.view.ViewGroup
import io.getstream.chat.android.client.models.Message
import io.getstream.chat.android.ui.message.list.adapter.MessageListListenerContainer

/**
 * Represents a handler that can handle custom attachments of certain type and create
 * ViewHolders for them.
 */
public interface AttachmentFactory {

    /**
     * Checks if this [AttachmentFactory] can consume attachments from the given message.
     *
     * @param message The message containing custom attachments that we are going to render.
     * @return True if the factory can handle the attachments from this message.
     */
    public fun canHandle(message: Message): Boolean

    /**
     * Create a ViewHolder for the custom attachments View which is aware of the parent's
     * ViewHolder lifecycle.
     *
     * @param message The message containing custom attachments that we are going to render.
     * @param listeners [MessageListListenerContainer] with listeners for the message list.
     * @param parent The parent View where the attachment content view is supposed to be placed.
     * @return An inner ViewHolder with the attachment content view.
     */
    public fun createViewHolder(
        message: Message,
        listeners: MessageListListenerContainer?,
        parent: ViewGroup,
    ): AttachmentViewHolder
}
