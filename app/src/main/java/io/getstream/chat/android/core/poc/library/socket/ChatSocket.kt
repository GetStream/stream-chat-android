package io.getstream.chat.android.core.poc.library.socket

import io.getstream.chat.android.core.poc.library.TokenProvider
import io.getstream.chat.android.core.poc.library.User
import io.getstream.chat.android.core.poc.library.call.ChatCall

interface ChatSocket {
    fun connect(user: User, tokenProvider: TokenProvider): ChatCall<ConnectionData>
    fun events(): ChatObservable
    fun disconnect()
}