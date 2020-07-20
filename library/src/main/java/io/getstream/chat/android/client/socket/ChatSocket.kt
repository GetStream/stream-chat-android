package io.getstream.chat.android.client.socket

import io.getstream.chat.android.client.models.User
import io.getstream.chat.android.client.utils.observable.ChatObservable

interface ChatSocket {
    fun connect(user: User)
    fun connectAnonymously()
    fun events(): ChatObservable
    fun addListener(listener: SocketListener)
    fun removeListener(listener: SocketListener)
    fun disconnect()
}
