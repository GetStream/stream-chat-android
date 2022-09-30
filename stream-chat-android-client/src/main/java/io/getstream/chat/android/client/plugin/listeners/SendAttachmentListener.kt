package io.getstream.chat.android.client.plugin.listeners

import io.getstream.chat.android.client.models.Message

public interface SendAttachmentListener {

    public suspend fun onAttachmentSendRequest(
        channelType: String,
        channelId: String,
        message: Message,
    )
}
