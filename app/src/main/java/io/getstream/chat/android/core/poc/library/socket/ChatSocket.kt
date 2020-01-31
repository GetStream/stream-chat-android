package io.getstream.chat.android.core.poc.library.socket

import io.getstream.chat.android.core.poc.library.TokenProvider
import io.getstream.chat.android.core.poc.library.User

interface ChatSocket {
    fun connect(user: User, tokenProvider: TokenProvider)
    fun events(): ChatObservable
    fun disconnect()
}