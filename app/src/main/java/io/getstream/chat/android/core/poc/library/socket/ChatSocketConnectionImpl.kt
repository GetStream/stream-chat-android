package io.getstream.chat.android.core.poc.library.socket

import io.getstream.chat.android.core.poc.library.CachedTokenProvider
import io.getstream.chat.android.core.poc.library.TokenProvider
import io.getstream.chat.android.core.poc.library.User

class ChatSocketConnectionImpl(
    val apiKey: String,
    val wssUrl: String,
    val tokenProvider: CachedTokenProvider
) {

    private val responseHandler = WSResponseHandlerImpl()

    fun connect(user: User? = null, callback: (User, Throwable?) -> Unit) {

        if (user == null) {
            connect(null, null, callback)
        } else {
            tokenProvider.getToken(object : TokenProvider.TokenProviderListener {
                override fun onSuccess(token: String) {
                    connect(user, token, callback)

                }
            })
        }
    }

    fun disconnect() {

    }

    private fun connect(user: User?, userToken: String?, listener: (User, Throwable?) -> Unit) {
        val webSocketService = StreamWebSocketService(
            wssUrl,
            apiKey,
            user,
            userToken,
            responseHandler
        )

        webSocketService.connect(listener)
    }
}