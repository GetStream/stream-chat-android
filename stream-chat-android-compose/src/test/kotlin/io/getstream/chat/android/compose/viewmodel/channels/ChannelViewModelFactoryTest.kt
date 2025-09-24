/*
 * Copyright (c) 2014-2025 Stream.io Inc. All rights reserved.
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

package io.getstream.chat.android.compose.viewmodel.channels

import androidx.lifecycle.ViewModel
import io.getstream.chat.android.client.ChatClient
import org.junit.Assert.assertEquals
import org.junit.Assert.assertThrows
import org.junit.Test
import org.junit.jupiter.api.assertInstanceOf
import org.mockito.kotlin.doReturn
import org.mockito.kotlin.mock

internal class ChannelViewModelFactoryTest {

    @Test
    fun `create should return correct instance`() {
        val sut = Fixture().get()

        val viewModel = sut.create(ChannelListViewModel::class.java)

        assertInstanceOf<ChannelListViewModel>(viewModel)
    }

    @Test
    fun `create should throw IllegalArgumentException for unsupported ViewModel class`() {
        val sut = Fixture().get()

        val exception = assertThrows(IllegalArgumentException::class.java) {
            sut.create(ViewModel::class.java)
        }

        assertEquals(
            "ChannelViewModelFactory can only create instances of ChannelListViewModel",
            exception.message,
        )
    }

    private class Fixture {
        private val mockChatClient = mock<ChatClient> {
            on { clientState } doReturn mock()
        }

        fun get(): ChannelViewModelFactory {
            return ChannelViewModelFactory(
                chatClient = mockChatClient,
            )
        }
    }
}
