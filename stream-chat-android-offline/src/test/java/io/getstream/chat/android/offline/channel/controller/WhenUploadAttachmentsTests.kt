package io.getstream.chat.android.offline.channel.controller

import io.getstream.chat.android.client.ChatClient
import io.getstream.chat.android.client.extensions.uploadId
import io.getstream.chat.android.client.models.Attachment
import io.getstream.chat.android.client.utils.Result
import io.getstream.chat.android.client.utils.SyncStatus
import io.getstream.chat.android.offline.ChatDomainImpl
import io.getstream.chat.android.offline.channel.ChannelController
import io.getstream.chat.android.offline.experimental.channel.logic.ChannelLogic
import io.getstream.chat.android.offline.experimental.channel.state.ChannelMutableState
import io.getstream.chat.android.offline.message.attachment.AttachmentUploader
import io.getstream.chat.android.offline.randomAttachment
import io.getstream.chat.android.offline.randomMessage
import io.getstream.chat.android.offline.randomUser
import io.getstream.chat.android.offline.repository.RepositoryFacade
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.test.TestCoroutineScope
import kotlinx.coroutines.test.runBlockingTest
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

        private val scope = TestCoroutineScope()
        private var uploader: AttachmentUploader = mock()
        private val chatDomainImpl = mock<ChatDomainImpl> {
            on { it.scope } doReturn scope
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

        fun get(): ChannelController {
            val mutableState = ChannelMutableState(
                "channelType",
                "channelId",
                scope,
                MutableStateFlow(randomUser()),
                MutableStateFlow(emptyMap())
            )
            return ChannelController(
                mutableState = mutableState,
                channelLogic = ChannelLogic(mutableState, chatDomainImpl),
                client = chatClient,
                domainImpl = chatDomainImpl,
                attachmentUploader = uploader
            )
        }
    }
}
