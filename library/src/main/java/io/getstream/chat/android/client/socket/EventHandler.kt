package io.getstream.chat.android.client.socket

import android.os.Handler
import android.os.Message
import io.getstream.chat.android.client.events.ChatEvent

class EventHandler(private val webSocketService: ChatSocketServiceImpl) : Handler() {
    override fun handleMessage(msg: Message) {
        super.handleMessage(msg)
        webSocketService.onRemoteEvent(msg.obj as ChatEvent)
    }
}