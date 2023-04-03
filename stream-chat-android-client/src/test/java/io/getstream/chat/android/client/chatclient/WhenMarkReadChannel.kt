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

import io.getstream.chat.android.client.ChatClient
import io.getstream.chat.android.client.plugin.Plugin
import io.getstream.chat.android.client.plugin.listeners.ChannelMarkReadListener
import io.getstream.chat.android.test.asCall
import io.getstream.result.Error
import io.getstream.result.Result
import kotlinx.coroutines.test.runTest
import org.amshove.kluent.shouldBeInstanceOf
import org.junit.jupiter.api.Test
import org.mockito.kotlin.any
import org.mockito.kotlin.doReturn
import org.mockito.kotlin.mock
import org.mockito.kotlin.whenever

internal class WhenMarkReadChannel : BaseChatClientTest() {

    @Test
    fun `Given offline plugin with failing precondition Should not make API call and return error result`() = runTest {
        val plugin = mock<ChannelMarkReadListenerPlugin> {
            onBlocking { it.onChannelMarkReadPrecondition(any(), any()) } doReturn Result.Failure(Error.GenericError(message = ""))
        }
        val sut = Fixture().givenPlugin(plugin).get()

        val result = sut.markRead("channelType", "channelId").await()

        result shouldBeInstanceOf Result.Failure::class
    }

    @Test
    fun `Given offline plugin with success precondition Should invoke API call`() = runTest {
        val plugin = mock<ChannelMarkReadListenerPlugin> {
            onBlocking { it.onChannelMarkReadPrecondition(any(), any()) } doReturn Result.Success(Unit)
        }
        val sut = Fixture().givenPlugin(plugin).get()

        val result = sut.markRead("channelType", "channelId").await()

        result shouldBeInstanceOf Result.Success::class
    }

    private inner class Fixture {

        init {
            whenever(api.markRead(any(), any(), any())) doReturn mock<Unit>().asCall()
        }

        fun givenPlugin(plugin: Plugin) = apply {
            plugins.add(plugin)
        }

        fun get(): ChatClient = chatClient.apply {
            plugins = this@WhenMarkReadChannel.plugins
        }
    }
}

private interface ChannelMarkReadListenerPlugin : Plugin, ChannelMarkReadListener
