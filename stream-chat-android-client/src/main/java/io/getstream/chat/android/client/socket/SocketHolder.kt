package io.getstream.chat.android.client.socket

import okhttp3.WebSocket
import okio.ByteString

internal class SocketHolder {
    private var webSocket: WebSocket? = null

    fun initiate(webSocket: WebSocket) {
        this.webSocket = webSocket
    }

    fun shutdown() {
        webSocket = null
    }

    fun send(text: String) = webSocket?.send(text) ?: false

    fun send(bytes: ByteString) = webSocket?.send(bytes) ?: false

    fun close(code: Int, reason: String?) = webSocket?.close(code, reason) ?: false

    fun cancel() = webSocket?.cancel() ?: Unit
}