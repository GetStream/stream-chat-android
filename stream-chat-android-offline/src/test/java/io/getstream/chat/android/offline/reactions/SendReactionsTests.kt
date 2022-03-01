package io.getstream.chat.android.offline.reactions

import com.nhaarman.mockitokotlin2.doReturn
import com.nhaarman.mockitokotlin2.mock
import com.nhaarman.mockitokotlin2.whenever
import io.getstream.chat.android.client.experimental.plugin.listeners.SendReactionListener
import io.getstream.chat.android.client.extensions.cidToTypeAndId
import io.getstream.chat.android.client.models.Message
import io.getstream.chat.android.client.models.Reaction
import io.getstream.chat.android.client.models.User
import io.getstream.chat.android.client.utils.SyncStatus
import io.getstream.chat.android.offline.ChatDomain
import io.getstream.chat.android.offline.ChatDomainImpl
import io.getstream.chat.android.offline.experimental.channel.state.toMutableState
import io.getstream.chat.android.offline.experimental.global.GlobalState
import io.getstream.chat.android.offline.experimental.plugin.listener.SendReactionListenerImpl
import io.getstream.chat.android.offline.experimental.plugin.logic.LogicRegistry
import io.getstream.chat.android.offline.experimental.plugin.state.StateRegistry
import io.getstream.chat.android.offline.extensions.addMyReaction
import io.getstream.chat.android.offline.repository.RepositoryFacade
import io.getstream.chat.android.test.TestCoroutineExtension
import io.getstream.chat.android.test.randomCID
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.test.runBlockingTest
import org.amshove.kluent.`should be equal to`
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.extension.RegisterExtension

@ExperimentalCoroutinesApi
internal class SendReactionsTests {

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

    @Test
    fun `When adding reaction Should add it to own and latest reactions`() = runBlockingTest {
        val cid = randomCID()
        val message = Message().apply {
            myReactions.forEach(::addMyReaction)
        }
        val reactionToAdd = Reaction().apply {
            userId = currentUser.id
            type = "type3"
            score = 789
            messageId = message.id
            user = currentUser
            syncStatus = SyncStatus.SYNC_NEEDED
        }
        val (sut, stateRegistry) = Fixture(testCoroutines.scope, currentUser)
            .givenMessageWithReactions(message)
            .get()

        sut.onSendReactionRequest(cid = cid, reaction = reactionToAdd, enforceUnique = false, currentUser = currentUser)

        val (channelType, channelId) = cid.cidToTypeAndId()
        val channelState = stateRegistry.channel(channelType = channelType, channelId = channelId).toMutableState()
        val result = channelState._messages.first()[message.id]
        result!!.ownReactions.size `should be equal to` myReactions.size + 1
        result.ownReactions.contains(reactionToAdd) `should be equal to` true
        result.latestReactions.size `should be equal to` myReactions.size + 1
        result.latestReactions.contains(reactionToAdd) `should be equal to` true
    }

    @Test
    fun `When adding reaction with enforce unique Should remove current user's other reactions`() = runBlockingTest {
        val cid = randomCID()
        val message = Message().apply {
            myReactions.forEach(::addMyReaction)
        }
        val reactionToAdd = Reaction().apply {
            userId = currentUser.id
            type = "type3"
            score = 789
            messageId = message.id
            user = currentUser
            syncStatus = SyncStatus.SYNC_NEEDED
            enforceUnique = true
        }
        val (sut, stateRegistry) = Fixture(testCoroutines.scope, currentUser)
            .givenMessageWithReactions(message)
            .get()

        sut.onSendReactionRequest(cid = cid, reaction = reactionToAdd, enforceUnique = true, currentUser = currentUser)

        val (channelType, channelId) = cid.cidToTypeAndId()
        val channelState = stateRegistry.channel(channelType = channelType, channelId = channelId).toMutableState()
        val result = channelState._messages.first()[message.id]
        result!!.ownReactions.size `should be equal to` 1
        result.ownReactions.first() `should be equal to` reactionToAdd
        result.latestReactions.size `should be equal to` 1
        result.latestReactions.contains(reactionToAdd) `should be equal to` true
    }

    @Test
    fun `When adding reaction with enforce unique Should properly update reactions count`() = runBlockingTest {
        val cid = randomCID()
        val message = Message().apply {
            myReactions.forEach(::addMyReaction)
        }
        val newReaction = Reaction().apply {
            userId = currentUser.id
            type = "type3"
            score = 789
        }
        val (sut, stateRegistry) = Fixture(testCoroutines.scope, currentUser)
            .givenMessageWithReactions(message)
            .get()

        sut.onSendReactionRequest(cid = cid, reaction = newReaction, enforceUnique = true, currentUser = currentUser)

        val (channelType, channelId) = cid.cidToTypeAndId()
        val channelState = stateRegistry.channel(channelType = channelType, channelId = channelId).toMutableState()
        val result = channelState._messages.first()[message.id]
        result!!.reactionCounts[newReaction.type] `should be equal to` 1
    }

    @Test
    fun `When adding reaction with enforce unique Should properly update reactions score`() = runBlockingTest {
        val cid = randomCID()
        val message = Message().apply {
            myReactions.forEach(::addMyReaction)
        }
        val newReaction = Reaction().apply {
            userId = currentUser.id
            type = "type3"
            score = 789
        }
        val (sut, stateRegistry) = Fixture(testCoroutines.scope, currentUser)
            .givenMessageWithReactions(message)
            .get()

        sut.onSendReactionRequest(cid = cid, reaction = newReaction, enforceUnique = true, currentUser = currentUser)

        val (channelType, channelId) = cid.cidToTypeAndId()
        val channelState = stateRegistry.channel(channelType = channelType, channelId = channelId).toMutableState()
        val result = channelState._messages.first()[message.id]
        result!!.reactionScores[newReaction.type] `should be equal to` newReaction.score
    }

    private class Fixture(scope: CoroutineScope, user: User) {

        private val stateRegistry = StateRegistry.getOrCreate(
            job = mock(),
            scope = scope,
            userStateFlow = MutableStateFlow(user),
            messageRepository = mock(),
            latestUsers = MutableStateFlow(emptyMap()),
        )
        private val logicRegistry = LogicRegistry.getOrCreate(stateRegistry)

        private var repos = mock<RepositoryFacade>()
        private val globalState = mock<GlobalState>()

        init {
            ChatDomain.instance = mock<ChatDomainImpl>()
        }

        suspend fun givenMessageWithReactions(message: Message): Fixture = apply {
            whenever(repos.selectMessage(message.id)) doReturn message
        }

        fun get(): Pair<SendReactionListener, StateRegistry> =
            SendReactionListenerImpl(logicRegistry, globalState, repos) to stateRegistry
    }
}
