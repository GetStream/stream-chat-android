package io.getstream.chat.android.client.channel.manager

import io.getstream.chat.android.client.models.Attachment
import io.getstream.chat.android.client.models.Message

/**
 * Todo: Move this to State module
 */
public interface ChannelStateManager {

    public fun upsertMessage(message: Message)

    public fun upsertMessages(messages: List<Message>)

    public fun updateAttachmentUploadState(messageId: String, uploadId: String, newState: Attachment.UploadState)
}
