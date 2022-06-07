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
import io.getstream.chat.android.client.models.User
import io.getstream.chat.android.core.internal.fsm.FiniteStateMachine

internal class UserStateService {
    private val logger = ChatLogger.get("UserStateService")

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
        defaultHandler { state, event ->
            logger.logE("Can't handle $event while being in state ${state::class.simpleName}")
            state
        }
        initialState(UserState.NotSet)
        state<UserState.NotSet> {
            onEvent<UserStateEvent.ConnectUser> { event -> UserState.UserSet(event.user) }
            onEvent<UserStateEvent.ConnectAnonymous> { UserState.Anonymous.Pending }
        }
        state<UserState.UserSet> {
            onEvent<UserStateEvent.UserUpdated> { event -> UserState.UserSet(event.user) }
            onEvent<UserStateEvent.UnsetUser> { UserState.NotSet }
        }
        state<UserState.Anonymous.Pending> {
            onEvent<UserStateEvent.UserUpdated> { event -> UserState.Anonymous.AnonymousUserSet(event.user) }
            onEvent<UserStateEvent.UnsetUser> { UserState.NotSet }
        }
        state<UserState.Anonymous.AnonymousUserSet> {
            onEvent<UserStateEvent.UserUpdated> { event -> UserState.Anonymous.AnonymousUserSet(event.user) }
            onEvent<UserStateEvent.UnsetUser> { UserState.NotSet }
        }
    }

    private sealed class UserStateEvent {
        class ConnectUser(val user: User) : UserStateEvent()
        class UserUpdated(val user: User) : UserStateEvent()
        object ConnectAnonymous : UserStateEvent()
        object UnsetUser : UserStateEvent()
    }
}
