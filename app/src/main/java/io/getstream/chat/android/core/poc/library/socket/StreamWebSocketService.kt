package io.getstream.chat.android.core.poc.library.socket

import android.os.Message
import android.util.Log
import io.getstream.chat.android.core.poc.library.Event
import io.getstream.chat.android.core.poc.library.EventType
import io.getstream.chat.android.core.poc.library.User
import io.getstream.chat.android.core.poc.library.json.ChatGson
import okhttp3.OkHttpClient
import okhttp3.Request
import okhttp3.WebSocket
import okhttp3.WebSocketListener
import java.io.UnsupportedEncodingException
import java.net.URLEncoder
import java.nio.charset.StandardCharsets
import java.util.*
import kotlin.math.floor
import kotlin.math.max
import kotlin.math.min


class StreamWebSocketService : WebSocketListener(), WebSocketService {

    private val NORMAL_CLOSURE_STATUS = 1000
    private val TAG = StreamWebSocketService::class.java.simpleName

    private var wsEndpoint: String = ""
    private var apiKey: String = ""
    private var userToken: String? = ""
    private var user: User? = null

    private val listener: EchoWebSocketListener = EchoWebSocketListener(this)
    private var httpClient = OkHttpClient()
    private var webSocket: WebSocket? = null
    private val webSocketListeners = mutableListOf<WsListener>()

    lateinit var connectionCallback: (ConnectionData, Throwable?) -> Unit

    val eventHandler = EventHandler(this)

    fun connectionRecovered() {
        webSocketListeners.forEach { it.connectionRecovered() }
    }

    fun tokenExpired() {
        webSocketListeners.forEach { it.tokenExpired() }
    }

    fun onError(error: WsErrorMessage) {
        webSocketListeners.forEach { it.onError(error) }
    }

    fun onWsEvent(event: Event) {
        webSocketListeners.forEach { it.onWSEvent(event) }
    }

    fun removeSocketListener(listener: WsListener) {
        webSocketListeners.remove(listener)
    }

    fun addSocketListener(listener: WsListener) {
        webSocketListeners.add(listener)
    }

    private val mOfflineNotifier = Runnable {
        if (!isHealthy)
            sendEventToHandlerThread(Event(EventType.CONNECTION_CHANGED, false))
    }

    private val reconnect = object : Runnable {
        override fun run() {
            if (isConnecting || isHealthy || shuttingDown)
                return
            destroyCurrentWSConnection()
            setupWS()
        }
    }

    private val healthCheck: Runnable = object : Runnable {
        override fun run() {
            if (shuttingDown) {
                Log.i(TAG, "connection is shutting down, quit health check")
                return
            }
            Log.i(TAG, "send health check")
            try {
                val event = Event(EventType.HEALTH_CHECK)
                webSocket!!.send(ChatGson.instance.toJson(event))
            } finally {
                eventHandler.postDelayed(this, healthCheckInterval)
            }
        }
    }

    private val monitor = object : Runnable {
        override fun run() {
            if (shuttingDown) {
                Log.i(TAG, "connection is shutting down, quit monitor")
                return
            }
            Log.i(TAG, "check connection health")
            val millisNow = Date().time
            val monitorInterval = 1000L

            lastEvent?.let {
                val diff = millisNow - it.time
                val checkInterval = healthCheckInterval + 10 * 1000
                if (diff > checkInterval) {
                    consecutiveFailures += 1
                    setHealth(false)
                    reconnect(true)
                }
            }

            eventHandler.postDelayed(healthCheck, monitorInterval)
        }

    }

    /**
     * The connection is considered resolved after the WS connection returned a good message
     */
    var connectionResolved = false

    /**
     * We only make 1 attempt to reconnectWebSocket at the same time..
     */
    var isConnecting = false

    var connected = false

    /**
     * Boolean that indicates if we have a working connection to the server
     */
    var isHealthy = false

    /**
     * Store the last event time for health checks
     */
    var lastEvent: Date? = null

