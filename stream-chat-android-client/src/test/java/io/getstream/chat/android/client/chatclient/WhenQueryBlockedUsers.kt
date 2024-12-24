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
import io.getstream.chat.android.client.plugin.Plugin
import io.getstream.chat.android.models.UserBlock
import io.getstream.chat.android.test.TestCall
import io.getstream.chat.android.test.callFrom
import io.getstream.result.Error
import io.getstream.result.Result
import io.getstream.result.call.Call
import org.amshove.kluent.shouldBeInstanceOf
import org.junit.jupiter.api.Test
import org.mockito.kotlin.any
import org.mockito.kotlin.doNothing
import org.mockito.kotlin.doReturn
import org.mockito.kotlin.mock
import org.mockito.kotlin.verify
import org.mockito.kotlin.whenever

internal class WhenQueryBlockedUsers : BaseChatClientTest() {

    @Test
    fun `Given queryBlockedUsers api call successful ChatClient should return success result`() {
        val apiResult = callFrom { emptyList<UserBlock>() }
        val plugin = mock<Plugin>().apply {
            doNothing().whenever(this).onQueryBlockedUsersResult(any())
        }
        val sut = Fixture()
            .givenPlugin(plugin)
            .givenQueryBlockedUsersUserApiResult(apiResult)
            .get()

        val result = sut.queryBlockedUsers().execute()

        result shouldBeInstanceOf Result.Success::class
        verify(plugin).onQueryBlockedUsersResult(result)
    }

    @Test
    fun `Given queryBlockedUsers api call fails ChatClient should return error result`() {
        val apiResult = TestCall<List<UserBlock>>(Result.Failure(Error.GenericError("error")))
        val plugin = mock<Plugin>().apply {
            doNothing().whenever(this).onQueryBlockedUsersResult(any())
        }
        val sut = Fixture()
            .givenPlugin(plugin)
            .givenQueryBlockedUsersUserApiResult(apiResult)
            .get()

        val result = sut.queryBlockedUsers().execute()

        result shouldBeInstanceOf Result.Failure::class
        verify(plugin).onQueryBlockedUsersResult(result)
    }

    private inner class Fixture {

        fun givenPlugin(plugin: Plugin) = apply {
            plugins.add(plugin)
        }

        fun givenQueryBlockedUsersUserApiResult(result: Call<List<UserBlock>>) = apply {
            whenever(api.queryBlockedUsers()) doReturn result
        }

        fun get(): ChatClient = chatClient.apply { plugins = this@WhenQueryBlockedUsers.plugins }
    }
}
