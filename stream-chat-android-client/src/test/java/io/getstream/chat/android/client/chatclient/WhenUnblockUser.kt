/*
 * Copyright (c) 2014-2024 Stream.io Inc. All rights reserved.
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
import io.getstream.chat.android.client.clientstate.UserState
import io.getstream.chat.android.client.plugin.Plugin
import io.getstream.chat.android.models.User
import io.getstream.chat.android.randomUser
import io.getstream.chat.android.test.TestCall
import io.getstream.chat.android.test.callFrom
import io.getstream.result.Error
import io.getstream.result.Result
import io.getstream.result.call.Call
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.test.runTest
import org.amshove.kluent.shouldBeInstanceOf
import org.junit.jupiter.api.Test
import org.mockito.kotlin.any
import org.mockito.kotlin.doNothing
import org.mockito.kotlin.doReturn
import org.mockito.kotlin.mock
import org.mockito.kotlin.never
import org.mockito.kotlin.verify
import org.mockito.kotlin.whenever

internal class WhenUnblockUser : BaseChatClientTest() {

    @Test
    fun `Given unblockUser api call successful ChatClient should return success result and update local data`() =
        runTest {
            val apiResult = callFrom { }
            val user = randomUser(id = "user1", blockedUserIds = listOf("user2"))
            val plugin = mock<Plugin>().apply {
                doNothing().whenever(this).onUnblockUserResult(any(), any())
            }
            val sut = Fixture()
                .givenPlugin(plugin)
                .givenUserState(user)
                .givenClientState(user)
                .givenUnblockUserApiResult(apiResult)
                .get()

            val result = sut.unblockUser("user2").await()

            result shouldBeInstanceOf Result.Success::class
            verify(plugin).onUnblockUserResult("user2", result)
            val expectedUser = user.copy(blockedUserIds = emptyList())
            verify(userStateService).onUserUpdated(expectedUser)
            verify(mutableClientState).setUser(expectedUser)
        }

    @Test
    fun `Given unblockUser api call fails ChatClient should return error result`() = runTest {
        val apiResult = TestCall<Unit>(Result.Failure(Error.GenericError("Error")))
        val user = randomUser(id = "user1", blockedUserIds = listOf("user2"))
        val plugin = mock<Plugin>().apply {
            doNothing().whenever(this).onUnblockUserResult(any(), any())
        }
        val sut = Fixture()
            .givenPlugin(plugin)
            .givenUserState(user)
            .givenClientState(user)
            .givenUnblockUserApiResult(apiResult)
            .get()

        val result = sut.unblockUser("user2").await()

        result shouldBeInstanceOf Result.Failure::class
        verify(plugin).onUnblockUserResult("user2", result)
        verify(userStateService, never()).onUserUpdated(any())
        verify(mutableClientState, never()).setUser(any())
    }

    private inner class Fixture {

        fun givenPlugin(plugin: Plugin) = apply {
            plugins.add(plugin)
        }

        fun givenUnblockUserApiResult(result: Call<Unit>) = apply {
            whenever(api.unblockUser(any())) doReturn result
        }

        fun givenUserState(user: User) = apply {
            // userStateService = mock()
            whenever(userStateService.state) doReturn UserState.UserSet(user)
        }

        fun givenClientState(user: User) = apply {
            whenever(mutableClientState.user) doReturn MutableStateFlow(user)
        }

        fun get(): ChatClient = chatClient.apply { plugins = this@WhenUnblockUser.plugins }
    }
}
