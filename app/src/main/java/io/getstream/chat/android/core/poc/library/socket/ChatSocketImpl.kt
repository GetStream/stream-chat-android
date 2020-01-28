package io.getstream.chat.android.core.poc.library.socket

import io.getstream.chat.android.core.poc.library.CachedTokenProvider
import io.getstream.chat.android.core.poc.library.TokenProvider
import io.getstream.chat.android.core.poc.library.User
import io.getstream.chat.android.core.poc.library.call.ChatCall
import io.getstream.chat.android.core.poc.library.gson.JsonParser
import io.getstream.chat.android.core.poc.library.gson.JsonParserImpl

class ChatSocketImpl(
    val apiKey: String,
    val wssUrl: String,
    val cachedTokenProvider: CachedTokenProvider,
    val jsonParser: JsonParser
) : ChatSocket {

    private val service = StreamWebSocketService(jsonParser)

    fun connect(): ChatCall<ConnectionData> {
        val result = ConnectionCall()

        val callback: (ConnectionData, Throwable?) -> Unit = { c, t ->
            result.deliverResult(c, t)
        }

        connect(null, null, callback)

        return result
    }

    override fun connect(user: User, tokenProvider: TokenProvider): ChatCall<ConnectionData> {

        cachedTokenProvider.setTokenProvider(tokenProvider)

        val result = ConnectionCall()

        val callback: (ConnectionData, Throwable?) -> Unit = { c, t ->
            result.deliverResult(c, t)
        }

        tokenProvider.getToken(object : TokenProvider.TokenProviderListener {
            override fun onSuccess(token: String) {
                connect(user, token, callback)
            }
        })

        return result
    }

    override fun events(): ChatObservable {
        return ChatObservable(service)
    }

    override fun disconnect() {
        service.disconnect()
    }

    private fun connect(
        user: User?,
        userToken: String?,
        listener: (ConnectionData, Throwable?) -> Unit
    ) {
        service.connect(wssUrl, apiKey, user, userToken, listener)
    }

}