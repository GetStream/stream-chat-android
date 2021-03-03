package io.getstream.chat.android.client.clientstate

import io.getstream.chat.android.client.models.User
import io.getstream.chat.android.core.internal.fsm.FiniteStateMachine

internal class ClientStateService {
    fun onConnected(user: User, connectionId: String) {
        stateMachine.sendEvent(ClientStateEvent.ConnectedEvent(user, connectionId))
    }

    fun onDisconnected() {
        stateMachine.sendEvent(ClientStateEvent.DisconnectedEvent)
    }

    fun onSetUser(user: User) {
        stateMachine.sendEvent(ClientStateEvent.SetUserEvent(user))
    }

    fun onSetAnonymousUser() {
        stateMachine.sendEvent(ClientStateEvent.SetAnonymousUserEvent)
    }

    fun onDisconnectRequested() {
        stateMachine.sendEvent(ClientStateEvent.DisconnectRequestedEvent)
    }

    private val stateMachine: FiniteStateMachine<ClientState, ClientStateEvent> by lazy {
        FiniteStateMachine {
            initialState(ClientState.Idle)

            defaultHandler { state, event -> state.failedToHandleEvent(event) }

            state<ClientState.Idle> {
                onEvent<ClientStateEvent.SetUserEvent> { _, event -> ClientState.User.Pending(event.user) }
                onEvent<ClientStateEvent.SetAnonymousUserEvent> { _, _ -> ClientState.Anonymous.Pending }
                onEvent<ClientStateEvent.DisconnectedEvent> { _, _ -> stay() }
                onEvent<ClientStateEvent.DisconnectRequestedEvent> { _, _ -> stay() }
                onEvent<ClientStateEvent.ConnectedEvent> { _, _ -> stay() }
            }

            state<ClientState.User.Pending> {
                onEvent<ClientStateEvent.ConnectedEvent> { _, event -> ClientState.User.Authorized.Connected(event.connectionId, event.user) }
                onEvent<ClientStateEvent.DisconnectedEvent> { _, _ -> stay() }
                onEvent<ClientStateEvent.DisconnectRequestedEvent> { _, _ -> ClientState.Idle }
            }

            state<ClientState.User.Authorized.Connected> {
                onEvent<ClientStateEvent.DisconnectedEvent> { state, _ ->
                    ClientState.User.Authorized.Disconnected(
                        state.connectionId,
                        state.user,
                    )
                }
                onEvent<ClientStateEvent.DisconnectRequestedEvent> { _, _ -> ClientState.Idle }
                onEvent<ClientStateEvent.ConnectedEvent> { _, _ -> stay() }
            }

            state<ClientState.User.Authorized.Disconnected> {
                onEvent<ClientStateEvent.DisconnectedEvent> { _, _ -> stay() }
                onEvent<ClientStateEvent.DisconnectRequestedEvent> { _, _ -> ClientState.Idle }
                onEvent<ClientStateEvent.ConnectedEvent> { _, event ->
                    ClientState.User.Authorized.Connected(
                        event.connectionId,
                        event.user,
                    )
                }
            }

            state<ClientState.Anonymous.Pending> {
                onEvent<ClientStateEvent.ConnectedEvent> { _, event -> ClientState.Anonymous.Authorized.Connected(event.connectionId, event.user) }
                onEvent<ClientStateEvent.DisconnectedEvent> { _, _ -> stay() }
                onEvent<ClientStateEvent.DisconnectRequestedEvent> { _, _ -> ClientState.Idle }
            }

            state<ClientState.Anonymous.Authorized.Connected> {
                onEvent<ClientStateEvent.DisconnectedEvent> { state, _ ->
                    ClientState.Anonymous.Authorized.Disconnected(
                        state.connectionId,
                        state.anonymousUser,
                    )
                }
                onEvent<ClientStateEvent.DisconnectRequestedEvent> { _, _ -> ClientState.Idle }
                onEvent<ClientStateEvent.ConnectedEvent> { _, _ -> stay() }
            }

            state<ClientState.Anonymous.Authorized.Disconnected> {
                onEvent<ClientStateEvent.DisconnectedEvent> { _, _ -> stay() }
                onEvent<ClientStateEvent.DisconnectRequestedEvent> { _, _ -> ClientState.Idle }
                onEvent<ClientStateEvent.ConnectedEvent> { _, event ->
                    ClientState.Anonymous.Authorized.Connected(
                        event.connectionId,
                        event.user,
                    )
                }
            }
        }
    }

    internal val state
        get() = stateMachine.state

    private fun ClientState.failedToHandleEvent(event: ClientStateEvent): Nothing =
        error("Cannot handle event $event while being in inappropriate state $this")

    private sealed class ClientStateEvent {
        data class SetUserEvent(val user: User) : ClientStateEvent()
        object SetAnonymousUserEvent : ClientStateEvent()
        data class ConnectedEvent(val user: User, val connectionId: String) : ClientStateEvent()
        object DisconnectRequestedEvent : ClientStateEvent()
        object DisconnectedEvent : ClientStateEvent()
    }
}
