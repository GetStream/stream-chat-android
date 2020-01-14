package io.getstream.chat.android.core.poc.library.socket

import android.os.Message
import com.google.gson.Gson
import io.getstream.chat.android.core.poc.library.Event
import io.getstream.chat.android.core.poc.library.EventType
import okhttp3.OkHttpClient
import okhttp3.Request
import okhttp3.WebSocket
import okhttp3.WebSocketListener
import java.util.*


class StreamWebSocketService(val wsURL: String, val webSocketListener: WSResponseHandler) :
    WebSocketListener(), WebSocketService {

    private val NORMAL_CLOSURE_STATUS = 1000
    private val TAG = StreamWebSocketService::class.java.simpleName

    protected var listener: EchoWebSocketListener? = null
    var httpClient: OkHttpClient? = null
    var webSocket: WebSocket? = null
    var eventThread: EventHandlerThread? = null

    private val mOfflineNotifier = Runnable {
        if (!isHealthy) {
            val wentOffline = Event(false)
            sendEventToHandlerThread(wentOffline)
        }
    }

    private val mReconnect = object : Runnable {
        override fun run() {
            if (isConnecting || isHealthy || shuttingDown) {
                return
            }
            destroyCurrentWSConnection()
            setupWS()
        }
    }

    private val mHealthCheck: Runnable = object : Runnable {
        override fun run() {
            if (shuttingDown) {
                //Log.i(TAG, "connection is shutting down, quit health check")
                return
            }
            //Log.i(TAG, "send health check")
            try {
                val event = Event()
                event.setType(EventType.HEALTH_CHECK)
                webSocket!!.send(Gson().toJson(event))
            } finally {
                eventThread!!.mHandler.postDelayed(this, healthCheckInterval.toLong())
            }
        }
    }

    private val mMonitor = object : Runnable {
        override fun run() {
            if (shuttingDown) {
                //Log.i(TAG, "connection is shutting down, quit monitor")
                return
            }
            //Log.i(TAG, "check connection health")
            val millisNow = Date().time
            val monitorInterval = 1000
            if (lastEvent != null) {
                if (millisNow - lastEvent!!.time > healthCheckInterval + 10 * 1000) {
                    consecutiveFailures += 1
                    setHealth(false)
                    reconnect(true)
                }
            }
            eventThread!!.mHandler.postDelayed(mHealthCheck, monitorInterval.toLong())
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

    override fun connect() {
        if (isConnecting) {
            //Log.w(TAG, "already connecting");
            return
        }

        wsId = 0;
        isConnecting = true
        resetConsecutiveFailures()

        // start the thread before setting up the websocket connection
        eventThread = EventHandlerThread(this)
        eventThread!!.name = "WSS - event handler thread"
        eventThread!!.start()

        // WS connection
        setupWS()

        shuttingDown = false
    }

    override fun disconnect() {
        webSocket!!.close(1000, "bye")
        shuttingDown = true
        eventThread!!.mHandler.removeCallbacksAndMessages(null)
        destroyCurrentWSConnection()
    }

    override fun webSocketListener(): WSResponseHandler {
        return webSocketListener
    }

    fun reconnect(delay: Boolean) {
        if (isConnecting || isHealthy || shuttingDown) {
            return
        }
        //Log.i(TAG, "schedule reconnection in " + getRetryInterval().toString() + "ms")
        eventThread!!.mHandler.postDelayed(
            mReconnect,
            if (delay) getRetryInterval() else 0.toLong()
        )
    }

    private fun startMonitor() {
        mHealthCheck.run()
        mMonitor.run()
    }

    fun setConnectionResolved() {
        connectionResolved = true
        startMonitor()
    }

    private fun getRetryInterval(): Long {
        val max = Math.min(500 + consecutiveFailures * 2000, 25000)
        val min = Math.min(
            Math.max(250, (consecutiveFailures - 1) * 2000),
            25000
        )
        return Math.floor(Math.random() * (max - min) + min).toLong()
    }

    fun resetConsecutiveFailures() {
        consecutiveFailures = 0
    }

    fun setHealth(healthy: Boolean) {
        //Log.i(TAG, "setHealth $healthy")
        if (healthy && !isHealthy) {
            isHealthy = true
            val wentOnline = Event(true)
            sendEventToHandlerThread(wentOnline)
        }
        if (!healthy && isHealthy) {
            isHealthy = false
            //Log.i(TAG, "spawn mOfflineNotifier")
            eventThread!!.mHandler.postDelayed(mOfflineNotifier, 5000)
        }
    }

    fun sendEventToHandlerThread(event: Event) {
        val eventMsg = Message()
        eventMsg.obj = event
        eventThread!!.mHandler.sendMessage(eventMsg)
    }

    private fun setupWS() {
        //Log.i(TAG, "setupWS")
        wsId++
        httpClient = OkHttpClient()
        val request: Request = Request.Builder().url(wsURL).build()
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

}