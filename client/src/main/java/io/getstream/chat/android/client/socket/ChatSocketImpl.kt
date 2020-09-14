package io.getstream.chat.android.client.socket

import io.getstream.chat.android.client.models.User
import io.getstream.chat.android.client.parser.ChatParser
import io.getstream.chat.android.client.token.TokenManager

internal class ChatSocketImpl(
    private val apiKey: String,
    private val wssUrl: String,
    tokenManager: TokenManager,
    parser: ChatParser
) : ChatSocket {

    private val eventsParser = EventsParser(parser)

    private val service = ChatSocketServiceImpl(
        eventsParser,
        tokenManager,
        SocketFactory(eventsParser, parser, tokenManager)
    )

    override val state: ChatSocketService.State
        get() = service.state

    override fun connectAnonymously() {
        service.connect(wssUrl, apiKey, null)
    }

    override fun connect(user: User) {
        service.connect(wssUrl, apiKey, user)
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
