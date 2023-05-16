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

import io.getstream.chat.android.client.errors.ChatError
import io.getstream.chat.android.client.errors.ChatNetworkError
import io.getstream.chat.android.client.events.ConnectedEvent
import io.getstream.chat.android.client.socket.SocketFactory
import io.getstream.chat.android.core.internal.fsm.FiniteStateMachine
import io.getstream.logging.StreamLog
import kotlinx.coroutines.flow.StateFlow

internal class ChatSocketStateService(initialState: State = State.Disconnected.Stopped) {
    private val logger = StreamLog.getLogger("Chat:ChatSocketStateService")

    suspend fun observer(onNewState: (State) -> Unit) {
        stateMachine.stateFlow.collect(onNewState)
    }

    /**
     * Require a reconnection.
     *
     * @param connectionConf The [SocketFactory.ConnectionConf] to be used to reconnect.
     */
    fun onReconnect(connectionConf: SocketFactory.ConnectionConf, forceReconnection: Boolean) {
        stateMachine.sendEvent(
            Event.Connect(
                connectionConf,
                when (forceReconnection) {
                    true -> ConnectionType.FORCE_RECONNECTION
                    false -> ConnectionType.AUTOMATIC_RECONNECTION
                },
            )
        )
    }

    /**
     * Require connection.
     *
     * @param connectionConf The [SocketFactory.ConnectionConf] to be used on the new connection.
     */
    fun onConnect(connectionConf: SocketFactory.ConnectionConf) {
        stateMachine.sendEvent(Event.Connect(connectionConf, ConnectionType.INITIAL_CONNECTION))
    }

    /**
     * Notify that the network is not available at the moment.
     */
    fun onNetworkNotAvailable() {
        stateMachine.sendEvent(Event.NetworkNotAvailable)
    }

    /**
     * Notify the WebSocket connection has been established.
     *
     * @param connectedEvent The [ConnectedEvent] received within the WebSocket connection.
     */
    fun onConnectionEstablished(connectedEvent: ConnectedEvent) {
        stateMachine.sendEvent(Event.ConnectionEstablished(connectedEvent))
    }

    /**
     * Notify that an unrecoverable error happened.
     *
     * @param error The [ChatNetworkError]
     */
    fun onUnrecoverableError(error: ChatNetworkError) {
        stateMachine.sendEvent(Event.UnrecoverableError(error))
    }

    /**
     * Notify that a network error happened.
     *
     * @param error The [ChatNetworkError]
     */
    fun onNetworkError(error: ChatNetworkError) {
        stateMachine.sendEvent(Event.NetworkError(error))
    }

    /**
     * Notify that the user want to disconnect the WebSocket connection.
     */
    fun onRequiredDisconnect() {
        stateMachine.sendEvent(Event.RequiredDisconnection)
    }

    /**
     * Notify that the connection should be stopped.
     */
    fun onStop() {
        stateMachine.sendEvent(Event.Stop)
    }

    /**
     * Notify that some WebSocket Event has been lost.
     */
    fun onWebSocketEventLost() {
        stateMachine.sendEvent(Event.WebSocketEventLost)
    }

    /**
     * Notify that the network is available at the moment.
     */
    fun onNetworkAvailable() {
        stateMachine.sendEvent(Event.NetworkAvailable)
    }

    /**
     * Notify that the connection should be resumed.
     */
    fun onResume() {
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
                onEvent<Event.Resume> { State.RestartConnection }
            }

            state<State.Disconnected.NetworkDisconnected> {
                onEvent<Event.Connect> { State.Connecting(it.connectionConf, it.connectionType) }
                onEvent<Event.ConnectionEstablished> { State.Connected(it.connectedEvent) }
                onEvent<Event.UnrecoverableError> { State.Disconnected.DisconnectedPermanently(it.error) }
                onEvent<Event.NetworkError> { State.Disconnected.DisconnectedTemporarily(it.error) }
                onEvent<Event.RequiredDisconnection> { State.Disconnected.DisconnectedByRequest }
                onEvent<Event.Stop> { State.Disconnected.Stopped }
                onEvent<Event.NetworkAvailable> { State.RestartConnection }
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
        object WebSocketEventLost : Event()

        /**
         * Event to notify Network is not available.
         */
        object NetworkNotAvailable : Event()

        /**
         * Event to notify Network is available.
         */
        object NetworkAvailable : Event()

        /**
         * Event to notify an Unrecoverable Error happened on the WebSocket connection.
         */
        data class UnrecoverableError(val error: ChatNetworkError) : Event()

        /**
         * Event to notify a network Error happened on the WebSocket connection.
         */
        data class NetworkError(val error: ChatNetworkError) : Event()

        /**
         * Event to stop WebSocket connection required by user.
         */
        object RequiredDisconnection : Event()

        /**
         * Event to stop WebSocket connection.
         */
        object Stop : Event()

        /**
         * Event to resume WebSocket connection.
         */
        object Resume : Event()
    }

    internal sealed class State {

        /**
         * State of socket when connection need to be reestablished.
         */
        object RestartConnection : State() { override fun toString() = "RestartConnection" }

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
             * State of socket when a [ChatError] happens.
             */
            data class DisconnectedTemporarily(val error: ChatNetworkError) : Disconnected()

            /**
             * State of socket when a connection is permanently disconnected.
             */
            data class DisconnectedPermanently(val error: ChatNetworkError) : Disconnected()
        }
    }
}
