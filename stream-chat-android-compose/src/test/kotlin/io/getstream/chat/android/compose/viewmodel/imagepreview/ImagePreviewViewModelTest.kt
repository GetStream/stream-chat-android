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

package io.getstream.chat.android.compose.viewmodel.imagepreview

import io.getstream.chat.android.client.ChatClient
import io.getstream.chat.android.client.models.Attachment
import io.getstream.chat.android.client.models.Message
import io.getstream.chat.android.client.models.User
import io.getstream.chat.android.client.setup.state.ClientState
import io.getstream.chat.android.offline.plugin.state.global.internal.GlobalMutableState
import io.getstream.chat.android.test.asCall
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.test.runTest
import org.amshove.kluent.`should be equal to`
import org.junit.jupiter.api.Test
import org.mockito.kotlin.any
import org.mockito.kotlin.doReturn
import org.mockito.kotlin.eq
import org.mockito.kotlin.mock
import org.mockito.kotlin.verify
import org.mockito.kotlin.whenever

@ExperimentalCoroutinesApi
internal class ImagePreviewViewModelTest {

    @Test
    fun `Given a message with image attachments When showing image gallery Should show the gallery`() = runTest {
        val viewModel = Fixture()
            .givenCurrentUser()
            .givenAttachments(mutableListOf(attachment1, attachment2))
            .get()

        viewModel.toggleGallery(true)
        viewModel.toggleImageOptions(true)

        viewModel.isShowingGallery `should be equal to` true
        viewModel.isShowingOptions `should be equal to` true
        viewModel.message.attachments.size `should be equal to` 2
    }

    @Test
    fun `Given a message with image attachments When showing image gallery and removing the images Should update or delete the message`() =
        runTest {
            val chatClient: ChatClient = mock()
            val viewModel = Fixture(chatClient)
                .givenCurrentUser()
                .givenAttachments(mutableListOf(attachment1, attachment2))
                .givenUpdateMessage()
                .givenDeleteMessage()
                .get()

            viewModel.toggleGallery(true)
            viewModel.deleteCurrentImage(attachment1)
            viewModel.deleteCurrentImage(attachment2)

            verify(chatClient).updateMessage(any(), eq(false))
            verify(chatClient).deleteMessage(MESSAGE_ID, false)
        }

    @Test
    fun `Given skip enrich URL set to true Should not enrich the url when updating the message`() =
        runTest {
            val skipEnrichUrl = true

            val chatClient: ChatClient = mock()
            val viewModel = Fixture(
                chatClient = chatClient,
                skipEnrichUrl = skipEnrichUrl
            ).givenCurrentUser()
                .givenAttachments(mutableListOf(attachment1, attachment2))
                .givenUpdateMessage()
                .givenDeleteMessage()
                .get()

            viewModel.toggleGallery(true)
            viewModel.deleteCurrentImage(attachment1)
            viewModel.deleteCurrentImage(attachment2)

            verify(chatClient).updateMessage(any(), eq(skipEnrichUrl))
            verify(chatClient).deleteMessage(MESSAGE_ID, false)
        }

    private class Fixture(
        private val chatClient: ChatClient = mock(),
        private val messageId: String = MESSAGE_ID,
        private val skipEnrichUrl: Boolean = false,
    ) {

        private val globalState: GlobalMutableState = mock()
        private val clientState: ClientState = mock()

        init {
            GlobalMutableState.instance = globalState
            whenever(chatClient.clientState) doReturn mock()
        }

        fun givenCurrentUser(currentUser: User = User(id = "Jc")) = apply {
            whenever(clientState.user) doReturn MutableStateFlow(currentUser)
        }

        fun givenAttachments(attachments: MutableList<Attachment>) = apply {
            val message = Message(id = messageId, attachments = attachments)
            whenever(chatClient.getMessage(messageId)) doReturn message.asCall()
        }

        fun givenUpdateMessage() = apply {
            whenever(chatClient.updateMessage(any(), eq(skipEnrichUrl))) doReturn Message().asCall()
        }

        fun givenDeleteMessage() = apply {
            whenever(chatClient.deleteMessage(any(), any())) doReturn Message().asCall()
        }

        fun get(): ImagePreviewViewModel {
            return ImagePreviewViewModel(
                chatClient = chatClient,
                messageId = messageId,
                skipEnrichUrl = skipEnrichUrl,
            )
        }
    }

    companion object {
        private const val MESSAGE_ID = "message-id"
        private val attachment1 = Attachment(type = "image", imageUrl = "http://example.com/img1.png")
        private val attachment2 = Attachment(type = "image", imageUrl = "http://example.com/img2.png")
    }
}
