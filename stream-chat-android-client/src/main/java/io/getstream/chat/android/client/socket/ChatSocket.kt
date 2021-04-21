@file:Suppress("DEPRECATION_ERROR")

package io.getstream.chat.android.client.socket

import io.getstream.chat.android.client.models.User

internal interface ChatSocket {
    fun connect(user: User)
    fun connectAnonymously()
    fun addListener(listener: SocketListener)
    fun removeListener(listener: SocketListener)
    fun disconnect()
}
