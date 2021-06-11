package io.getstream.chat.android.offline.message.messagesendingservice

import com.google.common.truth.Truth
import com.nhaarman.mockitokotlin2.any
import com.nhaarman.mockitokotlin2.argThat
import com.nhaarman.mockitokotlin2.doAnswer
import com.nhaarman.mockitokotlin2.doReturn
import com.nhaarman.mockitokotlin2.eq
import com.nhaarman.mockitokotlin2.mock
import com.nhaarman.mockitokotlin2.verify
import com.nhaarman.mockitokotlin2.whenever
import io.getstream.chat.android.client.channel.ChannelClient
import io.getstream.chat.android.client.models.Message
import io.getstream.chat.android.client.utils.SyncStatus
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
import io.getstream.chat.android.test.asCall
import io.getstream.chat.android.test.randomDate
import io.getstream.chat.android.test.randomString
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.test.TestCoroutineScope
import kotlinx.coroutines.test.runBlockingTest
import org.junit.jupiter.api.Test

internal class WhenSendNewMessage {
    @Test
    fun `Given message without attachments And offline Should return message as result with right data`() =
        runBlockingTest {
            val message = randomMessage(
                cid = "",
                id = "",
                attachments = mutableListOf(),
                type = "1232423432",
                text = "text123",
                createdLocallyAt = null,
                syncStatus = SyncStatus.COMPLETED
            )
            val sut = Fixture().givenOffline().givenCid("cid1").get()

            val result = sut.sendNewMessage(message)

            Truth.assertThat(result.isSuccess).isTrue()
            Truth.assertThat(result.data().cid).isEqualTo("cid1")
            Truth.assertThat(result.data().id).isNotEmpty()
            Truth.assertThat(result.data().type).isEqualTo("regular")
            Truth.assertThat(result.data().text).isEqualTo("text123")
            Truth.assertThat(result.data().createdLocallyAt).isNotNull()
            Truth.assertThat(result.data().syncStatus).isEqualTo(SyncStatus.SYNC_NEEDED)
        }

    @Test
    fun `Given message without attachments And offline Should update channel controller and repository`() =
        runBlockingTest {
            val message = randomMessage(id = "messageId1", cid = "cid1", attachments = mutableListOf())
            val channelController = mock<ChannelController>()
            val repositoryFacade = mock<RepositoryFacade>()
            val sut = Fixture()
                .givenChannelController(channelController)
                .givenRepositories(repositoryFacade)
                .givenOffline()
                .givenCid("cid1")
                .get()

            sut.sendNewMessage(message)

            verify(channelController).upsertMessage(argThat { id == "messageId1" })
            verify(repositoryFacade).insertMessage(argThat { id == "messageId1" }, eq(false))
            verify(repositoryFacade).updateLastMessageForChannel(eq("cid1"), argThat { id == "messageId1" })
        }

    @Test
    fun `Given message without attachments And online And success network call Should send to BE`() =
        runBlockingTest {
            val message =
                randomMessage(id = "messageId1", cid = "cid1", attachments = mutableListOf(), updatedAt = null)
            val channelClient = mock<ChannelClient>()
            val sut = Fixture()
                .givenOnline()
                .givenChannelClient(channelClient)
                .givenCid("cid1")
                .givenNetworkResponse(message)
                .get()

            sut.sendNewMessage(message)

            verify(channelClient).sendMessage(argThat { id == "messageId1" })
        }

    @Test
    fun `Given message without attachments And online And success network call Should return message from BE`() =
        runBlockingTest {
            val message =
                randomMessage(id = "messageId1", cid = "cid1", attachments = mutableListOf(), updatedAt = null)
            val networkMessage = message.copy(updatedAt = randomDate())
            val sut = Fixture()
                .givenOnline()
                .givenCid("cid1")
                .givenNetworkResponse(networkMessage)
                .get()

            val result = sut.sendNewMessage(message)

            Truth.assertThat(result.isSuccess).isTrue()
            Truth.assertThat(result.data()).isEqualTo(networkMessage)
        }

    @Test
    fun `Given message with attachments And online Should enqueue work to upload attachments`() =
        runBlockingTest {
            val message = randomMessage(id = "messageId1", attachments = mutableListOf(randomAttachment { }))
            val uploadWorker = mock<UploadAttachmentsWorker>()
            val sut = Fixture()
                .givenOnline()
                .givenCid("cid1")
                .givenChannelType("channelType")
                .givenChannelId("channelId")
                .givenAttachmentUploadWorker(uploadWorker)
                .get()

            sut.sendNewMessage(message)

            verify(uploadWorker).enqueueJob(eq("channelType"), eq("channelId"), eq("messageId1"))
        }

    private class Fixture {
        private var repositoryFacade = mock<RepositoryFacade>()
        private var channelClient = mock<ChannelClient>()
        private val chatDomainImpl = mock<ChatDomainImpl> {
            on(it.user) doReturn MutableStateFlow(randomUser())
            on(it.repos) doReturn repositoryFacade
            on(it.scope) doReturn TestCoroutineScope()
            on { generateMessageId() } doReturn randomString()
            on { getActiveQueries() } doReturn emptyList()
            on { callRetryService() } doReturn CallRetryService(DefaultRetryPolicy(), mock())
        }
        private var channelController = mock<ChannelController>()
        private var uploadAttachmentsWorker = mock<UploadAttachmentsWorker>()

        fun givenOffline() = apply {
            whenever(chatDomainImpl.online) doReturn MutableStateFlow(false)
        }

        fun givenOnline() = apply {
            whenever(chatDomainImpl.online) doReturn MutableStateFlow(true)
        }

        fun givenCid(cid: String) = apply {
            whenever(channelController.cid) doReturn cid
        }

        fun givenChannelController(channelController: ChannelController) = apply {
            this.channelController = channelController
        }

        fun givenRepositories(repositoryFacade: RepositoryFacade) = apply {
            this.repositoryFacade = repositoryFacade
            whenever(chatDomainImpl.repos) doReturn repositoryFacade
        }

        fun givenAttachmentUploadWorker(attachmentsWorker: UploadAttachmentsWorker) = apply {
            this.uploadAttachmentsWorker = attachmentsWorker
        }

        fun givenNetworkResponse(message: Message) = apply {
            whenever(channelClient.sendMessage(any())) doReturn message.asCall()
        }

        fun givenChannelClient(channelClient: ChannelClient) = apply {
            this.channelClient = channelClient
        }

        fun givenChannelType(channelType: String) = apply {
            whenever(channelController.channelType) doReturn channelType
        }

        fun givenChannelId(channelId: String) = apply {
            whenever(channelController.channelId) doReturn channelId
        }

        suspend fun get(): MessageSendingService {
            whenever(channelController.handleSendMessageSuccess(any())) doAnswer { invocationOnMock -> invocationOnMock.arguments.first() as Message }
            return MessageSendingService(chatDomainImpl, channelController, channelClient, uploadAttachmentsWorker)
        }
    }
}
