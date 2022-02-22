package io.getstream.chat.android.offline.message.messagesendingservice

import com.nhaarman.mockitokotlin2.any
import com.nhaarman.mockitokotlin2.argThat
import com.nhaarman.mockitokotlin2.doAnswer
import com.nhaarman.mockitokotlin2.doReturn
import com.nhaarman.mockitokotlin2.eq
import com.nhaarman.mockitokotlin2.mock
import com.nhaarman.mockitokotlin2.never
import com.nhaarman.mockitokotlin2.verify
import com.nhaarman.mockitokotlin2.whenever
import io.getstream.chat.android.client.ChatClient
import io.getstream.chat.android.client.channel.ChannelClient
import io.getstream.chat.android.client.extensions.cidToTypeAndId
import io.getstream.chat.android.client.extensions.uploadId
import io.getstream.chat.android.client.models.Attachment
import io.getstream.chat.android.client.models.Message
import io.getstream.chat.android.client.utils.SyncStatus
import io.getstream.chat.android.offline.ChatDomainImpl
import io.getstream.chat.android.offline.channel.ChannelController
import io.getstream.chat.android.offline.experimental.channel.logic.ChannelLogic
import io.getstream.chat.android.offline.experimental.global.GlobalMutableState
import io.getstream.chat.android.offline.experimental.global.GlobalState
import io.getstream.chat.android.offline.experimental.plugin.logic.LogicRegistry
import io.getstream.chat.android.offline.message.MessageSendingService
import io.getstream.chat.android.offline.message.attachment.UploadAttachmentsWorker
import io.getstream.chat.android.offline.model.ConnectionState
import io.getstream.chat.android.offline.randomAttachment
import io.getstream.chat.android.offline.randomMessage
import io.getstream.chat.android.offline.randomUser
import io.getstream.chat.android.offline.repository.RepositoryFacade
import io.getstream.chat.android.offline.utils.NoRetryPolicy
import io.getstream.chat.android.test.TestCoroutineExtension
import io.getstream.chat.android.test.asCall
import io.getstream.chat.android.test.randomDate
import io.getstream.chat.android.test.randomString
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.test.runBlockingTest
import org.amshove.kluent.shouldBeEqualTo
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.extension.RegisterExtension

internal class WhenSendNewMessage {

    companion object {
        @JvmField
        @RegisterExtension
        val testCoroutines = TestCoroutineExtension()
    }

    @Test
    fun `Given message without attachments And offline Should update channel controller and repository`() =
        runBlockingTest {
            val message = randomMessage(id = "messageId1", cid = "test_type:test_channel", attachments = mutableListOf())
            val channel = mock<ChannelLogic>()
            val repositoryFacade = mock<RepositoryFacade>()
            val sut = Fixture()
                .givenChannelLogic(channel)
                .givenRepositories(repositoryFacade)
                .givenOffline()
                .givenCid("test_type:test_channel")
                .get()

            sut.sendNewMessage(message)

            verify(channel).upsertMessage(argThat { id == "messageId1" })
            verify(repositoryFacade).insertMessage(argThat { id == "messageId1" }, eq(false))
            verify(repositoryFacade).updateLastMessageForChannel(eq("test_type:test_channel"), argThat { id == "messageId1" })
        }

    @Test
    fun `Given message without attachments And online And success network call Should send to BE`() =
        runBlockingTest {
            val message =
                randomMessage(id = "messageId1", cid = "test_type:test_channel", attachments = mutableListOf(), updatedAt = null)
            val channelClient = mock<ChannelClient>()
            val sut = Fixture()
                .givenOnline()
                .givenChannelClient(channelClient)
                .givenCid("test_type:test_channel")
                .givenNetworkResponse(message)
                .get()

            sut.sendNewMessage(message)

            verify(channelClient).sendMessageInternal(argThat { id == "messageId1" })
        }

    @Test
    fun `Given message without attachments And online And success network call Should return message from BE`() =
        runBlockingTest {
            val message =
                randomMessage(id = "messageId1", cid = "test_type:test_channel", attachments = mutableListOf(), updatedAt = null)
            val networkMessage = message.copy(updatedAt = randomDate())
            val channelClient = mock<ChannelClient>()
            val sut = Fixture()
                .givenOnline()
                .givenCid("test_type:test_channel")
                .givenChannelClient(channelClient)
                .givenNetworkResponse(networkMessage)
                .get()

            val result = sut.sendNewMessage(message)

            result.isSuccess
            result.data() shouldBeEqualTo networkMessage.copy(syncStatus = SyncStatus.COMPLETED)
        }

