/*
 * Copyright (c) 2014-2022 Stream.io Inc. All rights reserved.
 *
 * Licensed under the Stream License;
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *    https://github.com/GetStream/stream-chat-android/blob/main/LICENSE
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

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
import io.getstream.chat.android.core.internal.fsm.FiniteStateMachine
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.launchIn
import kotlinx.coroutines.flow.onEach
import kotlinx.coroutines.flow.onSubscription
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import okhttp3.WebSocket
import kotlin.math.pow
import kotlin.properties.Delegates

@Suppress("TooManyFunctions", "LongParameterList")
internal open class ChatSocket constructor(
    private val apiKey: String,
    private val wssUrl: String,
    private val tokenManager: TokenManager,
    private val socketFactory: SocketFactory,
    private val networkStateProvider: NetworkStateProvider,
    private val parser: ChatParser,
    private val coroutineScope: CoroutineScope,
) {
    private val logger = ChatLogger.get("ChatSocket")
    private var connectionConf: ConnectionConf = ConnectionConf.None
    private var socket: Socket? = null
    private var eventsParser: EventsParser? = null
    private var socketConnectionJob: Job? = null
    private val listeners = mutableSetOf<SocketListener>()
    private val eventUiHandler = Handler(Looper.getMainLooper())
    private val healthMonitor = HealthMonitor(
        object : HealthMonitor.HealthCallback {
            override fun reconnect() {
                if (state is State.DisconnectedTemporarily) {
                    this@ChatSocket.reconnect(connectionConf)
                }
            }

            override fun check() {
                (state as? State.Connected)?.let {
                    sendEvent(it.event)
                }
            }
        }
    )

    private val webSocketEventObserver = WebSocketEventObserver()
    private val networkStateListener = object : NetworkStateProvider.NetworkStateListener {
        override fun onConnected() {
            logger.logI("Network connected. Socket state: ${state.javaClass.simpleName}")
            if (state is State.DisconnectedTemporarily || state == State.NetworkDisconnected) {
                logger.logI("network connected, reconnecting socket")
                reconnect(connectionConf)
            }
        }

        override fun onDisconnected() {
            logger.logI("Network disconnected. Socket state: ${state.javaClass.simpleName}")
            healthMonitor.stop()
            if (state is State.Connected || state is State.Connecting) {
                state = State.NetworkDisconnected
            }
        }
    }

    private var reconnectionAttempts = 0

    @VisibleForTesting
    internal var state: State by Delegates.observable(
        State.DisconnectedTemporarily(null) as State
    ) { _, oldState, newState ->
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
                    callListeners { it.onDisconnected(DisconnectCause.NetworkNotAvailable) }
                }
                is State.DisconnectedByRequest -> {
                    shutdownSocketConnection()
                    healthMonitor.stop()
                    callListeners { it.onDisconnected(DisconnectCause.ConnectionReleased) }
                }
                is State.DisconnectedTemporarily -> {
                    shutdownSocketConnection()
                    healthMonitor.onDisconnected()
                    callListeners { it.onDisconnected(DisconnectCause.Error(newState.error)) }
                }
                is State.DisconnectedPermanently -> {
                    shutdownSocketConnection()
                    connectionConf = ConnectionConf.None
                    networkStateProvider.unsubscribe(networkStateListener)
                    healthMonitor.stop()
                    callListeners { it.onDisconnected(DisconnectCause.UnrecoverableError(newState.error)) }
                }
            }
        }
    }
        private set

    private val stateMachine: FiniteStateMachine<State, Event> by lazy {
        FiniteStateMachine {
            initialState(State.Disconnected)

            defaultHandler { state, event ->
                logger.logE("Cannot handle event $event while being in inappropriate state $this")
                state
            }

        }
    }

    open fun onSocketError(error: ChatError) {
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
                if (reconnectionAttempts < RETRY_LIMIT) {
                    coroutineScope.launch {
                        delay(DEFAULT_DELAY * reconnectionAttempts.toDouble().pow(2.0).toLong())
                        reconnect(connectionConf)
                        reconnectionAttempts += 1
                    }
                }
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

    open fun removeListener(listener: SocketListener) {
        synchronized(listeners) {
            listeners.remove(listener)
        }
    }

    open fun addListener(listener: SocketListener) {
        synchronized(listeners) {
            listeners.add(listener)
        }
    }

    open fun connectAnonymously() =
        connect(ConnectionConf.AnonymousConnectionConf(wssUrl, apiKey))

    open fun connect(user: User) =
        connect(ConnectionConf.UserConnectionConf(wssUrl, apiKey, user))

    private fun connect(connectionConf: ConnectionConf) {
        val isNetworkConnected = networkStateProvider.isConnected()
        logger.logI("Connect. Network available: $isNetworkConnected")

        webSocketEventObserver.eventsFlow
            .onEach(::handleWebSocketEvent)
            .launchIn(coroutineScope)

        this.connectionConf = connectionConf
        if (isNetworkConnected) {
            setupSocket(connectionConf)
        } else {
            state = State.NetworkDisconnected
        }
        networkStateProvider.subscribe(networkStateListener)
    }

    open fun disconnect() {
        reconnectionAttempts = 0
        state = State.DisconnectedPermanently(null)
    }

    open fun releaseConnection() {
        state = State.DisconnectedByRequest
    }

    open fun onConnectionResolved(event: ConnectedEvent) {
        state = State.Connected(event)
    }

    open fun onEvent(event: ChatEvent) {
        healthMonitor.ack()
        callListeners { listener -> listener.onEvent(event) }
    }

    internal open fun sendEvent(event: ChatEvent): Boolean {
        // TODO: Replace this with local read only state var.
        return when (stateMachine.state) {
            is State.Connected -> stateMachine.state.session.okHttpWebSocket.send(event)
            else -> false
        }
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
                    val socket = socketFactory.createAnonymousSocket(endpoint, apiKey)
                    state = State.Connecting(Session(socket))
                }
                is ConnectionConf.UserConnectionConf -> {
                    socketConnectionJob = coroutineScope.launch {
                        tokenManager.ensureTokenLoaded()
                        withContext(DispatcherProvider.Main) {
                            val socket = socketFactory.createNormalSocket(endpoint, apiKey, user)
                            state = State.Connecting(Session(socket))
                        }
                    }
                }
            }
        }
    }

    private fun shutdownSocketConnection() {
        socketConnectionJob?.cancel()
        eventsParser?.closeByClient()
        eventsParser = null
        socketHolder.close(EventsParser.CODE_CLOSE_SOCKET_FROM_CLIENT, "Connection close by client")
        socket = null
    }

    private fun callListeners(call: (SocketListener) -> Unit) {
        synchronized(listeners) {
            listeners.forEach { listener ->
                eventUiHandler.post { call(listener) }
            }
        }
    }

    private companion object {
        private const val RETRY_LIMIT = 3
        private const val DEFAULT_DELAY = 500
    }

    internal sealed class ConnectionConf {
        object None : ConnectionConf()
        data class AnonymousConnectionConf(val endpoint: String, val apiKey: String) : ConnectionConf()
        data class UserConnectionConf(val endpoint: String, val apiKey: String, val user: User) : ConnectionConf()
    }

    @VisibleForTesting
    internal sealed class State {
        data class Connecting(val session: Session) : State()
        data class Connected(val event: ConnectedEvent, val session: Session) : State()
        object NetworkDisconnected : State()
        class DisconnectedTemporarily(val error: ChatNetworkError?) : State()
        class DisconnectedPermanently(val error: ChatNetworkError?) : State()
        object DisconnectedByRequest : State()

        object Disconnecting : State()
        object Disconnected : State()
        object Destroyed : State()

        internal fun connectionIdOrError(): String = when (this) {
            is Connected -> event.connectionId
            else -> error("This state doesn't contain connectionId")
        }
    }

    internal data class Session(val socket: OkHttpWebSocket)

    internal sealed class Event {
        sealed class Lifecycle : Event() {
            object Started : Lifecycle()
            object Stopped : Lifecycle()
        }

        sealed class WebSocket : Event() {
            object Terminate : WebSocket()

            data class OnConnectionOpened<out WEB_SOCKET : Any>(val webSocket: WEB_SOCKET) : WebSocket()

            data class OnMessageReceived(val message: String) : WebSocket()

            /**
             * Invoked when the peer has indicated that no more incoming messages will be transmitted.
             *
             * @property shutdownReason Reason to shutdown from the peer.
             */
            data class OnConnectionClosing(val shutdownReason: ShutdownReason) : WebSocket()

            /**
             * Invoked when both peers have indicated that no more messages will be transmitted and the connection has been
             * successfully released. No further calls to this listener will be made.
             *
             * @property shutdownReason Reason to shutdown from the peer.
             */
            data class OnConnectionClosed(val shutdownReason: ShutdownReason) : WebSocket()

            /**
             * Invoked when a web socket has been closed due to an error reading from or writing to the network. Both outgoing
             * and incoming messages may have been lost. No further calls to this listener will be made.
             *
             * @property throwable The error causing the failure.
             */
            data class OnConnectionFailed(val throwable: Throwable) : WebSocket()
        }
    }
}
