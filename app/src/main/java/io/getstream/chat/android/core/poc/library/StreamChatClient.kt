package io.getstream.chat.android.core.poc.library

import android.text.TextUtils
import io.getstream.chat.android.core.poc.library.TokenProvider.TokenProviderListener
import io.getstream.chat.android.core.poc.library.api.ApiClientOptions
import io.getstream.chat.android.core.poc.library.api.RetrofitClient
import io.getstream.chat.android.core.poc.library.call.ChatCall
import io.getstream.chat.android.core.poc.library.rest.UpdateChannelRequest
import io.getstream.chat.android.core.poc.library.socket.ChatObservable
import io.getstream.chat.android.core.poc.library.socket.ChatSocketConnectionImpl
import io.getstream.chat.android.core.poc.library.socket.ConnectionData


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
    private var isAnonymous = false
    private var clientID = ""

    private val socket = ChatSocketConnectionImpl(apiKey, apiOptions.wssURL)

    fun getClientID(): String {
        return clientID
    }

    fun setAnonymousUser() {

    }

    fun setUser(
        user: User,
        provider: TokenProvider,
        callback: (Result<ConnectionData>) -> Unit
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

        api = ChatApiImpl(
            apiKey,
            RetrofitClient.getClient(
                apiOptions,
                tokenProvider!!
            ) { isAnonymous }!!.create(
                RetrofitApi::class.java
            )
        )

        socket.connect(user, this.tokenProvider!!).enqueue {

            if (it.isSuccess) {
                api.connectionId = it.data().connectionId
                api.userId = it.data().user.id
            }

            callback(it)
        }
    }

    fun events(): ChatObservable {
        return socket.events()
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

        socket.disconnect()

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

    fun stopWatching(channelType: String, channelId: String): ChatCall<Unit> {
        return api.stopWatching(channelType, channelId)
    }

    fun queryChannels(
        request: QueryChannelsRequest
    ): ChatCall<List<Channel>> {
        return api.queryChannels(request).map { response ->
            response.getChannels()
        }
    }

    fun updateChannel(
        channelType: String,
        channelId: String,
        updateMessage: Message,
        channelExtraData: Map<String, Any> = emptyMap()
    ): ChatCall<Channel> {
        val request = UpdateChannelRequest(channelExtraData, updateMessage)
        return api.updateChannel(channelType, channelId, request).map { response ->
            response.channel
        }
    }

    fun markAllRead(): ChatCall<Event> {
        return api.markAllRead().map {
            it.event
        }
    }

    fun reconnectWebSocket() {
        if (getUser() == null) {
            return
        }

//        if (webSocketService != null) {
//            return
//        }
        //connectionRecovered()

        connect(anonymousConnection)
    }
}