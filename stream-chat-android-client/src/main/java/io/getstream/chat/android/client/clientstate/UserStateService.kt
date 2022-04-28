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

import io.getstream.chat.android.client.models.User
import io.getstream.chat.android.core.internal.fsm.FiniteStateMachine

/**
 * @startuml
 * title UserState FSM
 *
 * state Anonymous {
 *  state Pending
 *  state "UserSet" as AnonymousUserSet
 * }
 *
 * NotSet --> UserSet : ConnectUser
 * NotSet --> Pending : ConnectAnonymous
 * NotSet -left-> NotSet : UnsetUser
 * NotSet --> NotSet : UserUpdated(user)
 *
 * UserSet --> UserSet : UserUpdated(user)
 * UserSet --> NotSet : UnsetUser
 *
 * Pending --> AnonymousUserSet : UserUpdated(user)
 * Pending --> NotSet : UnsetUser
 *
 * AnonymousUserSet --> AnonymousUserSet : UserUpdated(user)
 * AnonymousUserSet --> NotSet : UnsetUser
 *
 * [*] --> NotSet
 * @enduml
 */
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
            onEvent<UserStateEvent.UserUpdated> { _, _ -> stay() }
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
