package io.getstream.chat.android.offline.plugin.listener.internal

import io.getstream.chat.android.client.plugin.listeners.MarkThreadReadListener
import io.getstream.chat.android.offline.plugin.logic.internal.LogicRegistry

internal class MarkThreadReadListenerImpl(
    private val logic: LogicRegistry,
) : MarkThreadReadListener {

    override fun markThreadAsRead(channelType: String, channelId: String, parentMessageId: String) {
        logic.channel(channelType, channelId).markThreadAsRead(parentMessageId)
    }
}