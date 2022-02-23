package io.getstream.chat.android.ui.message.list.adapter.viewholder.attachment

import android.view.ViewGroup
import io.getstream.chat.android.client.models.Message
import io.getstream.chat.android.ui.message.list.adapter.MessageListListenerContainer

/**
 *
 */
public class AttachmentFactories(
    public val attachmentFactories: List<AttachmentFactory> = listOf(),
) {
    /**
     *
     */
    public fun canHandle(message: Message): Boolean {
        return attachmentFactories.any { it.canHandle(message) }
    }

    /**
     *
     */
    public fun createViewHolder(
        message: Message,
        listeners: MessageListListenerContainer?,
        parent: ViewGroup,
    ): AttachmentViewHolder {
        val factory = attachmentFactories.first { it.canHandle(message) }
        return factory.createViewHolder(message, listeners, parent)
    }
}
