package io.getstream.chat.android.core.poc.library

import android.text.TextUtils
import com.sun.security.ntlm.Client
import io.getstream.chat.android.core.poc.library.TokenProvider.TokenProviderListener
import io.getstream.chat.android.core.poc.library.api.ApiClientOptions
import io.getstream.chat.android.core.poc.library.api.RetrofitClient.getClient
import io.getstream.chat.android.core.poc.library.socket.StreamWebSocketService
import io.getstream.chat.android.core.poc.library.socket.StreamWebSocketServiceProvider
import io.getstream.chat.android.core.poc.library.socket.WSResponseHandler
import io.getstream.chat.android.core.poc.library.socket.WsErrorMessage
import java.io.UnsupportedEncodingException


class StreamChatClient(val apiKey: String) : WSResponseHandler {

    private val api = Api(RetrofitApiBuilder().build())
    private var anonymousConnection = false
    private val state = ClientState()
    var tokenProvider: CachedTokenProvider? = null
    var cacheUserToken: String = ""
    var fetchingToken = false
    var webSocketService: StreamWebSocketService? = null
    lateinit var apiClientOptions: ApiClientOptions
    private val connectSubRegistry: EventSubscriberRegistry<ClientConnectionCallback> =
        EventSubscriberRegistry()
    lateinit var uploadStorage: StreamPublicStorage
    private var isAnonymous = false
    private var clientID = ""
    private var connected = false
    private val activeChannelMap: Map<String, Channel> = HashMap()
    private var user: User? = null

    init {
        api.apiKey = apiKey
    }

    fun getClientID(): String {
        return clientID
    }

    fun setUser(user: User, provider: TokenProvider) {
        if (getUser() != null) {
            return
        }

        this.user = user
        api.userId = user.id


        state.currentUser = user
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

        connect(anonymousConnection)
    }

    private fun connect(anonymousConnection: Boolean) {

        val webSocketServiceProvider = StreamWebSocketServiceProvider(apiClientOptions, apiKey)

        if (anonymousConnection) {
            try {
                webSocketService = webSocketServiceProvider.provideWebSocketService(
                    getUser()!!,
                    null,
                    this,
                    anonymousConnection
                )
            } catch (e: UnsupportedEncodingException) {
                onError(e.message.toString(), ClientErrorCode.JSON_ENCODING)
            }

        } else {

            val respH: WSResponseHandler = this

            tokenProvider.getToken(object : TokenProviderListener {
                override fun onSuccess(token: String) {
                    try {
                        webSocketService = webSocketServiceProvider.provideWebSocketService(
                            getUser(),
                            token,
                            respH,
                            anonymousConnection
                        )
                    } catch (e: UnsupportedEncodingException) {
                        onError(e.message.toString(), ClientErrorCode.JSON_ENCODING)
                    }
                }
            })
        }

        val retrofitApi = getClient(apiClientOptions, tokenProvider) { isAnonymous }!!.create(
            RetrofitApi::class.java
        )
        uploadStorage = StreamPublicStorage(this, retrofitApi)
        webSocketService!!.connect()
    }

    private fun onError(errMsg: String, errCode: Int) {
        val subs: List<ClientConnectionCallback> = connectSubRegistry.getSubscribers()
        connectSubRegistry.clear()
        for (waiter in subs) {
            waiter.onError(errMsg, errCode)
        }
    }

    fun getState(): ClientState {
        return ClientState()
    }

    fun fromCurrentUser(entity: UserEntity): Boolean {
        val otherUserId = entity.getUserId() ?: return false
        return if (getUser() == null) false else TextUtils.equals(getUserId(), otherUserId)
    }

    fun getUserId(): String {
        return ""
    }

    fun getUser(): User? {
        return user
    }

    fun disconnect() {
        if (state.currentUser == null) {
            //log
        } else {
            //log
        }

        disconnectWebSocket()

        // unset token facilities
        tokenProvider = null
        fetchingToken = false
        cacheUserToken = ""

        builtinHandler.dispatchUserDisconnected()
        for (handler in subRegistry.getSubscribers()) {
            handler.dispatchUserDisconnected()
        }

        // clear local state
        state.reset()
        activeChannelMap.clear()
    }

    fun queryChannels(
        request: QueryChannelsRequest
    ): Call<List<Channel>> {
        return api.queryChannels(request).map { response ->
            response.getChannels()
        }
    }

    fun getActiveChannels(): List<Channel> {
        val values: Collection<Channel> = activeChannelMap.values
        return ArrayList(values)
    }

    fun reconnectWebSocket() {
        if (getUser() == null) {
            return
        }

        if (webSocketService != null) {
            return
        }
        connectionRecovered()

        connect(anonymousConnection);
    }

    fun onSetUserCompleted(callback: ClientConnectionCallback) {
        if (connected) {
            callback.onSuccess(getUser())
        } else {
            connectSubRegistry.addSubscription(callback)
        }
    }

    override fun onWSEvent(event: Event) {
        TODO("not implemented") //To change body of created functions use File | Settings | File Templates.
    }

    override fun connectionResolved(event: Event) {
        clientID = event.connectionId
        if (event.me != null && !event.isAnonymous) state.currentUser = event.me

        // mark as connect, any new callbacks will automatically be executed
        connected = true

        // call onSuccess for everyone that was waiting
        val subs = connectSubRegistry.getSubscribers()
        connectSubRegistry.clear()
        for (waiter in subs) {
            waiter.onSuccess(getUser())
        }
    }

    override fun connectionRecovered() {

    }

    override fun tokenExpired() {

    }

    override fun onError(error: WsErrorMessage?) {

    }

    fun disconnectWebSocket() {
        if (webSocketService != null) {
            webSocketService!!.disconnect()
            webSocketService = null
            clientID = ""
        }
        onWSEvent(Event(false))
        connected = false
    }
}