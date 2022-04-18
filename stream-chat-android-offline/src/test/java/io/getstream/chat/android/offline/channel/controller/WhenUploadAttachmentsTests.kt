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

package io.getstream.chat.android.offline.channel.controller

import io.getstream.chat.android.client.ChatClient
import io.getstream.chat.android.client.extensions.uploadId
import io.getstream.chat.android.client.models.Attachment
import io.getstream.chat.android.client.models.Message
import io.getstream.chat.android.client.utils.Result
import io.getstream.chat.android.client.utils.SyncStatus
import io.getstream.chat.android.client.uploader.attachment.AttachmentUploader
import io.getstream.chat.android.client.uploader.attachment.worker.UploadAttachmentsWorker
import io.getstream.chat.android.offline.plugin.logic.internal.LogicRegistry
import io.getstream.chat.android.offline.randomAttachment
import io.getstream.chat.android.offline.randomMessage
import io.getstream.chat.android.offline.repository.builder.internal.RepositoryFacade
import io.getstream.chat.android.test.positiveRandomLong
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.test.runTest
import org.amshove.kluent.`should be`
import org.amshove.kluent.shouldBeTrue
import org.junit.jupiter.api.Test
import org.mockito.kotlin.any
import org.mockito.kotlin.argThat
import org.mockito.kotlin.doAnswer
import org.mockito.kotlin.doReturn
import org.mockito.kotlin.doThrow
import org.mockito.kotlin.eq
import org.mockito.kotlin.mock
import org.mockito.kotlin.verify
import org.mockito.kotlin.whenever

@ExperimentalCoroutinesApi
internal class WhenUploadAttachmentsTests {
    private val channelType = "channelType"
    private val channelId = "channelId"

    private val attachmentsSent = mutableListOf(
        randomAttachment {
            this.uploadState = Attachment.UploadState.Success
        }
    )

    private val attachmentsPending = mutableListOf(
        randomAttachment {
            this.uploadState = Attachment.UploadState.InProgress(positiveRandomLong(30), positiveRandomLong(50) + 30)
        }
    )

    private val defaultMessageSentAttachments = randomMessage(
        attachments = attachmentsSent
    )

    private val defaultMessagePendingAttachments = randomMessage(
        attachments = attachmentsPending
    )

    @Test
    fun `when there's no attachment with pending status, there's no need to try to send attachments`() =
        runTest {
            val repositoryFacade = mock<RepositoryFacade> {
                on(it.selectMessage(defaultMessageSentAttachments.id)) doReturn defaultMessageSentAttachments
                on(it.selectMessage(defaultMessagePendingAttachments.id)) doReturn defaultMessagePendingAttachments
            }

            val sut = Fixture().givenRepository(repositoryFacade).get()
            val result = sut.uploadAttachmentsForMessage(
                channelType,
                channelId,
                defaultMessageSentAttachments.id,
            )

            result.isSuccess `should be` true
        }

    @Test
    fun `when there's a pending attachment, it should be uploaded`() = runTest {
        val repositoryFacade = mock<RepositoryFacade> {
            on(it.selectMessage(defaultMessageSentAttachments.id)) doReturn defaultMessageSentAttachments
            on(it.selectMessage(defaultMessagePendingAttachments.id)) doReturn defaultMessagePendingAttachments
        }

        val sut = Fixture().givenRepository(repositoryFacade).get()
        val result = sut.uploadAttachmentsForMessage(
            channelType,
            channelId,
            defaultMessagePendingAttachments.id,
        )

        // verify(sut).uploadAttachments(any())
    }

    @Test
    fun `when not all attachments have state as success, it should return error`() = runTest {
        val repositoryFacade = mock<RepositoryFacade> {
            on(it.selectMessage(defaultMessageSentAttachments.id)) doReturn defaultMessageSentAttachments
            on(it.selectMessage(defaultMessagePendingAttachments.id)) doReturn defaultMessagePendingAttachments
        }
        val result = Fixture().givenRepository(repositoryFacade).get()
            .uploadAttachmentsForMessage(
                channelType,
                channelId,
                defaultMessagePendingAttachments.id,
            )

        result.isError.shouldBeTrue()
    }

    @Test
    fun `when user can not be set, it should return an error`() = runTest {
        val result = Fixture().givenChatClientNoStoredCredentials().get()
            .uploadAttachmentsForMessage(
                channelType,
                channelId,
                defaultMessagePendingAttachments.id,
            )

        result.isError.shouldBeTrue()
    }

    @Test
    fun `Given exception when upload Should insert message with failed sync status to repo`() = runTest {
        val attachmentUploader = mock<AttachmentUploader> {
            on(it.uploadAttachment(any(), any(), any(), any())) doThrow IllegalStateException("Error")
        }
        val repository = mock<RepositoryFacade>()
        val message = randomMessage(
            id = "messageId123",
            attachments = mutableListOf(
                randomAttachment {
                    uploadState = Attachment.UploadState.Idle
                    uploadId = "uploadId123"
                }
            )
        )
        val sut =
            Fixture().givenAttachmentUploader(attachmentUploader).givenRepository(repository).givenMessage(message)
                .get()

        sut.uploadAttachmentsForMessage(channelType, channelId, message.id)

        verify(repository).insertMessage(
            argThat { id == "messageId123" && syncStatus == SyncStatus.FAILED_PERMANENTLY },
            eq(false)
        )
    }

