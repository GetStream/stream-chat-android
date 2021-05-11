package io.getstream.chat.android.client.clientstate

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

        sut.onConnected(randomString())

        sut.state shouldBeEqualTo ClientState.Idle
    }

    @Test
    fun `Given Idle state When connection requested Should move to state pending`() {
        val sut = Fixture().please()

        sut.onConnectionRequested()

        sut.state shouldBeInstanceOf ClientState.Pending::class
    }

    @Test
    fun `Given user pending state When disconnected requested Should move to Idle state`() {
        val sut = Fixture().givenConnectionPendingState().please()

        sut.onDisconnectRequested()

        sut.state shouldBeEqualTo ClientState.Idle
    }

    @Test
    fun `Given user pending state When disconnected Should stay on the same state`() {
        val sut = Fixture().givenConnectionPendingState().please()
        val expectedState = sut.state

        sut.onDisconnected()

        sut.state shouldBeEqualTo expectedState
    }

    @Test
    fun `Given user pending state When connected Should move to user connected state`() {
        val sut = Fixture().givenConnectionPendingState().please()

        sut.onConnected("connectionId")

        sut.state shouldBeInstanceOf ClientState.Connected::class
        val connectedState = sut.state as ClientState.Connected
        connectedState.connectionId shouldBeEqualTo "connectionId"
    }

    @Test
    fun `Given connected state When disconnected Should move to disconnected state`() {
        val sut = Fixture().givenConnectedState().please()

        sut.onDisconnected()

        sut.state shouldBeInstanceOf ClientState.Disconnected::class
    }

    @Test
    fun `Given connected state When disconnect requested Should move to idle state`() {
        val sut = Fixture().givenConnectedState().please()

        sut.onDisconnectRequested()

        sut.state shouldBeEqualTo ClientState.Idle
    }

    @Test
    fun `Given connected When connect Should stay in connected state`() {
        val sut = Fixture().givenConnectedState().please()

        sut.onConnected("connectionId")

        sut.state shouldBeInstanceOf ClientState.Connected::class
    }

    @Test
    fun `Given disconnected state When disconnected Should stay in the same state`() {
        val sut = Fixture().givenDisconnectedState().please()
        val expectedState = sut.state

        sut.onDisconnected()

        sut.state shouldBeEqualTo expectedState
    }

    @Test
    fun `Given disconnected state When disconnect requested Should move to state idle`() {
        val sut = Fixture().givenDisconnectedState().please()

        sut.onDisconnectRequested()

        sut.state shouldBeEqualTo ClientState.Idle
    }

    @Test
    fun `Given user disconnected state When connected Should move to state connected`() {
        val sut = Fixture().givenDisconnectedState().please()

        sut.onConnected("someConnectionId")

        sut.state shouldBeInstanceOf ClientState.Connected::class
        val connectedState = sut.state as ClientState.Connected
        connectedState.connectionId shouldBeEqualTo "someConnectionId"
    }

    @Test
    fun `Given connection pending state When disconnect requested Should move to idle state`() {
        val sut = Fixture().givenConnectionPendingState().please()

        sut.onDisconnectRequested()

        sut.state shouldBeInstanceOf ClientState.Idle::class
    }

    private class Fixture {
        private val clientStateService = ClientStateService()

        fun please() = clientStateService

        fun givenConnectionPendingState(): Fixture {
            clientStateService.onConnectionRequested()
            return this
        }

        fun givenConnectedState(connectionId: String = randomString()): Fixture {
            givenConnectionPendingState()
            clientStateService.onConnected(connectionId)
            return this
        }

        fun givenDisconnectedState(): Fixture {
            givenConnectedState(randomString())
            clientStateService.onDisconnected()
            return this
        }
    }
}
