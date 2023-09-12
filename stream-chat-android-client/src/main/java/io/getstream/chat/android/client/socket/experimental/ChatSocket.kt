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

package io.getstream.chat.android.client.socket.experimental

import io.getstream.chat.android.client.LifecycleHandler
import io.getstream.chat.android.client.StreamLifecycleObserver
import io.getstream.chat.android.client.clientstate.DisconnectCause
import io.getstream.chat.android.client.errors.ChatError
import io.getstream.chat.android.client.errors.ChatErrorCode
import io.getstream.chat.android.client.errors.ChatNetworkError
import io.getstream.chat.android.client.events.ChatEvent
import io.getstream.chat.android.client.events.ConnectedEvent
import io.getstream.chat.android.client.events.HealthEvent
import io.getstream.chat.android.client.models.User
import io.getstream.chat.android.client.network.NetworkStateProvider
import io.getstream.chat.android.client.scope.UserScope
import io.getstream.chat.android.client.socket.HealthMonitor
import io.getstream.chat.android.client.socket.SocketFactory
import io.getstream.chat.android.client.socket.SocketListener
import io.getstream.chat.android.client.socket.experimental.ChatSocketStateService.State
import io.getstream.chat.android.client.socket.experimental.ws.StreamWebSocket
import io.getstream.chat.android.client.socket.experimental.ws.StreamWebSocketEvent
import io.getstream.chat.android.client.token.TokenManager
import io.getstream.chat.android.client.utils.stringify
import io.getstream.chat.android.core.internal.coroutines.DispatcherProvider
import io.getstream.logging.StreamLog
import kotlinx.coroutines.Job
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.launchIn
import kotlinx.coroutines.flow.onEach
import kotlinx.coroutines.launch
import kotlinx.coroutines.withTimeout

