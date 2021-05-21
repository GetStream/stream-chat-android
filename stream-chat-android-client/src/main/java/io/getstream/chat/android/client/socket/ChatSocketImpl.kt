@file:Suppress("DEPRECATION_ERROR")

package io.getstream.chat.android.client.socket

import io.getstream.chat.android.client.models.User
import io.getstream.chat.android.client.network.NetworkStateProvider
import io.getstream.chat.android.client.parser.ChatParser
import io.getstream.chat.android.client.token.TokenManager
import kotlinx.coroutines.CoroutineScope

internal class ChatSocketImpl(
    private val apiKey: String,
    private val wssUrl: String,
    tokenManager: TokenManager,
    parser: ChatParser,
    networkStateProvider: NetworkStateProvider,
    coroutineScope: CoroutineScope,
) : ChatSocket {

    private val service = ChatSocketServiceImpl(
        tokenManager,
        SocketFactory(parser, tokenManager),
        networkStateProvider,
        parser,
        coroutineScope,
    )

    override fun connectAnonymously() {
        service.anonymousConnect(wssUrl, apiKey)
    }

    override fun connect(user: User) {
        service.userConnect(wssUrl, apiKey, user)
    }

    override fun disconnect() {
        service.disconnect()
    }

    override fun disconnectTemporary() {
        service.disconnectRequested()
    }

    override fun addListener(listener: SocketListener) {
        service.addListener(listener)
    }

    override fun removeListener(listener: SocketListener) {
        service.removeListener(listener)
    }
}
