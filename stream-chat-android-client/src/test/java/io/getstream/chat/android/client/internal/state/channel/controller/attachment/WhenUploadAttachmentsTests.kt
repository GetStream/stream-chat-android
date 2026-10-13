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

package io.getstream.chat.android.client.internal.state.channel.controller.attachment

import io.getstream.chat.android.client.ChatClient
import io.getstream.chat.android.client.attachment.AttachmentUploader
import io.getstream.chat.android.client.attachment.worker.UploadAttachmentsWorker
import io.getstream.chat.android.client.extensions.EXTRA_UPLOAD_ID
import io.getstream.chat.android.client.extensions.uploadId
import io.getstream.chat.android.client.internal.state.plugin.logic.channel.internal.legacy.ChannelStateLogic
import io.getstream.chat.android.client.internal.state.plugin.state.channel.internal.ChannelStateLegacyImpl
import io.getstream.chat.android.client.persistance.repository.MessageRepository
import io.getstream.chat.android.models.Attachment
import io.getstream.chat.android.models.Message
import io.getstream.chat.android.models.SyncStatus
import io.getstream.chat.android.positiveRandomLong
import io.getstream.chat.android.randomAttachment
import io.getstream.chat.android.randomMessage
import io.getstream.result.Error
import io.getstream.result.Result
import kotlinx.coroutines.Job
import kotlinx.coroutines.currentCoroutineContext
import kotlinx.coroutines.isActive
import kotlinx.coroutines.launch
import kotlinx.coroutines.test.runTest
import org.amshove.kluent.shouldBeEqualTo
import org.amshove.kluent.shouldBeInstanceOf
import org.junit.jupiter.api.Test
import org.mockito.kotlin.any
import org.mockito.kotlin.anyOrNull
import org.mockito.kotlin.argThat
import org.mockito.kotlin.argumentCaptor
import org.mockito.kotlin.doAnswer
import org.mockito.kotlin.doReturn
import org.mockito.kotlin.doSuspendableAnswer
import org.mockito.kotlin.doThrow
import org.mockito.kotlin.mock
import org.mockito.kotlin.never
import org.mockito.kotlin.times
import org.mockito.kotlin.verify
import org.mockito.kotlin.whenever

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
                on(it.uploadAttachment(any(), any(), any(), anyOrNull(), anyOrNull())) doThrow IllegalStateException("Error")
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
                    on(it.uploadAttachment(any(), any(), any(), anyOrNull(), anyOrNull())) doThrow IllegalStateException("Error")
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
                            anyOrNull(),
                            anyOrNull(),
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
                    on(it.uploadAttachment(any(), any(), any(), anyOrNull(), anyOrNull())) doAnswer { invocation ->
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

            verify(repository, times(2)).insertMessage(
                argThat {
                    attachments.run {
                        size == 2 &&
                            any { it.uploadId == "uploadId1" && it.uploadState is Attachment.UploadState.Success } &&
                            any { it.uploadId == "uploadId2" && it.uploadState == Attachment.UploadState.Success }
                    }
                },
            )
        }

    @Test
    fun `Given a later upload throws Should keep the earlier successful upload as uploaded`() = runTest {
        val first = randomAttachment().copy(uploadState = Attachment.UploadState.Idle, extraData = mapOf(EXTRA_UPLOAD_ID to "uploadId1"))
        val second = randomAttachment().copy(uploadState = Attachment.UploadState.Idle, extraData = mapOf(EXTRA_UPLOAD_ID to "uploadId2"))
        val attachmentUploader = mock<AttachmentUploader> {
            on(it.uploadAttachment(any(), any(), argThat { uploadId == "uploadId1" }, anyOrNull(), anyOrNull())) doReturn
                Result.Success(first.copy(uploadState = Attachment.UploadState.Success))
            on(it.uploadAttachment(any(), any(), argThat { uploadId == "uploadId2" }, anyOrNull(), anyOrNull())) doThrow
                IllegalStateException("Error")
        }
        val repository = mock<MessageRepository>()
        val message = randomMessage(id = "messageId123", attachments = mutableListOf(first, second))
        val sut = Fixture().givenAttachmentUploader(attachmentUploader)
            .givenMessageRepository(repository)
            .givenMessage(message)
            .get()

        sut.uploadAttachmentsForMessage(message.id)

        verify(repository).insertMessage(
            argThat {
                attachments.any { it.uploadId == "uploadId1" && it.uploadState == Attachment.UploadState.Success } &&
                    attachments.any { it.uploadId == "uploadId2" && it.uploadState is Attachment.UploadState.Failed }
            },
        )
    }

    @Test
    fun `Given the worker is stopped while an upload completes Should persist it and not start the next upload`() =
        runTest {
            val first = randomAttachment().copy(uploadState = Attachment.UploadState.Idle, extraData = mapOf(EXTRA_UPLOAD_ID to "uploadId1"))
            val second = randomAttachment().copy(uploadState = Attachment.UploadState.Idle, extraData = mapOf(EXTRA_UPLOAD_ID to "uploadId2"))
            lateinit var workerJob: Job
            val attachmentUploader = mock<AttachmentUploader> {
                on(it.uploadAttachment(any(), any(), argThat { uploadId == "uploadId1" }, anyOrNull(), anyOrNull())) doSuspendableAnswer {
                    workerJob.cancel()
                    // Like Call.await: a cancelled caller gets a canceled error although the upload finished.
                    if (currentCoroutineContext().isActive) {
                        Result.Success(first.copy(uploadState = Attachment.UploadState.Success, assetUrl = "url1"))
                    } else {
                        Result.Failure(Error.GenericError("The call was canceled before completing its execution."))
                    }
                }
            }
            val repository = mock<MessageRepository>()
            val message = randomMessage(id = "messageId123", attachments = mutableListOf(first, second))
            val sut = Fixture().givenAttachmentUploader(attachmentUploader)
                .givenMessageRepository(repository)
                .givenMessage(message)
                .get()

            workerJob = launch { sut.uploadAttachmentsForMessage(message.id) }
            workerJob.join()

            workerJob.isCancelled shouldBeEqualTo true
            verify(repository).insertMessage(
                argThat {
                    syncStatus == message.syncStatus &&
                        attachments.any { it.uploadId == "uploadId1" && it.assetUrl == "url1" } &&
                        attachments.any { it.uploadId == "uploadId2" && it.uploadState == Attachment.UploadState.Idle }
                },
            )
            verify(attachmentUploader, never())
                .uploadAttachment(any(), any(), argThat { uploadId == "uploadId2" }, anyOrNull(), anyOrNull())
            // The final write still publishes a terminal state, which releases the sender.
            verify(repository).insertMessage(
                argThat {
                    attachments.any { it.uploadId == "uploadId1" && it.uploadState == Attachment.UploadState.Success } &&
                        attachments.any { it.uploadId == "uploadId2" && it.uploadState is Attachment.UploadState.Failed }
                },
            )
        }

    @Test
    fun `Given a logout flushed the database during an upload Should not store the message again`() = runTest {
        val attachment = randomAttachment().copy(uploadState = Attachment.UploadState.Idle, extraData = mapOf(EXTRA_UPLOAD_ID to "uploadId1"))
        val message = randomMessage(id = "messageId123", attachments = mutableListOf(attachment))
        var flushed = false
        lateinit var workerJob: Job
        val repository = mock<MessageRepository> {
            onBlocking { selectMessage(message.id) } doAnswer { message.takeUnless { flushed } }
        }
        val attachmentUploader = mock<AttachmentUploader> {
            on(it.uploadAttachment(any(), any(), any(), anyOrNull(), anyOrNull())) doSuspendableAnswer {
                // The logout clears the database, then stops the worker, while the upload completes.
                flushed = true
                workerJob.cancel()
                Result.Success(attachment.copy(uploadState = Attachment.UploadState.Success, assetUrl = "url1"))
            }
        }
        val sut = Fixture().givenAttachmentUploader(attachmentUploader)
            .givenMessageRepository(repository)
            .get()

        workerJob = launch { sut.uploadAttachmentsForMessage(message.id) }
        workerJob.join()

        verify(repository, never()).insertMessage(any())
    }

    @Test
    fun `Given an upload succeeds Should replace only that attachment in the channel state`() = runTest {
        val uploadedEarlier = randomAttachment().copy(
            uploadState = Attachment.UploadState.Success,
            assetUrl = "url0",
            extraData = emptyMap(),
        )
        val pending = randomAttachment().copy(uploadState = Attachment.UploadState.Idle, extraData = mapOf(EXTRA_UPLOAD_ID to "uploadId1"))
        // Like the real uploader, the result no longer carries the upload id.
        val uploaded = pending.copy(uploadState = Attachment.UploadState.Success, assetUrl = "url1", extraData = emptyMap())
        val attachmentUploader = mock<AttachmentUploader> {
            on(it.uploadAttachment(any(), any(), any(), anyOrNull(), anyOrNull())) doReturn Result.Success(uploaded)
        }
        val message = randomMessage(id = "messageId123", attachments = mutableListOf(uploadedEarlier, pending))
        val fixture = Fixture().givenAttachmentUploader(attachmentUploader)
            .givenMessage(message)
            .givenMessageInChannelState(message)

        fixture.get().uploadAttachmentsForMessage(message.id)

        // The first upsert is the update made as soon as the upload succeeds, before the final write.
        val upserted = argumentCaptor<Message>()
        verify(fixture.channelStateLogic(), times(2)).upsertMessage(upserted.capture())
        upserted.firstValue.attachments shouldBeEqualTo listOf(uploadedEarlier, uploaded)
    }

    @Test
    fun `Given an attachment without a file Should not upload it`() = runTest {
        val link = randomAttachment().copy(upload = null, uploadState = null)
        val attachmentUploader = mock<AttachmentUploader>()
        val message = randomMessage(id = "messageId123", attachments = mutableListOf(link))
        val sut = Fixture().givenAttachmentUploader(attachmentUploader)
            .givenMessage(message)
            .get()

        val result = sut.uploadAttachmentsForMessage(message.id)

        result shouldBeInstanceOf Result.Success::class
        verify(attachmentUploader, never()).uploadAttachment(any(), any(), any(), anyOrNull(), anyOrNull())
    }

    @Test
    fun `Given an earlier upload failed Should store a later success without marking the message failed`() = runTest {
        val first = randomAttachment().copy(uploadState = Attachment.UploadState.Idle, extraData = mapOf(EXTRA_UPLOAD_ID to "uploadId1"))
        val second = randomAttachment().copy(uploadState = Attachment.UploadState.Idle, extraData = mapOf(EXTRA_UPLOAD_ID to "uploadId2"))
        val attachmentUploader = mock<AttachmentUploader> {
            on(it.uploadAttachment(any(), any(), argThat { uploadId == "uploadId1" }, anyOrNull(), anyOrNull())) doReturn
                Result.Failure(Error.GenericError("Error"))
            on(it.uploadAttachment(any(), any(), argThat { uploadId == "uploadId2" }, anyOrNull(), anyOrNull())) doReturn
                Result.Success(second.copy(uploadState = Attachment.UploadState.Success, assetUrl = "url2"))
        }
        val repository = mock<MessageRepository>()
        val message = randomMessage(
            id = "messageId123",
            attachments = mutableListOf(first, second),
            syncStatus = SyncStatus.AWAITING_ATTACHMENTS,
        )
        val sut = Fixture().givenAttachmentUploader(attachmentUploader)
            .givenMessageRepository(repository)
            .givenMessage(message)
            .get()

        sut.uploadAttachmentsForMessage(message.id)

        val inserted = argumentCaptor<Message>()
        verify(repository, times(2)).insertMessage(inserted.capture())
        with(inserted.firstValue) {
            syncStatus shouldBeEqualTo SyncStatus.AWAITING_ATTACHMENTS
            attachments.first { it.uploadId == "uploadId1" }.uploadState shouldBeEqualTo Attachment.UploadState.Idle
            attachments.first { it.uploadId == "uploadId2" }.assetUrl shouldBeEqualTo "url2"
        }
        inserted.lastValue.syncStatus shouldBeEqualTo SyncStatus.FAILED_PERMANENTLY
    }

    @Test
    fun `Given an attachment is marked uploaded without an asset url Should upload it`() = runTest {
        val attachment = randomAttachment().copy(
            uploadState = Attachment.UploadState.Success,
            assetUrl = null,
            extraData = mapOf(EXTRA_UPLOAD_ID to "uploadId1"),
        )
        val attachmentUploader = mock<AttachmentUploader> {
            on(it.uploadAttachment(any(), any(), any(), anyOrNull(), anyOrNull())) doReturn
                Result.Success(attachment.copy(assetUrl = "url1"))
        }
        val message = randomMessage(id = "messageId123", attachments = mutableListOf(attachment))
        val sut = Fixture().givenAttachmentUploader(attachmentUploader)
            .givenMessage(message)
            .get()

        sut.uploadAttachmentsForMessage(message.id)

        verify(attachmentUploader).uploadAttachment(any(), any(), argThat { uploadId == "uploadId1" }, anyOrNull(), anyOrNull())
    }

    private class Fixture {
        private val channelType = "channelType"
        private val channelId = "channelId"
        private var uploader: AttachmentUploader = mock()
        private var messageRepository: MessageRepository = mock()
        private val channelStateLegacyImpl: ChannelStateLegacyImpl = mock()

        private val channelStateLogic: ChannelStateLogic =
            mock {
                on(it.writeChannelState()) doReturn channelStateLegacyImpl
                on(it.channelState()) doReturn channelStateLegacyImpl
            }

        private val chatClient = mock<ChatClient> {
            whenever(it.channel(any())) doReturn mock()
            whenever(it.containsStoredCredentials()) doReturn true
        }

        fun givenAttachmentUploader(attachmentUploader: AttachmentUploader) =
            apply {
                uploader = attachmentUploader
            }

        fun givenMessageInChannelState(message: Message) = apply {
            whenever(channelStateLegacyImpl.getMessageById(message.id)) doReturn message
        }

        fun channelStateLogic(): ChannelStateLogic = channelStateLogic

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
