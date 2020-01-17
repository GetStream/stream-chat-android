package io.getstream.chat.android.core.poc.library.socket

import io.getstream.chat.android.core.poc.library.*
import io.getstream.chat.android.core.poc.library.call.ChatCallImpl
import io.getstream.chat.android.core.poc.library.errors.SocketChatError

class ChatSocketConnectionImpl(
    val apiKey: String,
    val wssUrl: String,
    val tokenProvider: CachedTokenProvider
) {

    private val responseHandler = WSResponseHandlerImpl()

    fun connect(user: User? = null): Call<ConnectionData> {


        val call = SSS()

        val callback: (ConnectionData, Throwable?) -> Unit = { c, t ->
            call.deliverResult(c, t)
        }

        if (user == null) {
            connect(null, null, callback)
        } else {
            tokenProvider.getToken(object : TokenProvider.TokenProviderListener {
                override fun onSuccess(token: String) {
                    connect(user, token, callback)

                }
            })
        }

        return call
    }

    fun disconnect() {

    }

    private fun connect(
        user: User?,
        userToken: String?,
        listener: (ConnectionData, Throwable?) -> Unit
    ) {
        val webSocketService = StreamWebSocketService(
            wssUrl,
            apiKey,
            user,
            userToken,
            responseHandler
        )

        webSocketService.connect(listener)
    }

    private class SSS : ChatCallImpl<ConnectionData>() {

        lateinit var callback: (Result<ConnectionData>) -> Unit

        fun deliverResult(connection: ConnectionData, error: Throwable?) {

            if (canceled) return

            if (error == null) {
                callback(Result(connection, null))
            } else {
                callback(Result(connection, SocketChatError("Connection error, see cause", error)))
            }
        }

        override fun execute(): Result<ConnectionData> {
            return Result(null, SocketChatError("Sync socket connection is not supportd"))
        }

        override fun enqueue(callback: (Result<ConnectionData>) -> Unit) {
            this.callback = callback
        }

    }
}