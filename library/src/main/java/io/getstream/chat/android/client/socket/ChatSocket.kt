package io.getstream.chat.android.client.socket

import io.getstream.chat.android.client.TokenProvider
import io.getstream.chat.android.client.User

interface ChatSocket {
    fun connectAnonymously()
    fun connect(user: User, tokenProvider: TokenProvider)
    fun events(): ChatObservable
    fun disconnect()
}