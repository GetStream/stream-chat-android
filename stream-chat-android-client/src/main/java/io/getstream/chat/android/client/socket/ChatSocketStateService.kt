/*
 * Copyright (c) 2014-2026 Stream.io Inc. All rights reserved.
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

import io.getstream.chat.android.client.events.ConnectedEvent
import io.getstream.chat.android.core.internal.fsm.FiniteStateMachine
import io.getstream.log.taggedLogger
import io.getstream.result.Error
import kotlinx.coroutines.flow.StateFlow

internal class ChatSocketStateService(initialState: State = State.Disconnected.Stopped) {
    private val logger by taggedLogger("Chat:SocketState")

    suspend fun observer(onNewState: suspend (State) -> Unit) {
        stateMachine.stateFlow.collect(onNewState)
    }

    /**
     * Require a reconnection.
     *
     * @param connectionConf The [SocketFactory.ConnectionConf] to be used to reconnect.
     */
    suspend fun onReconnect(connectionConf: SocketFactory.ConnectionConf, forceReconnection: Boolean) {
        logger.v {
            "[onReconnect] user.id: '${connectionConf.user.id}', isReconnection: ${connectionConf.isReconnection}"
        }
        stateMachine.sendEvent(
            Event.Connect(
                connectionConf,
                when (forceReconnection) {
                    true -> ConnectionType.FORCE_RECONNECTION
                    false -> ConnectionType.AUTOMATIC_RECONNECTION
                },
            ),
        )
    }

    /**
     * Require connection.
     *
     * @param connectionConf The [SocketFactory.ConnectionConf] to be used on the new connection.
     */
    suspend fun onConnect(connectionConf: SocketFactory.ConnectionConf) {
        logger.v {
            "[onConnect] user.id: '${connectionConf.user.id}', isReconnection: ${connectionConf.isReconnection}"
        }
        stateMachine.sendEvent(Event.Connect(connectionConf, ConnectionType.INITIAL_CONNECTION))
    }

    /**
     * Notify that the network is not available at the moment.
     */
    suspend fun onNetworkNotAvailable() {
        logger.w { "[onNetworkNotAvailable] no args" }
        stateMachine.sendEvent(Event.NetworkNotAvailable)
    }

    /**
     * Notify the WebSocket connection has been established.
     *
     * @param connectedEvent The [ConnectedEvent] received within the WebSocket connection.
     */
    suspend fun onConnectionEstablished(connectedEvent: ConnectedEvent) {
        logger.i {
            "[onConnected] user.id: '${connectedEvent.me.id}', connectionId: ${connectedEvent.connectionId}"
        }
        stateMachine.sendEvent(Event.ConnectionEstablished(connectedEvent))
    }

    /**
     * Notify that an unrecoverable error happened.
     *
     * @param error The [Error.NetworkError]
     */
    suspend fun onUnrecoverableError(error: Error.NetworkError) {
        logger.e { "[onUnrecoverableError] error: $error" }
        stateMachine.sendEvent(Event.UnrecoverableError(error))
    }

    /**
     * Notify that a network error happened.
     *
     * @param error The [Error.NetworkError]
     */
    suspend fun onNetworkError(error: Error.NetworkError) {
        logger.e { "[onNetworkError] error: $error" }
        stateMachine.sendEvent(Event.NetworkError(error))
    }

    /**
     * Notify that the user want to disconnect the WebSocket connection.
     */
    suspend fun onRequiredDisconnect() {
        logger.i { "[onRequiredDisconnect] no args" }
        stateMachine.sendEvent(Event.RequiredDisconnection)
    }

    /**
     * Notify that the connection should be stopped.
     */
    suspend fun onStop() {
        logger.i { "[onStop] no args" }
        stateMachine.sendEvent(Event.Stop)
    }

    /**
     * Notify that some WebSocket Event has been lost.
     */
    suspend fun onWebSocketEventLost() {
        logger.w { "[onWebSocketEventLost] no args" }
        stateMachine.sendEvent(Event.WebSocketEventLost)
    }

    /**
     * Notify that the network is available at the moment.
     */
    suspend fun onNetworkAvailable() {
        logger.i { "[onNetworkAvailable] no args" }
        stateMachine.sendEvent(Event.NetworkAvailable)
    }

    /**
     * Notify that the connection should be resumed.
     */
    suspend fun onResume() {
        logger.v { "[onResume] no args" }
        stateMachine.sendEvent(Event.Resume)
    }

    /**
     * Current state of the WebSocket connection.
     */
    val currentState: State
        get() = stateMachine.state

    /**
     * Current state of the WebSocket connection as [StateFlow].
     */
    val currentStateFlow: StateFlow<State>
        get() = stateMachine.stateFlow

    private val stateMachine: FiniteStateMachine<State, Event> by lazy {
        FiniteStateMachine {
            initialState(initialState)

            defaultHandler { state, event ->
                logger.e { "Cannot handle event $event while being in inappropriate state $state" }
                state
            }

            state<State.RestartConnection> {
                onEvent<Event.Connect> { State.Connecting(it.connectionConf, it.connectionType) }
                onEvent<Event.ConnectionEstablished> { State.Connected(it.connectedEvent) }
                onEvent<Event.WebSocketEventLost> { State.Disconnected.WebSocketEventLost }
                onEvent<Event.NetworkNotAvailable> { State.Disconnected.NetworkDisconnected }
                onEvent<Event.UnrecoverableError> { State.Disconnected.DisconnectedPermanently(it.error) }
                onEvent<Event.NetworkError> { State.Disconnected.DisconnectedTemporarily(it.error) }
                onEvent<Event.RequiredDisconnection> { State.Disconnected.DisconnectedByRequest }
                onEvent<Event.Stop> { State.Disconnected.Stopped }
            }

            state<State.Connecting> {
                onEvent<Event.Connect> { State.Connecting(it.connectionConf, it.connectionType) }
                onEvent<Event.ConnectionEstablished> { State.Connected(it.connectedEvent) }
                onEvent<Event.WebSocketEventLost> { State.Disconnected.WebSocketEventLost }
                onEvent<Event.NetworkNotAvailable> { State.Disconnected.NetworkDisconnected }
                onEvent<Event.UnrecoverableError> { State.Disconnected.DisconnectedPermanently(it.error) }
                onEvent<Event.NetworkError> { State.Disconnected.DisconnectedTemporarily(it.error) }
                onEvent<Event.RequiredDisconnection> { State.Disconnected.DisconnectedByRequest }
                onEvent<Event.Stop> { State.Disconnected.Stopped }
            }

            state<State.Connected> {
                onEvent<Event.ConnectionEstablished> { State.Connected(it.connectedEvent) }
                onEvent<Event.WebSocketEventLost> { State.Disconnected.WebSocketEventLost }
                onEvent<Event.NetworkNotAvailable> { State.Disconnected.NetworkDisconnected }
                onEvent<Event.UnrecoverableError> { State.Disconnected.DisconnectedPermanently(it.error) }
                onEvent<Event.NetworkError> { State.Disconnected.DisconnectedTemporarily(it.error) }
                onEvent<Event.RequiredDisconnection> { State.Disconnected.DisconnectedByRequest }
                onEvent<Event.Stop> { State.Disconnected.Stopped }
            }

            state<State.Disconnected.Stopped> {
                onEvent<Event.RequiredDisconnection> { State.Disconnected.DisconnectedByRequest }
                onEvent<Event.Connect> { State.Connecting(it.connectionConf, it.connectionType) }
                onEvent<Event.Resume> { State.RestartConnection(RestartReason.LIFECYCLE_RESUME) }
            }

            state<State.Disconnected.NetworkDisconnected> {
                onEvent<Event.Connect> { State.Connecting(it.connectionConf, it.connectionType) }
                onEvent<Event.ConnectionEstablished> { State.Connected(it.connectedEvent) }
                onEvent<Event.UnrecoverableError> { State.Disconnected.DisconnectedPermanently(it.error) }
                onEvent<Event.NetworkError> { State.Disconnected.DisconnectedTemporarily(it.error) }
                onEvent<Event.RequiredDisconnection> { State.Disconnected.DisconnectedByRequest }
                onEvent<Event.Stop> { State.Disconnected.Stopped }
                onEvent<Event.NetworkAvailable> { State.RestartConnection(RestartReason.NETWORK_AVAILABLE) }
            }

            state<State.Disconnected.WebSocketEventLost> {
                onEvent<Event.Connect> { State.Connecting(it.connectionConf, it.connectionType) }
                onEvent<Event.ConnectionEstablished> { State.Connected(it.connectedEvent) }
                onEvent<Event.NetworkNotAvailable> { State.Disconnected.NetworkDisconnected }
                onEvent<Event.UnrecoverableError> { State.Disconnected.DisconnectedPermanently(it.error) }
                onEvent<Event.NetworkError> { State.Disconnected.DisconnectedTemporarily(it.error) }
                onEvent<Event.RequiredDisconnection> { State.Disconnected.DisconnectedByRequest }
                onEvent<Event.Stop> { State.Disconnected.Stopped }
            }

            state<State.Disconnected.DisconnectedByRequest> {
                onEvent<Event.RequiredDisconnection> { currentState }
                onEvent<Event.Connect> {
                    when (it.connectionType) {
                        ConnectionType.INITIAL_CONNECTION -> State.Connecting(it.connectionConf, it.connectionType)
                        ConnectionType.AUTOMATIC_RECONNECTION -> this
                        ConnectionType.FORCE_RECONNECTION -> State.Connecting(it.connectionConf, it.connectionType)
                    }
                }
            }

            state<State.Disconnected.DisconnectedTemporarily> {
                onEvent<Event.Connect> { State.Connecting(it.connectionConf, it.connectionType) }
                onEvent<Event.ConnectionEstablished> { State.Connected(it.connectedEvent) }
                onEvent<Event.NetworkNotAvailable> { State.Disconnected.NetworkDisconnected }
                onEvent<Event.WebSocketEventLost> { State.Disconnected.WebSocketEventLost }
                onEvent<Event.UnrecoverableError> { State.Disconnected.DisconnectedPermanently(it.error) }
                onEvent<Event.NetworkError> { State.Disconnected.DisconnectedTemporarily(it.error) }
                onEvent<Event.RequiredDisconnection> { State.Disconnected.DisconnectedByRequest }
                onEvent<Event.Stop> { State.Disconnected.Stopped }
            }

            state<State.Disconnected.DisconnectedPermanently> {
                onEvent<Event.Connect> {
                    when (it.connectionType) {
                        ConnectionType.INITIAL_CONNECTION -> State.Connecting(it.connectionConf, it.connectionType)
                        ConnectionType.AUTOMATIC_RECONNECTION -> this
                        ConnectionType.FORCE_RECONNECTION -> State.Connecting(it.connectionConf, it.connectionType)
                    }
                }
                onEvent<Event.RequiredDisconnection> { State.Disconnected.DisconnectedByRequest }
            }
        }
    }

    internal enum class ConnectionType {
        INITIAL_CONNECTION,
        AUTOMATIC_RECONNECTION,
        FORCE_RECONNECTION,
    }

    internal enum class RestartReason {
        LIFECYCLE_RESUME,
        NETWORK_AVAILABLE,
    }

    private sealed class Event {

        /**
         * Event to start a new connection.
         */
        data class Connect(
            val connectionConf: SocketFactory.ConnectionConf,
            val connectionType: ConnectionType,
        ) : Event()

        /**
         * Event to notify the connection was established.
         */
        data class ConnectionEstablished(val connectedEvent: ConnectedEvent) : Event()

        /**
         * Event to notify some WebSocket event has been lost.
         */
        object WebSocketEventLost : Event() { override fun toString() = "WebSocketEventLost" }

        /**
         * Event to notify Network is not available.
         */
        object NetworkNotAvailable : Event() { override fun toString() = "NetworkNotAvailable" }

        /**
         * Event to notify Network is available.
         */
        object NetworkAvailable : Event() { override fun toString() = "NetworkAvailable" }

        /**
         * Event to notify an Unrecoverable Error happened on the WebSocket connection.
         */
        data class UnrecoverableError(val error: Error.NetworkError) : Event()

        /**
         * Event to notify a network Error happened on the WebSocket connection.
         */
        data class NetworkError(val error: Error.NetworkError) : Event()

        /**
         * Event to stop WebSocket connection required by user.
         */
        object RequiredDisconnection : Event() { override fun toString() = "RequiredDisconnection" }

        /**
         * Event to stop WebSocket connection.
         */
        object Stop : Event() { override fun toString() = "Stop" }

        /**
         * Event to resume WebSocket connection.
         */
        object Resume : Event() { override fun toString() = "Resume" }
    }

    internal sealed class State {

        /**
         * State of socket when connection need to be reestablished.
         */
        data class RestartConnection(val reason: RestartReason) : State()

        /**
         * State of socket when connection is being establishing.
         */
        data class Connecting(
            val connectionConf: SocketFactory.ConnectionConf,
            val connectionType: ConnectionType,
        ) : State()

        /**
         * State of socket when the connection is established.
         */
        data class Connected(val event: ConnectedEvent) : State()

        /**
         * State of socket when connection is being disconnected.
         */
        sealed class Disconnected : State() {

            /**
             * State of socket when is stopped.
             */
            object Stopped : Disconnected() { override fun toString() = "Disconnected.Stopped" }

            /**
             * State of socket when network is disconnected.
             */
            object NetworkDisconnected : Disconnected() { override fun toString() = "Disconnected.Network" }

            /**
             * State of socket when HealthEvent is lost.
             */
            object WebSocketEventLost : Disconnected() { override fun toString() = "Disconnected.InactiveWS" }

            /**
             * State of socket when is disconnected by customer request.
             */
            object DisconnectedByRequest : Disconnected() { override fun toString() = "Disconnected.ByRequest" }

            /**
             * State of socket when a [Error] happens.
             */
            data class DisconnectedTemporarily(val error: Error.NetworkError) : Disconnected()

            /**
             * State of socket when a connection is permanently disconnected.
             */
            data class DisconnectedPermanently(val error: Error.NetworkError) : Disconnected()
        }
    }
}
