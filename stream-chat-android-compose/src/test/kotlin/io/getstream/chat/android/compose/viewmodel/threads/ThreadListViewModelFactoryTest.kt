/*
 * Copyright (c) 2014-2026 Stream.io Inc. All rights reserved.
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

package io.getstream.chat.android.compose.viewmodel.threads

import androidx.lifecycle.ViewModel
import io.getstream.chat.android.client.ChatClient
import io.getstream.chat.android.client.api.models.QueryThreadsRequest
import kotlinx.coroutines.test.TestScope
import org.junit.Assert.assertEquals
import org.junit.Assert.assertThrows
import org.junit.Test
import org.junit.jupiter.api.assertInstanceOf
import org.mockito.kotlin.any
import org.mockito.kotlin.doReturn
import org.mockito.kotlin.mock

internal class ThreadListViewModelFactoryTest {

    @Test
    fun `create should return correct instance`() {
        val sut = Fixture().get()

        val viewModel = sut.create(ThreadListViewModel::class.java)

        assertInstanceOf<ThreadListViewModel>(viewModel)
    }

    @Test
    fun `create should throw IllegalArgumentException for unsupported ViewModel class`() {
        val sut = Fixture().get()

        val exception = assertThrows(IllegalArgumentException::class.java) {
            sut.create(ViewModel::class.java)
        }

        assertEquals(
            "ThreadsViewModelFactory can only create instances of ThreadListViewModel",
            exception.message,
        )
    }

    private class Fixture {

        private val mockChatClient: ChatClient = mock {
            on { clientState } doReturn mock()
            on { inheritScope(any()) } doReturn TestScope()
        }

        fun get() = ThreadsViewModelFactory(
            query = QueryThreadsRequest(),
            chatClient = mockChatClient,
        )
    }
}
