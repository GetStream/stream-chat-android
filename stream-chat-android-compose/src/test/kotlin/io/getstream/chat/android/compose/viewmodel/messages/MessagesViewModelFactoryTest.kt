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

package io.getstream.chat.android.compose.viewmodel.messages

import android.content.ClipboardManager
import android.content.Context
import androidx.lifecycle.ViewModel
import io.getstream.chat.android.client.ChatClient
import io.getstream.chat.android.client.setup.state.ClientState
import io.getstream.chat.android.models.InitializationState
import io.getstream.chat.android.randomCID
import io.getstream.chat.android.randomUser
import io.getstream.chat.android.test.TestCoroutineRule
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.test.TestScope
import org.junit.Assert.assertEquals
import org.junit.Assert.assertThrows
import org.junit.Rule
import org.junit.Test
import org.junit.jupiter.api.assertInstanceOf
import org.mockito.kotlin.any
import org.mockito.kotlin.doReturn
import org.mockito.kotlin.mock

internal class MessagesViewModelFactoryTest {

    @get:Rule
    val testCoroutineRule = TestCoroutineRule()

    @Test
    fun `create should return correct MessageComposerViewModel instance`() {
        val sut = Fixture().get()

        val viewModel = sut.create(MessageComposerViewModel::class.java)

        assertInstanceOf<MessageComposerViewModel>(viewModel)
    }

    @Test
    fun `create should return correct MessageListViewModel instance`() {
        val sut = Fixture().get()

        val viewModel = sut.create(MessageListViewModel::class.java)

        assertInstanceOf<MessageListViewModel>(viewModel)
    }

    @Test
    fun `create should return correct AttachmentsPickerViewModel instance`() {
        val sut = Fixture().get()

        val viewModel = sut.create(AttachmentsPickerViewModel::class.java)

        assertInstanceOf<AttachmentsPickerViewModel>(viewModel)
    }

    @Test
    fun `create should throw IllegalArgumentException for unsupported ViewModel class`() {
        val sut = Fixture().get()

        val exception = assertThrows(IllegalArgumentException::class.java) {
            sut.create(ViewModel::class.java)
        }

        assertEquals(
            "MessagesViewModelFactory can only create instances of the following classes: " +
                "MessageComposerViewModel, MessageListViewModel, AttachmentsPickerViewModel",
            exception.message,
        )
    }

    private class Fixture {

        private val cid = randomCID()

        private val mockContext: Context = mock {
            on { applicationContext } doReturn it
            on { getSystemService(Context.CLIPBOARD_SERVICE) } doReturn mock<ClipboardManager>()
        }

        private val mockChatClient: ChatClient = mock {
            val mockClientState: ClientState = mock {
                on { initializationState } doReturn MutableStateFlow(InitializationState.NOT_INITIALIZED)
                on { user } doReturn MutableStateFlow(randomUser())
            }
            on { clientState } doReturn mockClientState
            on { inheritScope(any()) } doReturn TestScope()
            on { audioPlayer } doReturn mock()
            on { getAppSettings() } doReturn mock()
        }

        init {
            object : ChatClient.ChatClientBuilder() {
                override fun internalBuild(): ChatClient = mockChatClient
            }.build()
        }

        fun get() = MessagesViewModelFactory(
            context = mockContext,
            channelId = cid,
            chatClient = mockChatClient,
        )
    }
}
