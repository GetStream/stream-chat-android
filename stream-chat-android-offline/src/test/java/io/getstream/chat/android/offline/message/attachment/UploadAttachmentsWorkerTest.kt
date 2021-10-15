package io.getstream.chat.android.offline.message.attachment

import com.nhaarman.mockitokotlin2.any
import com.nhaarman.mockitokotlin2.doReturn
import com.nhaarman.mockitokotlin2.eq
import com.nhaarman.mockitokotlin2.mock
import com.nhaarman.mockitokotlin2.never
import com.nhaarman.mockitokotlin2.verify
import com.nhaarman.mockitokotlin2.whenever
import io.getstream.chat.android.client.ChatClient
import io.getstream.chat.android.client.models.Attachment
import io.getstream.chat.android.offline.ChatDomainImpl
import io.getstream.chat.android.offline.channel.ChannelController
import io.getstream.chat.android.offline.randomAttachment
import io.getstream.chat.android.offline.randomMessage
import io.getstream.chat.android.offline.repository.RepositoryFacade
import io.getstream.chat.android.test.positiveRandomLong
import io.getstream.chat.android.test.randomString
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.runBlocking
import kotlinx.coroutines.test.runBlockingTest
import org.amshove.kluent.shouldBeTrue
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test

@ExperimentalCoroutinesApi
internal class UploadAttachmentsWorkerTest {

    private lateinit var repositoryFacade: RepositoryFacade
    private lateinit var channelController: ChannelController
    private lateinit var chatDomainImpl: ChatDomainImpl
    private lateinit var chatClient: ChatClient

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
    private val defaultChannelType = randomString()
    private val defaultChannelId = randomString()

    @BeforeEach
    fun setUp() {
        runBlocking {
            repositoryFacade = mock {
                on(it.selectMessage(defaultMessageSentAttachments.id)) doReturn defaultMessageSentAttachments
                on(it.selectMessage(defaultMessagePendingAttachments.id)) doReturn defaultMessagePendingAttachments
            }

            channelController = mock()
            chatClient = mock {
                on(it.containsStoredCredentials()) doReturn true
            }

            chatDomainImpl = mock {
                on(it.channel(defaultChannelType, defaultChannelId)) doReturn channelController
                on(it.repos) doReturn repositoryFacade
                on(it.user) doReturn MutableStateFlow(null)
            }
        }
    }

    @Test
    fun `when there's no attachment with pending status, there's no need to try to send attachments`() =
        runBlockingTest {
            UploadAttachmentsWorker(mock())
                .uploadAttachmentsForMessage(
                    defaultChannelType,
                    defaultChannelId,
                    defaultMessageSentAttachments.id,
                    chatDomainImpl,
                    chatClient
                )

            verify(channelController, never()).uploadAttachments(any())
        }

    @Test
    fun `when there's a pending attachment, it should be uploaded`() = runBlockingTest {
        UploadAttachmentsWorker(mock())
            .uploadAttachmentsForMessage(
                defaultChannelType,
                defaultChannelId,
                defaultMessagePendingAttachments.id,
                chatDomainImpl,
                chatClient
            )

        verify(channelController).uploadAttachments(any())
    }

    @Test
    fun `when not all attachments have state as success, it should return error`() = runBlockingTest {
        whenever(channelController.uploadAttachments(eq(defaultMessagePendingAttachments)))
            .doReturn(attachmentsPending)

        val result = UploadAttachmentsWorker(mock())
            .uploadAttachmentsForMessage(
                defaultChannelType,
                defaultChannelId,
                defaultMessagePendingAttachments.id,
                chatDomainImpl,
                chatClient
            )

        result.isError.shouldBeTrue()
    }

    @Test
    fun `when user can not be set, it should return an error`() = runBlockingTest {
        whenever(chatClient.containsStoredCredentials()) doReturn false

        val result = UploadAttachmentsWorker(mock())
            .uploadAttachmentsForMessage(
                defaultChannelType,
                defaultChannelId,
                defaultMessagePendingAttachments.id,
                chatDomainImpl,
                chatClient
            )

        result.isError.shouldBeTrue()
    }
}
