package io.getstream.chat.android.core.poc.library.socket

import android.os.Handler
import android.os.Message
import io.getstream.chat.android.core.poc.library.events.ChatEvent

class EventHandler(private val webSocketService: StreamWebSocketService) : Handler() {
    override fun handleMessage(msg: Message) {
        super.handleMessage(msg)
        webSocketService.onWsEvent(msg.obj as ChatEvent)
    }
}