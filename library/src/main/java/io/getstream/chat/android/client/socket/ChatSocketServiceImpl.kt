package io.getstream.chat.android.client.socket

import android.os.Message
import io.getstream.chat.android.client.ChatClient
import io.getstream.chat.android.client.errors.ChatError
import io.getstream.chat.android.client.errors.ChatErrorCode
import io.getstream.chat.android.client.errors.ChatNetworkError
import io.getstream.chat.android.client.events.ChatEvent
import io.getstream.chat.android.client.events.ConnectedEvent
import io.getstream.chat.android.client.logger.ChatLogger
import io.getstream.chat.android.client.models.User
import io.getstream.chat.android.client.parser.ChatParser
import io.getstream.chat.android.client.socket.ChatSocketService.State
import io.getstream.chat.android.client.token.TokenManager
import okhttp3.OkHttpClient
import okhttp3.Request
import okhttp3.WebSocket
import java.io.UnsupportedEncodingException
import java.net.URLEncoder
import java.nio.charset.StandardCharsets
import java.util.*


internal class ChatSocketServiceImpl(
    val chatParser: ChatParser,
    val tokenManager: TokenManager
) : ChatSocketService {

    private val logger = ChatLogger.get("SocketService")

    private var endpoint: String = ""
    private var apiKey: String = ""
    private var user: User? = null
    private val eventsParser = EventsParser(this, chatParser)
    private var httpClient = OkHttpClient()
    private var socket: WebSocket? = null
    private val listeners = mutableListOf<SocketListener>()

    private var connectionId = 0
    private val eventHandler = EventHandler(this)
    private val healthMonitor = HealthMonitor(this)

    override var state: State = State.Disconnected(false)

    override fun setLastEventDate(date: Date) {
        healthMonitor.lastEventDate = date
    }

    override fun onSocketError(error: ChatError) {

        if (error is ChatNetworkError && error.streamCode == ChatErrorCode.TOKEN_EXPIRED.code) {
            updateState(State.Error(error))
            tokenManager.expireToken()
            tokenManager.loadSync()

            //check if it's still in the current state
            if (state is State.Error) {
                updateState(State.Disconnected(true))
                clearState()
                healthMonitor.onError()
            }

        } else if (error is ChatNetworkError && error.streamCode == ChatErrorCode.API_KEY_NOT_FOUND.code) {
            updateState(State.Error(error))
            updateState(State.Disconnected(false))
            clearState()
        } else {
            if (state is State.Connected || state is State.Connecting) {
                updateState(State.Error(error))
                updateState(State.Disconnected(true))
                clearState()
                healthMonitor.onError()
            }
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
        user: User?
    ) {
        logger.logI("connect")

        if (state is State.Connecting || state is State.Connected) {
            updateState(State.Disconnected(true))
            clearState()
        }

        this.endpoint = endpoint
        this.apiKey = apiKey
        this.user = user
        this.connectionId = 0
        this.healthMonitor.reset()

        setupSocket()
    }

    override fun disconnect() {
        updateState(State.Disconnected(false))
        clearState()
    }

    override fun onConnectionResolved(event: ConnectedEvent) {
        updateState(State.Connected(event))
        startMonitor()
    }

    override fun onEvent(event: ChatEvent) {
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
                val token = tokenManager.getToken()
                "$baseWsUrl&authorization=$token&stream-auth-type=jwt"
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
        data["X-STREAM-CLIENT"] = ChatClient.instance().getVersion()
        return chatParser.toJson(data)
    }

}