package io.getstream.chat.android.client.socket

import android.os.Message
import io.getstream.chat.android.client.errors.ChatError
import io.getstream.chat.android.client.events.ChatEvent
import io.getstream.chat.android.client.events.ConnectedEvent
import io.getstream.chat.android.client.logger.ChatLogger
import io.getstream.chat.android.client.models.User
import io.getstream.chat.android.client.parser.ChatParser
import io.getstream.chat.android.client.socket.ChatSocketService.State
import okhttp3.OkHttpClient
import okhttp3.Request
import okhttp3.WebSocket
import java.io.UnsupportedEncodingException
import java.net.URLEncoder
import java.nio.charset.StandardCharsets
import java.util.*


class ChatSocketServiceImpl(val chatParser: ChatParser) : ChatSocketService {

    private val logger = ChatLogger.get("SocketService")

    private var endpoint: String = ""
    private var apiKey: String = ""
    private var userToken: String? = ""
    private var user: User? = null
    private val eventsParser = EventsParser(this, chatParser)
    private var httpClient = OkHttpClient()
    private var socket: WebSocket? = null
    private val listeners = mutableListOf<SocketListener>()

    private var connectionId = 0
    private val eventHandler = EventHandler(this)
    private val healthMonitor = HealthMonitor(this)

    override var state: State = State.Disconnected

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
        endpoint: String,
        apiKey: String,
        user: User?,
        userToken: String?
    ) {
        logger.logI("connect")

        if (state is State.Connecting || state is State.Connected) {
            disconnect()
        }

        this.endpoint = endpoint
        this.apiKey = apiKey
        this.user = user
        this.userToken = userToken
        this.connectionId = 0
        this.healthMonitor.reset()

        setupSocket()
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

    internal fun setupSocket() {

        logger.logI("setupSocket")

        updateState(State.Connecting)

        connectionId++
        val url = buildUrl()
        val request = Request.Builder().url(url).build()

        logger.logI("httpClient.newWebSocket: $url")

        socket = httpClient.newWebSocket(request, eventsParser)
    }

    private fun clearState() {
        healthMonitor.reset()
        socket?.cancel()
        socket?.close(1000, "bye")
        socket = null
    }

    private fun startMonitor() {
        healthMonitor.start()
    }

    private fun updateState(state: State) {

        logger.logI("updateState: ${state.javaClass.simpleName}")

        this.state = state

        when (state) {
            is State.Error -> {

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

    private fun buildUrl(): String {
        var json = buildUserDetailJson(user)
        return try {
            json = URLEncoder.encode(json, StandardCharsets.UTF_8.name())
            val baseWsUrl: String =
                endpoint + "connect?json=" + json + "&api_key=" + apiKey
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

}