package io.getstream.chat.android.client.socket

import io.getstream.chat.android.client.events.ChatEvent
import io.getstream.chat.android.client.parser.ChatParser
import okhttp3.WebSocket

internal class Socket(val socket: WebSocket, val parser: ChatParser) {

    fun send(event: ChatEvent) {
        socket.send(parser.toJson(event))
    }

    fun close(code: Int, reason: String) {
        socket.close(code, reason)
    }
}
