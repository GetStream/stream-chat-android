package io.getstream.chat.android.client.socket

import android.os.Message
import com.facebook.stetho.okhttp3.StethoInterceptor
import io.getstream.chat.android.client.errors.ChatError
import io.getstream.chat.android.client.events.ChatEvent
import io.getstream.chat.android.client.events.ConnectedEvent
import io.getstream.chat.android.client.logger.ChatLogger
import io.getstream.chat.android.client.models.User
import io.getstream.chat.android.client.parser.ChatParser
import okhttp3.OkHttpClient
import okhttp3.Request
import okhttp3.WebSocket
import java.io.UnsupportedEncodingException
import java.net.URLEncoder
import java.nio.charset.StandardCharsets
import java.util.*


class ChatSocketServiceImpl(val chatParser: ChatParser) : ChatSocketService {

    private val logger = ChatLogger.get("SocketService")

    private var wsEndpoint: String = ""
    private var apiKey: String = ""
    private var userToken: String? = ""
    private var user: User? = null
    private val eventsParser = EventsParser(this, chatParser)
    private var httpClient = OkHttpClient()
    private var socket: WebSocket? = null
    private var initConnectionListener: InitConnectionListener? = null
    private val listeners = mutableListOf<SocketListener>()

    private var wsId = 0
    private val eventHandler = EventHandler(this)
    private val healthMonitor = HealthMonitor(this)

    internal var state: State = State.Disconnected

    fun setLastEventDate(date: Date) {
        healthMonitor.lastEventDate = date
    }

    fun onSocketError(error: ChatError) {

        if (state is State.Connected || state is State.Connecting) {
            updateState(State.Error(error))
            updateState(State.Disconnected)
            clearState()
            healthMonitor.onError()
        }
    }

    fun onRemoteEvent(event: ChatEvent) {
        listeners.forEach { it.onEvent(event) }
    }

    override fun removeListener(listener: SocketListener) {
        listeners.remove(listener)
    }

    override fun addListener(listener: SocketListener) {
        listeners.add(listener)
    }

    override fun connect(
        wsEndpoint: String,
        apiKey: String,
        user: User?,
        userToken: String?,
        listener: InitConnectionListener?
    ) {
        logger.logI("connect")

        if (state is State.Connecting || state is State.Connected) {
            disconnect()
        }

        this.wsEndpoint = wsEndpoint
        this.apiKey = apiKey
        this.user = user
        this.userToken = userToken
        this.initConnectionListener = listener
        wsId = 0
        healthMonitor.reset()

        setupWs()
    }

    override fun disconnect() {
        updateState(State.Disconnected)
        clearState()
    }

    fun onConnectionResolved(event: ConnectedEvent) {
        updateState(State.Connected(event))
        startMonitor()
    }

    fun onEvent(event: ChatEvent) {
        val eventMsg = Message()
        eventMsg.obj = event
        eventHandler.sendMessage(eventMsg)
    }

    internal fun sendEvent(event: ChatEvent) {
        socket?.send(chatParser.toJson(event))
    }

    internal fun setupWs() {

        logger.logI("setupWs")

        updateState(State.Connecting)

        wsId++
        val url = getWsUrl()
        val request = Request.Builder().url(url).build()

        httpClient = OkHttpClient.Builder()
            .addNetworkInterceptor(StethoInterceptor())
            .build()

        socket = httpClient.newWebSocket(request, eventsParser)
    }

    private fun clearState() {
        initConnectionListener = null
        healthMonitor.reset()
        socket?.cancel()
        socket?.close(1000, "bye")
        socket = null
    }

    private fun startMonitor() {
        healthMonitor.start()
    }

    private fun updateState(state: State) {

        logger.logI("updateState: {${state.javaClass.simpleName}}")

        this.state = state

        when (state) {
            is State.Error -> {

                initConnectionListener?.onError(state.error)
                initConnectionListener = null

                eventHandler.post {
                    listeners.forEach { it.onError(state.error) }
                }
            }
            is State.Connecting -> {
                eventHandler.post {
                    listeners.forEach { it.onConnecting() }
                }
            }
            is State.Connected -> {

                initConnectionListener?.onSuccess(
                    InitConnectionListener.ConnectionData(
                        state.event.me,
                        state.event.connectionId
                    )
                )
                initConnectionListener = null

                eventHandler.post {
                    listeners.forEach { it.onConnected(state.event) }
                }
            }
            is State.Disconnected -> {
                eventHandler.post {
                    listeners.forEach { it.onDisconnected() }
                }
            }
        }
    }

    private fun getWsUrl(): String {
        var json = buildUserDetailJson(user)
        return try {
            json = URLEncoder.encode(json, StandardCharsets.UTF_8.name())
            val baseWsUrl: String =
                wsEndpoint + "connect?json=" + json + "&api_key=" + apiKey
            if (user == null) {
                "$baseWsUrl&stream-auth-type=anonymous"
            } else {
                "$baseWsUrl&authorization=$userToken&stream-auth-type=jwt"
            }
        } catch (throwable: Throwable) {
            throw UnsupportedEncodingException("Unable to encode user details json: $json")
        }
    }

    private fun buildUserDetailJson(user: User?): String {
        val data = mutableMapOf<String, Any>()
        user?.let {
            data["user_details"] = user
            data["user_id"] = user.id
        }
        data["server_determines_connection_id"] = true
        return chatParser.toJson(data)
    }

    sealed class State {
        object Disconnected : State()
        object Connecting : State()
        class Connected(val event: ConnectedEvent) : State()
        class Error(val error: ChatError) : State()
    }

}