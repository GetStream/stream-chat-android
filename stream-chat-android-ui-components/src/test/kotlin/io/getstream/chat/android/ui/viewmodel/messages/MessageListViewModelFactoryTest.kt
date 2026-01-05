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

package io.getstream.chat.android.ui.viewmodel.messages

import io.getstream.chat.android.client.ChatClient
import io.getstream.chat.android.client.setup.state.ClientState
import io.getstream.chat.android.models.InitializationState
import io.getstream.chat.android.models.User
import io.getstream.chat.android.randomUser
import io.getstream.chat.android.test.InstantTaskExecutorExtension
import io.getstream.chat.android.test.TestCoroutineExtension
import io.getstream.chat.android.ui.common.feature.messages.list.MessageListController
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.test.TestScope
import kotlinx.coroutines.test.runTest
import org.amshove.kluent.shouldBeEqualTo
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.extension.ExtendWith
import org.junit.jupiter.api.extension.RegisterExtension
import org.mockito.kotlin.any
import org.mockito.kotlin.doReturn
import org.mockito.kotlin.mock

@ExtendWith(TestCoroutineExtension::class)
internal class MessageListViewModelFactoryTest {

    @JvmField
    @RegisterExtension
    val instantExecutorExtension: InstantTaskExecutorExtension = InstantTaskExecutorExtension()

    @Test
    fun `test message list view model`() = runTest {
        val scope = TestScope()
        val stubUser: StateFlow<User> = MutableStateFlow(randomUser())
        val stubInitializationState: StateFlow<InitializationState> = MutableStateFlow(
            InitializationState.NOT_INITIALIZED,
        )
        val mockClientState: ClientState = mock {
            on { initializationState } doReturn stubInitializationState
            on { user } doReturn stubUser
        }
        val mockChatClient: ChatClient = mock {
            on { clientState } doReturn mockClientState
            on { inheritScope(any()) } doReturn scope
        }

        val cid = "test_cid"
        val messageId = "test_message_id"
        val parentMessageId = "test_parent_message_id"
        val messageListViewModelFactory = MessageListViewModelFactory(
            context = mock(),
            cid = cid,
            messageId = messageId,
            parentMessageId = parentMessageId,
            chatClient = mockChatClient,
            clientState = mockClientState,
            mediaRecorder = mock(),
        )

        val messageListViewModel = messageListViewModelFactory.create(MessageListViewModel::class.java)
        val clazz = MessageListController::class.java
        val messageIdField = clazz.getDeclaredField("messageId").apply { isAccessible = true }
        val parentMessageIdField = clazz.getDeclaredField("parentMessageId").apply { isAccessible = true }
        val foundMessageId = messageIdField.get(messageListViewModel.messageListController)
        val foundParentMessageId = parentMessageIdField.get(messageListViewModel.messageListController)
        foundMessageId shouldBeEqualTo messageId
        foundParentMessageId shouldBeEqualTo parentMessageId
    }
}
