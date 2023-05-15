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

package io.getstream.chat.android.client.chatclient

import io.getstream.chat.android.client.Mother
import io.getstream.chat.android.client.clientstate.SocketState
import io.getstream.chat.android.client.clientstate.UserState
import io.getstream.chat.android.client.models.User
import io.getstream.chat.android.test.randomString
import org.amshove.kluent.invoking
import org.amshove.kluent.`should throw`
import org.amshove.kluent.shouldThrow
import org.amshove.kluent.`with message`
import org.junit.jupiter.api.Disabled
import org.junit.jupiter.api.Test
import org.mockito.Mockito
import org.mockito.kotlin.clearInvocations
import org.mockito.kotlin.doReturn
import org.mockito.kotlin.verify
import org.mockito.kotlin.whenever

internal class WhenReconnectSocket : BaseChatClientTest() {

    @Test
    fun `Given idle connection state Should do nothing`() {
        val sut = Fixture().givenIdleConnectionState().clearSocketInvocations().get()

        invoking {
            sut.reconnectSocket()
        } `should throw` IllegalStateException::class `with message` "Invalid user state null without user being set!"

        Mockito.verifyNoInteractions(socket)
    }

    @Test
    fun `Given pending connection state Should do nothing`() {
        val sut = Fixture().givenPendingConnectionState().clearSocketInvocations().get()

        sut.reconnectSocket()

        Mockito.verifyNoInteractions(socket)
    }

    @Test
    fun `Given connected connection state Should do nothing`() {
        val sut = Fixture().givenConnectedConnectionState().clearSocketInvocations().get()

        sut.reconnectSocket()

        Mockito.verifyNoInteractions(socket)
    }

    @Test
    fun `Given disconnected connection state And User set state Should connect to socket`() {
        val user = Mother.randomUser()
        val sut = Fixture().givenDisconnectedConnectionState().givenUserSetState(user).get()

        sut.reconnectSocket()

        verify(socket).reconnectUser(user, isAnonymous = false, forceReconnection = true)
    }

    @Test
    fun `Given disconnected connection state And Anonymous user set state Should connect to socket anonymously`() {
        val user = Mother.randomUser()
        val sut = Fixture().givenDisconnectedConnectionState().givenAnonymousUserSetState(user).get()

        sut.reconnectSocket()

        verify(socket).reconnectUser(user, isAnonymous = true, forceReconnection = true)
    }

    @Disabled
    @Test
    fun `Given disconnected connection state And user not set state Should throw exception`() {
        val sut = Fixture().givenDisconnectedConnectionState().givenUserNotSetState().get()

        invoking { sut.reconnectSocket() }.shouldThrow(IllegalStateException::class)
    }

    inner class Fixture {
        fun givenIdleConnectionState() = apply {
            whenever(socketStateService.state) doReturn SocketState.Idle
        }

        fun givenPendingConnectionState() = apply {
            whenever(socketStateService.state) doReturn SocketState.Pending
        }

        fun givenConnectedConnectionState() = apply {
            whenever(socketStateService.state) doReturn SocketState.Connected(randomString())
        }

        fun givenDisconnectedConnectionState() = apply {
            whenever(socketStateService.state) doReturn SocketState.Disconnected
        }

        fun givenUserSetState(user: User) = apply {
            whenever(userStateService.state) doReturn UserState.UserSet(user)
        }

        fun givenAnonymousUserSetState() = apply {
            whenever(userStateService.state) doReturn UserState.AnonymousUserSet(Mother.randomUser())
        }

        fun givenAnonymousUserSetState(user: User) = apply {
            whenever(userStateService.state) doReturn UserState.AnonymousUserSet(user)
        }

        fun givenUserNotSetState() = apply {
            whenever(userStateService.state) doReturn UserState.NotSet
        }

        fun clearSocketInvocations() = apply {
            clearInvocations(socket)
        }

        fun get() = chatClient
    }
}
