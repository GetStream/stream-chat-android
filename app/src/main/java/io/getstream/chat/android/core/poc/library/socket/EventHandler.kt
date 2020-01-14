package io.getstream.chat.android.core.poc.library.socket

import android.os.Handler
import android.os.Message
import io.getstream.chat.android.core.poc.library.Event

class EventHandler(private val webSocketService: WebSocketService) : Handler() {
    override fun handleMessage(msg: Message) {
        super.handleMessage(msg)
        webSocketService.webSocketListener().onWSEvent(msg.obj as Event)
    }
}