package io.getstream.chat.android.client.socket

import android.os.Handler
import android.os.Looper
import androidx.annotation.VisibleForTesting
import io.getstream.chat.android.client.clientstate.DisconnectCause
import io.getstream.chat.android.client.errors.ChatError
import io.getstream.chat.android.client.errors.ChatErrorCode
import io.getstream.chat.android.client.errors.ChatNetworkError
import io.getstream.chat.android.client.events.ChatEvent
import io.getstream.chat.android.client.events.ConnectedEvent
import io.getstream.chat.android.client.logger.ChatLogger
import io.getstream.chat.android.client.models.User
import io.getstream.chat.android.client.network.NetworkStateProvider
import io.getstream.chat.android.client.parser.ChatParser
import io.getstream.chat.android.client.token.TokenManager
import io.getstream.chat.android.core.internal.coroutines.DispatcherProvider
import io.getstream.chat.android.core.internal.exhaustive
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Job
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import kotlin.properties.Delegates

internal class ChatSocketServiceImpl constructor(
    private val tokenManager: TokenManager,
    private val socketFactory: SocketFactory,
    private val networkStateProvider: NetworkStateProvider,
    private val parser: ChatParser,
    private val coroutineScope: CoroutineScope,
) : ChatSocketService {
    private val logger = ChatLogger.get("SocketService")
    private var connectionConf: ConnectionConf = ConnectionConf.None
    private var socket: Socket? = null
    private var eventsParser: EventsParser? = null
    private var socketConnectionJob: Job? = null
    private val listeners = mutableListOf<SocketListener>()
    private val eventUiHandler = Handler(Looper.getMainLooper())
    private val healthMonitor = HealthMonitor(
        object : HealthMonitor.HealthCallback {
            override fun reconnect() = this@ChatSocketServiceImpl.reconnect(connectionConf)
            override fun check() {
                (state as? State.Connected)?.let {
                    sendEvent(it.event)
                }
            }
        }
    )
    private val networkStateListener = object : NetworkStateProvider.NetworkStateListener {
        override fun onConnected() {
            if (state is State.DisconnectedTemporarily || state == State.NetworkDisconnected) {
                logger.logI("network connected, reconnecting socket")
                reconnect(connectionConf)
            }
        }

        override fun onDisconnected() {
            state = State.NetworkDisconnected
        }
    }

    @VisibleForTesting
    internal var state: State by Delegates.observable(
        State.DisconnectedTemporarily(null) as State,
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
                    is State.NetworkDisconnected -> {
                        shutdownSocketConnection()
                        healthMonitor.stop()
                        callListeners { it.onDisconnected() }
                        callListeners { it.onDisconnected(DisconnectCause.NetworkNotAvailable) }
                    }
                    is State.DisconnectedByRequest -> {
                        shutdownSocketConnection()
                        healthMonitor.stop()
                        callListeners { it.onDisconnected(DisconnectCause.AppBackgrounded) }
                    }
                    is State.DisconnectedTemporarily -> {
                        shutdownSocketConnection()
                        healthMonitor.onDisconnected()
                        callListeners { it.onDisconnected() }
                        callListeners { it.onDisconnected(DisconnectCause.Error(newState.error)) }
                    }
                    is State.DisconnectedPermanently -> {
                        shutdownSocketConnection()
                        connectionConf = ConnectionConf.None
                        networkStateProvider.unsubscribe(networkStateListener)
                        healthMonitor.stop()
                        callListeners { it.onDisconnected() }
                        callListeners { it.onDisconnected(DisconnectCause.UnrecoverableError(newState.error)) }
                    }
                }
            }
        }
    )
        private set

    override fun onSocketError(error: ChatError) {
        if (state !is State.DisconnectedPermanently) {
            logger.logE(error)
            callListeners { it.onError(error) }
            (error as? ChatNetworkError)?.let(::onChatNetworkError)
        }
    }

    private fun onChatNetworkError(error: ChatNetworkError) {
        if (ChatErrorCode.isAuthenticationError(error.streamCode)) {
            tokenManager.expireToken()
        }

        when (error.streamCode) {
            ChatErrorCode.PARSER_ERROR.code,
            ChatErrorCode.CANT_PARSE_CONNECTION_EVENT.code,
            ChatErrorCode.CANT_PARSE_EVENT.code,
            ChatErrorCode.UNABLE_TO_PARSE_SOCKET_EVENT.code,
            ChatErrorCode.NO_ERROR_BODY.code,
            -> {
                // Nothing to do on that case
            }
            ChatErrorCode.UNDEFINED_TOKEN.code,
            ChatErrorCode.INVALID_TOKEN.code,
            ChatErrorCode.API_KEY_NOT_FOUND.code,
            ChatErrorCode.VALIDATION_ERROR.code,
            -> {
                state = State.DisconnectedPermanently(error)
            }
            else -> {
                state = State.DisconnectedTemporarily(error)
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

    override fun anonymousConnect(endpoint: String, apiKey: String) =
        connect(ConnectionConf.AnonymousConnectionConf(endpoint, apiKey))

    override fun userConnect(endpoint: String, apiKey: String, user: User) =
        connect(ConnectionConf.UserConnectionConf(endpoint, apiKey, user))

    private fun connect(connectionConf: ConnectionConf) {
        logger.logI("connect")
        this.connectionConf = connectionConf
        if (networkStateProvider.isConnected()) {
            setupSocket(connectionConf)
        } else {
            state = State.NetworkDisconnected
        }
        networkStateProvider.subscribe(networkStateListener)
    }

    override fun disconnect() {
        state = State.DisconnectedPermanently(null)
    }

    override fun disconnectRequested() {
        state = State.DisconnectedByRequest
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

    private fun reconnect(connectionConf: ConnectionConf) {
        shutdownSocketConnection()
        setupSocket(connectionConf)
    }

    private fun setupSocket(connectionConf: ConnectionConf) {
        logger.logI("setupSocket")
        with(connectionConf) {
            when (this) {
                is ConnectionConf.None -> {
                    state = State.DisconnectedPermanently(null)
                }
                is ConnectionConf.AnonymousConnectionConf -> {
                    state = State.Connecting
                    socket = socketFactory.createAnonymousSocket(createNewEventsParser(), endpoint, apiKey)
                }
                is ConnectionConf.UserConnectionConf -> {
                    state = State.Connecting
                    socketConnectionJob = coroutineScope.launch {
                        tokenManager.ensureTokenLoaded()
                        withContext(DispatcherProvider.Main) {
                            socket = socketFactory.createNormalSocket(createNewEventsParser(), endpoint, apiKey, user)
                        }
                    }
                }
            }.exhaustive
        }
    }

    private fun createNewEventsParser(): EventsParser = EventsParser(parser, this).also {
        eventsParser = it
    }

    private fun shutdownSocketConnection() {
        eventsParser?.closeByClient()
        eventsParser = null
        socketConnectionJob?.cancel()
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

    internal sealed class ConnectionConf {
        object None : ConnectionConf()
        data class AnonymousConnectionConf(val endpoint: String, val apiKey: String) : ConnectionConf()
        data class UserConnectionConf(val endpoint: String, val apiKey: String, val user: User) : ConnectionConf()
    }

    @VisibleForTesting
    internal sealed class State {
        object Connecting : State()
        data class Connected(val event: ConnectedEvent) : State()
        object NetworkDisconnected : State()
        class DisconnectedTemporarily(val error: ChatNetworkError?) : State()
        class DisconnectedPermanently(val error: ChatNetworkError?) : State()
        object DisconnectedByRequest : State()
    }
}
