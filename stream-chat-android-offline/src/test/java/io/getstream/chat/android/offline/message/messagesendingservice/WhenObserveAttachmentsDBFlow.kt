package io.getstream.chat.android.offline.message.messagesendingservice

import com.nhaarman.mockitokotlin2.any
import com.nhaarman.mockitokotlin2.argThat
import com.nhaarman.mockitokotlin2.doAnswer
import com.nhaarman.mockitokotlin2.doReturn
import com.nhaarman.mockitokotlin2.mock
import com.nhaarman.mockitokotlin2.verify
import com.nhaarman.mockitokotlin2.whenever
import io.getstream.chat.android.client.channel.ChannelClient
import io.getstream.chat.android.client.models.Attachment
import io.getstream.chat.android.client.models.Message
import io.getstream.chat.android.offline.ChatDomainImpl
import io.getstream.chat.android.offline.channel.ChannelController
import io.getstream.chat.android.offline.message.MessageSendingService
import io.getstream.chat.android.offline.message.attachment.UploadAttachmentsWorker
import io.getstream.chat.android.offline.randomAttachment
import io.getstream.chat.android.offline.randomMessage
import io.getstream.chat.android.offline.randomUser
import io.getstream.chat.android.offline.repository.RepositoryFacade
import io.getstream.chat.android.offline.utils.CallRetryService
import io.getstream.chat.android.offline.utils.DefaultRetryPolicy
import io.getstream.chat.android.test.TestCoroutineRule
import io.getstream.chat.android.test.asCall
import io.getstream.chat.android.test.positiveRandomLong
import io.getstream.chat.android.test.randomString
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.runBlocking
import kotlinx.coroutines.test.runBlockingTest
import org.junit.jupiter.api.Test
import org.mockito.Mockito

internal class WhenObserveAttachmentsDBFlow {

    private val testCoroutineRule = TestCoroutineRule()

    @Test
    fun `Given db attachments flow is observed And all attachments has success upload state Should send message through BE`() =
        testCoroutineRule.scope.runBlockingTest {
            val attachment = randomAttachment {
                title = "attachmentTitle"
                uploadState = Attachment.UploadState.Idle
            }
            val channelClient = mock<ChannelClient>()
            val sendMessage = randomMessage(id = "messageId1", attachments = mutableListOf(attachment))
            val flow = Fixture()
                .givenChannelClient(channelClient)
                .givenSendMessageCalled(sendMessage)
                .get()

            flow.value = listOf(attachment.copy(uploadState = Attachment.UploadState.Success))

            verify(channelClient).sendMessage(argThat { id == "messageId1" && attachments.first().title == "attachmentTitle" })
        }

    @Test
    fun `Given db attachments flow is observed And not all attachments has success upload state Should not send message through BE`() =
        testCoroutineRule.scope.runBlockingTest {
            val attachment1 = randomAttachment { uploadState = Attachment.UploadState.Idle }
            val attachment2 = randomAttachment {
                uploadState = Attachment.UploadState.InProgress(positiveRandomLong(30), positiveRandomLong(50) + 30)
            }
            val sendMessage = randomMessage(attachments = mutableListOf(attachment1, attachment2))
            val channelClient = mock<ChannelClient>()
            val flow = Fixture()
                .givenChannelClient(channelClient)
                .givenSendMessageCalled(sendMessage)
                .get()

            flow.value = listOf(
                attachment1.copy(uploadState = Attachment.UploadState.InProgress(positiveRandomLong(90), positiveRandomLong(10) + 90)),
                attachment2.copy(uploadState = Attachment.UploadState.Success)
            )

            Mockito.verifyNoInteractions(channelClient)
        }

    private inner class Fixture {
        private val flow = MutableStateFlow<List<Attachment>>(emptyList())
        private val repositoryFacade = mock<RepositoryFacade>()
        private var channelClient = mock<ChannelClient>()
        private val chatDomainImpl = mock<ChatDomainImpl> {
            on(it.user) doReturn MutableStateFlow(randomUser())
            on(it.repos) doReturn repositoryFacade
            on(it.scope) doReturn testCoroutineRule.scope
            on { generateMessageId() } doReturn randomString()
            on { getActiveQueries() } doReturn emptyList()
            on { callRetryService() } doReturn CallRetryService(DefaultRetryPolicy(), mock())
        }
        private var channelController = mock<ChannelController>()
        private var uploadAttachmentsWorker = mock<UploadAttachmentsWorker>()

        private val messageSendingService: MessageSendingService by lazy {
            runBlocking {
                whenever(channelController.handleSendMessageSuccess(any())) doAnswer { invocationOnMock ->
                    invocationOnMock.arguments.first() as Message
                }
            }
            MessageSendingService(chatDomainImpl, channelController, channelClient, uploadAttachmentsWorker)
        }

        fun givenChannelClient(channelClient: ChannelClient) = apply {
            this.channelClient = channelClient
        }

        suspend fun givenSendMessageCalled(message: Message) = apply {
            whenever(chatDomainImpl.online) doReturn MutableStateFlow(true)
            whenever(channelClient.sendMessage(any())) doReturn message.asCall()
            whenever(repositoryFacade.observeAttachmentsForMessage(any())) doReturn flow
            messageSendingService.sendMessage(message)
        }

        fun get(): MutableStateFlow<List<Attachment>> {
            return flow
        }
    }
}
