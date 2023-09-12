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

package io.getstream.chat.android.client.clientstate

import io.getstream.chat.android.core.internal.fsm.FiniteStateMachine
import io.getstream.logging.StreamLog
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.withTimeout

internal class SocketStateService {
    private val logger = StreamLog.getLogger("Chat:SocketStateService")

    suspend fun onConnected(connectionId: String) {
        stateMachine.sendEvent(ClientStateEvent.ConnectedEvent(connectionId))
    }

    suspend fun onDisconnected() {
        stateMachine.sendEvent(ClientStateEvent.DisconnectedEvent)
    }

    suspend fun onConnectionRequested() {
        stateMachine.sendEvent(ClientStateEvent.ConnectionRequested)
    }

    suspend fun onDisconnectRequested() {
        stateMachine.sendEvent(ClientStateEvent.DisconnectRequestedEvent)
    }

    suspend fun onSocketUnrecoverableError() {
        stateMachine.sendEvent(ClientStateEvent.ForceDisconnect)
    }

    private val stateMachine: FiniteStateMachine<SocketState, ClientStateEvent> by lazy {
        FiniteStateMachine {
            initialState(SocketState.Idle)

            defaultHandler { state, event ->
                logger.e { "Cannot handle event $event while being in inappropriate state: $state" }
                state
            }

            state<SocketState.Idle> {
                onEvent<ClientStateEvent.ConnectionRequested> { SocketState.Pending }
            }

            state<SocketState.Pending> {
                onEvent<ClientStateEvent.ConnectedEvent> { event -> SocketState.Connected(event.connectionId) }
                onEvent<ClientStateEvent.DisconnectRequestedEvent> { SocketState.Idle }
                onEvent<ClientStateEvent.ForceDisconnect> { SocketState.Idle }
            }

            state<SocketState.Connected> {
                onEvent<ClientStateEvent.DisconnectedEvent> { SocketState.Disconnected }
                onEvent<ClientStateEvent.DisconnectRequestedEvent> { SocketState.Idle }
                onEvent<ClientStateEvent.ForceDisconnect> { SocketState.Idle }
            }

            state<SocketState.Disconnected> {
                onEvent<ClientStateEvent.DisconnectRequestedEvent> { SocketState.Idle }
                onEvent<ClientStateEvent.ConnectedEvent> { event -> SocketState.Connected(event.connectionId) }
                onEvent<ClientStateEvent.ForceDisconnect> { SocketState.Idle }
            }
        }
    }

    internal val state
        get() = stateMachine.state

    internal val stateFlow
        get() = stateMachine.stateFlow

    /**
     * Awaits until [SocketState.Connected] is set.
     *
     * @param timeoutInMillis Timeout time in milliseconds.
     */
    internal suspend fun awaitConnection(timeoutInMillis: Long = DEFAULT_CONNECTION_TIMEOUT) {
        awaitState<SocketState.Connected>(timeoutInMillis)
    }

    /**
     * Awaits until specified [SocketState] is set.
     *
     * @param timeoutInMillis Timeout time in milliseconds.
     */
    internal suspend inline fun <reified T : SocketState> awaitState(timeoutInMillis: Long) {
        withTimeout(timeoutInMillis) {
            stateMachine.stateFlow.first { it is T }
        }
    }

    private sealed class ClientStateEvent {
        object ConnectionRequested : ClientStateEvent() { override fun toString(): String = "ConnectionRequested" }
        data class ConnectedEvent(val connectionId: String) : ClientStateEvent()
        object DisconnectRequestedEvent : ClientStateEvent() {
            override fun toString(): String = "DisconnectRequestedEvent"
        }

        object DisconnectedEvent : ClientStateEvent() { override fun toString(): String = "DisconnectedEvent" }
        object ForceDisconnect : ClientStateEvent() { override fun toString(): String = "ForceDisconnect" }
    }

    private companion object {
        private const val DEFAULT_CONNECTION_TIMEOUT = 60_000L
    }
}
