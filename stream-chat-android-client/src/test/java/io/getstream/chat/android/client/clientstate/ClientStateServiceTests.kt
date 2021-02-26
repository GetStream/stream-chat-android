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
    fun `Given Idle state When disconnected requested Should stay in Idle state`() {
        val sut = Fixture().please()

        sut.onDisconnectRequested()

        sut.state shouldBeEqualTo ClientState.Idle
    }

    @Test
    fun `Given Idle state When connected Should stay in Idle state`() {
        val sut = Fixture().please()
        val user = Mother.randomUser()

        sut.onConnected(user, randomString())

        sut.state shouldBeEqualTo ClientState.Idle
    }

    @Test
    fun `Given Idle state When set user Should move to state user pending`() {
        val sut = Fixture().please()
        val user = Mother.randomUser()

        sut.onSetUser(user)

        sut.state shouldBeInstanceOf ClientState.User.Pending::class
        (sut.state as ClientState.User.Pending).user shouldBeEqualTo user
    }

    @Test
    fun `Given Idle state When set anonymous user Should move to state anonymous`() {
        val sut = Fixture().please()

        sut.onSetAnonymousUser()

        sut.state shouldBeEqualTo ClientState.Anonymous.Pending
    }

    @Test
    fun `Given user pending state When disconnected requested Should move to Idle state`() {
        val sut = Fixture().givenUserAuthorizationPendingState().please()

        sut.onDisconnectRequested()

        sut.state shouldBeEqualTo ClientState.Idle
    }

    @Test
    fun `Given user pending state When disconnected Should stay on the same state`() {
        val sut = Fixture().givenUserAuthorizationPendingState().please()
        val expectedState = sut.state

        sut.onDisconnected()

        sut.state shouldBeEqualTo expectedState
    }

    @Test
    fun `Given user pending state When connected Should move to user connected state`() {
        val user = Mother.randomUser()
        val sut = Fixture().givenUserAuthorizationPendingState().please()

        sut.onConnected(user, "connectionId")

        sut.state shouldBeInstanceOf ClientState.User.Authorized.Connected::class
        val connectedState = sut.state as ClientState.User.Authorized.Connected
        connectedState.user shouldBeEqualTo user
        connectedState.connectionId shouldBeEqualTo "connectionId"
    }

    @Test
    fun `Given user connected state When disconnected Should move to disconnected state`() {
        val user = Mother.randomUser()
        val sut = Fixture().givenUserConnectedState(user, "connectionId").please()

        sut.onDisconnected()

        sut.state shouldBeInstanceOf ClientState.User.Authorized.Disconnected::class
        val state = sut.state as ClientState.User.Authorized.Disconnected
        state.connectionId shouldBeEqualTo "connectionId"
        state.user shouldBeEqualTo user
    }

    @Test
    fun `Given user connected state When disconnect requested Should move to idle state`() {
        val user = Mother.randomUser()
        val sut = Fixture().givenUserConnectedState(user, "connectionId").please()

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
        val sut = Fixture().givenUserDisconnectedState(user, "connectionId").please()
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
    fun `Given anonymous pending state When disconnected requested Should move to Idle state`() {
        val sut = Fixture().givenAnonymousPendingState().please()

        sut.onDisconnectRequested()

        sut.state shouldBeEqualTo ClientState.Idle
    }

    @Test
    fun `Given anonymous pending state When disconnected Should stay on the same state`() {
        val sut = Fixture().givenAnonymousPendingState().please()
        val expectedState = sut.state

        sut.onDisconnected()

        sut.state shouldBeEqualTo expectedState
    }

    @Test
    fun `Given anonymous pending state When connected Should move to state anonymous user connected`() {
        val user = Mother.randomUser()
        val sut = Fixture().givenAnonymousPendingState().please()

        sut.onConnected(user, "connectionId")

        sut.state shouldBeInstanceOf ClientState.Anonymous.Authorized.Connected::class
        val connectedState = sut.state as ClientState.Anonymous.Authorized.Connected
        connectedState.connectionId shouldBeEqualTo "connectionId"
        connectedState.anonymousUser shouldBeEqualTo user
    }

    @Test
    fun `Given anonymous user connected When disconnected Should move to state anonymous user disconnected`() {
        val user = Mother.randomUser()
        val sut = Fixture().givenAnonymousUserConnectedState(user, "connectionId").please()

        sut.onDisconnected()

        sut.state shouldBeInstanceOf ClientState.Anonymous.Authorized.Disconnected::class
        val disconnectedState = sut.state as ClientState.Anonymous.Authorized.Disconnected
        disconnectedState.anonymousUser shouldBeEqualTo user
        disconnectedState.connectionId shouldBeEqualTo "connectionId"
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
        val sut = Fixture().givenAnonymousUserDisconnectedState().please()

        sut.onConnected(user, "someConnectionId")

        sut.state shouldBeInstanceOf ClientState.Anonymous.Authorized.Connected::class
        val connectedState = sut.state as ClientState.Anonymous.Authorized.Connected
        connectedState.connectionId shouldBeEqualTo "someConnectionId"
        connectedState.anonymousUser shouldBeEqualTo user
    }

    @Test
    fun `Given user pending state When disconnect requested Should move to idle state`() {
        val sut = Fixture().givenUserAuthorizationPendingState().please()

        sut.onDisconnectRequested()

        sut.state shouldBeInstanceOf ClientState.Idle::class
    }

    @Test
    fun `Given anonymous user pending state When disconnect requested Should move to idle state`() {
        val sut = Fixture().givenAnonymousPendingState().please()

        sut.onDisconnectRequested()

        sut.state shouldBeInstanceOf ClientState.Idle::class
    }

    private class Fixture {
        private val clientStateService = ClientStateService()

        fun please() = clientStateService

        fun givenUserAuthorizationPendingState(user: User = Mother.randomUser()): Fixture {
            clientStateService.onSetUser(user)
            return this
        }

        fun givenUserConnectedState(
            user: User = Mother.randomUser(),
            connectionId: String = randomString(),
        ): Fixture {
            givenUserAuthorizationPendingState()
            clientStateService.onConnected(user, connectionId)
            return this
        }

        fun givenUserDisconnectedState(
            user: User = Mother.randomUser(),
            connectionId: String = randomString(),
        ): Fixture {
            givenUserConnectedState(user, connectionId)
            clientStateService.onDisconnected()
            return this
        }

        fun givenAnonymousPendingState(): Fixture {
            clientStateService.onSetAnonymousUser()
            return this
        }

        fun givenAnonymousUserConnectedState(
            anonymousUser: User = Mother.randomUser(),
            connectionId: String = randomString(),
        ): Fixture {
            givenAnonymousPendingState()
            clientStateService.onConnected(anonymousUser, connectionId)
            return this
        }

        fun givenAnonymousUserDisconnectedState(
            anonymousUser: User = Mother.randomUser(),
            connectionId: String = randomString(),
        ): Fixture {
            givenAnonymousUserConnectedState(anonymousUser, connectionId)
            clientStateService.onDisconnected()
            return this
        }
    }
}
