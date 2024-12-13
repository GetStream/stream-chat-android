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

package io.getstream.chat.android.client.channel

import android.annotation.SuppressLint
import io.getstream.chat.android.client.ChatClient
import io.getstream.chat.android.client.MockClientBuilder
import io.getstream.chat.android.test.TestCoroutineExtension
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.test.runTest
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.extension.RegisterExtension
import org.mockito.Mockito.mock
import org.mockito.Mockito.spy
import org.mockito.Mockito.verify

internal class ChannelPinTest {

    companion object {
        const val CHANNEL_TYPE = "type"
        const val CHANNEL_ID = "id"
        const val USER_ID = "jc"

        @JvmField
        @RegisterExtension
        val testCoroutines = TestCoroutineExtension()
    }

    @SuppressLint("CheckResult")
    @Test
    fun `When pin is called, it propagates the call to ChatClient`() {
        // Given
        val chatClient = mock<ChatClient>()
        val channelClient = ChannelClient(CHANNEL_TYPE, CHANNEL_ID, chatClient)

        // When
        channelClient.pin()

        // Then
        verify(chatClient).pinChannel(CHANNEL_TYPE, CHANNEL_ID)
    }

    @SuppressLint("CheckResult")
    @Test
    fun `When unpin is called, it propagates the call to ChatClient`() {
        // Given
        val chatClient = mock<ChatClient>()
        val channelClient = ChannelClient(CHANNEL_TYPE, CHANNEL_ID, chatClient)

        // When
        channelClient.unpin()

        // Then
        verify(chatClient).unpinChannel(CHANNEL_TYPE, CHANNEL_ID)
    }

    @OptIn(ExperimentalCoroutinesApi::class)
    @SuppressLint("CheckResult")
    @Test
    fun `When pinChannel is called it does partial update with pinned true`() {
        // Given
        val chatClient = MockClientBuilder(testCoroutineExtension = testCoroutines).build()
        val spyClient = spy(chatClient)
        // When
        spyClient.pinChannel(CHANNEL_TYPE, CHANNEL_ID)

        // Then
        verify(spyClient).partialUpdateMember(CHANNEL_TYPE, CHANNEL_ID, USER_ID, set = mapOf("pinned" to true))
    }

    @OptIn(ExperimentalCoroutinesApi::class)
    @SuppressLint("CheckResult")
    @Test
    fun `When unpinChannel is called it does partial update with pinned as unset option`() = runTest {
        // Given
        val chatClient = MockClientBuilder(testCoroutineExtension = testCoroutines).build()
        val spyClient = spy(chatClient)
        // When
        spyClient.unpinChannel(CHANNEL_TYPE, CHANNEL_ID)

        // Then
        verify(spyClient).partialUpdateMember(CHANNEL_TYPE, CHANNEL_ID, USER_ID, unset = listOf("pinned"))
    }
}
