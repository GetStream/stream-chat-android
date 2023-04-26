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
import io.getstream.logging.StreamLog

internal class UserStateService {
    private val logger = StreamLog.getLogger("Chat:UserStateService")

    fun onUserUpdated(user: User) {
        logger.d { "[onUserUpdated] user id: ${user.id}" }
        fsm.sendEvent(UserStateEvent.UserUpdated(user))
    }

    fun onSetUser(user: User, isAnonymous: Boolean) {
        logger.d { "[onSetUser] user id: ${user.id}" }
        if (isAnonymous) {
            fsm.sendEvent(UserStateEvent.ConnectAnonymous(user))
        } else {
            fsm.sendEvent(UserStateEvent.ConnectUser(user))
        }
    }

    fun onLogout() {
        logger.d { "[onLogout]" }
        fsm.sendEvent(UserStateEvent.UnsetUser)
    }

    fun onSocketUnrecoverableError() {
        logger.d { "[onSocketUnrecoverableError]" }
        fsm.sendEvent(UserStateEvent.UnsetUser)
    }

    internal val state: UserState
        get() = fsm.state

    private val fsm = FiniteStateMachine<UserState, UserStateEvent> {
        defaultHandler { state, event ->
            logger.e { "Can't handle $event while being in state ${state::class.simpleName}" }
            state
        }
        initialState(UserState.NotSet)
        state<UserState.NotSet> {
            onEvent<UserStateEvent.ConnectUser> { event -> UserState.UserSet(event.user) }
            onEvent<UserStateEvent.ConnectAnonymous> { event -> UserState.AnonymousUserSet(event.user) }
            onEvent<UserStateEvent.UnsetUser> { state }
        }
        state<UserState.UserSet> {
            onEvent<UserStateEvent.UserUpdated> { event -> UserState.UserSet(event.user) }
            onEvent<UserStateEvent.UnsetUser> { UserState.NotSet }
        }
        state<UserState.AnonymousUserSet> {
            onEvent<UserStateEvent.UserUpdated> { event -> UserState.AnonymousUserSet(event.user) }
            onEvent<UserStateEvent.UnsetUser> { UserState.NotSet }
        }
    }

    private sealed class UserStateEvent {
        class ConnectUser(val user: User) : UserStateEvent()
        class UserUpdated(val user: User) : UserStateEvent()
        class ConnectAnonymous(val user: User) : UserStateEvent()
        object UnsetUser : UserStateEvent()
    }
}
