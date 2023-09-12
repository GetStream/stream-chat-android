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

import io.getstream.chat.android.test.TestCoroutineExtension
import io.getstream.chat.android.test.randomString
import kotlinx.coroutines.test.runTest
import org.amshove.kluent.shouldBe
import org.amshove.kluent.shouldBeEqualTo
import org.amshove.kluent.shouldBeInstanceOf
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.extension.RegisterExtension

internal class SocketStateServiceTests {

    companion object {
        @JvmField
        @RegisterExtension
        val testCoroutines = TestCoroutineExtension()
    }

    @Test
    fun `When initialized Should have Idle state`() = runTest {
        val sut = Fixture().please()

        sut.state shouldBeEqualTo SocketState.Idle
    }

    @Test
    fun `Given Idle state When disconnected Should stay in Idle state`() = runTest {
        val sut = Fixture().please()

        sut.onDisconnected()

        sut.state shouldBeEqualTo SocketState.Idle
    }

    @Test
    fun `Given Idle state When disconnected requested Should stay in Idle state`() = runTest {
        val sut = Fixture().please()

        sut.onDisconnectRequested()

        sut.state shouldBeEqualTo SocketState.Idle
    }

    @Test
    fun `Given Idle state When connected Should stay in Idle state`() = runTest {
        val sut = Fixture().please()

        sut.onConnected(randomString())

        sut.state shouldBeEqualTo SocketState.Idle
    }

    @Test
    fun `Given Idle state When connection requested Should move to state pending`() = runTest {
        val sut = Fixture().please()

        sut.onConnectionRequested()

        sut.state shouldBeInstanceOf SocketState.Pending::class
    }

    @Test
    fun `Given user pending state When disconnected requested Should move to Idle state`() = runTest {
        val sut = Fixture().givenConnectionPendingState().please()

        sut.onDisconnectRequested()

        sut.state shouldBeEqualTo SocketState.Idle
    }

    @Test
    fun `Given user pending state When disconnected Should stay on the same state`() = runTest {
        val sut = Fixture().givenConnectionPendingState().please()
        val expectedState = sut.state

        sut.onDisconnected()

        sut.state shouldBeEqualTo expectedState
    }

    @Test
    fun `Given user pending state When connected Should move to user connected state`() = runTest {
        val sut = Fixture().givenConnectionPendingState().please()

        sut.onConnected("connectionId")

        sut.state shouldBeInstanceOf SocketState.Connected::class
        val connectedState = sut.state as SocketState.Connected
        connectedState.connectionId shouldBeEqualTo "connectionId"
    }

    @Test
    fun `Given connected state When disconnected Should move to disconnected state`() = runTest {
        val sut = Fixture().givenConnectedState().please()

        sut.onDisconnected()

        sut.state shouldBeInstanceOf SocketState.Disconnected::class
    }

    @Test
    fun `Given connected state When disconnect requested Should move to idle state`() = runTest {
        val sut = Fixture().givenConnectedState().please()

        sut.onDisconnectRequested()

        sut.state shouldBeEqualTo SocketState.Idle
    }

    @Test
    fun `Given connected When connect Should stay in connected state`() = runTest {
        val sut = Fixture().givenConnectedState().please()

        sut.onConnected("connectionId")

        sut.state shouldBeInstanceOf SocketState.Connected::class
    }

    @Test
    fun `Given disconnected state When disconnected Should stay in the same state`() = runTest {
        val sut = Fixture().givenDisconnectedState().please()
        val expectedState = sut.state

        sut.onDisconnected()

        sut.state shouldBeEqualTo expectedState
    }

    @Test
    fun `Given disconnected state When disconnect requested Should move to state idle`() = runTest {
        val sut = Fixture().givenDisconnectedState().please()

        sut.onDisconnectRequested()

        sut.state shouldBeEqualTo SocketState.Idle
    }

    @Test
    fun `Given user disconnected state When connected Should move to state connected`() = runTest {
        val sut = Fixture().givenDisconnectedState().please()

        sut.onConnected("someConnectionId")

        sut.state shouldBeInstanceOf SocketState.Connected::class
        val connectedState = sut.state as SocketState.Connected
        connectedState.connectionId shouldBeEqualTo "someConnectionId"
    }

    @Test
    fun `Given connection pending state When disconnect requested Should move to idle state`() = runTest {
        val sut = Fixture().givenConnectionPendingState().please()

        sut.onDisconnectRequested()

        sut.state shouldBeInstanceOf SocketState.Idle::class
    }

    @Test
    fun `Given connected state When socket unrecoverable error occurs Should move to idle state`() = runTest {
        val sut = Fixture().givenConnectedState().please()

        sut.onSocketUnrecoverableError()

        sut.state shouldBe SocketState.Idle
    }

    @Test
    fun `Given connection pending state When socket unrecoverable error occurs Should move to idle state`() = runTest {
        val sut = Fixture().givenConnectionPendingState().please()

        sut.onSocketUnrecoverableError()

        sut.state shouldBe SocketState.Idle
    }

    @Test
    fun `Given disconnected state When socket unrecoverable error occurs Should move to idle state`() = runTest {
        val sut = Fixture().givenDisconnectedState().please()

        sut.onSocketUnrecoverableError()

        sut.state shouldBe SocketState.Idle
    }

    private class Fixture {
        private val clientStateService = SocketStateService()

        fun please() = clientStateService

        suspend fun givenConnectionPendingState(): Fixture {
            clientStateService.onConnectionRequested()
            return this
        }

        suspend fun givenConnectedState(connectionId: String = randomString()): Fixture {
            givenConnectionPendingState()
            clientStateService.onConnected(connectionId)
            return this
        }

        suspend fun givenDisconnectedState(): Fixture {
            givenConnectedState(randomString())
            clientStateService.onDisconnected()
            return this
        }
    }
}
