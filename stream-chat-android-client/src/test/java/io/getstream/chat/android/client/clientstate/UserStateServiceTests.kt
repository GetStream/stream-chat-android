package io.getstream.chat.android.client.clientstate

import com.google.common.truth.Truth
import io.getstream.chat.android.client.Mother
import io.getstream.chat.android.client.models.User
import org.junit.jupiter.api.Test

internal class UserStateServiceTests {

    @Test
    fun `Given Idle state When set user Should move to user set state`() {
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

    /*@Test
    fun onLogout() {
    }*/

    /*@Test
    fun onSetAnonymous() {
    }*/
/*
    @Test
    fun onSocketUnrecoverableError() {
    }*/

    private class Fixture {
        private val userStateService = UserStateService()

        fun givenUserSetState(user: User = Mother.randomUser()) = apply { userStateService.onSetUser(user) }

        fun please() = userStateService
    }
}
