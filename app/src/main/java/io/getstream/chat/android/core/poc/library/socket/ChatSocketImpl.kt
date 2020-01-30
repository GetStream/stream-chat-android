package io.getstream.chat.android.core.poc.library.socket

import io.getstream.chat.android.core.poc.library.CachedTokenProvider
import io.getstream.chat.android.core.poc.library.TokenProvider
import io.getstream.chat.android.core.poc.library.User
import io.getstream.chat.android.core.poc.library.gson.JsonParser
import io.getstream.chat.android.core.poc.library.gson.JsonParserImpl
import java.util.*

class ChatSocketImpl(
    val apiKey: String,
    val wssUrl: String,
    val cachedTokenProvider: CachedTokenProvider,
    val jsonParser: JsonParser
) : ChatSocket {

    private val service = StreamWebSocketService(jsonParser)

    override fun connect(): ChatObservable {
        connect(null, null)
        return events()
    }

    override fun connect(user: User, tokenProvider: TokenProvider): ChatObservable {

        cachedTokenProvider.setTokenProvider(tokenProvider)

        tokenProvider.getToken(object : TokenProvider.TokenProviderListener {
            override fun onSuccess(token: String) {
                connect(user, token)
            }
        })

        return events()
    }

    override fun events(): ChatObservable {
        return ChatObservable(service)
    }

    override fun disconnect() {
        service.disconnect()
    }

    private fun connect(
        user: User?,
        userToken: String?
    ) {
        service.connect(wssUrl, apiKey, user, userToken, SocketListener())
    }

}