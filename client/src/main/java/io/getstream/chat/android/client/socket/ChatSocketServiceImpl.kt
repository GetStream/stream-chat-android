package io.getstream.chat.android.client.socket

import android.os.Handler
import android.os.Looper
import io.getstream.chat.android.client.errors.ChatError
import io.getstream.chat.android.client.errors.ChatErrorCode
import io.getstream.chat.android.client.errors.ChatNetworkError
import io.getstream.chat.android.client.events.ChatEvent
import io.getstream.chat.android.client.events.ConnectedEvent
import io.getstream.chat.android.client.logger.ChatLogger
import io.getstream.chat.android.client.models.User
import io.getstream.chat.android.client.token.TokenManager
import kotlin.properties.Delegates

internal class ChatSocketServiceImpl private constructor(
    private val tokenManager: TokenManager,
    private val socketFactory: SocketFactory
) : ChatSocketService {
    private val logger = ChatLogger.get("SocketService")
    private var endpoint: String = ""
    private var apiKey: String = ""
    private var user: User? = null
    private var socket: Socket? = null
    private val listeners = mutableListOf<SocketListener>()
    private val eventUiHandler = Handler(Looper.getMainLooper())
    private val healthMonitor = HealthMonitor(
        object : HealthMonitor.HealthCallback {
            override fun reconnect() = setupSocket()
            override fun check() {
                (state as? State.Connected)?.let {
                    sendEvent(it.event)
                }
            }
        }
    )

    private var state: State by Delegates.observable(
        State.Disconnected(true) as State,
        { _, oldState, newState ->
            if (oldState != newState) {
                logger.logI("updateState: ${newState.javaClass.simpleName}")
                when (newState) {
                    is State.Error -> {
                        healthMonitor.onError()
                        callListeners { it.onError(newState.error) }
                    }
                    is State.Connecting -> {
                        healthMonitor.stop()
                        callListeners { it.onConnecting() }
                    }
                    is State.Connected -> {
                        healthMonitor.start()
                        callListeners { it.onConnected(newState.event) }
                    }
                    is State.Disconnected -> {
                        if (!newState.connectionWillFollow) {
                            healthMonitor.stop()
                        }
                        releaseSocket()
                        callListeners { it.onDisconnected() }
                    }
                }
            }
        }
    )

    override fun onSocketError(error: ChatError) {
        logger.logE(error)
        if (error is ChatNetworkError && error.streamCode == ChatErrorCode.TOKEN_EXPIRED.code) {
            state = State.Error(error)
            tokenManager.expireToken()
            tokenManager.loadSync()
        } else if (error is ChatNetworkError && error.streamCode == ChatErrorCode.API_KEY_NOT_FOUND.code) {
            state = State.Error(error)
            state = State.Disconnected(false)
        } else {
            if (state is State.Connected || state is State.Connecting) {
                state = State.Error(error)
                state = State.Disconnected(true)
            }
        }
    }

    override fun removeListener(listener: SocketListener) {
        synchronized(listeners) {
            listeners.remove(listener)
        }
    }

    override fun addListener(listener: SocketListener) {
        synchronized(listeners) {
            listeners.add(listener)
        }
    }

    override fun connect(
        endpoint: String,
        apiKey: String,
        user: User?
    ) {
        state = State.Disconnected(true)
        logger.logI("connect")
        this.endpoint = endpoint
        this.apiKey = apiKey
        this.user = user
        setupSocket()
    }

    override fun disconnect() {
        state = State.Disconnected(false)
    }

    override fun onConnectionResolved(event: ConnectedEvent) {
        state = State.Connected(event)
    }

    override fun onEvent(event: ChatEvent) {
        healthMonitor.ack()
        callListeners { listener -> listener.onEvent(event) }
    }

    internal fun sendEvent(event: ChatEvent) {
        socket?.send(event)
    }

    private fun setupSocket() {
        logger.logI("setupSocket")
        state = State.Connecting
        socket = socketFactory.create(endpoint, apiKey, user)
    }

    private fun releaseSocket() {
        socket?.close(1000, "Connection close by client")
        socket = null
    }

    private fun callListeners(call: (SocketListener) -> Unit) {
        synchronized(listeners) {
            listeners.forEach { listener ->
                eventUiHandler.post { call(listener) }
            }
        }
    }

    private sealed class State {
        object Connecting : State()
        data class Connected(val event: ConnectedEvent) : State()
        data class Disconnected(val connectionWillFollow: Boolean) : State()
        data class Error(val error: ChatError) : State()
    }

    companion object {
        fun create(
            tokenManager: TokenManager,
            socketFactory: SocketFactory,
            eventsParser: EventsParser
        ): ChatSocketServiceImpl = ChatSocketServiceImpl(tokenManager, socketFactory).also { eventsParser.setSocketService(it) }
    }
}
