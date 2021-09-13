package io.getstream.chat.android.client.clientstate

import io.getstream.chat.android.client.Mother
import io.getstream.chat.android.client.models.User
import org.amshove.kluent.shouldBeEqualTo
import org.amshove.kluent.shouldBeInstanceOf
import org.junit.jupiter.api.Test

internal class UserStateServiceTests {

    @Test
    fun `Given user not set state When set user Should move to user set state`() {
        val user = Mother.randomUser()
        val sut = Fixture().please()

        sut.onSetUser(user)

        sut.state.shouldBeInstanceOf<UserState.UserSet>()
        sut.state.userOrError() shouldBeEqualTo user
    }

    @Test
    fun `Given user set state When user updated Should update value in state`() {
        val user1 = Mother.randomUser()
        val user2 = Mother.randomUser()
        val sut = Fixture().givenUserSetState(user1).please()

        sut.onUserUpdated(user2)

        sut.state.shouldBeInstanceOf<UserState.UserSet>()
        sut.state.userOrError() shouldBeEqualTo user2
    }

    @Test
    fun `Given user set state When logout Should move to user not set state`() {
        val sut = Fixture().givenUserSetState().please()

        sut.onLogout()

        sut.state shouldBeEqualTo UserState.NotSet
    }

    @Test
    fun `Given anonymous user pending state When logout Should move to user not set state`() {
        val sut = Fixture().givenAnonymousPendingState().please()

        sut.onLogout()

        sut.state shouldBeEqualTo UserState.NotSet
    }

    @Test
    fun `Given anonymous user set state When logout Should move to user not set state`() {
        val sut = Fixture().givenAnonymousUserState().please()

        sut.onLogout()

        sut.state shouldBeEqualTo UserState.NotSet
    }

    @Test
    fun `Given user not set state When set anonymous Should move to anonymous pending state`() {
        val sut = Fixture().please()

        sut.onSetAnonymous()

        sut.state shouldBeEqualTo UserState.Anonymous.Pending
    }

    @Test
    fun `Given user set state When socket unrecoverable error occurs Should move to user not set state`() {
        val sut = Fixture().givenUserSetState().please()

        sut.onSocketUnrecoverableError()

        sut.state shouldBeEqualTo UserState.NotSet
    }

    @Test
    fun `Given anonymous user set state When socket unrecoverable error occurs Should move to user not set state`() {
        val sut = Fixture().givenAnonymousUserState().please()

        sut.onSocketUnrecoverableError()

        sut.state shouldBeEqualTo UserState.NotSet
    }

    @Test
    fun `Given anonymous user pending state When socket unrecoverable error occurs Should move to user not set state`() {
        val sut = Fixture().givenAnonymousPendingState().please()

        sut.onSocketUnrecoverableError()

        sut.state shouldBeEqualTo UserState.NotSet
    }

    @Test
    fun `Given user not set state When logout Should stay`() {
        val sut = Fixture().please()

        sut.onLogout()

        sut.state shouldBeEqualTo UserState.NotSet
    }

    @Test
    fun `Given anonymous user state User should be able to be updated`() {
        val user = Mother.randomUser()
        val sut = Fixture().givenAnonymousUserState(user).please()

        sut.onUserUpdated(user)

        sut.state.shouldBeInstanceOf<UserState.Anonymous.AnonymousUserSet>()
    }

    private class Fixture {
        private val userStateService = UserStateService()

        fun givenUserSetState(user: User = Mother.randomUser()) = apply { userStateService.onSetUser(user) }

        fun givenAnonymousPendingState() = apply { userStateService.onSetAnonymous() }

        fun givenAnonymousUserState(user: User = Mother.randomUser()) = apply {
            givenAnonymousPendingState()
            userStateService.onUserUpdated(user)
        }

        fun please() = userStateService
    }
}
