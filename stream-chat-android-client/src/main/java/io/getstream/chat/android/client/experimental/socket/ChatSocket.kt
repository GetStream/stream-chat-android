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

package io.getstream.chat.android.client.experimental.socket

import android.os.Handler
import android.os.Looper
import io.getstream.chat.android.client.clientstate.DisconnectCause
import io.getstream.chat.android.client.errors.ChatError
import io.getstream.chat.android.client.errors.ChatErrorCode
import io.getstream.chat.android.client.errors.ChatNetworkError
import io.getstream.chat.android.client.events.ChatEvent
import io.getstream.chat.android.client.events.ConnectedEvent
import io.getstream.chat.android.client.experimental.socket.lifecycle.ConnectLifecyclePublisher
import io.getstream.chat.android.client.experimental.socket.lifecycle.LifecyclePublisher
import io.getstream.chat.android.client.experimental.socket.lifecycle.combine
import io.getstream.chat.android.client.experimental.socket.ws.OkHttpWebSocket
import io.getstream.chat.android.client.logger.ChatLogger
import io.getstream.chat.android.client.models.User
import io.getstream.chat.android.client.parser.ChatParser
import io.getstream.chat.android.client.socket.HealthMonitor
import io.getstream.chat.android.client.socket.SocketErrorMessage
import io.getstream.chat.android.client.socket.SocketListener
import io.getstream.chat.android.client.token.TokenManager
import io.getstream.chat.android.core.internal.fsm.FiniteStateMachine
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.launchIn
import kotlinx.coroutines.flow.onEach
import kotlinx.coroutines.launch
import kotlin.math.pow

