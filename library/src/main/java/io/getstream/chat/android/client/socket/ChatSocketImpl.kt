package io.getstream.chat.android.client.socket

import io.getstream.chat.android.client.models.User
import io.getstream.chat.android.client.parser.ChatParser
import io.getstream.chat.android.client.token.CachedTokenProvider
import io.getstream.chat.android.client.token.TokenProvider
import io.getstream.chat.android.client.utils.observable.ChatObservableImpl

class ChatSocketImpl(
    private val apiKey: String,
    private val wssUrl: String,
    private val cachedTokenProvider: CachedTokenProvider,
    private val chatParser: ChatParser
) : ChatSocket {

    private val service = ChatSocketServiceImpl(chatParser)

    override fun connectAnonymously(listener: InitConnectionListener?) {
        connect(null, null, listener)
    }

    override fun connect(user: User, listener: InitConnectionListener?) {
        cachedTokenProvider.getToken(object : TokenProvider.TokenProviderListener {
            override fun onSuccess(token: String) {
                connect(user, token, listener)
            }
        })
    }

    override fun events(): ChatObservableImpl {
        return ChatObservableImpl(
            service
        )
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

    private fun connect(user: User?, userToken: String?, listener: InitConnectionListener?) {
        service.connect(wssUrl, apiKey, user, userToken, listener)
    }

}