package io.getstream.chat.android.offline.channel.controller

import com.nhaarman.mockitokotlin2.any
import com.nhaarman.mockitokotlin2.argThat
import com.nhaarman.mockitokotlin2.doAnswer
import com.nhaarman.mockitokotlin2.doReturn
import com.nhaarman.mockitokotlin2.doThrow
import com.nhaarman.mockitokotlin2.eq
import com.nhaarman.mockitokotlin2.mock
import com.nhaarman.mockitokotlin2.verify
import com.nhaarman.mockitokotlin2.whenever
import io.getstream.chat.android.client.ChatClient
import io.getstream.chat.android.client.extensions.uploadId
import io.getstream.chat.android.client.models.Attachment
import io.getstream.chat.android.client.utils.Result
import io.getstream.chat.android.client.utils.SyncStatus
import io.getstream.chat.android.offline.ChatDomainImpl
import io.getstream.chat.android.offline.channel.ChannelController
import io.getstream.chat.android.offline.message.attachment.AttachmentUploader
import io.getstream.chat.android.offline.randomAttachment
import io.getstream.chat.android.offline.randomMessage
import io.getstream.chat.android.offline.repository.RepositoryFacade
import kotlinx.coroutines.test.TestCoroutineScope
import kotlinx.coroutines.test.runBlockingTest
import org.junit.jupiter.api.Test

internal class WhenUploadAttachmentsTests {

    @Test
    fun `Given exception when upload Should insert message with failed sync status to repo`() = runBlockingTest {
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
        val sut = Fixture().givenAttachmentUploader(attachmentUploader).givenRepository(repository).get()

        sut.uploadAttachments(message)

        verify(repository).insertMessage(
            argThat { id == "messageId123" && syncStatus == SyncStatus.FAILED_PERMANENTLY },
            eq(false)
        )
    }

    @Test
    fun `Given uploaded and not uploaded attachments And exception when upload Should insert message with 2 attachments`() =
        runBlockingTest {
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
            val sut = Fixture().givenAttachmentUploader(attachmentUploader).givenRepository(repository).get()

            sut.uploadAttachments(message)

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
        runBlockingTest {
            val attachmentUploader = mock<AttachmentUploader> {
                on(it.uploadAttachment(any(), any(), any(), any())) doReturn Result.error(IllegalArgumentException("Error:-)"))
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
            val sut = Fixture().givenAttachmentUploader(attachmentUploader).givenRepository(repository).get()

            sut.uploadAttachments(message)

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
        runBlockingTest {
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
            val sut = Fixture().givenAttachmentUploader(attachmentUploader).givenRepository(repository).get()

            sut.uploadAttachments(message)

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
        private val chatDomainImpl = mock<ChatDomainImpl> {
            on { it.scope } doReturn TestCoroutineScope()
            on { it.appContext } doReturn mock()
        }
        private val chatClient = mock<ChatClient> {
            whenever(it.channel(any())) doReturn mock()
        }

        fun givenAttachmentUploader(attachmentUploader: AttachmentUploader) = apply {
            uploader = attachmentUploader
        }

        fun givenRepository(repository: RepositoryFacade) = apply {
            whenever(chatDomainImpl.repos) doReturn repository
        }

        fun get(): ChannelController =
            ChannelController(
                "channelType",
                "channelId",
                client = chatClient,
                domainImpl = chatDomainImpl,
                attachmentUploader = uploader
            )
    }
}
