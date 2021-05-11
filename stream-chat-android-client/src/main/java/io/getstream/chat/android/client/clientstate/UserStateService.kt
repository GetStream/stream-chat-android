package io.getstream.chat.android.client.clientstate

import io.getstream.chat.android.client.models.User
import io.getstream.chat.android.core.internal.fsm.FiniteStateMachine

internal class UserStateService {
    fun onUserSet(user: User) {
        fsm.sendEvent(UserStateEvent.UserConnected(user))
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

    internal val state: UserState
        get() = fsm.state

    private val fsm = FiniteStateMachine<UserState, UserStateEvent> {
        initialState(UserState.NotSet)
        state<UserState.NotSet> {
            onEvent<UserStateEvent.ConnectUser> { _, event -> UserState.User.Pending(event.user) }
            onEvent<UserStateEvent.ConnectAnonymous> { _, _ -> UserState.Anonymous.Pending }
        }
        state<UserState.User.Pending> {
            onEvent<UserStateEvent.UserConnected> { _, event -> UserState.User.UserSet(event.user) }
            onEvent<UserStateEvent.UserSetFailed> { _, _ -> UserState.NotSet }
        }
        state<UserState.Anonymous.Pending> {
            onEvent<UserStateEvent.UserConnected> { _, event -> UserState.Anonymous.AnonymousUserSet(event.user) }
            onEvent<UserStateEvent.UserSetFailed> { _, _ -> UserState.NotSet }
        }
    }

    private sealed class UserStateEvent {
        class ConnectUser(val user: User) : UserStateEvent()
        class UserConnected(val user: User) : UserStateEvent()
        object ConnectAnonymous : UserStateEvent()
        object UserSetFailed : UserStateEvent()
        object UnsetUser : UserStateEvent()
    }
}
