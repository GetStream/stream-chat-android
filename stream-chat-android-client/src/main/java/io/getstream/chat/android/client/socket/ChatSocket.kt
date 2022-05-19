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
import io.getstream.chat.android.client.clientstate.DisconnectCause
import io.getstream.chat.android.client.errors.ChatError
import io.getstream.chat.android.client.errors.ChatErrorCode
import io.getstream.chat.android.client.errors.ChatNetworkError
import io.getstream.chat.android.client.events.ChatEvent
import io.getstream.chat.android.client.events.ConnectedEvent
import io.getstream.chat.android.client.logger.ChatLogger
import io.getstream.chat.android.client.models.User
import io.getstream.chat.android.client.parser.ChatParser
import io.getstream.chat.android.client.token.TokenManager
import io.getstream.chat.android.core.internal.fsm.FiniteStateMachine
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.distinctUntilChanged
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
    private val lifecycleObservers: List<LifecycleObserver>,
) {
    private val logger = ChatLogger.get("ChatSocket")

    private var connectionConf: SocketFactory.ConnectionConf? = null
    private var socketConnectionJob: Job? = null
    private val listeners = mutableSetOf<SocketListener>()
    private val eventUiHandler = Handler(Looper.getMainLooper())
    private val healthMonitor = HealthMonitor(
        object : HealthMonitor.HealthCallback {
            override fun reconnect() {
                val state = stateMachine.state
                if (state is State.Disconnected && state.disconnectCause is DisconnectCause.Error) {
                    this@ChatSocket.reconnect(connectionConf)
                }
            }

            override fun check() {
                (stateMachine.state as? State.Connected)?.let { state -> state.event?.let { sendEvent(it) } }
            }
        }
    )

    private var reconnectionAttempts = 0

    private var connectionEventReceived = false

    init {
        combine(*lifecycleObservers.map { it.lifecycleEvents }.toTypedArray()) {
            listOf(*it).combineLifecycleState()
        }
            .distinctUntilChanged { old, new -> old == new || old.isStopped() && new.isStopped() }
            .onEach { stateMachine.sendEvent(it) }
            .launchIn(coroutineScope)

        startObservers()
    }

    private val stateMachine: FiniteStateMachine<State, Event> by lazy {
        FiniteStateMachine {
            initialState(State.Disconnected(DisconnectCause.Error(null)))

            defaultHandler { state, event ->
                logger.logE("Cannot handle event $event while being in inappropriate state $this")
                state
            }

            state<State.Connecting> {
                onEnter {
                    healthMonitor.stop()
                }
                onEvent<Event.WebSocket.OnConnectionOpened<*>> {
                    State.Connected(event = null, session = session)
                }
                onEvent<Event.WebSocket.Terminate> {
                    // TODO: transition to retry state here.
                    this
                }
            }

            state<State.Connected> {
                onEnter {
                    if (it is Event.WebSocket.OnConnectedEventReceived) {
                        healthMonitor.start()
                        callListeners { listener -> listener.onConnected(it.connectedEvent) }
                    }
                }
                onEvent<Event.Lifecycle.Started> {
                    // no-op
                    this
                }
                onEvent<Event.WebSocket.OnConnectedEventReceived> {
                    State.Connected(event = it.connectedEvent, session = session)
                }
                onEvent<Event.Lifecycle.Stopped> { event ->
                    initiateShutdown(event)
                    State.Disconnecting(event.disconnectCause)
                }
                onEvent<Event.Lifecycle.Terminate> {
                    session.socket.cancel()
                    State.Destroyed
                }
                onEvent<Event.WebSocket.Terminate> {
                    // TODO: transition to retry state here.
                    this
                }
            }

            state<State.Disconnected> {
                onEvent<Event.Lifecycle.Started> {
                    connectionConf?.let {
                        val webSocket = open(it)
                        State.Connecting(session = Session(webSocket))
                    } ?: this
                }
            }

            state<State.DisconnectedPermanently> {
            }

            state<State.Disconnecting> {
                onEnter {
                    if (disconnectCause is DisconnectCause.NetworkNotAvailable || disconnectCause is DisconnectCause.ConnectionReleased) {
                        healthMonitor.stop()
                    } else if (disconnectCause is DisconnectCause.Error) {
                        healthMonitor.onDisconnected()
                    }
                }
                onEvent<Event.WebSocket.Terminate> {
                    callListeners { it.onDisconnected(this.disconnectCause) }
                    State.Disconnected(this.disconnectCause)
                }
            }

            state<State.Destroyed> {
                onEnter { disposeObservers() }
            }
        }
    }

    internal val state
        get() = stateMachine.state

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
            is Event.Lifecycle.Stopped.WithReason -> session.socket.close(state.shutdownReason)
            is Event.Lifecycle.Stopped.AndAborted -> session.socket.cancel()
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
                // state = State.DisconnectedTemporarily(error)
                stateMachine.sendEvent(Event.Lifecycle.Stopped.WithReason(DisconnectCause.Error(error)))
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

    open fun setConnectionConf(user: User?) {
        connectionConf = user?.let { SocketFactory.ConnectionConf.UserConnectionConf(wssUrl, apiKey, user) }
            ?: SocketFactory.ConnectionConf.AnonymousConnectionConf(wssUrl, apiKey)
    }

    fun reconnectAnonymously() {
        reconnect(SocketFactory.ConnectionConf.AnonymousConnectionConf(wssUrl, apiKey))
    }

    fun reconnectUser(user: User) {
        reconnect(SocketFactory.ConnectionConf.UserConnectionConf(wssUrl, apiKey, user))
    }

    open fun disconnect() {
        reconnectionAttempts = 0
        state = State.DisconnectedPermanently(null)
    }

    open fun onEvent(event: ChatEvent) {
        healthMonitor.ack()
        callListeners { listener -> listener.onEvent(event) }
    }

    internal open fun sendEvent(event: ChatEvent): Boolean {
        return when (val state = stateMachine.state) {
            is State.Connected -> state.session.socket.send(event)
            else -> false
        }
    }

    // TODO: Refactor reconnect logic.
    private fun reconnect(connectionConf: SocketFactory.ConnectionConf?) {
        shutdownSocketConnection()
        setupSocket(connectionConf?.asReconnectionConf())
    }

    private fun setupSocket(connectionConf: SocketFactory.ConnectionConf?) {
        logger.logI("setupSocket")
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
                    connectionEventReceived = true
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

    private fun shutdownSocketConnection() {
        socketConnectionJob?.cancel()
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
