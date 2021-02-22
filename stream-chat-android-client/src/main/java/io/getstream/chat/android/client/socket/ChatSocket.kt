@file:Suppress("DEPRECATION_ERROR")

package io.getstream.chat.android.client.socket

import io.getstream.chat.android.client.models.User
import io.getstream.chat.android.client.utils.observable.ChatObservable

internal interface ChatSocket {
    fun connect(user: User)
    fun connectAnonymously()
    @Deprecated(
        message = "Use addListener and removeListener directly instead (or the subscribe methods of ChatClient)",
        level = DeprecationLevel.ERROR,
    )
    fun events(): ChatObservable
    fun addListener(listener: SocketListener)
    fun removeListener(listener: SocketListener)
    fun disconnect()
}
