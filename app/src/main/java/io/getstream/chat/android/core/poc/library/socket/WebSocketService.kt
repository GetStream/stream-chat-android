package io.getstream.chat.android.core.poc.library.socket

import io.getstream.chat.android.core.poc.library.User

interface WebSocketService {
    fun connect(listener: (User, Throwable?) -> Unit)
    fun disconnect()
    fun webSocketListener(): WSResponseHandler
}
