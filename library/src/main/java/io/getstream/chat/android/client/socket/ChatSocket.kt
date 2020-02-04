package io.getstream.chat.android.client.socket

import io.getstream.chat.android.client.User
import io.getstream.chat.android.client.observable.ChatObservable

interface ChatSocket {
    fun connectAnonymously()
    fun connect(user: User)
    fun events(): ChatObservable
    fun addListener(listener: SocketListener)
    fun removeListener(listener: SocketListener)
    fun disconnect()
}