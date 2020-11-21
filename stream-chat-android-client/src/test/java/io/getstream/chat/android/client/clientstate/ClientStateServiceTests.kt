package io.getstream.chat.android.client.clientstate

import io.getstream.chat.android.client.Mother
import io.getstream.chat.android.client.models.User
import io.getstream.chat.android.test.randomString
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
    fun `Given Idle state When set user Should move to state user pending without token`() {
        val sut = Fixture().please()
        val user = Mother.randomUser()

        sut.onSetUser(user)

        sut.state shouldBeInstanceOf ClientState.User.Pending.WithoutToken::class
        (sut.state as ClientState.User.Pending.WithoutToken).user shouldBeEqualTo user
    }

    @Test
    fun `Given Idle state When set anonymous user Should move to state anonymous pending without token`() {
        val sut = Fixture().please()

        sut.onSetAnonymousUser()

        sut.state shouldBeEqualTo ClientState.Anonymous.Pending.WithoutToken
    }

    @Test
    fun `Given user pending without token state When token received Should move to state user pending with token`() {
        val sut = Fixture().givenUserAuthorizationPendingWithoutTokenState().please()

        sut.onTokenReceived("someToken")

        sut.state shouldBeInstanceOf ClientState.User.Pending.WithToken::class
        (sut.state as ClientState.User.Pending.WithToken).token shouldBeEqualTo "someToken"
    }

    @Test
    fun `Given user pending with token state When connected Should move to user connected state`() {
        val user = Mother.randomUser()
        val sut = Fixture().givenUserAuthorizationPendingWithTokenState("token").please()

        sut.onConnected(user, "connectionId")

        sut.state shouldBeInstanceOf ClientState.User.Authorized.Connected::class
        val connectedState = sut.state as ClientState.User.Authorized.Connected
        connectedState.token shouldBeEqualTo "token"
        connectedState.user shouldBeEqualTo user
        connectedState.connectionId shouldBeEqualTo "connectionId"
    }

    @Test
    fun `Given user connected state When disconnected Should move to disconnected state`() {
        val user = Mother.randomUser()
        val sut = Fixture().givenUserConnectedState(user, "token", "connectionId").please()

        sut.onDisconnected()

        sut.state shouldBeInstanceOf ClientState.User.Authorized.Disconnected::class
        val state = sut.state as ClientState.User.Authorized.Disconnected
        state.connectionId shouldBeEqualTo "connectionId"
        state.token shouldBeEqualTo "token"
        state.user shouldBeEqualTo user
    }

    @Test
    fun `Given user connected state When disconnect requested Should move to idle state`() {
        val user = Mother.randomUser()
        val sut = Fixture().givenUserConnectedState(user, "token", "connectionId").please()

        sut.onDisconnectRequested()

        sut.state shouldBeEqualTo ClientState.Idle
    }

    @Test
    fun `Given user connected When connect Should stay in connected state`() {
        val sut = Fixture().givenUserConnectedState().please()

        sut.onConnected(Mother.randomUser(), "connectionId")

        sut.state shouldBeInstanceOf ClientState.User.Authorized.Connected::class
    }

    @Test
    fun `Given user disconnected state When disconnected Should stay in the same state`() {
        val user = Mother.randomUser()
        val sut = Fixture().givenUserDisconnectedState(user, "token", "connectionId").please()
        val expectedState = sut.state

        sut.onDisconnected()

        sut.state shouldBeEqualTo expectedState
    }

    @Test
    fun `Given user disconnected state When disconnect requested Should move to state idle`() {
        val sut = Fixture().givenUserDisconnectedState().please()

        sut.onDisconnectRequested()

        sut.state shouldBeEqualTo ClientState.Idle
    }

    @Test
    fun `Given user disconnected state When connected Should move to state connected`() {
        val user = Mother.randomUser()
        val sut = Fixture().givenUserDisconnectedState().please()

        sut.onConnected(user, "someConnectionId")

        sut.state shouldBeInstanceOf ClientState.User.Authorized.Connected::class
        val connectedState = sut.state as ClientState.User.Authorized.Connected
        connectedState.user shouldBeEqualTo user
        connectedState.connectionId shouldBeEqualTo "someConnectionId"
    }

    @Test
    fun `Given anonymous pending without token state When token received Should move to state anonymous pending with token`() {
        val sut = Fixture().givenAnonymousPendingWithoutTokenState().please()

        sut.onTokenReceived("someToken")

        sut.state shouldBeInstanceOf ClientState.Anonymous.Pending.WithToken::class
        (sut.state as ClientState.Anonymous.Pending.WithToken).token shouldBeEqualTo "someToken"
    }

    @Test
    fun `Given anonymous pending with token state When connected Should move to state anonymous user connected`() {
        val user = Mother.randomUser()
        val sut = Fixture().givenAnonymousPendingWithTokenState("token").please()

        sut.onConnected(user, "connectionId")

        sut.state shouldBeInstanceOf ClientState.Anonymous.Authorized.Connected::class
        val connectedState = sut.state as ClientState.Anonymous.Authorized.Connected
        connectedState.token shouldBeEqualTo "token"
        connectedState.connectionId shouldBeEqualTo "connectionId"
        connectedState.anonymousUser shouldBeEqualTo user
    }

    @Test
    fun `Given anonymous user connected When disconnected Should move to state anonymous user disconnected`() {
        val user = Mother.randomUser()
        val sut = Fixture().givenAnonymousUserConnectedState(user, "token", "connectionId").please()

        sut.onDisconnected()

        sut.state shouldBeInstanceOf ClientState.Anonymous.Authorized.Disconnected::class
        val disconnectedState = sut.state as ClientState.Anonymous.Authorized.Disconnected
        disconnectedState.anonymousUser shouldBeEqualTo user
        disconnectedState.connectionId shouldBeEqualTo "connectionId"
        disconnectedState.token shouldBeEqualTo "token"
    }

    @Test
    fun `Given anonymous user connected When disconnect requested Should move to state idle`() {
        val sut = Fixture().givenAnonymousUserConnectedState().please()

        sut.onDisconnectRequested()

        sut.state shouldBeEqualTo ClientState.Idle
    }

    @Test
    fun `Given anonymous user connected When connect Should stay in connected state`() {
        val sut = Fixture().givenAnonymousUserConnectedState().please()

        sut.onConnected(Mother.randomUser(), "connectionId")

        sut.state shouldBeInstanceOf ClientState.Anonymous.Authorized.Connected::class
    }

    @Test
    fun `Given anonymous user disconnected state When disconnected Should stay in the same state`() {
        val sut = Fixture().givenAnonymousUserDisconnectedState().please()
        val expectedState = sut.state

        sut.onDisconnected()

        sut.state shouldBeEqualTo expectedState
    }

    @Test
    fun `Given anonymous user disconnected state When disconnect requested Should move to state idle`() {
        val sut = Fixture().givenAnonymousUserDisconnectedState().please()

        sut.onDisconnectRequested()

        sut.state shouldBeEqualTo ClientState.Idle
    }

    @Test
    fun `Given anonymous user disconnected state When connected requested Should move to state anonymous user connected`() {
        val user = Mother.randomUser()
        val sut = Fixture().givenAnonymousUserDisconnectedState(token = "token").please()

        sut.onConnected(user, "someConnectionId")

        sut.state shouldBeInstanceOf ClientState.Anonymous.Authorized.Connected::class
        val connectedState = sut.state as ClientState.Anonymous.Authorized.Connected
        connectedState.token shouldBeEqualTo "token"
        connectedState.connectionId shouldBeEqualTo "someConnectionId"
        connectedState.anonymousUser shouldBeEqualTo user
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

        fun givenUserConnectedState(
            user: User = Mother.randomUser(),
            token: String = randomString(),
            connectionId: String = randomString()
        ): Fixture {
            givenUserAuthorizationPendingWithTokenState(token)
            clientStateService.onConnected(user, connectionId)
            return this
        }

        fun givenUserDisconnectedState(
            user: User = Mother.randomUser(),
            token: String = randomString(),
            connectionId: String = randomString()
        ): Fixture {
            givenUserConnectedState(user, token, connectionId)
            clientStateService.onDisconnected()
            return this
        }

        fun givenAnonymousPendingWithoutTokenState(): Fixture {
            clientStateService.onSetAnonymousUser()
            return this
        }

        fun givenAnonymousPendingWithTokenState(token: String = randomString()): Fixture {
            givenAnonymousPendingWithoutTokenState()
            clientStateService.onTokenReceived(token)
            return this
        }

        fun givenAnonymousUserConnectedState(
            anonymousUser: User = Mother.randomUser(),
            token: String = randomString(),
            connectionId: String = randomString()
        ): Fixture {
            givenAnonymousPendingWithTokenState(token)
            clientStateService.onConnected(anonymousUser, connectionId)
            return this
        }

        fun givenAnonymousUserDisconnectedState(
            anonymousUser: User = Mother.randomUser(),
            token: String = randomString(),
            connectionId: String = randomString()
        ): Fixture {
            givenAnonymousUserConnectedState(anonymousUser, token, connectionId)
            clientStateService.onDisconnected()
            return this
        }
    }
}
