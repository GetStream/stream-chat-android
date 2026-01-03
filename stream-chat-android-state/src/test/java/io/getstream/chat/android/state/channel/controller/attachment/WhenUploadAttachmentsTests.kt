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

package io.getstream.chat.android.state.channel.controller.attachment

import io.getstream.chat.android.client.ChatClient
import io.getstream.chat.android.client.attachment.AttachmentUploader
import io.getstream.chat.android.client.attachment.worker.UploadAttachmentsWorker
import io.getstream.chat.android.client.extensions.EXTRA_UPLOAD_ID
import io.getstream.chat.android.client.extensions.uploadId
import io.getstream.chat.android.client.persistance.repository.MessageRepository
import io.getstream.chat.android.models.Attachment
import io.getstream.chat.android.models.Message
import io.getstream.chat.android.models.SyncStatus
import io.getstream.chat.android.positiveRandomLong
import io.getstream.chat.android.randomAttachment
import io.getstream.chat.android.randomMessage
import io.getstream.chat.android.state.plugin.logic.channel.internal.legacy.ChannelStateLogic
import io.getstream.chat.android.state.plugin.state.channel.internal.ChannelStateLegacyImpl
import io.getstream.result.Error
import io.getstream.result.Result
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.test.runTest
import org.amshove.kluent.shouldBeInstanceOf
import org.junit.jupiter.api.Test
import org.mockito.kotlin.any
import org.mockito.kotlin.argThat
import org.mockito.kotlin.doAnswer
import org.mockito.kotlin.doReturn
import org.mockito.kotlin.doThrow
import org.mockito.kotlin.mock
import org.mockito.kotlin.verify
import org.mockito.kotlin.whenever

@ExperimentalCoroutinesApi
internal class WhenUploadAttachmentsTests {

    private val attachmentsSent = mutableListOf(
        randomAttachment().copy(uploadState = Attachment.UploadState.Success),
    )

    private val attachmentsPending = mutableListOf(
        randomAttachment().copy(
            uploadState = Attachment.UploadState.InProgress(positiveRandomLong(30), positiveRandomLong(50) + 30),
        ),
    )

    private val defaultMessageSentAttachments = randomMessage(
        attachments = attachmentsSent,
    )

    private val defaultMessagePendingAttachments = randomMessage(
        attachments = attachmentsPending,
    )

    @Test
    fun `when there's no attachment with pending status, there's no need to try to send attachments`() =
        runTest {
            val repositoryFacade = mock<MessageRepository> {
                on(it.selectMessage(defaultMessageSentAttachments.id)) doReturn defaultMessageSentAttachments
                on(it.selectMessage(defaultMessagePendingAttachments.id)) doReturn defaultMessagePendingAttachments
            }

            val sut = Fixture().givenMessageRepository(repositoryFacade).get()
            val result = sut.uploadAttachmentsForMessage(
                defaultMessageSentAttachments.id,
            )

            result shouldBeInstanceOf Result.Success::class
        }

    @Test
    fun `when there's a pending attachment, it should be uploaded`() = runTest {
        val repositoryFacade = mock<MessageRepository> {
            on(it.selectMessage(defaultMessageSentAttachments.id)) doReturn defaultMessageSentAttachments
            on(it.selectMessage(defaultMessagePendingAttachments.id)) doReturn defaultMessagePendingAttachments
        }

        val sut = Fixture().givenMessageRepository(repositoryFacade).get()
        val result = sut.uploadAttachmentsForMessage(
            defaultMessagePendingAttachments.id,
        )

        // verify(sut).uploadAttachments(any())
    }

    @Test
    fun `when not all attachments have state as success, it should return error`() = runTest {
        val repositoryFacade = mock<MessageRepository> {
            on(it.selectMessage(defaultMessageSentAttachments.id)) doReturn defaultMessageSentAttachments
            on(it.selectMessage(defaultMessagePendingAttachments.id)) doReturn defaultMessagePendingAttachments
        }
        val result = Fixture().givenMessageRepository(repositoryFacade).get()
            .uploadAttachmentsForMessage(
                defaultMessagePendingAttachments.id,
            )

        result shouldBeInstanceOf Result.Failure::class
    }

    @Test
    fun `when user can not be set, it should return an error`() = runTest {
        val result = Fixture()
            .givenChatClientNoStoredCredentials()
            .givenMessage(randomMessage())
            .get()
            .uploadAttachmentsForMessage(
                defaultMessagePendingAttachments.id,
            )

        result shouldBeInstanceOf Result.Failure::class
    }

    @Test
    fun `Given exception when upload Should insert message with failed sync status to repo`() = runTest {
        val attachmentUploader =
            mock<AttachmentUploader> {
                on(it.uploadAttachment(any(), any(), any(), any())) doThrow IllegalStateException("Error")
            }
        val repository = mock<MessageRepository>()
        val message = randomMessage(
            id = "messageId123",
            attachments = mutableListOf(
                randomAttachment().copy(
                    uploadState = Attachment.UploadState.Idle,
                    extraData = mutableMapOf(
                        EXTRA_UPLOAD_ID to "uploadId123",
                    ),
                ),
            ),
        )
        val sut =
            Fixture().givenAttachmentUploader(attachmentUploader)
                .givenMessageRepository(repository)
                .givenMessage(message)
                .get()

        sut.uploadAttachmentsForMessage(message.id)

        verify(repository).insertMessage(
            argThat { id == "messageId123" && syncStatus == SyncStatus.FAILED_PERMANENTLY },
        )
    }

