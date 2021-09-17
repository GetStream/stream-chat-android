package io.getstream.chat.android.offline

import android.os.Handler
import com.nhaarman.mockitokotlin2.any
import com.nhaarman.mockitokotlin2.doAnswer
import com.nhaarman.mockitokotlin2.doReturn
import com.nhaarman.mockitokotlin2.mock
import io.getstream.chat.android.client.ChatClient
import io.getstream.chat.android.client.models.Attachment
import io.getstream.chat.android.client.utils.Result
import io.getstream.chat.android.client.utils.SyncStatus
import io.getstream.chat.android.offline.channel.ChannelController
import io.getstream.chat.android.offline.repository.RepositoryFacade
import io.getstream.chat.android.offline.repository.database.ChatDatabase
import io.getstream.chat.android.test.TestCall
import io.getstream.chat.android.test.TestCoroutineExtension
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.test.runBlockingTest
import org.amshove.kluent.shouldBeEqualTo
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.extension.RegisterExtension

@ExperimentalCoroutinesApi
internal class ChatDomainImplTest {

    companion object {
        @JvmField
        @RegisterExtension
        val testCoroutines = TestCoroutineExtension()
    }

    @Test
    fun `When create a new channel without author should set current user as author and return channel with author`() =
        testCoroutines.scope.runBlockingTest {
            val newChannel = randomChannel(cid = "channelType:channelId", createdBy = randomUser())
            val sut = Fixture().get()

            val result = sut.createNewChannel(newChannel)

            result.isSuccess shouldBeEqualTo true
            result.data().createdBy shouldBeEqualTo sut.user.value
        }

    @Test
    fun `Given a sync needed message with uploaded attachment Should perform retry correctly`() {
        testCoroutines.scope.runBlockingTest {
            val syncNeededMessageWithSuccessAttachment = randomMessage(
                syncStatus = SyncStatus.SYNC_NEEDED,
                attachments = mutableListOf(randomAttachment { uploadState = Attachment.UploadState.Success }),
            )
            val client = mock<ChatClient> {
                on { it.channel(any()) } doAnswer {
                    mock {
                        on { deleteMessage(any(), any()) } doAnswer {
                            TestCall(Result.success(syncNeededMessageWithSuccessAttachment))
                        }
                    }
                }
            }
            val repositoryFacade = mock<RepositoryFacade> {
                onBlocking { selectMessagesSyncNeeded() } doReturn listOf(syncNeededMessageWithSuccessAttachment)
                onBlocking { selectMessagesWaitForAttachments() } doReturn emptyList()
            }
            val sut = Fixture(client)
                .withRepositoryFacade(repositoryFacade)
                .withActiveChannel(cid = syncNeededMessageWithSuccessAttachment.cid, channelController = mock())
                .get()

            val result = sut.retryMessages()

            result.size shouldBeEqualTo 1
            result.first() shouldBeEqualTo syncNeededMessageWithSuccessAttachment
        }
    }

    @Test
    fun `Given an awaiting attachments message Should perform retry correctly`() {
        testCoroutines.scope.runBlockingTest {
            val awaitingAttachmentsMessage = randomMessage(
                syncStatus = SyncStatus.AWAITING_ATTACHMENTS,
                attachments = mutableListOf(
                    randomAttachment { uploadState = Attachment.UploadState.InProgress },
                    randomAttachment { uploadState = Attachment.UploadState.Success },
                ),
            )
            val repositoryFacade = mock<RepositoryFacade> {
                onBlocking { selectMessagesSyncNeeded() } doReturn emptyList()
                onBlocking { selectMessagesWaitForAttachments() } doReturn listOf(awaitingAttachmentsMessage)
            }
            val sut = Fixture()
                .withRepositoryFacade(repositoryFacade)
                .withActiveChannel(cid = awaitingAttachmentsMessage.cid, channelController = mock())
                .get()

            val result = sut.retryMessages()

            result.size shouldBeEqualTo 1
            result.first() shouldBeEqualTo awaitingAttachmentsMessage
        }
    }

    @Test
    fun `Given a message without attachments Should perform retry correctly`() {
        testCoroutines.scope.runBlockingTest {
            val message = randomMessage(syncStatus = SyncStatus.SYNC_NEEDED)
            val client = mock<ChatClient> {
                on { it.channel(any()) } doAnswer {
                    mock {
                        on { deleteMessage(any(), any()) } doAnswer {
                            TestCall(Result.success(message))
                        }
                    }
                }
            }
            val repositoryFacade = mock<RepositoryFacade> {
                onBlocking { selectMessagesSyncNeeded() } doReturn listOf(message)
                onBlocking { selectMessagesWaitForAttachments() } doReturn emptyList()
            }
            val sut = Fixture(client)
                .withRepositoryFacade(repositoryFacade)
                .withActiveChannel(cid = message.cid, channelController = mock())
                .get()

            val result = sut.retryMessages()

            result.size shouldBeEqualTo 1
            result.first() shouldBeEqualTo message
        }
    }

    private class Fixture(client: ChatClient = mock { on { it.channel(any()) } doReturn mock() }) {
        private val db: ChatDatabase = mock {
            on { userDao() } doReturn mock()
            on { channelConfigDao() } doReturn mock()
            on { channelStateDao() } doReturn mock()
            on { queryChannelsDao() } doReturn mock()
            on { messageDao() } doReturn mock()
            on { reactionDao() } doReturn mock()
            on { syncStateDao() } doReturn mock()
            on { attachmentDao() } doReturn mock()
        }
        private val handler: Handler = mock()
        private val offlineEnabled = true
        private val userPresence = true
        private val recoveryEnabled = true

        private val chatDomainImpl = ChatDomainImpl(
            client,
            db,
            handler,
            offlineEnabled,
            userPresence,
            recoveryEnabled,
            false,
            mock(),
        ).also {
            it.setUser(randomUser())
        }

        fun withRepositoryFacade(repositoryFacade: RepositoryFacade) = apply {
            chatDomainImpl.repos = repositoryFacade
        }

        fun withActiveChannel(cid: String, channelController: ChannelController) = apply {
            chatDomainImpl.addActiveChannel(cid = cid, channelController = channelController)
        }

        fun get(): ChatDomainImpl = chatDomainImpl
    }
}
