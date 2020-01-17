package io.getstream.chat.android.core.poc.library.socket

import io.getstream.chat.android.core.poc.library.CachedTokenProvider
import io.getstream.chat.android.core.poc.library.Event
import io.getstream.chat.android.core.poc.library.TokenProvider
import io.getstream.chat.android.core.poc.library.User
import io.getstream.chat.android.core.poc.library.call.ChatCall

class ChatSocketConnectionImpl(
    val apiKey: String,
    val wssUrl: String
) {

    private val service = StreamWebSocketService()

    fun connect(): ChatCall<ConnectionData> {
        val result = ConnectionCall()

        val callback: (ConnectionData, Throwable?) -> Unit = { c, t ->
            result.deliverResult(c, t)
        }

        connect(null, null, callback)

        return result
    }

    fun connect(user: User, tokenProvider: CachedTokenProvider): ChatCall<ConnectionData> {

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

    fun events(): ChatObservable {

        val result = ChatObservable()

        service.addSocketListener(object : WsListener {
            override fun onWSEvent(event: Event) {
                result.onNext(event)
            }

            override fun connectionResolved(event: Event) {

            }

            override fun connectionRecovered() {

            }

            override fun tokenExpired() {

            }

            override fun onError(error: WsErrorMessage) {

            }
        })

        return result
    }

    fun disconnect() {
        service.disconnect()
    }

    private fun connect(
        user: User?,
        userToken: String?,
        listener: (ConnectionData, Throwable?) -> Unit
    ) {
        service.connect(wssUrl, apiKey, user, userToken, listener)
    }

    class ChatObservable {

        private val subscribtions = mutableListOf<Subscription>()

        fun onNext(event: Event) {
            subscribtions.forEach { it.onNext(event) }
        }

        fun subscribe(listener: (Event) -> Unit): Subscription {
            val result = Subscription(this, listener)
            subscribtions.add(result)
            return result
        }

        class Subscription(val observable: ChatObservable, var listener: ((Event) -> Unit)?) {

            fun unsubscribe() {
                listener = null
                observable.subscribtions.remove(this)
            }

            fun onNext(event: Event) {
                listener?.invoke(event)
            }
        }
    }

}