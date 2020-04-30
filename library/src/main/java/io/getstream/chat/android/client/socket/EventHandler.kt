package io.getstream.chat.android.client.socket

import android.os.Handler
import android.os.Message
import io.getstream.chat.android.client.events.ChatEvent

internal class EventHandler(private val socketService: ChatSocketServiceImpl) : Handler() {
    override fun handleMessage(msg: Message) {
        super.handleMessage(msg)
        socketService.onRemoteEvent(msg.obj as ChatEvent)
    }
}