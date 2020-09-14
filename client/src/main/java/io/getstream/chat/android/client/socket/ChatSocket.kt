package io.getstream.chat.android.client.socket

import io.getstream.chat.android.client.models.User

interface ChatSocket {
    val state: ChatSocketService.State
    fun connect(user: User)
    fun connectAnonymously()
    fun addListener(listener: SocketListener)
    fun removeListener(listener: SocketListener)
    fun disconnect()
}
