package io.getstream.chat.android.offline.channel.controller

import com.nhaarman.mockitokotlin2.any
import com.nhaarman.mockitokotlin2.doAnswer
import com.nhaarman.mockitokotlin2.doReturn
import com.nhaarman.mockitokotlin2.inOrder
import com.nhaarman.mockitokotlin2.mock
import com.nhaarman.mockitokotlin2.verify
import com.nhaarman.mockitokotlin2.whenever
import io.getstream.chat.android.client.ChatClient
import io.getstream.chat.android.client.errors.ChatError
import io.getstream.chat.android.client.errors.ChatNetworkError
import io.getstream.chat.android.client.models.Message
import io.getstream.chat.android.client.models.Reaction
import io.getstream.chat.android.client.models.User
import io.getstream.chat.android.client.utils.Result
import io.getstream.chat.android.client.utils.SyncStatus
import io.getstream.chat.android.offline.ChatDomainImpl
import io.getstream.chat.android.offline.channel.ChannelController
import io.getstream.chat.android.offline.extensions.addMyReaction
import io.getstream.chat.android.offline.repository.RepositoryFacade
import io.getstream.chat.android.test.TestCoroutineExtension
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.Job
import kotlinx.coroutines.runBlocking
import kotlinx.coroutines.test.runBlockingTest
import org.amshove.kluent.`should be equal to`
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.extension.RegisterExtension

@ExperimentalCoroutinesApi
internal class ChannelControllerReactionsTest {

    companion object {
        @JvmField
        @RegisterExtension
        val testCoroutines = TestCoroutineExtension()
    }

    private val currentUser = User()
    private val myReactions: List<Reaction> = listOf(
        Reaction().apply {
            userId = currentUser.id
            type = "type1"
            score = 123
        },
        Reaction().apply {
            userId = currentUser.id
            type = "type2"
            score = 234
        },
    )

    private val mockReaction: Reaction = mock {
        on(it.messageId) doReturn ""
        on(it.userId) doReturn currentUser.id
        on(it.type) doReturn "type1"
    }

    private val newReaction = Reaction().apply {
        userId = currentUser.id
        type = "type3"
        score = 789
    }

    @Test
    fun `when add reaction should add it to own and latest reactions`() =
        runBlockingTest {
            val sut = Fixture(testCoroutines.scope, currentUser)
                .givenMockedRepositories()
                .givenMessageWithReactions(myReactions)
                .get()

            sut.sendReaction(newReaction, enforceUnique = false)

            val result = sut.messages.value.first()
            result.ownReactions.size `should be equal to` myReactions.size + 1
            result.ownReactions.contains(newReaction) `should be equal to` true
            result.latestReactions.size `should be equal to` myReactions.size + 1
            result.latestReactions.contains(newReaction) `should be equal to` true
        }

    @Test
    fun `when add reaction with enforce unique should remove current user other reactions`() =
        runBlockingTest {
            val sut = Fixture(testCoroutines.scope, currentUser)
                .givenMockedRepositories()
                .givenMessageWithReactions(myReactions)
                .get()

            sut.sendReaction(newReaction, enforceUnique = true)

            val result = sut.messages.value.first()
            result.ownReactions.size `should be equal to` 1
            result.ownReactions.first() `should be equal to` newReaction
            result.latestReactions.size `should be equal to` 1
            result.latestReactions.contains(newReaction) `should be equal to` true
        }

    @Test
    fun `when add reaction with enforce unique should update reactions count`() =
        runBlockingTest {
            val sut = Fixture(testCoroutines.scope, currentUser)
                .givenMockedRepositories()
                .givenMessageWithReactions(myReactions)
                .get()

            sut.sendReaction(newReaction, enforceUnique = true)

            val result = sut.messages.value.first()
            result.reactionCounts[newReaction.type] `should be equal to` 1
        }

    @Test
    fun `when add reaction with enforce unique should update reactions score`() =
        runBlockingTest {
            val sut = Fixture(testCoroutines.scope, currentUser)
                .givenMockedRepositories()
                .givenMessageWithReactions(myReactions)
                .get()

            sut.sendReaction(newReaction, enforceUnique = true)

            val result = sut.messages.value.first()
            result.reactionScores[newReaction.type] `should be equal to` newReaction.score
        }

    @Test
    fun `when delete reaction should remove it from own and latest reactions`() =
        runBlockingTest {
            val sut = Fixture(testCoroutines.scope, currentUser)
                .givenMockedRepositories()
                .givenMessageWithReactions(myReactions)
                .get()

            val deletedReaction = myReactions.first()

            sut.deleteReaction(deletedReaction)

            val result = sut.messages.value.first()
            result.ownReactions.size `should be equal to` myReactions.size - 1
            result.ownReactions.contains(deletedReaction) `should be equal to` false
            result.latestReactions.contains(deletedReaction) `should be equal to` false
        }