@Suppress("TooManyFunctions", "LongParameterList")
internal open class ChatSocket constructor(
    private val apiKey: String,
    private val wssUrl: String,
    private val tokenManager: TokenManager,
    private val socketFactory: SocketFactory,
    private val coroutineScope: CoroutineScope,
    private val parser: ChatParser,
    private val lifecycleObservers: List<LifecyclePublisher>,
) {
    private val logger = ChatLogger.get("ChatSocket")

    private var connectLifecyclePublisher = ConnectLifecyclePublisher()
    private var connectionConf: SocketFactory.ConnectionConf? = null
    private val listeners = mutableSetOf<SocketListener>()
    private val eventUiHandler = Handler(Looper.getMainLooper())
    private val healthMonitor = HealthMonitor(
        checkCallback = {
            (stateMachine.state as? State.Connected)?.let { state -> state.event?.let { sendEvent(it) } }
        },
        reconnectCallback = {
            val state = stateMachine.state
            if (state is State.Disconnected && state.disconnectCause is DisconnectCause.Error) {
                connectionConf?.let { connect(it.asReconnectionConf()) }
            }
        }
    )

    private var reconnectionAttempts = 0

    private var connectionEventReceived = false

    private var latestUnhandledLifecycleEvent: Event.Lifecycle? = null

    init {
        (lifecycleObservers + connectLifecyclePublisher).combine()
            .onEach {
                logger.logD("Received lifecycle event: $it")
                stateMachine.sendEvent(it)
            }
            .launchIn(coroutineScope)
        startObservers()
    }

    private val stateMachine: FiniteStateMachine<State, Event> by lazy {
        FiniteStateMachine {
            initialState(State.Disconnected(DisconnectCause.Error(null)))

            defaultHandler { state, event ->
                logger.logE("Cannot handle event $event while being in inappropriate state $state")
                state
            }

            state<State.Disconnected> {
                onEnter {
                    logger.logD("Entered into state $this from $it")
                    when (disconnectCause) {
                        is DisconnectCause.NetworkNotAvailable, is DisconnectCause.ConnectionReleased -> {
                            healthMonitor.stop()
                        }
                        is DisconnectCause.Error -> {
                            healthMonitor.onDisconnected()
                        }
                        else -> {
                            healthMonitor.stop()
                            connectionConf = null
                        }
                    }
                    callListeners { listener -> listener.onDisconnected(this.disconnectCause) }
                }
                onEvent<Event.Lifecycle.Started> {
                    connectionConf?.let {
                        val webSocket = open(it)
                        State.Connecting(webSocket = webSocket)
                    } ?: this
                }
                onEvent<Event.Lifecycle.Stopped> {
                    // no-op
                    this
                }
                onEvent<Event.Lifecycle.Terminate> {
                    State.Destroyed
                }
            }

            state<State.Connecting> {
                onEnter {
                    healthMonitor.stop()
                    callListeners { listener -> listener.onConnecting() }
                }
                onEvent<Event.WebSocket.OnConnectionOpened<*>> {
                    State.Connected(event = null, webSocket = webSocket)
                }
                onEvent<Event.WebSocket.Terminate> {
                    // We do transition to Disconnected state here because the connection can be reconnected with health callback.
                    State.Disconnected(DisconnectCause.Error(null))
                    // TODO: Improve retry logic independent of HealthMonitor.
                }
            }

            state<State.Connected> {
                onEnter {
                    logger.logD("Entered into state $this from $it")
                    if (it is Event.WebSocket.OnConnectedEventReceived) {
                        connectionEventReceived = true
                        healthMonitor.start()
                        callListeners { listener -> listener.onConnected(it.connectedEvent) }
                    }
                }
                onEvent<Event.Lifecycle.Started> {
                    // no-op
                    this
                }
                onEvent<Event.Lifecycle.Stopped> { event ->
                    initiateShutdown(event)
                    State.Disconnecting(event.disconnectCause)
                }
                onEvent<Event.WebSocket.OnConnectedEventReceived> {
                    State.Connected(event = it.connectedEvent, webSocket = webSocket)
                }
                onEvent<Event.Lifecycle.Terminate> {
                    webSocket.cancel()
                    State.Destroyed
                }
                onEvent<Event.WebSocket.Terminate> {
                    // We do transition to Disconnected state here because the connection can be reconnected with health callback.
                    State.Disconnected(DisconnectCause.Error(null))
                    // TODO: Improve retry logic independent of HealthMonitor.
                }
            }

            state<State.Disconnecting> {
                onEvent<Event.WebSocket.Terminate> {
                    State.Disconnected(this.disconnectCause)
                }
            }

            state<State.Destroyed> {
                onEnter { disposeObservers() }
            }
        }
    }

    private fun handlePendingEvent() {
        latestUnhandledLifecycleEvent?.let { stateMachine.sendEvent(it) }
        latestUnhandledLifecycleEvent = null
    }

    internal val state
        get() = stateMachine.state

    fun connect(user: User?) {
        connectionConf = user?.let { SocketFactory.ConnectionConf.UserConnectionConf(wssUrl, apiKey, user) }
            ?: SocketFactory.ConnectionConf.AnonymousConnectionConf(wssUrl, apiKey)
        connectLifecyclePublisher.onConnect()
    }

    private fun connect(connectionConf: SocketFactory.ConnectionConf) {
        this.connectionConf = connectionConf
        connectLifecyclePublisher.onConnect()
    }

    fun disconnect(cause: DisconnectCause? = null) {
        connectLifecyclePublisher.onDisconnect(cause)
    }

    private fun open(connectionConf: SocketFactory.ConnectionConf): OkHttpWebSocket {
        return with(connectionConf) {
            val socket = socketFactory.createSocket(this)
            socket.open()
                .onEach { handleEvent(it) }.launchIn(coroutineScope)
            socket
        }
    }

    private fun startObservers() {
        lifecycleObservers.forEach { it.observe() }
    }

    private fun disposeObservers() {
        lifecycleObservers.forEach { it.dispose() }
    }

    private fun State.Connected.initiateShutdown(state: Event.Lifecycle.Stopped) {
        when (state) {
            is Event.Lifecycle.Stopped.WithReason -> webSocket.close(state.shutdownReason)
            is Event.Lifecycle.Stopped.AndAborted -> webSocket.cancel()
        }
    }

    open fun onSocketError(error: ChatError) {
        logger.logE(error)
        callListeners { it.onError(error) }
        (error as? ChatNetworkError)?.let(::onChatNetworkError)
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
                        connectionConf?.let {
                            delay(DEFAULT_DELAY * reconnectionAttempts.toDouble().pow(2.0).toLong())
                            reconnect(it)
                            reconnectionAttempts += 1
                        }
                    }
                }
            }
            ChatErrorCode.UNDEFINED_TOKEN.code,
            ChatErrorCode.INVALID_TOKEN.code,
            ChatErrorCode.API_KEY_NOT_FOUND.code,
            ChatErrorCode.VALIDATION_ERROR.code,
            -> {
                disconnect(DisconnectCause.UnrecoverableError(error))
            }
            else -> {
                disconnect(DisconnectCause.Error(error))
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

    fun reconnectAnonymously() {
        reconnect(SocketFactory.ConnectionConf.AnonymousConnectionConf(wssUrl, apiKey))
    }

    fun reconnectUser(user: User) {
        reconnect(SocketFactory.ConnectionConf.UserConnectionConf(wssUrl, apiKey, user))
    }

    open fun onEvent(event: ChatEvent) {
        healthMonitor.ack()
        callListeners { listener -> listener.onEvent(event) }
    }

    /**
     * Attempt to send [event] to the web socket connection.
     * This method returns true only if socket is connected and [okhttp3.WebSocket.send] returns true. In all other cases,
     * it returns false.
     *
     * @see [okhttp3.WebSocket.send]
     */
    internal open fun sendEvent(event: ChatEvent): Boolean {
        return when (val state = stateMachine.state) {
            is State.Connected -> state.webSocket.send(event)
            else -> false
        }
    }

    private fun reconnect(connectionConf: SocketFactory.ConnectionConf) {
        disconnect(DisconnectCause.Error(ChatNetworkError.create(ChatErrorCode.PARSER_ERROR)))
        connect(connectionConf.asReconnectionConf())
    }

    private fun handleEvent(event: Event.WebSocket) {
        if (event is Event.WebSocket.OnMessageReceived) {
            val text = event.message
            try {
                logger.logI(text)
                val errorMessage = parser.fromJsonOrError(text, SocketErrorMessage::class.java)
                val errorData = errorMessage.data()
                if (errorMessage.isSuccess && errorData.error != null) {
                    val error = errorData.error
                    onSocketError(ChatNetworkError.create(error.code, error.message, error.statusCode))
                } else {
                    handleChatEvent(text)
                }
            } catch (t: Throwable) {
                logger.logE("onMessage", t)
                onSocketError(ChatNetworkError.create(ChatErrorCode.UNABLE_TO_PARSE_SOCKET_EVENT))
            }
        }
        stateMachine.sendEvent(event)
    }

    private fun handleChatEvent(text: String) {
        val eventResult = parser.fromJsonOrError(text, ChatEvent::class.java)
        if (eventResult.isSuccess) {
            val event = eventResult.data()
            if (!connectionEventReceived) {
                if (event is ConnectedEvent) {
                    stateMachine.sendEvent(Event.WebSocket.OnConnectedEventReceived(event))
                } else {
                    onSocketError(ChatNetworkError.create(ChatErrorCode.CANT_PARSE_CONNECTION_EVENT))
                }
            } else {
                onEvent(event)
            }
        } else {
            onSocketError(ChatNetworkError.create(ChatErrorCode.CANT_PARSE_EVENT, eventResult.error().cause))
        }
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
}
