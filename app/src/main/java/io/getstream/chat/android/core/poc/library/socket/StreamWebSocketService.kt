package io.getstream.chat.android.core.poc.library.socket

import android.os.Message
import android.util.Log
import com.google.gson.Gson
import io.getstream.chat.android.core.poc.library.Event
import io.getstream.chat.android.core.poc.library.EventType
import io.getstream.chat.android.core.poc.library.User
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


class StreamWebSocketService(
    val wsEndpoint: String,
    val apiKey: String,
    val user: User?,
    val userToken: String?,
    val webSocketListener: WSResponseHandler
) :
    WebSocketListener(), WebSocketService {

    private val NORMAL_CLOSURE_STATUS = 1000
    private val TAG = StreamWebSocketService::class.java.simpleName
    private var listener: EchoWebSocketListener? = null
    private var httpClient: OkHttpClient? = null
    private var webSocket: WebSocket? = null
    var eventThread: EventHandlerThread? = null
    lateinit var connectionCallback: (User, Throwable?) -> Unit

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
                webSocket!!.send(Gson().toJson(event))
            } finally {
                eventThread!!.handler.postDelayed(this, healthCheckInterval.toLong())
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
            val monitorInterval = 1000
            if (lastEvent != null) {
                if (millisNow - lastEvent!!.time > healthCheckInterval + 10 * 1000) {
                    consecutiveFailures += 1
                    setHealth(false)
                    reconnect(true)
                }
            }
            eventThread!!.handler.postDelayed(healthCheck, monitorInterval.toLong())
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
    val healthCheckInterval = 30 * 1000

    /**
     * consecutive failures influence the duration of the timeout
     */
    var consecutiveFailures = 0

    var shuttingDown = false
    var wsId = 0

    override fun connect(listener: (User, Throwable?) -> Unit) {
        if (isConnecting) {
            Log.w(TAG, "already connecting")
            return
        }

        this.connectionCallback = listener

        wsId = 0;
        isConnecting = true
        resetConsecutiveFailures()

        eventThread = EventHandlerThread(this).apply {
            name = "WSS - event handler thread"
            start()
        }

        setupWS()

        shuttingDown = false
    }

    override fun disconnect() {
        webSocket!!.close(1000, "bye")
        shuttingDown = true
        eventThread!!.handler.removeCallbacksAndMessages(null)
        destroyCurrentWSConnection()
    }

    override fun webSocketListener(): WSResponseHandler {
        return webSocketListener
    }

    fun reconnect(delay: Boolean) {
        if (isConnecting || isHealthy || shuttingDown) {
            return
        }
        Log.i(TAG, "schedule reconnection in " + getRetryInterval().toString() + "ms")
        eventThread!!.handler.postDelayed(
            reconnect,
            if (delay) getRetryInterval() else 0.toLong()
        )
    }

    private fun startMonitor() {
        healthCheck.run()
        monitor.run()
    }

    fun setConnectionResolved(user:User) {
        connectionCallback(user, null)
        connectionResolved = true
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
            eventThread!!.handler.postDelayed(mOfflineNotifier, 5000)
        }
    }

    fun sendEventToHandlerThread(event: Event) {
        val eventMsg = Message()
        eventMsg.obj = event
        eventThread!!.handler.sendMessage(eventMsg)
    }

    private fun setupWS() {
        wsId++
        httpClient = OkHttpClient()
        val request: Request = Request.Builder().url(getWsUrl()).build()
        listener = EchoWebSocketListener(this)
        webSocket = httpClient!!.newWebSocket(request, listener)
        httpClient!!.dispatcher().executorService().shutdown()
    }

    private fun destroyCurrentWSConnection() {
        try {
            httpClient!!.dispatcher().cancelAll()
        } catch (e: Exception) {
            e.printStackTrace()
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
        return Gson().toJson(data)
    }

}