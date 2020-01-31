package io.getstream.chat.android.core.poc.library.socket

import android.os.Message
import com.facebook.stetho.okhttp3.StethoInterceptor
import io.getstream.chat.android.core.poc.app.utils.StethoWebSocketsFactory
import io.getstream.chat.android.core.poc.library.EventType
import io.getstream.chat.android.core.poc.library.User
import io.getstream.chat.android.core.poc.library.errors.ChatError
import io.getstream.chat.android.core.poc.library.events.ChatEvent
import io.getstream.chat.android.core.poc.library.events.ConnectedEvent
import io.getstream.chat.android.core.poc.library.events.LocalEvent
import io.getstream.chat.android.core.poc.library.gson.JsonParser
import okhttp3.OkHttpClient
import okhttp3.Request
import okhttp3.WebSocket
import java.io.UnsupportedEncodingException
import java.net.URLEncoder
import java.nio.charset.StandardCharsets
import java.util.*
import kotlin.math.floor
import kotlin.math.max
import kotlin.math.min


class StreamWebSocketService(val jsonParser: JsonParser) : WebSocketService {

    private var wsEndpoint: String = ""
    private var apiKey: String = ""
    private var userToken: String? = ""
    private var user: User? = null
    private val listener: EventsParser = EventsParser(this, jsonParser)
    private var httpClient = OkHttpClient()
    private var webSocket: WebSocket? = null
    private val webSocketListeners = mutableListOf<SocketListener>()
    private var state: State = State.Disconnected
    private var lastEvent: Date? = null
    private val healthCheckInterval = 30 * 1000L
    private var consecutiveFailures = 0
    private var wsId = 0

    private val eventHandler = EventHandler(this)

    fun setLastEventDate(date: Date) {
        this.lastEvent = date
    }

    fun onSocketError(error: ChatError) {

        if (state is State.Connected || state is State.Connecting) {
            updateState(State.Error(error))
            updateState(State.Disconnected)
            clearWebSocket()
            reconnect()
        }
    }

    fun onRemoteEvent(event: ChatEvent) {
        webSocketListeners.forEach { it.onRemoteEvent(event) }
    }

    fun removeSocketListener(listener: SocketListener) {
        webSocketListeners.remove(listener)
    }

    fun addSocketListener(listener: SocketListener) {
        webSocketListeners.add(listener)
    }

    private val mOfflineNotifier = Runnable {
        onEvent(LocalEvent(EventType.CONNECTION_CHANGED))
    }

    private val reconnect = Runnable {
        setupWS()
    }

    private val healthCheck: Runnable = Runnable {
        if (state is State.Connected) {
            val event = LocalEvent(EventType.HEALTH_CHECK)
            webSocket?.send(jsonParser.toJson(event))
        }
    }

    private val monitor = Runnable {
        if (state is State.Connected) {
            val millisNow = Date().time
            val monitorInterval = 1000L

            lastEvent?.let {
                val diff = millisNow - it.time
                val checkInterval = healthCheckInterval + 10 * 1000
                if (diff > checkInterval) {
                    consecutiveFailures += 1
                    reconnect()
                }
            }

            eventHandler.postDelayed(healthCheck, monitorInterval)
        }
    }

    override fun connect(
        wsEndpoint: String,
        apiKey: String,
        user: User?,
        userToken: String?,
        listener: SocketListener
    ) {
        if (state != State.Disconnected) {
            return
        }

        addSocketListener(listener)

        this.wsEndpoint = wsEndpoint
        this.apiKey = apiKey
        this.user = user
        this.userToken = userToken
        wsId = 0
        consecutiveFailures = 0

        setupWS()
    }

    override fun disconnect() {

        updateState(State.Disconnected)
        //webSocketListeners.clear()
        clearWebSocket()
        eventHandler.removeCallbacksAndMessages(null)
    }

    private fun clearWebSocket() {
        consecutiveFailures = 0
        consecutiveFailures++
        lastEvent = null
        webSocket?.cancel()
        webSocket?.close(1000, "bye")
        webSocket = null
    }

    private fun reconnect() {
        eventHandler.postDelayed(
            reconnect,
            getRetryInterval()
        )
    }

    private fun startMonitor() {
        healthCheck.run()
        monitor.run()
    }

    fun onConnectionResolved(event: ConnectedEvent) {
        updateState(State.Connected(event))
        startMonitor()
    }

    private fun getRetryInterval(): Long {
        val max = min(500 + consecutiveFailures * 2000, 25000)
        val min = min(
            max(250, (consecutiveFailures - 1) * 2000), 25000
        )
        return floor(Math.random() * (max - min) + min).toLong()
    }

//    fun setHealth(healthy: Boolean) {
//        Log.i(TAG, "setHealth $healthy")
//        if (healthy && !isHealthy) {
//            isHealthy = true
//            onEvent(LocalEvent(EventType.CONNECTION_CHANGED))
//        } else if (!healthy && isHealthy) {
//            isHealthy = false
//            Log.i(TAG, "spawn mOfflineNotifier")
//            eventHandler.postDelayed(mOfflineNotifier, 5000)
//        }
//    }

    fun onEvent(event: ChatEvent) {
        val eventMsg = Message()
        eventMsg.obj = event
        eventHandler.sendMessage(eventMsg)
    }

    private fun setupWS() {

        updateState(State.Connecting)

        wsId++
        val url = getWsUrl()
        val request: Request = Request.Builder().url(url).build()

        httpClient = OkHttpClient.Builder()
            .addNetworkInterceptor(StethoInterceptor())
            .build()

        //webSocket = httpClient.newWebSocket(request, listener)
        webSocket = StethoWebSocketsFactory(httpClient).newWebSocket(request, listener)

        //httpClient.dispatcher.executorService.shutdown()
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
        return jsonParser.toJson(data)
    }

    private fun updateState(state: State) {

        this.state = state

        when (state) {
            is State.Error -> {
                eventHandler.post {
                    webSocketListeners.forEach { it.onError(state.error) }
                }
            }
            is State.Connecting -> {
                eventHandler.post {
                    webSocketListeners.forEach { it.onConnecting() }
                }
            }
            is State.Connected -> {
                eventHandler.post {
                    webSocketListeners.forEach { it.onConnected(state.event) }
                }
            }
            is State.Disconnected -> {
                eventHandler.post {
                    webSocketListeners.forEach { it.onDisconnected() }
                }
            }
        }
    }

    sealed class State {
        object Disconnected : State()
        object Connecting : State()
        class Connected(val event: ConnectedEvent) : State()
        class Error(val error: ChatError) : State()
    }

}