    /**
     * Send a health check message every 30 seconds
     */
    val healthCheckInterval = 30 * 1000L

    /**
     * consecutive failures influence the duration of the timeout
     */
    var consecutiveFailures = 0

    var anonymousAuth = false

    var shuttingDown = false
    var wsId = 0

    override fun connect(
        wsEndpoint: String,
        apiKey: String,
        user: User?,
        userToken: String?,
        listener: (ConnectionData, Throwable?) -> Unit
    ) {
        if (isConnecting) {
            Log.w(TAG, "already connecting")
            return
        }

        if (user == null) {
            anonymousAuth = true
            this.user = User(id = UUID.randomUUID().toString())
        } else {
            this.user = user
        }
        this.wsEndpoint = wsEndpoint
        this.apiKey = apiKey
        this.userToken = userToken
        this.connectionCallback = listener

        wsId = 0
        isConnecting = true

        resetConsecutiveFailures()

        setupWS()

        shuttingDown = false
    }

    override fun disconnect() {
        shuttingDown = true
        connectionResolved = false
        connected = false
        isHealthy = false
        lastEvent = null
        sendEventToHandlerThread(Event(EventType.CONNECTION_CHANGED, false))
        //webSocket?.cancel()
        webSocket?.close(1000, "bye")
        webSocket = null
        eventHandler.removeCallbacksAndMessages(null)
        destroyCurrentWSConnection()
    }

    fun reconnect(delay: Boolean) {
        if (isConnecting || isHealthy || shuttingDown) {
            return
        }
        Log.i(TAG, "schedule reconnection in " + getRetryInterval().toString() + "ms")
        eventHandler.postDelayed(
            reconnect,
            if (delay) getRetryInterval() else 0L
        )
    }

    private fun startMonitor() {
        healthCheck.run()
        monitor.run()
    }

    fun setConnectionResolved(connectionId: String, user: User) {
        connectionResolved = true
        connected = true
        val connectionData = ConnectionData(connectionId, user)
        connectionCallback(connectionData, null)
        startMonitor()
    }

    private fun getRetryInterval(): Long {
        val max = min(500 + consecutiveFailures * 2000, 25000)
        val min = min(
            max(250, (consecutiveFailures - 1) * 2000), 25000
        )
        return floor(Math.random() * (max - min) + min).toLong()
    }

    fun resetConsecutiveFailures() {
        consecutiveFailures = 0
    }

    fun setHealth(healthy: Boolean) {
        Log.i(TAG, "setHealth $healthy")
        if (healthy && !isHealthy) {
            isHealthy = true
            sendEventToHandlerThread(Event(EventType.CONNECTION_CHANGED, true))
        } else if (!healthy && isHealthy) {
            isHealthy = false
            Log.i(TAG, "spawn mOfflineNotifier")
            eventHandler.postDelayed(mOfflineNotifier, 5000)
        }
    }

    fun sendEventToHandlerThread(event: Event) {
        val eventMsg = Message()
        eventMsg.obj = event
        eventHandler.sendMessage(eventMsg)
    }

    private fun setupWS() {
        wsId++
        val wsUrl = getWsUrl()
        val request: Request = Request.Builder().url(wsUrl).build()
        httpClient = OkHttpClient()
        webSocket = httpClient.newWebSocket(request, listener)
        httpClient.dispatcher.executorService.shutdown()
    }

    private fun destroyCurrentWSConnection() {
        try {
            httpClient.dispatcher.cancelAll()
//            httpClient.connectionPool.evictAll()
//            httpClient.dispatcher.executorService.shutdown()
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }

    private fun getWsUrl(): String {
        var json = buildUserDetailJson(user)
        return try {
            json = URLEncoder.encode(json, StandardCharsets.UTF_8.name())
            val baseWsUrl: String = wsEndpoint + "connect?json=" + json + "&api_key=" + apiKey

            if (anonymousAuth) {
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
        return ChatGson.instance.toJson(data)
    }

}