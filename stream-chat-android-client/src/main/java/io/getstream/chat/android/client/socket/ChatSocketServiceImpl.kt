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
            override fun reconnect() = this@ChatSocketServiceImpl.reconnect()
            override fun check() {
                (state as? State.Connected)?.let {
                    sendEvent(it.event)
                }
            }
        }
    )

    private var state: State by Delegates.observable(
        State.Disconnected as State,
        { _, oldState, newState ->
            if (oldState != newState) {
                logger.logI("updateState: ${newState.javaClass.simpleName}")
                when (newState) {
                    is State.Connecting -> {
                        healthMonitor.stop()
                        callListeners { it.onConnecting() }
                    }
                    is State.Connected -> {
                        healthMonitor.start()
                        callListeners { it.onConnected(newState.event) }
                    }
                    is State.Disconnected -> {
                        releaseSocket()
                        healthMonitor.onDisconnected()
                        callListeners { it.onDisconnected() }
                    }
                    is State.DisconnectedPermanently -> {
                        releaseSocket()
                        healthMonitor.stop()
                        callListeners { it.onDisconnected() }
                    }
                }
            }
        }
    )

    override fun onSocketError(error: ChatError) {
        if (state != State.DisconnectedPermanently) {
            logger.logE(error)
            callListeners { it.onError(error) }
            (error as? ChatNetworkError)?.let(::onChatNetworkError)
        }
    }

    private fun onChatNetworkError(error: ChatNetworkError) = when (error.streamCode) {
        ChatErrorCode.PARSER_ERROR.code,
        ChatErrorCode.CANT_PARSE_CONNECTION_EVENT.code,
        ChatErrorCode.CANT_PARSE_EVENT.code,
        ChatErrorCode.UNABLE_TO_PARSE_SOCKET_EVENT.code,
        ChatErrorCode.NO_ERROR_BODY.code -> {
            // Nothing to do on that case
        }
        ChatErrorCode.TOKEN_EXPIRED.code -> {
            tokenManager.expireToken()
            tokenManager.loadSync()
            state = State.Disconnected
        }
        ChatErrorCode.UNDEFINED_TOKEN.code,
        ChatErrorCode.INVALID_TOKEN.code,
        ChatErrorCode.API_KEY_NOT_FOUND.code -> {
            state = State.DisconnectedPermanently
        }
        ChatErrorCode.NETWORK_FAILED.code,
        ChatErrorCode.SOCKET_CLOSED.code,
        ChatErrorCode.SOCKET_FAILURE.code -> {
            state = State.Disconnected
        }
        else -> {
            state = State.Disconnected
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
        state = State.Disconnected
        logger.logI("connect")
        this.endpoint = endpoint
        this.apiKey = apiKey
        this.user = user
        setupSocket()
    }

    override fun disconnect() {
        state = State.DisconnectedPermanently
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

    private fun reconnect() {
        releaseSocket()
        setupSocket()
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
        object Disconnected : State()
        object DisconnectedPermanently : State()
    }

    companion object {
        fun create(
            tokenManager: TokenManager,
            socketFactory: SocketFactory,
            eventsParser: EventsParser
        ): ChatSocketServiceImpl {
            return ChatSocketServiceImpl(tokenManager, socketFactory)
                .also { eventsParser.setSocketService(it) }
        }
    }
}
