package io.getstream.chat.android.client.clientstate

import io.getstream.chat.android.client.models.User
import io.getstream.chat.android.core.internal.fsm.FiniteStateMachine

internal class UserStateService {
    fun onUserUpdated(user: User) {
        fsm.sendEvent(UserStateEvent.UserUpdated(user))
    }

    fun onSetUser(user: User) {
        fsm.sendEvent(UserStateEvent.ConnectUser(user))
    }

    fun onSetAnonymous() {
        fsm.sendEvent(UserStateEvent.ConnectAnonymous)
    }

    fun onLogout() {
        fsm.sendEvent(UserStateEvent.UnsetUser)
    }

    fun onSocketUnrecoverableError() {
        fsm.sendEvent(UserStateEvent.UnsetUser)
    }

    internal val state: UserState
        get() = fsm.state

    private val fsm = FiniteStateMachine<UserState, UserStateEvent> {
        defaultHandler { state, event -> error("Can't handle $event while being in state ${state::class.simpleName}") }
        initialState(UserState.NotSet)
        state<UserState.NotSet> {
            onEvent<UserStateEvent.ConnectUser> { _, event -> UserState.UserSet(event.user) }
            onEvent<UserStateEvent.ConnectAnonymous> { _, _ -> UserState.Anonymous.Pending }
            onEvent<UserStateEvent.UnsetUser> { _, _ -> stay() }
        }
        state<UserState.UserSet> {
            onEvent<UserStateEvent.UserUpdated> { _, event -> UserState.UserSet(event.user) }
            onEvent<UserStateEvent.UnsetUser> { _, _ -> UserState.NotSet }
        }
        state<UserState.Anonymous.Pending> {
            onEvent<UserStateEvent.UserUpdated> { _, event -> UserState.Anonymous.AnonymousUserSet(event.user) }
            onEvent<UserStateEvent.UnsetUser> { _, _ -> UserState.NotSet }
        }
        state<UserState.Anonymous.AnonymousUserSet> {
            onEvent<UserStateEvent.ConnectAnonymous> { _, _ -> UserState.Anonymous.Pending }
            onEvent<UserStateEvent.UserUpdated> { _, event -> UserState.Anonymous.AnonymousUserSet(event.user) }
            onEvent<UserStateEvent.UnsetUser> { _, _ -> UserState.NotSet }
        }
    }

    private sealed class UserStateEvent {
        class ConnectUser(val user: User) : UserStateEvent()
        class UserUpdated(val user: User) : UserStateEvent()
        object ConnectAnonymous : UserStateEvent()
        object UnsetUser : UserStateEvent()
    }
}
