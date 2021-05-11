package io.getstream.chat.android.client.clientstate

import io.getstream.chat.android.core.internal.fsm.FiniteStateMachine

internal class ClientStateService {
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

    private val stateMachine: FiniteStateMachine<ClientState, ClientStateEvent> by lazy {
        FiniteStateMachine {
            initialState(ClientState.Idle)

            defaultHandler { state, event -> state.failedToHandleEvent(event) }

            state<ClientState.Idle> {
                onEvent<ClientStateEvent.ConnectionRequested> { _, _ -> ClientState.Pending }
                onEvent<ClientStateEvent.DisconnectedEvent> { _, _ -> stay() }
                onEvent<ClientStateEvent.DisconnectRequestedEvent> { _, _ -> stay() }
                onEvent<ClientStateEvent.ConnectedEvent> { _, _ -> stay() }
            }

            state<ClientState.Pending> {
                onEvent<ClientStateEvent.ConnectedEvent> { _, event -> ClientState.Connected(event.connectionId) }
                onEvent<ClientStateEvent.DisconnectedEvent> { _, _ -> stay() }
                onEvent<ClientStateEvent.DisconnectRequestedEvent> { _, _ -> ClientState.Idle }
            }

            state<ClientState.Connected> {
                onEvent<ClientStateEvent.DisconnectedEvent> { _, _ -> ClientState.Disconnected }
                onEvent<ClientStateEvent.DisconnectRequestedEvent> { _, _ -> ClientState.Idle }
                onEvent<ClientStateEvent.ConnectedEvent> { _, _ -> stay() }
            }

            state<ClientState.Disconnected> {
                onEvent<ClientStateEvent.DisconnectedEvent> { _, _ -> stay() }
                onEvent<ClientStateEvent.DisconnectRequestedEvent> { _, _ -> ClientState.Idle }
                onEvent<ClientStateEvent.ConnectedEvent> { _, event -> ClientState.Connected(event.connectionId) }
            }
        }
    }

    internal val state
        get() = stateMachine.state

    private fun ClientState.failedToHandleEvent(event: ClientStateEvent): Nothing =
        error("Cannot handle event $event while being in inappropriate state $this")

    private sealed class ClientStateEvent {
        object ConnectionRequested : ClientStateEvent()
        data class ConnectedEvent(val connectionId: String) : ClientStateEvent()
        object DisconnectRequestedEvent : ClientStateEvent()
        object DisconnectedEvent : ClientStateEvent()
    }
}
