package io.getstream.chat.android.client.clientstate

import io.getstream.chat.android.client.models.User
import io.getstream.chat.android.client.utils.Event
import io.getstream.chat.android.client.utils.FiniteStateMachine

internal class ClientStateService {
    fun onConnected(user: User, connectionId: String) {
        stateMachine.sendEvent(ChatClientEvent.ConnectedEvent(user, connectionId))
    }

    fun onDisconnected() {
        stateMachine.sendEvent(ChatClientEvent.DisconnectedEvent)
    }

    fun onSetUser(user: User) {
        stateMachine.sendEvent(ChatClientEvent.SetUserEvent(user))
    }

    fun onSetAnonymousUser() {
        stateMachine.sendEvent(ChatClientEvent.SetAnonymousUserEvent)
    }

    fun onTokenReceived(token: String) {
        stateMachine.sendEvent(ChatClientEvent.TokenReceivedEvent(token))
    }

    private val stateMachine: FiniteStateMachine<ClientState, ChatClientEvent> = FiniteStateMachine {
        initialState(ClientState.Idle)

        defaultHandler { state, event -> state.inappropriateStateError("handling event $event") }

        state<ClientState.Idle> {
            onEvent<ChatClientEvent.DisconnectedEvent> { _, _ -> stay() }
        }

        state<ClientState.Idle> {
            onEvent<ChatClientEvent.SetUserEvent> { _, event ->
                ClientState.UserState.AuthorizationPending.AuthorizationPendingWithoutToken(event.user)
            }
            onEvent<ChatClientEvent.SetAnonymousUserEvent> { _, _ ->
                ClientState.AnonymousUserState.AnonymousUserPending.AnonymousPendingWithoutToken
            }
        }

        state<ClientState.UserState.AuthorizationPending.AuthorizationPendingWithoutToken> {
            onEvent<ChatClientEvent.TokenReceivedEvent> { state, event ->
                ClientState.UserState.AuthorizationPending.AuthorizationPendingWithToken(state.user, event.token)
            }
        }

        state<ClientState.UserState.AuthorizationPending.AuthorizationPendingWithToken> {
            onEvent<ChatClientEvent.ConnectedEvent> { state, event ->
                ClientState.UserState.UserAuthorized.Connected(event.connectionId, event.user, state.token)
            }
        }

        state<ClientState.UserState.UserAuthorized.Connected> {
            onEvent<ChatClientEvent.DisconnectedEvent> { state, _ ->
                ClientState.UserState.UserAuthorized.Disconnected(state.connectionId, state.user, state.token)
            }
        }

        state<ClientState.UserState.UserAuthorized.Disconnected> {
            onEvent<ChatClientEvent.DisconnectedEvent> { _, _ -> stay()  }
        }

        state<ClientState.AnonymousUserState.AnonymousUserPending.AnonymousPendingWithoutToken> {
            onEvent<ChatClientEvent.TokenReceivedEvent> { _, event ->
                ClientState.AnonymousUserState.AnonymousUserPending.AnonymousPendingWithToken(event.token)
            }
        }

        state<ClientState.AnonymousUserState.AnonymousUserPending.AnonymousPendingWithToken> {
            onEvent<ChatClientEvent.ConnectedEvent> { state, event ->
                ClientState.AnonymousUserState.AnonymousUserAuthorized.AnonymousUserConnected(
                    event.connectionId,
                    event.user,
                    state.token
                )
            }
        }

        state<ClientState.AnonymousUserState.AnonymousUserAuthorized.AnonymousUserConnected> {
            onEvent<ChatClientEvent.DisconnectedEvent> { state, _ ->
                ClientState.AnonymousUserState.AnonymousUserAuthorized.AnonymousUserDisconnected(
                    state.connectionId,
                    state.anonymousUser,
                    state.token
                )
            }
        }

        state<ClientState.AnonymousUserState.AnonymousUserAuthorized.AnonymousUserDisconnected> {
            onEvent<ChatClientEvent.DisconnectedEvent> { _, _ -> stay()  }
        }
    }

    internal val state = stateMachine.state

    private sealed class ChatClientEvent : Event {
        class SetUserEvent(val user: User) : ChatClientEvent()
        object SetAnonymousUserEvent : ChatClientEvent()
        class TokenReceivedEvent(val token: String) : ChatClientEvent()
        class ConnectedEvent(val user: User, val connectionId: String) : ChatClientEvent()
        object DisconnectedEvent : ChatClientEvent()
    }
}