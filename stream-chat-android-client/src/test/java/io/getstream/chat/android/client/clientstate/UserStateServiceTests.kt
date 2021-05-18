package io.getstream.chat.android.client.clientstate

import com.google.common.truth.Truth
import io.getstream.chat.android.client.Mother
import io.getstream.chat.android.client.models.User
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.assertThrows
import java.lang.IllegalStateException

internal class UserStateServiceTests {

    @Test
    fun `Given user not set state When set user Should move to user set state`() {
        val user = Mother.randomUser()
        val sut = Fixture().please()

        sut.onSetUser(user)

        Truth.assertThat(sut.state).isInstanceOf(UserState.UserSet::class.java)
        Truth.assertThat(sut.state.userOrError()).isEqualTo(user)
    }

    @Test
    fun `Given user set state When user updated Should update value in state`() {
        val user1 = Mother.randomUser()
        val user2 = Mother.randomUser()
        val sut = Fixture().givenUserSetState(user1).please()

        sut.onUserUpdated(user2)

        Truth.assertThat(sut.state).isInstanceOf(UserState.UserSet::class.java)
        Truth.assertThat(sut.state.userOrError()).isEqualTo(user2)
    }

    @Test
    fun `Given Idle state When logout Should throw an exception`() {
        val sut = Fixture().please()

        assertThrows<IllegalStateException> { sut.onLogout() }
    }

    @Test
    fun `Given user set state When logout Should move to user not set state`() {
        val sut = Fixture().givenUserSetState().please()

        sut.onLogout()

        Truth.assertThat(sut.state).isEqualTo(UserState.NotSet)
    }

    @Test
    fun `Given anonymous user pending state When logout Should move to user not set state`() {
        val sut = Fixture().givenAnonymousPendingState().please()

        sut.onLogout()

        Truth.assertThat(sut.state).isEqualTo(UserState.NotSet)
    }

    @Test
    fun `Given anonymous user set state When logout Should move to user not set state`() {
        val sut = Fixture().givenAnonymousUserState().please()

        sut.onLogout()

        Truth.assertThat(sut.state).isEqualTo(UserState.NotSet)
    }

    @Test
    fun `Given user not set state When set anonymous Should move to anonymous pending state`() {
        val sut = Fixture().please()

        sut.onSetAnonymous()

        Truth.assertThat(sut.state).isEqualTo(UserState.Anonymous.Pending)
    }

    @Test
    fun `Given user set state When socket unrecoverable error occurs Should move to user not set state`() {
        val sut = Fixture().givenUserSetState().please()

        sut.onSocketUnrecoverableError()

        Truth.assertThat(sut.state).isEqualTo(UserState.NotSet)
    }

    @Test
    fun `Given anonymous user set state When socket unrecoverable error occurs Should move to user not set state`() {
        val sut = Fixture().givenAnonymousUserState().please()

        sut.onSocketUnrecoverableError()

        Truth.assertThat(sut.state).isEqualTo(UserState.NotSet)
    }

    @Test
    fun `Given anonymous user pending state When socket unrecoverable error occurs Should move to user not set state`() {
        val sut = Fixture().givenAnonymousPendingState().please()

        sut.onSocketUnrecoverableError()

        Truth.assertThat(sut.state).isEqualTo(UserState.NotSet)
    }

    @Test
    fun `Given user not set state When logout Should stay`() {
        val sut = Fixture().please()

        sut.onLogout()

        Truth.assertThat(sut.state).isEqualTo(UserState.NotSet)
    }

    private class Fixture {
        private val userStateService = UserStateService()

        fun givenUserSetState(user: User = Mother.randomUser()) = apply { userStateService.onSetUser(user) }

        fun givenAnonymousPendingState() = apply { userStateService.onSetAnonymous() }

        fun givenAnonymousUserState() = apply {
            givenAnonymousPendingState()
            userStateService.onUserUpdated(Mother.randomUser())
        }

        fun please() = userStateService
    }
}
