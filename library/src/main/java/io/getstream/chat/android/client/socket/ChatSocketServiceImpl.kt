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
import io.getstream.chat.android.client.socket.ChatSocketService.State
import io.getstream.chat.android.client.token.TokenManager
import java.util.*


internal class ChatSocketServiceImpl(
    eventsParser: EventsParser,
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
    private val healthMonitor = HealthMonitor(this)

    override var state: State = State.Disconnected(false)

    init {
        eventsParser.setSocketService(this)
    }

    override fun setLastEventDate(date: Date) {
        healthMonitor.lastEventDate = date
    }

    override fun onSocketError(error: ChatError) {

        logger.logE(error)

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
        logger.logI("connect")

        if (state is State.Connecting || state is State.Connected) {
            updateState(State.Disconnected(true))
            clearState()
        }

        this.endpoint = endpoint
        this.apiKey = apiKey
        this.user = user
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
        callListeners { listener -> listener.onEvent(event) }
    }

    internal fun sendEvent(event: ChatEvent) {
        socket?.send(event)
    }

    internal fun setupSocket() {
        logger.logI("setupSocket")
        updateState(State.Connecting)
        socket = socketFactory.create(endpoint, apiKey, user)
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
                callListeners { it.onError(state.error) }
            }
            is State.Connecting -> {
                callListeners { it.onConnecting() }
            }
            is State.Connected -> {
                callListeners { it.onConnected(state.event) }
            }
            is State.Disconnected -> {
                callListeners { it.onDisconnected() }
            }
        }
    }

    private fun callListeners(call: (SocketListener) -> Unit) {

        synchronized(listeners) {
            listeners.forEach { listener ->
                eventUiHandler.post { call(listener) }
            }
        }

    }


}