@Suppress("TooManyFunctions", "LongParameterList")
internal class ChatSocket private constructor(
    private val apiKey: String,
    private val wssUrl: String,
    private val tokenManager: TokenManager,
    private val socketFactory: SocketFactory,
    private val coroutineScope: UserScope,
    private val lifecycleObserver: StreamLifecycleObserver,
    private val networkStateProvider: NetworkStateProvider,
) {
    private var streamWebSocket: StreamWebSocket? = null
    private val logger = StreamLog.getLogger("Chat:SocketExp")
    private var connectionConf: SocketFactory.ConnectionConf? = null
    private val listeners = mutableSetOf<SocketListener>()
    private val chatSocketStateService = ChatSocketStateService()
    private var socketStateObserverJob: Job? = null
    private val healthMonitor = HealthMonitor(
        userScope = coroutineScope,
        checkCallback = { (chatSocketStateService.currentState as? State.Connected)?.event?.let(::sendEvent) },
        reconnectCallback = {
            val socketState = chatSocketStateService.currentState
            logger.i { "[reconnectCallback] health monitor triggered reconnect; state: $socketState" }
            chatSocketStateService.onWebSocketEventLost()
        }
    )
    private val lifecycleHandler = object : LifecycleHandler {
        override suspend fun resume() {
            logger.i { "[onAppResume] no args" }
            chatSocketStateService.onResume()
        }

        override suspend fun stopped() {
            logger.i { "[onAppStop] no args" }
            chatSocketStateService.onStop()
        }
    }
    private val networkStateListener = object : NetworkStateProvider.NetworkStateListener {
        override suspend fun onConnected() {
            logger.i { "[onNetworkConnected] no args" }
            chatSocketStateService.onNetworkAvailable()
        }

        override suspend fun onDisconnected() {
            logger.i { "[onNetworkDisconnected] no args" }
            chatSocketStateService.onNetworkNotAvailable()
        }
    }

    @Suppress("ComplexMethod")
    private fun observeSocketStateService(): Job {
        var socketListenerJob: Job? = null

        suspend fun connectUser(connectionConf: SocketFactory.ConnectionConf) {
            logger.d { "[connectUser] connectionConf: $connectionConf" }
            coroutineScope.launch { startObservers() }
            this.connectionConf = connectionConf
            socketListenerJob?.cancel()
            when (networkStateProvider.isConnected()) {
                true -> {
                    streamWebSocket = socketFactory.createSocket(connectionConf).apply {
                        socketListenerJob = listen().onEach {
                            when (it) {
                                is StreamWebSocketEvent.Error -> handleError(it.chatError)
                                is StreamWebSocketEvent.Message -> handleEvent(it.chatEvent)
                            }
                        }.launchIn(coroutineScope)
                    }
                }
                false -> chatSocketStateService.onNetworkNotAvailable()
            }
        }

        suspend fun reconnect(connectionConf: SocketFactory.ConnectionConf) {
            logger.d { "[reconnect] connectionConf: $connectionConf" }
            connectUser(connectionConf.asReconnectionConf())
        }

        return coroutineScope.launch {
            chatSocketStateService.observer { state ->
                logger.i { "[updateState] newState: ${state.javaClass.simpleName}" }
                when (state) {
                    is State.RestartConnection -> {
                        connectionConf?.let { chatSocketStateService.onReconnect(it, false) }
                    }
                    is State.Connected -> {
                        healthMonitor.ack()
                        callListeners { listener -> listener.onConnected(state.event) }
                    }
                    is State.Connecting -> {
                        callListeners { listener -> listener.onConnecting() }
                        when (state.connectionType) {
                            ChatSocketStateService.ConnectionType.INITIAL_CONNECTION ->
                                connectUser(state.connectionConf)
                            ChatSocketStateService.ConnectionType.AUTOMATIC_RECONNECTION ->
                                reconnect(state.connectionConf.asReconnectionConf())
                            ChatSocketStateService.ConnectionType.FORCE_RECONNECTION ->
                                reconnect(state.connectionConf.asReconnectionConf())
                        }
                    }
                    is State.Disconnected -> {
                        when (state) {
                            is State.Disconnected.DisconnectedByRequest -> {
                                streamWebSocket?.close()
                                healthMonitor.stop()
                                coroutineScope.launch { disposeObservers() }
                            }
                            is State.Disconnected.NetworkDisconnected -> {
                                streamWebSocket?.close()
                                healthMonitor.stop()
                            }
                            is State.Disconnected.Stopped -> {
                                streamWebSocket?.close()
                                healthMonitor.stop()
                                disposeNetworkStateObserver()
                            }
                            is State.Disconnected.DisconnectedPermanently -> {
                                streamWebSocket?.close()
                                healthMonitor.stop()
                                coroutineScope.launch { disposeObservers() }
                            }
                            is State.Disconnected.DisconnectedTemporarily -> {
                                healthMonitor.onDisconnected()
                            }
                            is State.Disconnected.WebSocketEventLost -> {
                                streamWebSocket?.close()
                                connectionConf?.let { chatSocketStateService.onReconnect(it, false) }
                            }
                        }
                        callListeners { listener -> listener.onDisconnected(cause = state.cause) }
                    }
                }
            }
        }
    }

    suspend fun connectUser(user: User, isAnonymous: Boolean) {
        logger.i { "[connectUser] isAnonymous: $isAnonymous, user: $user" }
        socketStateObserverJob?.cancel()
        socketStateObserverJob = observeSocketStateService()
        chatSocketStateService.onConnect(
            when (isAnonymous) {
                true -> SocketFactory.ConnectionConf.AnonymousConnectionConf(wssUrl, apiKey, user)
                false -> SocketFactory.ConnectionConf.UserConnectionConf(wssUrl, apiKey, user)
            }
        )
    }

    suspend fun disconnect() {
        logger.i { "[disconnect] connectionConf: $connectionConf" }
        connectionConf = null
        chatSocketStateService.onRequiredDisconnect()
    }

    private suspend fun handleEvent(chatEvent: ChatEvent) {
        when (chatEvent) {
            is ConnectedEvent -> chatSocketStateService.onConnectionEstablished(chatEvent)
            is HealthEvent -> healthMonitor.ack()
            else -> callListeners { listener -> listener.onEvent(chatEvent) }
        }
    }

    private suspend fun startObservers() {
        lifecycleObserver.observe(lifecycleHandler)
        networkStateProvider.subscribe(networkStateListener)
    }

    private suspend fun disposeObservers() {
        lifecycleObserver.dispose(lifecycleHandler)
        disposeNetworkStateObserver()
    }

    private fun disposeNetworkStateObserver() {
        networkStateProvider.unsubscribe(networkStateListener)
    }

    private suspend fun handleError(error: ChatError) {
        logger.e { error.stringify() }
        when (error) {
            is ChatNetworkError -> onChatNetworkError(error)
            else -> callListeners { it.onError(error) }
        }
    }

    private suspend fun onChatNetworkError(error: ChatNetworkError) {
        if (ChatErrorCode.isAuthenticationError(error.streamCode)) {
            tokenManager.expireToken()
        }

        when (error.streamCode) {
            ChatErrorCode.UNDEFINED_TOKEN.code,
            ChatErrorCode.INVALID_TOKEN.code,
            ChatErrorCode.API_KEY_NOT_FOUND.code,
            ChatErrorCode.VALIDATION_ERROR.code,
            -> {
                logger.d {
                    "One unrecoverable error happened. Error: ${error.stringify()}. Error code: ${error.streamCode}"
                }
                chatSocketStateService.onUnrecoverableError(error)
            }
            else -> chatSocketStateService.onNetworkError(error)
        }
    }

    fun removeListener(listener: SocketListener) {
        synchronized(listeners) {
            listeners.remove(listener)
        }
    }

    fun addListener(listener: SocketListener) {
        synchronized(listeners) {
            listeners.add(listener)
        }
    }

    /**
     * Attempt to send [event] to the web socket connection.
     * Returns true only if socket is connected and [okhttp3.WebSocket.send] returns true, otherwise false
     *
     * @see [okhttp3.WebSocket.send]
     */
    internal fun sendEvent(event: ChatEvent): Boolean = streamWebSocket?.send(event) ?: false

    internal fun isConnected(): Boolean = chatSocketStateService.currentState is State.Connected

    /**
     * Awaits until [State.Connected] is set.
     *
     * @param timeoutInMillis Timeout time in milliseconds.
     */
    internal suspend fun awaitConnection(timeoutInMillis: Long = DEFAULT_CONNECTION_TIMEOUT) {
        awaitState<State.Connected>(timeoutInMillis)
    }

    /**
     * Awaits until specified [State] is set.
     *
     * @param timeoutInMillis Timeout time in milliseconds.
     */
    internal suspend inline fun <reified T : State> awaitState(timeoutInMillis: Long) {
        withTimeout(timeoutInMillis) {
            chatSocketStateService.currentStateFlow.first { it is T }
        }
    }

    /**
     * Get connection id of this connection.
     */
    internal fun connectionIdOrError(): String = when (val state = chatSocketStateService.currentState) {
        is State.Connected -> state.event.connectionId
        else -> error("This state doesn't contain connectionId")
    }

    suspend fun reconnectUser(user: User, isAnonymous: Boolean, forceReconnection: Boolean) {
        chatSocketStateService.onReconnect(
            when (isAnonymous) {
                true -> SocketFactory.ConnectionConf.AnonymousConnectionConf(wssUrl, apiKey, user)
                false -> SocketFactory.ConnectionConf.UserConnectionConf(wssUrl, apiKey, user)
            },
            forceReconnection
        )
    }

    private fun callListeners(call: (SocketListener) -> Unit) {
        synchronized(listeners) {
            listeners.forEach { listener ->
                coroutineScope.launch(DispatcherProvider.Main) { call(listener) }
            }
        }
    }

    private val State.Disconnected.cause
        get() = when (this) {
            is State.Disconnected.DisconnectedByRequest,
            is State.Disconnected.Stopped -> DisconnectCause.ConnectionReleased
            is State.Disconnected.NetworkDisconnected -> DisconnectCause.NetworkNotAvailable
            is State.Disconnected.DisconnectedPermanently -> DisconnectCause.UnrecoverableError(error)
            is State.Disconnected.DisconnectedTemporarily -> DisconnectCause.Error(error)
            is State.Disconnected.WebSocketEventLost -> DisconnectCause.WebSocketNotAvailable
        }

    companion object {
        private const val DEFAULT_CONNECTION_TIMEOUT = 60_000L

        fun create(
            apiKey: String,
            wssUrl: String,
            tokenManager: TokenManager,
            socketFactory: SocketFactory,
            coroutineScope: UserScope,
            lifecycleObserver: StreamLifecycleObserver,
            networkStateProvider: NetworkStateProvider,
        ): ChatSocket =
            ChatSocket(
                apiKey,
                wssUrl,
                tokenManager,
                socketFactory,
                coroutineScope,
                lifecycleObserver,
                networkStateProvider,
            )
    }
}