    @Test
    fun `when deleting a reaction while offline, status must be right and reaction inserted`() =
        runBlockingTest {
            val sut = Fixture(testCoroutines.scope, currentUser)
                .givenMockedRepositories()
                .givenMessageWithReactions(myReactions)
                .get()

            whenever(sut.domainImpl.isOnline()) doReturn false

            val result = sut.deleteReaction(mockReaction)

            inOrder(mockReaction) {
                verify(mockReaction).syncStatus = SyncStatus.IN_PROGRESS
                verify(mockReaction).syncStatus = SyncStatus.SYNC_NEEDED
            }

            result.isSuccess `should be equal to` true

            verify(sut.domainImpl.repos).insertReaction(mockReaction)
        }

    @Test
    fun `when deleting reaction FAILED, the sync status is right and reaction is inserted`() =
        runBlockingTest {
            val sut = Fixture(testCoroutines.scope, currentUser)
                .givenMockedRepositories()
                .givenMessageWithReactions(myReactions)
                .get()

            whenever(sut.domainImpl.isOnline()) doReturn true
            whenever(sut.domainImpl.runAndRetry<Message>(any())) doAnswer { Result(ChatError()) }

            val result = sut.deleteReaction(mockReaction)

            inOrder(mockReaction) {
                verify(mockReaction).syncStatus = SyncStatus.IN_PROGRESS
                verify(mockReaction).syncStatus = SyncStatus.SYNC_NEEDED
            }

            result.isError `should be equal to` true
        }

    @Test
    fun `when deleting reaction failed PERMANENTLY, the sync status is right and reaction is inserted`() =
        runBlockingTest {
            val sut = Fixture(testCoroutines.scope, currentUser)
                .givenMockedRepositories()
                .givenMessageWithReactions(myReactions)
                .get()

            val chatError = ChatNetworkError.create(
                description = "error",
                streamCode = 401,
                statusCode = 401
            )

            whenever(sut.domainImpl.isOnline()) doReturn true
            whenever(sut.domainImpl.runAndRetry<Message>(any())) doAnswer { Result(chatError) }

            val result = sut.deleteReaction(mockReaction)

            inOrder(mockReaction) {
                verify(mockReaction).syncStatus = SyncStatus.IN_PROGRESS
                verify(mockReaction).syncStatus = SyncStatus.FAILED_PERMANENTLY
            }

            result.isError `should be equal to` true
        }

    @Test
    fun `when deleting a reaction SUCCESSFULLY, the sync status is right and message is inserted`() =
        runBlockingTest {
            val sut = Fixture(testCoroutines.scope, currentUser)
                .givenMockedRepositories()
                .givenMessageWithReactions(myReactions)
                .get()

            whenever(sut.domainImpl.isOnline()) doReturn true
            whenever(sut.domainImpl.runAndRetry<Message>(any())) doAnswer { Result(Message()) }

            val result = sut.deleteReaction(mockReaction)

            inOrder(mockReaction) {
                verify(mockReaction).syncStatus = SyncStatus.IN_PROGRESS
                verify(mockReaction).syncStatus = SyncStatus.COMPLETED
            }

            result.isSuccess `should be equal to` true
        }

    private class Fixture(scope: CoroutineScope, user: User) {
        private val repos: RepositoryFacade = mock()
        private val channelControllerImpl: ChannelController

        val chatClient: ChatClient = mock()
        val chatDomainImpl: ChatDomainImpl = mock()

        init {
            whenever(chatDomainImpl.currentUser) doReturn user
            whenever(chatDomainImpl.job) doReturn Job()
            whenever(chatDomainImpl.scope) doReturn scope
            whenever(chatDomainImpl.repos) doReturn repos
            channelControllerImpl = ChannelController("channelType", "channelId", chatClient, chatDomainImpl)
        }

        fun givenMockedRepositories(): Fixture {
            runBlocking {
                whenever(repos.selectUserReactionsToMessage(any(), any())) doReturn emptyList()
            }
            return this
        }

        fun givenMessageWithReactions(myReactions: List<Reaction>): Fixture {
            runBlocking {
                val message = Message().apply {
                    myReactions.forEach(::addMyReaction)
                }
                channelControllerImpl.upsertMessage(message)
            }
            return this
        }

        fun get(): ChannelController = channelControllerImpl
    }
}
