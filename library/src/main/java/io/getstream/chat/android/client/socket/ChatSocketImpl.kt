package io.getstream.chat.android.client.socket

import io.getstream.chat.android.client.CachedTokenProvider
import io.getstream.chat.android.client.TokenProvider
import io.getstream.chat.android.client.User
import io.getstream.chat.android.client.gson.JsonParser
import io.getstream.chat.android.client.logger.StreamLogger

class ChatSocketImpl(
    val apiKey: String,
    val wssUrl: String,
    val cachedTokenProvider: CachedTokenProvider,
    val jsonParser: JsonParser,
    logger: StreamLogger?
) : ChatSocket {

    private val service = ChatSocketService(jsonParser)

    override fun connectAnonymously() {
        connect(null, null)
    }

    override fun connect(user: User, tokenProvider: TokenProvider) {

        cachedTokenProvider.setTokenProvider(tokenProvider)

        tokenProvider.getToken(object : TokenProvider.TokenProviderListener {
            override fun onSuccess(token: String) {
                connect(user, token)
            }
        })
    }

    override fun events(): ChatObservable {
        return ChatObservable(service)
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

    private fun connect(user: User?, userToken: String?) {
        service.connect(wssUrl, apiKey, user, userToken)
    }

}