    @Test
    fun `Given message with attachments And online Should enqueue work to upload attachments and Should not upload message`() =
        runBlockingTest {
            val message = randomMessage(id = "messageId1", attachments = mutableListOf(randomAttachment { }))
            val uploadWorker = mock<UploadAttachmentsWorker>()
            val channelClient = mock<ChannelClient>()
            val sut = Fixture()
                .givenOnline()
                .givenCid("test_type:test_channel")
                .givenChannelClient(channelClient)
                .givenAttachmentUploadWorker(uploadWorker)
                .get()

            sut.sendNewMessage(message)

            verify(uploadWorker).enqueueJob(eq("test_type"), eq("test_channel"), eq("messageId1"))
            verify(channelClient, never()).sendMessage(any())
        }

    @Test
    fun `Given message with uploadId and failed upload state Should insert message to DB and keep uploadId and update upload state to Idle`() =
        runBlockingTest {
            val message = randomMessage(
                attachments = mutableListOf(
                    randomAttachment {
                        uploadId = "uploadId123"
                        uploadState = Attachment.UploadState.Failed(mock())
                    }
                )
            )
            val repositoryFacade = mock<RepositoryFacade>()
            val channelClient = mock<ChannelClient>()
            val sut = Fixture().givenRepositories(repositoryFacade)
                .givenCid("test_type:test_channel")
                .givenChannelClient(channelClient)
                .givenNetworkResponse(message)
                .get()

            sut.sendNewMessage(message)

            verify(repositoryFacade).insertMessage(
                argThat { messageForInsert ->
                    messageForInsert.attachments.first()
                        .run { uploadId == "uploadId123" && uploadState == Attachment.UploadState.Idle }
                },
                eq(false)
            )
        }

    private class Fixture {
        private var repositoryFacade = mock<RepositoryFacade>()
        private val chatClient = mock<ChatClient> {
            on(it.retryPolicy) doReturn NoRetryPolicy()
        }
        private var channelClient = mock<ChannelClient>()
        private val chatDomainImpl = mock<ChatDomainImpl> {
            on(it.user) doReturn MutableStateFlow(randomUser())
            on(it.repos) doReturn repositoryFacade
            on(it.scope) doReturn testCoroutines.scope
            on { generateMessageId() } doReturn randomString()
            on { getActiveQueries() } doReturn emptyList()
        }
        private var channelController = mock<ChannelController>()
        private var uploadAttachmentsWorker = mock<UploadAttachmentsWorker>()

        /** For plugin approach */
        private var channel = mock<ChannelLogic>()
        private val logicRegistry = mock<LogicRegistry> {
            onGeneric { channel(any(), any()) } doReturn channel
        }
        private val globalState: GlobalState = mock<GlobalMutableState> {
            on(it.user) doReturn MutableStateFlow(randomUser())
        }

        fun givenOffline() = apply {
            whenever(chatDomainImpl.connectionState) doReturn MutableStateFlow(ConnectionState.OFFLINE)
            whenever(globalState.connectionState) doReturn MutableStateFlow(ConnectionState.OFFLINE)
        }

        fun givenOnline() = apply {
            whenever(chatDomainImpl.connectionState) doReturn MutableStateFlow(ConnectionState.CONNECTED)
            whenever(chatDomainImpl.isOnline()) doReturn true

            // For plugin approach.
            whenever(globalState.connectionState) doReturn MutableStateFlow(ConnectionState.CONNECTED)
            whenever(globalState.isOnline()) doReturn true
        }

        fun givenCid(cid: String) = apply {
            whenever(channel.cid) doReturn cid
            whenever(channelController.cid) doReturn cid
            val (channelType, channelId) = cid.cidToTypeAndId()
            whenever(channelController.channelType) doReturn channelType
            whenever(channelController.channelId) doReturn channelId
        }

        fun givenChannelController(channelController: ChannelController) = apply {
            this.channelController = channelController
        }

        fun givenChannelLogic(channel: ChannelLogic) = apply {
            this.channel = channel
            whenever(logicRegistry.channel(any(), any())) doReturn this.channel
        }

        fun givenRepositories(repositoryFacade: RepositoryFacade) = apply {
            this.repositoryFacade = repositoryFacade
            whenever(chatDomainImpl.repos) doReturn repositoryFacade
        }

        fun givenAttachmentUploadWorker(attachmentsWorker: UploadAttachmentsWorker) = apply {
            this.uploadAttachmentsWorker = attachmentsWorker
        }

        fun givenNetworkResponse(message: Message) = apply {
            whenever(channelClient.sendMessageInternal(any())) doReturn message.asCall()
        }

        fun givenChannelClient(channelClient: ChannelClient) = apply {
            whenever(chatClient.channel(any())) doReturn channelClient
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
            return MessageSendingService(
                logic = logicRegistry,
                globalState,
                channelController.channelType,
                channelController.channelId,
                chatDomainImpl.scope,
                chatDomainImpl.repos,
                uploadAttachmentsWorker,
                chatClient
            )
        }
    }
}
