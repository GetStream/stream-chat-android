package io.getstream.chat.android.offline.channel.controller

import com.nhaarman.mockitokotlin2.doReturn
import com.nhaarman.mockitokotlin2.mock
import com.nhaarman.mockitokotlin2.whenever
import io.getstream.chat.android.client.ChatClient
import io.getstream.chat.android.client.models.Message
import io.getstream.chat.android.client.models.Reaction
import io.getstream.chat.android.client.models.User
import io.getstream.chat.android.offline.ChatDomainImpl
import io.getstream.chat.android.offline.SynchronizedCoroutineTest
import io.getstream.chat.android.offline.channel.ChannelController
import io.getstream.chat.android.offline.experimental.channel.logic.ChannelLogic
import io.getstream.chat.android.offline.experimental.channel.state.ChannelMutableState
import io.getstream.chat.android.offline.extensions.addMyReaction
import io.getstream.chat.android.offline.repository.RepositoryFacade
import io.getstream.chat.android.offline.utils.NoRetryPolicy
import io.getstream.chat.android.test.TestCoroutineExtension
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.Job
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.runBlocking
import kotlinx.coroutines.test.TestCoroutineScope
import org.amshove.kluent.`should be equal to`
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.extension.RegisterExtension

@ExperimentalCoroutinesApi
internal class ChannelControllerReactionsTest : SynchronizedCoroutineTest {

    companion object {
        @JvmField
        @RegisterExtension
        val testCoroutines = TestCoroutineExtension()
    }

    override fun getTestScope(): TestCoroutineScope = testCoroutines.scope

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
        coroutineTest {
            val sut = Fixture(testCoroutines.scope, currentUser)
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
        coroutineTest {
            val sut = Fixture(testCoroutines.scope, currentUser)
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
        coroutineTest {
            val sut = Fixture(testCoroutines.scope, currentUser)
                .givenMessageWithReactions(myReactions)
                .get()

            sut.sendReaction(newReaction, enforceUnique = true)

            val result = sut.messages.value.first()
            result.reactionCounts[newReaction.type] `should be equal to` 1
        }

    @Test
    fun `when add reaction with enforce unique should update reactions score`() =
        coroutineTest {
            val sut = Fixture(testCoroutines.scope, currentUser)
                .givenMessageWithReactions(myReactions)
                .get()

            sut.sendReaction(newReaction, enforceUnique = true)

            val result = sut.messages.value.first()
            result.reactionScores[newReaction.type] `should be equal to` newReaction.score
        }

    private class Fixture(scope: CoroutineScope, user: User) {
        private val repos: RepositoryFacade = mock()
        private val channelControllerImpl: ChannelController
        val chatClient: ChatClient = mock {
            on(it.retryPolicy) doReturn NoRetryPolicy()
        }
        val chatDomainImpl: ChatDomainImpl = mock()

        init {
            val userFlow = MutableStateFlow(user)
            whenever(chatDomainImpl.user) doReturn userFlow
            whenever(chatDomainImpl.job) doReturn Job()
            whenever(chatDomainImpl.scope) doReturn scope
            whenever(chatDomainImpl.repos) doReturn repos
            val mutableState =
                ChannelMutableState(
                    "channelType",
                    "channelId",
                    scope,
                    userFlow,
                    MutableStateFlow(mapOf(user.id to user))
                )
            channelControllerImpl = ChannelController(
                mutableState,
                ChannelLogic(mutableState, chatDomainImpl),
                chatClient,
                chatDomainImpl,
                messageSendingServiceFactory = mock(),
            )
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
