/*
 * Copyright (c) 2014-2026 Stream.io Inc. All rights reserved.
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

import io.getstream.chat.android.models.User
import io.getstream.chat.android.randomUser
import kotlinx.coroutines.test.runTest
import org.amshove.kluent.shouldBeEqualTo
import org.amshove.kluent.shouldBeInstanceOf
import org.junit.jupiter.api.Test

internal class UserStateServiceTests {

    @Test
    fun `Given user not set state When set user Should move to user set state`() = runTest {
        val user = randomUser()
        val sut = Fixture().please()

        sut.onSetUser(user, false)

        sut.state.shouldBeInstanceOf<UserState.UserSet>()
        sut.state.userOrError() shouldBeEqualTo user
    }

    @Test
    fun `Given user set state When user updated Should update value in state`() = runTest {
        val user1 = randomUser()
        val user2 = randomUser()
        val sut = Fixture().givenUserSetState(user1).please()

        sut.onUserUpdated(user2)

        sut.state.shouldBeInstanceOf<UserState.UserSet>()
        sut.state.userOrError() shouldBeEqualTo user2
    }

    @Test
    fun `Given user set state When logout Should move to user not set state`() = runTest {
        val sut = Fixture().givenUserSetState().please()

        sut.onLogout()

        sut.state shouldBeEqualTo UserState.NotSet
    }

    @Test
    fun `Given anonymous user pending state When logout Should move to user not set state`() = runTest {
        val sut = Fixture().givenAnonymousPendingState().please()

        sut.onLogout()

        sut.state shouldBeEqualTo UserState.NotSet
    }

    @Test
    fun `Given anonymous user set state When logout Should move to user not set state`() = runTest {
        val sut = Fixture().givenAnonymousUserState().please()

        sut.onLogout()

        sut.state shouldBeEqualTo UserState.NotSet
    }

    @Test
    fun `Given user not set state When set anonymous Should move to anonymous user state`() = runTest {
        val sut = Fixture().please()

        val anonymousUser = User(id = "!anon")
        sut.onSetUser(anonymousUser, true)

        sut.state.shouldBeInstanceOf<UserState.AnonymousUserSet>()
        sut.state.userOrError() shouldBeEqualTo anonymousUser
    }

    @Test
    fun `Given user set state When socket unrecoverable error occurs Should move to user not set state`() = runTest {
        val sut = Fixture().givenUserSetState().please()

        sut.onSocketUnrecoverableError()

        sut.state shouldBeEqualTo UserState.NotSet
    }

    @Test
    fun `Given anonymous user set state When socket unrecoverable error occurs Should move to user not set state`() = runTest {
        val sut = Fixture().givenAnonymousUserState().please()

        sut.onSocketUnrecoverableError()

        sut.state shouldBeEqualTo UserState.NotSet
    }

    @Test
    fun `Given anonymous user pending state When socket unrecoverable error occurs Should move to user not set state`() = runTest {
        val sut = Fixture().givenAnonymousPendingState().please()

        sut.onSocketUnrecoverableError()

        sut.state shouldBeEqualTo UserState.NotSet
    }

    @Test
    fun `Given user not set state When logout Should stay`() = runTest {
        val sut = Fixture().please()

        sut.onLogout()

        sut.state shouldBeEqualTo UserState.NotSet
    }

    @Test
    fun `Given anonymous user state User should be able to be updated`() = runTest {
        val user = randomUser()
        val sut = Fixture().givenAnonymousUserState(user).please()

        sut.onUserUpdated(user)

        sut.state.shouldBeInstanceOf<UserState.AnonymousUserSet>()
    }

    private class Fixture {
        private val userStateService = UserStateService()

        suspend fun givenUserSetState(user: User = randomUser()) = apply {
            userStateService.onSetUser(user, false)
        }

        suspend fun givenAnonymousPendingState() = apply { userStateService.onSetUser(User(id = "!anon"), true) }

        suspend fun givenAnonymousUserState(user: User = randomUser()) = apply {
            givenAnonymousPendingState()
            userStateService.onUserUpdated(user)
        }

        fun please() = userStateService
    }
}
