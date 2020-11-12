package io.getstream.chat.android.client.clientstate

import io.getstream.chat.android.client.Mother
import io.getstream.chat.android.client.models.User
import org.amshove.kluent.shouldBeEqualTo
import org.amshove.kluent.shouldBeInstanceOf
import org.junit.jupiter.api.Test

internal class ClientStateServiceTests {

    @Test
    fun `When initialized Should have Idle state`() {
        val sut = Fixture().please()

        sut.state shouldBeEqualTo ClientState.Idle
    }

    @Test
    fun `Given Idle state When disconnected Should stay in Idle state`() {
        val sut = Fixture().please()

        sut.onDisconnected()

        sut.state shouldBeEqualTo ClientState.Idle
    }

    @Test
    fun `Given Idle state When set user Should move to state UserAuthorizationPendingWithoutToken`() {
        val sut = Fixture().please()
        val user = Mother.randomUser()

        sut.onSetUser(user)

        sut.state shouldBeInstanceOf ClientState.UserState.AuthorizationPending.AuthorizationPendingWithoutToken::class
        (sut.state as ClientState.UserState.AuthorizationPending.AuthorizationPendingWithoutToken).user shouldBeEqualTo user
    }

    @Test
    fun `Given Idle state When set anonymous user Should move to state AnonymousPendingWithoutToken`() {
        val sut = Fixture().please()

        sut.onSetAnonymousUser()

        sut.state shouldBeEqualTo ClientState.AnonymousUserState.AnonymousUserPending.AnonymousPendingWithoutToken
    }

    @Test
    fun `Given user authorization pending without token state When token received Should move to state AuthorizationPendingWithToken`() {
        val sut = Fixture().givenUserAuthorizationPendingWithoutTokenState().please()

        sut.onTokenReceived("someToken")

        sut.state shouldBeInstanceOf ClientState.UserState.AuthorizationPending.AuthorizationPendingWithToken::class
        (sut.state as ClientState.UserState.AuthorizationPending.AuthorizationPendingWithToken).token shouldBeEqualTo "someToken"
    }

    @Test
    fun `Given authorization pending with token state When connected Should move to user connected state`() {
        val user = Mother.randomUser()
        val sut = Fixture().givenUserAuthorizationPendingWithTokenState("token").please()

        sut.onConnected(user, "connectionId")

        sut.state shouldBeInstanceOf ClientState.UserState.UserAuthorized.Connected::class
        val connectedState = sut.state as ClientState.UserState.UserAuthorized.Connected
        connectedState.token shouldBeEqualTo "token"
        connectedState.user shouldBeEqualTo user
        connectedState.connectionId shouldBeEqualTo "connectionId"
    }


    private class Fixture {
        private val clientStateService = ClientStateService()

        fun please() = clientStateService

        fun givenUserAuthorizationPendingWithoutTokenState(user: User = Mother.randomUser()): Fixture {
            clientStateService.onSetUser(user)
            return this
        }

        fun givenUserAuthorizationPendingWithTokenState(token: String): Fixture {
            givenUserAuthorizationPendingWithoutTokenState()
            clientStateService.onTokenReceived(token)
            return this
        }
    }
}