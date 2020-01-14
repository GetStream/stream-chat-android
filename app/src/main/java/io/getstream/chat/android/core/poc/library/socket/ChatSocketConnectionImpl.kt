package io.getstream.chat.android.core.poc.library.socket

import io.getstream.chat.android.core.poc.library.CachedTokenProvider
import io.getstream.chat.android.core.poc.library.TokenProvider
import io.getstream.chat.android.core.poc.library.User
import io.getstream.chat.android.core.poc.library.api.ApiClientOptions

class ChatSocketConnectionImpl(
    val apiKey: String,
    val wssUrl: String,
    val tokenProvider: CachedTokenProvider
) {

    private val responseHandler = WSResponseHandlerImpl()

    fun connect(user: User? = null) {

        if (user == null) {
            connect(null, null)
        } else {

            tokenProvider.getToken(object : TokenProvider.TokenProviderListener {
                override fun onSuccess(token: String) {
                    connect(user, token)
                }
            })
        }
    }

    private fun connect(user: User?, userToken: String?) {
        val webSocketService = StreamWebSocketService(
            wssUrl,
            apiKey,
            user,
            userToken,
            responseHandler
        )

        webSocketService.connect()
    }
}