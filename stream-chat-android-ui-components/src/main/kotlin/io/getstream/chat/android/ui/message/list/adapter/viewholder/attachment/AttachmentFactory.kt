package io.getstream.chat.android.ui.message.list.adapter.viewholder.attachment

import android.view.ViewGroup
import io.getstream.chat.android.client.models.Message
import io.getstream.chat.android.ui.message.list.adapter.MessageListListenerContainer

/**
 *
 */
public interface AttachmentFactory {

    /**
     * Checks if this [AttachmentFactory] is responsible for handling the given list of attachments.
     *
     * @param message
     * @return
     */
    public fun canHandle(message: Message, ): Boolean

    /**
     *
     *
     * @param message
     * @param listeners
     * @param parent
     * @return
     */
    public fun createViewHolder(
        message: Message,
        listeners: MessageListListenerContainer?,
        parent: ViewGroup,
    ): AttachmentViewHolder
}
