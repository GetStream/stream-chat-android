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

import io.getstream.chat.android.client.logger.ChatLogger
import io.getstream.chat.android.core.internal.fsm.FiniteStateMachine

internal class SocketStateService {
    private val logger = ChatLogger.get("SocketStateService")

    fun onConnected(connectionId: String) {
        stateMachine.sendEvent(ClientStateEvent.ConnectedEvent(connectionId))
    }

    fun onDisconnected() {
        stateMachine.sendEvent(ClientStateEvent.DisconnectedEvent)
    }

    fun onConnectionRequested() {
        stateMachine.sendEvent(ClientStateEvent.ConnectionRequested)
    }

    fun onDisconnectRequested() {
        stateMachine.sendEvent(ClientStateEvent.DisconnectRequestedEvent)
    }

    fun onSocketUnrecoverableError() {
        stateMachine.sendEvent(ClientStateEvent.ForceDisconnect)
    }

    private val stateMachine: FiniteStateMachine<SocketState, ClientStateEvent> by lazy {
        FiniteStateMachine {
            initialState(SocketState.Idle)

            defaultHandler { state, event ->
                logger.logE("Cannot handle event $event while being in inappropriate state $this")
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

    private sealed class ClientStateEvent {
        object ConnectionRequested : ClientStateEvent()
        data class ConnectedEvent(val connectionId: String) : ClientStateEvent()
        object DisconnectRequestedEvent : ClientStateEvent()
        object DisconnectedEvent : ClientStateEvent()
        object ForceDisconnect : ClientStateEvent()
    }
}
