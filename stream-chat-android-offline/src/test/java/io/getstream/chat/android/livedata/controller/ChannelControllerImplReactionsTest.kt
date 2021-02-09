package io.getstream.chat.android.livedata.controller

import com.nhaarman.mockitokotlin2.any
import com.nhaarman.mockitokotlin2.doReturn
import com.nhaarman.mockitokotlin2.mock
import com.nhaarman.mockitokotlin2.whenever
import io.getstream.chat.android.client.ChatClient
import io.getstream.chat.android.client.models.Message
import io.getstream.chat.android.client.models.Reaction
import io.getstream.chat.android.client.models.User
import io.getstream.chat.android.livedata.ChatDomainImpl
import io.getstream.chat.android.livedata.extensions.addMyReaction
import io.getstream.chat.android.livedata.repository.RepositoryFacade
import io.getstream.chat.android.test.InstantTaskExecutorExtension
import io.getstream.chat.android.test.TestCoroutineExtension
import io.getstream.chat.android.test.getOrAwaitValue
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.Job
import kotlinx.coroutines.runBlocking
import kotlinx.coroutines.test.runBlockingTest
import org.amshove.kluent.`should be equal to`
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.extension.ExtendWith
import org.junit.jupiter.api.extension.RegisterExtension

@ExperimentalCoroutinesApi
@ExtendWith(InstantTaskExecutorExtension::class)
internal class ChannelControllerImplReactionsTest {

    @JvmField
    @RegisterExtension
    val testCoroutines = TestCoroutineExtension()

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

            val result = sut.messages.getOrAwaitValue().first()
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

            val result = sut.messages.getOrAwaitValue().first()
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

            val result = sut.messages.getOrAwaitValue().first()
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

            val result = sut.messages.getOrAwaitValue().first()
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

            val result = sut.messages.getOrAwaitValue().first()
            result.ownReactions.size `should be equal to` myReactions.size - 1
            result.ownReactions.contains(deletedReaction) `should be equal to` false
            result.latestReactions.contains(deletedReaction) `should be equal to` false
        }

    private class Fixture(scope: CoroutineScope, user: User) {
        private val chatClient: ChatClient = mock()
        private val chatDomainImpl: ChatDomainImpl = mock()
        private val repos: RepositoryFacade = mock()
        private val channelControllerImpl: ChannelControllerImpl

        init {
            whenever(chatDomainImpl.currentUser) doReturn user
            whenever(chatDomainImpl.job) doReturn Job()
            whenever(chatDomainImpl.scope) doReturn scope
            whenever(chatDomainImpl.repos) doReturn repos
            channelControllerImpl = ChannelControllerImpl("channelType", "channelId", chatClient, chatDomainImpl)
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

        fun get(): ChannelControllerImpl = channelControllerImpl
    }
}
