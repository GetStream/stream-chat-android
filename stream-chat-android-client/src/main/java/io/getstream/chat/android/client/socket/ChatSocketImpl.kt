package io.getstream.chat.android.client.socket

import io.getstream.chat.android.client.models.User
import io.getstream.chat.android.client.network.NetworkStateProvider
import io.getstream.chat.android.client.parser.ChatParser
import io.getstream.chat.android.client.token.TokenManager
import io.getstream.chat.android.client.utils.observable.ChatObservable
import io.getstream.chat.android.client.utils.observable.ChatObservableImpl

internal class ChatSocketImpl(
    private val apiKey: String,
    private val wssUrl: String,
    tokenManager: TokenManager,
    parser: ChatParser,
    networkStateProvider: NetworkStateProvider
) : ChatSocket {

    private val eventsParser = EventsParser(parser)
    private val service = ChatSocketServiceImpl.create(
        tokenManager,
        SocketFactory(eventsParser, parser, tokenManager),
        eventsParser,
        networkStateProvider
    )

    override fun connectAnonymously() {
        service.anonymousConnect(wssUrl, apiKey)
    }

    override fun connect(user: User) {
        service.userConnect(wssUrl, apiKey, user)
    }

    override fun events(): ChatObservable {
        return ChatObservableImpl(service)
    }

    override fun disconnect() {
        service.disconnect()
    }

    override fun addListener(listener: SocketListener) {
        service.addListener(listener)
    }

    override fun removeListener(listener: SocketListener) {
        service.removeListener(listener)
    }
}