    @Test
    fun `Given uploaded and not uploaded attachments And exception when upload Should insert message with 2 attachments`() =
        runTest {
            val attachmentUploader = mock<AttachmentUploader> {
                on(it.uploadAttachment(any(), any(), any(), any())) doThrow IllegalStateException("Error")
            }
            val repository = mock<RepositoryFacade>()
            val message = randomMessage(
                id = "messageId123",
                attachments = mutableListOf(
                    randomAttachment {
                        uploadState = Attachment.UploadState.Idle
                        uploadId = "uploadId1"
                    },
                    randomAttachment {
                        uploadState = Attachment.UploadState.Success
                        uploadId = "uploadId2"
                    }
                )
            )
            val sut =
                Fixture().givenAttachmentUploader(attachmentUploader).givenRepository(repository).givenMessage(message)
                    .get()

            sut.uploadAttachmentsForMessage(channelType, channelId, message.id)

            verify(repository).insertMessage(
                argThat {
                    attachments.run {
                        size == 2 &&
                            any { it.uploadId == "uploadId1" && it.uploadState is Attachment.UploadState.Failed } &&
                            any { it.uploadId == "uploadId2" && it.uploadState == Attachment.UploadState.Success }
                    }
                },
                eq(false)
            )
        }

    @Test
    fun `Given uploaded and not uploaded attachments And failure when upload Should insert message with 2 attachments`() =
        runTest {
            val attachmentUploader = mock<AttachmentUploader> {
                on(
                    it.uploadAttachment(
                        any(),
                        any(),
                        any(),
                        any()
                    )
                ) doReturn Result.error(IllegalArgumentException("Error:-)"))
            }
            val repository = mock<RepositoryFacade>()
            val message = randomMessage(
                id = "messageId123",
                attachments = mutableListOf(
                    randomAttachment {
                        uploadState = Attachment.UploadState.Idle
                        uploadId = "uploadId1"
                    },
                    randomAttachment {
                        uploadState = Attachment.UploadState.Success
                        uploadId = "uploadId2"
                    }
                )
            )
            val sut =
                Fixture().givenAttachmentUploader(attachmentUploader).givenRepository(repository).givenMessage(message)
                    .get()

            sut.uploadAttachmentsForMessage(channelType, channelId, message.id)

            verify(repository).insertMessage(
                argThat {
                    attachments.run {
                        size == 2 &&
                            any { it.uploadId == "uploadId1" && it.uploadState is Attachment.UploadState.Failed } &&
                            any { it.uploadId == "uploadId2" && it.uploadState == Attachment.UploadState.Success }
                    }
                },
                eq(false)
            )
        }

    @Test
    fun `Given uploaded and not uploaded attachments And upload succeed Should insert message with 2 uploaded attachments`() =
        runTest {
            val attachmentUploader = mock<AttachmentUploader> {
                on(it.uploadAttachment(any(), any(), any(), any())) doAnswer { invocation ->
                    val attachment = invocation.arguments[2] as Attachment
                    Result(attachment.copy(uploadState = Attachment.UploadState.Success))
                }
            }
            val repository = mock<RepositoryFacade>()
            val message = randomMessage(
                id = "messageId123",
                attachments = mutableListOf(
                    randomAttachment {
                        uploadState = Attachment.UploadState.Idle
                        uploadId = "uploadId1"
                    },
                    randomAttachment {
                        uploadState = Attachment.UploadState.Success
                        uploadId = "uploadId2"
                    }
                )
            )
            val sut =
                Fixture().givenAttachmentUploader(attachmentUploader).givenRepository(repository).givenMessage(message)
                    .get()

            sut.uploadAttachmentsForMessage(channelType, channelId, message.id)

            verify(repository).insertMessage(
                argThat {
                    attachments.run {
                        size == 2 &&
                            any { it.uploadId == "uploadId1" && it.uploadState is Attachment.UploadState.Success } &&
                            any { it.uploadId == "uploadId2" && it.uploadState == Attachment.UploadState.Success }
                    }
                },
                eq(false)
            )
        }

    private class Fixture {
        private var uploader: AttachmentUploader = mock()
        private var repos: RepositoryFacade = mock()
        private var logicRegistry: LogicRegistry = mock() {
            on(it.channel(any(), any())) doReturn mock()
        }

        private val chatClient = mock<ChatClient> {
            whenever(it.channel(any())) doReturn mock()
            whenever(it.containsStoredCredentials()) doReturn true
        }

        fun givenAttachmentUploader(attachmentUploader: AttachmentUploader) = apply {
            uploader = attachmentUploader
        }

        fun givenRepository(repository: RepositoryFacade) = apply {
            repos = repository
        }

        suspend fun givenMessage(message: Message) = apply {
            whenever(repos.selectMessage(any())) doReturn message
        }

        fun givenChatClientNoStoredCredentials() = apply {
            whenever(chatClient.containsStoredCredentials()) doReturn false
        }

        fun get(): UploadAttachmentsWorker {
            return UploadAttachmentsWorker(
                logic = logicRegistry,
                repos = repos,
                chatClient = chatClient,
                attachmentUploader = uploader
            )
        }
    }
}
