/*
 * Copyright (c) 2014-2023 Stream.io Inc. All rights reserved.
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

import io.getstream.chat.android.client.ChatClient
import io.getstream.chat.android.client.Mother
import io.getstream.chat.android.client.clientstate.SocketState
import io.getstream.chat.android.client.clientstate.UserState
import io.getstream.chat.android.client.models.Channel
import io.getstream.chat.android.client.models.User
import io.getstream.chat.android.client.plugin.Plugin
import io.getstream.chat.android.client.plugin.listeners.FetchCurrentUserListener
import io.getstream.chat.android.client.test.randomUser
import io.getstream.chat.android.client.utils.Result
import io.getstream.chat.android.test.asCall
import io.getstream.chat.android.test.randomString
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.test.runTest
import org.amshove.kluent.`should be`
import org.amshove.kluent.`should be equal to`
import org.junit.jupiter.api.Test
import org.mockito.kotlin.any
import org.mockito.kotlin.doReturn
import org.mockito.kotlin.mock
import org.mockito.kotlin.verify
import org.mockito.kotlin.verifyNoInteractions
import org.mockito.kotlin.whenever

@OptIn(ExperimentalCoroutinesApi::class)
internal class WhenFetchCurrentUser : BaseChatClientTest() {

    @Test
    fun `When user is not set`() = runTest {
        /* Given */
        val plugin = mock<FetchCurrentUserListenerPlugin> {
            onBlocking { it.onFetchCurrentUserResult(any()) } doReturn Unit
        }
        val sut = Fixture().givenPlugin(plugin)
            .givenUserNotSetState()
            .get()

        /* When */
        val result = sut.fetchCurrentUser().await()

        /* Then */
        result.isError `should be` true
        result.error().message `should be equal to` "User is not set, can't fetch current user"
        verifyNoInteractions(currentUserFetcher)
        verify(plugin).onFetchCurrentUserResult(any())
    }

    @Test
    fun `When socket is connected`() = runTest {
        /* Given */
        val plugin = mock<FetchCurrentUserListenerPlugin> {
            onBlocking { it.onFetchCurrentUserResult(any()) } doReturn Unit
        }
        val sut = Fixture().givenPlugin(plugin)
            .givenConnectedConnectionState()
            .get()

        /* When */
        val result = sut.fetchCurrentUser().await()

        /* Then */
        result.isError `should be` true
        result.error().message `should be equal to` "Socket is connected, can't fetch current user"
        verifyNoInteractions(currentUserFetcher)
        verify(plugin).onFetchCurrentUserResult(any())
    }

    @Test
    fun `When socket is disconnected and user is set`() = runTest {
        val plugin = mock<FetchCurrentUserListenerPlugin> {
            onBlocking { it.onFetchCurrentUserResult(any()) } doReturn Unit
        }
        val user = randomUser()
        val sut = Fixture().givenPlugin(plugin)
            .givenDisconnectedConnectionState()
            .givenUserSetState(user)
            .givenFetchedCurrentUser(user)
            .get()

        val result = sut.fetchCurrentUser().await()

        result.isSuccess `should be` true
        result.data() `should be equal to` user
        verify(currentUserFetcher).fetch()
        verify(plugin).onFetchCurrentUserResult(any())
    }

    private inner class Fixture {

        init {
            whenever(socketStateService.state) doReturn SocketState.Connected(connectionId = "connectionId")
            whenever(api.queryChannel(any(), any(), any())) doReturn mock<Channel>().asCall()
        }

        fun givenPlugin(plugin: Plugin) = apply {
            plugins.add(plugin)
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

        suspend fun givenFetchedCurrentUser(user: User) = apply {
            whenever(currentUserFetcher.fetch()) doReturn Result.success(user)
        }

        fun givenUserNotSetState() = apply {
            whenever(userStateService.state) doReturn UserState.NotSet
        }

        fun get(): ChatClient = chatClient.apply {
            plugins = this@WhenFetchCurrentUser.plugins
        }
    }
}

private interface FetchCurrentUserListenerPlugin : Plugin, FetchCurrentUserListener
