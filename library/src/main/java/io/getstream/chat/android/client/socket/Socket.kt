package io.getstream.chat.android.client.socket

import io.getstream.chat.android.client.events.ChatEvent

internal interface Socket {
    fun send(event: ChatEvent)
    fun cancel()
    fun close(code: Int, reason: String)
}