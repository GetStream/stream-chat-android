package io.getstream.chat.android.core.poc.library

import android.text.TextUtils
import io.getstream.chat.android.core.poc.library.TokenProvider.TokenProviderListener
import io.getstream.chat.android.core.poc.library.api.ApiClientOptions
import io.getstream.chat.android.core.poc.library.api.RetrofitClient
import io.getstream.chat.android.core.poc.library.socket.ChatSocketConnectionImpl
import io.getstream.chat.android.core.poc.library.socket.ConnectionData
import io.getstream.chat.android.core.poc.library.socket.StreamWebSocketService


class StreamChatClient(
    val apiKey: String,
    val apiOptions: ApiClientOptions
) {

    private lateinit var api: ChatApiImpl
    private var anonymousConnection = false
    private val state = ClientState()
    private var tokenProvider: CachedTokenProvider? = null
    private var cacheUserToken: String = ""
    private var fetchingToken = false
    private var webSocketService: StreamWebSocketService? = null
    private var isAnonymous = false
    private var clientID = ""
    private var connected = false

    fun getClientID(): String {
        return clientID
    }

    fun setAnonymousUser() {

    }

    fun setUser(
        user: User,
        provider: TokenProvider,
        callback: (ConnectionData, Throwable?) -> Unit
    ) {

        state.user = user
        //api.userId = user.id

        state.user = user
        val listeners = mutableListOf<TokenProviderListener>()

        this.tokenProvider = object : CachedTokenProvider {
            override fun getToken(listener: TokenProviderListener) {
                if (cacheUserToken.isNotEmpty()) {
                    listener.onSuccess(cacheUserToken)
                    return
                }

                if (fetchingToken) {
                    listeners.add(listener)
                    return
                } else {
                    // token is not in cache and there are no in-flight requests, go fetch it
                    fetchingToken = true
                }

                provider.getToken(object : TokenProviderListener {
                    override fun onSuccess(token: String) {
                        fetchingToken = false
                        listener.onSuccess(token)
                        for (l in listeners)
                            l.onSuccess(token)
                        listeners.clear()
                    }
                })
            }

            override fun tokenExpired() {
                cacheUserToken = ""
            }

        }

        val socket = ChatSocketConnectionImpl(apiKey, apiOptions.wssURL, this.tokenProvider!!)

        api = ChatApiImpl(
            apiKey,
            RetrofitClient.getClient(
                apiOptions,
                tokenProvider!!
            ) { isAnonymous }!!.create(
                RetrofitApi::class.java
            )
        )

        socket.connect(user) { connection, error ->
            api.clientId = connection.connectionId
            api.userId = connection.user.id
            callback(connection, error)
        }


        //connect(anonymousConnection)
    }

    private fun connect(anonymousConnection: Boolean) {

    }

    fun getState(): ClientState {
        return state
    }

    fun fromCurrentUser(entity: UserEntity): Boolean {
        val otherUserId = entity.getUserId() ?: return false
        return if (getUser() == null) false else TextUtils.equals(getUserId(), otherUserId)
    }

    fun getUserId(): String {
        return ""
    }

    fun getUser(): User? {
        return state.user
    }

    fun disconnect() {
        if (state.user == null) {
            //log
        } else {
            //log
        }

        disconnectWebSocket()

        // unset token facilities
        tokenProvider = null
        fetchingToken = false
        cacheUserToken = ""

        //builtinHandler.dispatchUserDisconnected()
        //for (handler in subRegistry.getSubscribers()) {
        //    handler.dispatchUserDisconnected()
        //}

        // clear local state
        state.reset()
        //activeChannelMap.clear()
    }

    fun queryChannels(
        request: QueryChannelsRequest
    ): Call<List<Channel>> {
        return api.queryChannels(request).map { response ->
            response.getChannels()
        }
    }

    fun reconnectWebSocket() {
        if (getUser() == null) {
            return
        }

        if (webSocketService != null) {
            return
        }
        //connectionRecovered()

        connect(anonymousConnection);
    }

    fun disconnectWebSocket() {
        if (webSocketService != null) {
            webSocketService!!.disconnect()
            webSocketService = null
            clientID = ""
        }
        //onWSEvent(Event(false))
        connected = false
    }
}