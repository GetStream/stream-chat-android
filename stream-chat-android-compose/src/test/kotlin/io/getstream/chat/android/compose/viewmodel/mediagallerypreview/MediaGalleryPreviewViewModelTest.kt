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

package io.getstream.chat.android.compose.viewmodel.mediagallerypreview

import io.getstream.chat.android.client.ChatClient
import io.getstream.chat.android.client.api.state.GlobalState
import io.getstream.chat.android.client.internal.state.plugin.internal.StatePlugin
import io.getstream.chat.android.client.setup.state.ClientState
import io.getstream.chat.android.compose.viewmodel.mediapreview.MediaGalleryPreviewViewModel
import io.getstream.chat.android.models.Attachment
import io.getstream.chat.android.models.ConnectionState
import io.getstream.chat.android.models.Message
import io.getstream.chat.android.models.User
import io.getstream.chat.android.test.TestCoroutineExtension
import io.getstream.chat.android.test.asCall
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.test.runTest
import org.amshove.kluent.`should be equal to`
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.extension.RegisterExtension
import org.mockito.kotlin.any
import org.mockito.kotlin.doReturn
import org.mockito.kotlin.eq
import org.mockito.kotlin.mock
import org.mockito.kotlin.verify
import org.mockito.kotlin.whenever

@ExperimentalCoroutinesApi
internal class MediaGalleryPreviewViewModelTest {

    @Test
    fun `Given a message with media attachments When showing media gallery Should show the gallery`() = runTest {
        val viewModel = Fixture()
            .givenCurrentUser()
            .givenAttachments(mutableListOf(attachment1, attachment2))
            .get()

        viewModel.toggleGallery(true)
        viewModel.toggleMediaOptions(true)

        viewModel.isShowingGallery `should be equal to` true
        viewModel.isShowingOptions `should be equal to` true
        viewModel.message.attachments.size `should be equal to` 2
    }

    @Test
    fun `Given a message with media attachments When showing media gallery and removing the media Should update or delete the message`() =
        runTest {
            val chatClient: ChatClient = mock()
            val viewModel = Fixture(chatClient)
                .givenCurrentUser()
                .givenAttachments(mutableListOf(attachment1, attachment2))
                .givenUpdateMessage()
                .givenDeleteMessage()
                .get()

            viewModel.deleteCurrentMediaAttachment(attachment1)
            viewModel.deleteCurrentMediaAttachment(attachment2)

            verify(chatClient).updateMessage(any())
            verify(chatClient).deleteMessage(MESSAGE_ID, false)
        }

    private class Fixture(
        private val chatClient: ChatClient = mock(),
        private val messageId: String = MESSAGE_ID,
        private val globalState: GlobalState = mock(),
    ) {

        private val clientState: ClientState = mock()

        init {
            val statePlugin: StatePlugin = mock()
            whenever(statePlugin.resolveDependency(eq(GlobalState::class))) doReturn globalState
            whenever(chatClient.plugins) doReturn listOf(statePlugin)
            whenever(chatClient.clientState) doReturn clientState
        }

        fun givenCurrentUser(currentUser: User = User(id = "Jc")) = apply {
            whenever(clientState.user) doReturn MutableStateFlow(currentUser)
            whenever(clientState.connectionState) doReturn MutableStateFlow(ConnectionState.Connected)
        }

        fun givenAttachments(attachments: MutableList<Attachment>) = apply {
            val message = Message(id = messageId, attachments = attachments)
            whenever(chatClient.getMessage(messageId)) doReturn message.asCall()
        }

        fun givenUpdateMessage() = apply {
            whenever(chatClient.updateMessage(any())) doReturn Message().asCall()
        }

        fun givenDeleteMessage() = apply {
            whenever(chatClient.deleteMessage(any(), any())) doReturn Message().asCall()
        }

        fun get(): MediaGalleryPreviewViewModel {
            return MediaGalleryPreviewViewModel(
                chatClient = chatClient,
                messageId = messageId,
                clientState = clientState,
            )
        }
    }

    companion object {
        @JvmField
        @RegisterExtension
        val testCoroutines = TestCoroutineExtension()
        private const val MESSAGE_ID = "message-id"

        /**
         * [MediaGalleryPreviewViewModel.deleteCurrentMediaAttachment] uses the URL field to compare attachments.
         */
        private val attachment1 =
            Attachment(
                type = "image",
                imageUrl = "http://example.com/img1.png",
                assetUrl = "http://example.com/img1.png",
            )
        private val attachment2 =
            Attachment(
                type = "video",
                assetUrl = "http://example.com/img2.png",
            )
    }
}
