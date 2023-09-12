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
import io.getstream.chat.android.client.call.Call
import io.getstream.chat.android.client.clientstate.SocketState
import io.getstream.chat.android.client.clientstate.UserState
import io.getstream.chat.android.client.errors.ChatError
import io.getstream.chat.android.client.models.ConnectionData
import io.getstream.chat.android.client.models.User
import io.getstream.chat.android.client.plugin.factory.PluginFactory
import io.getstream.chat.android.client.token.TokenProvider
import io.getstream.chat.android.client.utils.Result
import io.getstream.chat.android.test.randomString
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.test.runTest
import org.amshove.kluent.`should be equal to`
import org.junit.jupiter.api.Test
import org.mockito.kotlin.any
import org.mockito.kotlin.argThat
import org.mockito.kotlin.clearInvocations
import org.mockito.kotlin.doReturn
import org.mockito.kotlin.mock
import org.mockito.kotlin.never
import org.mockito.kotlin.times
import org.mockito.kotlin.verify
import org.mockito.kotlin.verifyNoInteractions
import org.mockito.kotlin.verifyNoMoreInteractions
import org.mockito.kotlin.whenever

internal class WhenConnectUser : BaseChatClientTest() {

    @Test
    fun `Given user set and socket in idle state and user with the same id Should return an error`() = runTest {
        val pluginFactory: PluginFactory = mock()
        val user = Mother.randomUser { id = "userId" }
        val sut = Fixture()
            .givenUserAndToken(user, "token")
            .givenIdleConnectionState()
            .givenUserSetState(Mother.randomUser { id = "userId" })
            .givenPluginFactory(pluginFactory)
            .get()

        val result = sut.connectUser(user, "token").await()

        verify(userStateService, times(3)).state
        verify(userStateService).onLogout()
        verify(socket).disconnect()
        verifyNoMoreInteractions(socket)
        verifyNoMoreInteractions(userStateService)
        verifyNoInteractions(tokenManager)
        verifyNoInteractions(pluginFactory)
        result `should be equal to` Result.error(ChatError("Failed to connect user. Please check you haven't connected a user already."))
    }

    @Test
    fun `Given user set and socket in idle state and user with different id Should Not connect to the socket, update the user and token provider`() = runTest {
        val sut = Fixture()
            .givenUserAndToken(Mother.randomUser { id = "differentUserId" }, "token")
            .givenIdleConnectionState()
            .givenUserSetState(Mother.randomUser { id = "userId" })
            .get()

        sut.connectUser(Mother.randomUser { id = "differentUserId" }, "token").enqueue()

        verify(socket, never()).connectUser(any(), any())
        verify(userStateService, never()).onUserUpdated(any())
        verify(tokenManager, never()).setTokenProvider(any())
    }

    @Test
    fun `Given user not set Should set the user`() = runTest {
        val user = Mother.randomUser { id = "userId" }
        val sut = Fixture()
            .givenUserAndToken(user, "token")
            .givenUserNotSetState()
            .get()

        sut.connectUser(user, "token").enqueue()

        verify(userStateService).onSetUser(user, false)
    }

    @Test
    fun `Given user not set Should update token provider`() {
        val tokenProviderMock = mock<TokenProvider>()
        whenever(tokenProviderMock.loadToken()) doReturn "token"
        val sut = Fixture()
            .givenUserAndToken(Mother.randomUser { id = "userId" }, "token")
            .givenUserNotSetState()
            .get()

        sut.connectUser(Mother.randomUser { id = "userId" }, tokenProviderMock).enqueue()

        verify(tokenManager).setTokenProvider(any())
    }

    @Test
    fun `Given user not set Should connect to the socket`() {
        val user = Mother.randomUser { id = "userId" }
        val sut = Fixture()
            .givenUserAndToken(user, "token")
            .givenUserNotSetState()
            .get()

        sut.connectUser(user, "token").enqueue()

        verify(socket).connectUser(user, isAnonymous = false)
    }

    @Test
    fun `Given user not set and config with warmup Should do warmup`() {
        val sut = Fixture()
            .givenUserAndToken(Mother.randomUser { id = "userId" }, "token")
            .givenUserNotSetState()
            .givenWarmUpEnabled()
            .get()

        sut.connectUser(Mother.randomUser { id = "userId" }, "token").enqueue()

        verify(api).warmUp()
    }

    @Test
    fun `Given user not set Should invoke pre set listeners`() {
        val pluginFactory: PluginFactory = mock()
        val sut = Fixture()
            .givenUserAndToken(Mother.randomUser { id = "userId" }, "token")
            .givenUserNotSetState()
            .givenPluginFactory(pluginFactory)
            .get()

        sut.connectUser(Mother.randomUser { id = "userId" }, "token").enqueue()

        verify(pluginFactory).get(argThat { id == "userId" })
    }

    @Test
    fun `Given user set and user with different id Should call init connection listener with error`() = runTest {
        val connectionDataCallback: Call.Callback<ConnectionData> = mock()
        val sut = Fixture()
            .givenUserAndToken(Mother.randomUser { id = "userId" }, "token")
            .givenUserSetState(Mother.randomUser { id = "userId1" })
            .get()

        sut.connectUser(Mother.randomUser { id = "userId2" }, "token").enqueue(connectionDataCallback)
        delay(100L)
        verify(connectionDataCallback).onResult(argThat { isError })
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

        fun givenUserNotSetState() = apply {
            whenever(userStateService.state) doReturn UserState.NotSet
        }

        fun givenWarmUpEnabled() = apply {
            whenever(config.warmUp) doReturn true
        }

        fun givenPluginFactory(pluginFactory: PluginFactory) = apply {
            pluginFactories.clear()
            pluginFactories.add(pluginFactory)
        }

        fun givenUserAndToken(user: User, token: String) = apply {
            whenever(tokenUtils.getUserId(token)) doReturn user.id
            whenever(clientState.user) doReturn MutableStateFlow(user)
        }

        fun clearSocketInvocations() = apply {
            clearInvocations(socket)
        }

        fun get() = chatClient
    }
}
