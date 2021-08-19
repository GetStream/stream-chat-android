package io.getstream.chat.android.client.clientstate

import io.getstream.chat.android.core.internal.fsm.FiniteStateMachine

internal class SocketStateService {
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

            defaultHandler { state, event -> state.failedToHandleEvent(event) }

            state<SocketState.Idle> {
                onEvent<ClientStateEvent.ConnectionRequested> { _, _ -> SocketState.Pending }
                onEvent<ClientStateEvent.DisconnectedEvent> { _, _ -> stay() }
                onEvent<ClientStateEvent.DisconnectRequestedEvent> { _, _ -> stay() }
                onEvent<ClientStateEvent.ConnectedEvent> { _, _ -> stay() }
                onEvent<ClientStateEvent.ForceDisconnect> { _, _ -> stay() }
            }

            state<SocketState.Pending> {
                onEvent<ClientStateEvent.ConnectedEvent> { _, event -> SocketState.Connected(event.connectionId) }
                onEvent<ClientStateEvent.DisconnectedEvent> { _, _ -> stay() }
                onEvent<ClientStateEvent.DisconnectRequestedEvent> { _, _ -> SocketState.Idle }
                onEvent<ClientStateEvent.ForceDisconnect> { _, _ -> SocketState.Idle }
            }

            state<SocketState.Connected> {
                onEvent<ClientStateEvent.DisconnectedEvent> { _, _ -> SocketState.Disconnected }
                onEvent<ClientStateEvent.DisconnectRequestedEvent> { _, _ -> SocketState.Idle }
                onEvent<ClientStateEvent.ConnectedEvent> { _, _ -> stay() }
                onEvent<ClientStateEvent.ForceDisconnect> { _, _ -> SocketState.Idle }
            }

            state<SocketState.Disconnected> {
                onEvent<ClientStateEvent.DisconnectedEvent> { _, _ -> stay() }
                onEvent<ClientStateEvent.DisconnectRequestedEvent> { _, _ -> SocketState.Idle }
                onEvent<ClientStateEvent.ConnectionRequested> { _, _ -> SocketState.Pending }
                onEvent<ClientStateEvent.ConnectedEvent> { _, event -> SocketState.Connected(event.connectionId) }
                onEvent<ClientStateEvent.ForceDisconnect> { _, _ -> SocketState.Idle }
            }
        }
    }

    internal val state
        get() = stateMachine.state

    private fun SocketState.failedToHandleEvent(event: ClientStateEvent): Nothing =
        error("Cannot handle event $event while being in inappropriate state $this")

    private sealed class ClientStateEvent {
        object ConnectionRequested : ClientStateEvent()
        data class ConnectedEvent(val connectionId: String) : ClientStateEvent()
        object DisconnectRequestedEvent : ClientStateEvent()
        object DisconnectedEvent : ClientStateEvent()
        object ForceDisconnect : ClientStateEvent()
    }
}
