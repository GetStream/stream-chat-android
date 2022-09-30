package io.getstream.chat.android.offline.plugin.listener.internal

import io.getstream.chat.android.client.models.Message
import io.getstream.chat.android.client.plugin.listeners.SendAttachmentListener
import io.getstream.chat.android.offline.plugin.logic.internal.LogicRegistry

internal class SendAttachmentListenerState(private val logic: LogicRegistry): SendAttachmentListener {

    override suspend fun onAttachmentSendRequest(channelType: String, channelId: String, message: Message) {
        val channel = logic.channel(channelType, channelId)

        channel.upsertMessage(message)
        logic.threadFromMessage(message)?.upsertMessage(message)

        // Update flow for currently running queries
        logic.getActiveQueryChannelsLogic().forEach { query -> query.refreshChannelState(channel.cid) }
    }
}