    @Test
    fun `Given uploaded and not uploaded attachments And exception when upload Should insert message with 2 attachments`() =
        runTest {
            val attachmentUploader =
                mock<AttachmentUploader> {
                    on(it.uploadAttachment(any(), any(), any(), any())) doThrow IllegalStateException("Error")
                }
            val repository = mock<MessageRepository>()
            val message = randomMessage(
                id = "messageId123",
                attachments = mutableListOf(
                    randomAttachment().copy(
                        uploadState = Attachment.UploadState.Idle,
                        extraData = mapOf(EXTRA_UPLOAD_ID to "uploadId1"),
                    ),
                    randomAttachment().copy(
                        uploadState = Attachment.UploadState.Success,
                        extraData = mapOf(EXTRA_UPLOAD_ID to "uploadId2"),
                    ),
                ),
            )
            val sut =
                Fixture().givenAttachmentUploader(attachmentUploader)
                    .givenMessageRepository(repository)
                    .givenMessage(message)
                    .get()

            sut.uploadAttachmentsForMessage(message.id)

            verify(repository).insertMessage(
                argThat {
                    attachments.run {
                        size == 2 &&
                            any { it.uploadId == "uploadId1" && it.uploadState is Attachment.UploadState.Failed } &&
                            any { it.uploadId == "uploadId2" && it.uploadState == Attachment.UploadState.Success }
                    }
                },
            )
        }

    @Test
    fun `Given uploaded and not uploaded attachments And failure when upload Should insert message with 2 attachments`() =
        runTest {
            val attachmentUploader =
                mock<AttachmentUploader> {
                    on(
                        it.uploadAttachment(
                            any(),
                            any(),
                            any(),
                            any(),
                        ),
                    ) doReturn Result.Failure(
                        Error.ThrowableError(
                            message = "",
                            cause = IllegalArgumentException("Error:-)"),
                        ),
                    )
                }
            val repository = mock<MessageRepository>()
            val message = randomMessage(
                id = "messageId123",
                attachments = mutableListOf(
                    randomAttachment().copy(
                        uploadState = Attachment.UploadState.Idle,
                        extraData = mapOf(EXTRA_UPLOAD_ID to "uploadId1"),
                    ),
                    randomAttachment().copy(
                        uploadState = Attachment.UploadState.Success,
                        extraData = mapOf(EXTRA_UPLOAD_ID to "uploadId2"),
                    ),
                ),
            )
            val sut =
                Fixture().givenAttachmentUploader(attachmentUploader)
                    .givenMessageRepository(repository)
                    .givenMessage(message)
                    .get()

            sut.uploadAttachmentsForMessage(message.id)

            verify(repository).insertMessage(
                argThat {
                    attachments.run {
                        size == 2 &&
                            any { it.uploadId == "uploadId1" && it.uploadState is Attachment.UploadState.Failed } &&
                            any { it.uploadId == "uploadId2" && it.uploadState == Attachment.UploadState.Success }
                    }
                },
            )
        }

    @Test
    fun `Given uploaded and not uploaded attachments And upload succeed Should insert message with 2 uploaded attachments`() =
        runTest {
            val attachmentUploader =
                mock<AttachmentUploader> {
                    on(it.uploadAttachment(any(), any(), any(), any())) doAnswer { invocation ->
                        val attachment = invocation.arguments[2] as Attachment
                        Result.Success(attachment.copy(uploadState = Attachment.UploadState.Success))
                    }
                }
            val repository = mock<MessageRepository>()
            val message = randomMessage(
                id = "messageId123",
                attachments = mutableListOf(
                    randomAttachment().copy(
                        uploadState = Attachment.UploadState.Idle,
                        extraData = mapOf(EXTRA_UPLOAD_ID to "uploadId1"),
                    ),
                    randomAttachment().copy(
                        uploadState = Attachment.UploadState.Success,
                        extraData = mapOf(EXTRA_UPLOAD_ID to "uploadId2"),
                    ),
                ),
            )
            val sut =
                Fixture().givenAttachmentUploader(attachmentUploader)
                    .givenMessageRepository(repository)
                    .givenMessage(message)
                    .get()

            sut.uploadAttachmentsForMessage(message.id)

            verify(repository).insertMessage(
                argThat {
                    attachments.run {
                        size == 2 &&
                            any { it.uploadId == "uploadId1" && it.uploadState is Attachment.UploadState.Success } &&
                            any { it.uploadId == "uploadId2" && it.uploadState == Attachment.UploadState.Success }
                    }
                },
            )
        }

    private class Fixture {
        private val channelType = "channelType"
        private val channelId = "channelId"
        private var uploader: AttachmentUploader = mock()
        private var messageRepository: MessageRepository = mock()
        private val channelMutableState: ChannelStateLegacyImpl = mock()

        private val channelStateLogic: ChannelStateLogic =
            mock {
                on(it.writeChannelState()) doReturn channelMutableState
                on(it.channelState()) doReturn channelMutableState
            }

        private val chatClient = mock<ChatClient> {
            whenever(it.channel(any())) doReturn mock()
            whenever(it.containsStoredCredentials()) doReturn true
        }

        fun givenAttachmentUploader(attachmentUploader: AttachmentUploader) =
            apply {
                uploader = attachmentUploader
            }

        fun givenMessageRepository(repository: MessageRepository) = apply {
            messageRepository = repository
        }

        suspend fun givenMessage(message: Message) = apply {
            whenever(messageRepository.selectMessage(any())) doReturn message
        }

        fun givenChatClientNoStoredCredentials() = apply {
            whenever(chatClient.containsStoredCredentials()) doReturn false
        }

        fun get(): UploadAttachmentsWorker {
            return UploadAttachmentsWorker(
                channelType,
                channelId,
                channelStateLogic = channelStateLogic,
                messageRepository = messageRepository,
                chatClient = chatClient,
                attachmentUploader = uploader,
            )
        }
    }
}
