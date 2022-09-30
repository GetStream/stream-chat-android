package io.getstream.chat.android.offline.plugin.listener.internal

import io.getstream.chat.android.client.models.Message
import io.getstream.chat.android.client.persistance.repository.ChannelRepository
import io.getstream.chat.android.client.persistance.repository.MessageRepository
import io.getstream.chat.android.client.plugin.listeners.SendAttachmentListener

internal class SendAttachmentsListenerDatabase(
    private val messageRepository: MessageRepository,
    private val channelRepository: ChannelRepository
): SendAttachmentListener {

    override suspend fun onAttachmentSendRequest(channelType: String, channelId: String, message: Message) {
        // we insert early to ensure we don't lose messages
        messageRepository.insertMessage(message)
        channelRepository.updateLastMessageForChannel(message.cid, message)
    }
}
