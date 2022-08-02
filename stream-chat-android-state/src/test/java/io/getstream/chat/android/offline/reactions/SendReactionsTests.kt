/*
 * Copyright (c) 2014-2022 Stream.io Inc. All rights reserved.
 *
 * Licensed under the Stream License;
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *    https://github.com/GetStream/stream-chat-android/blob/main/LICENSE
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package io.getstream.chat.android.offline.reactions

import io.getstream.chat.android.client.ChatClient
import io.getstream.chat.android.client.extensions.cidToTypeAndId
import io.getstream.chat.android.client.extensions.internal.addMyReaction
import io.getstream.chat.android.client.models.Message
import io.getstream.chat.android.client.models.Reaction
import io.getstream.chat.android.client.models.User
import io.getstream.chat.android.client.persistance.repository.RepositoryFacade
import io.getstream.chat.android.client.plugin.listeners.SendReactionListener
import io.getstream.chat.android.client.setup.state.ClientState
import io.getstream.chat.android.client.utils.SyncStatus
import io.getstream.chat.android.offline.plugin.listener.internal.SendReactionListenerImpl
import io.getstream.chat.android.offline.plugin.logic.internal.LogicRegistry
import io.getstream.chat.android.offline.plugin.state.StateRegistry
import io.getstream.chat.android.offline.plugin.state.channel.internal.toMutableState
import io.getstream.chat.android.offline.plugin.state.global.internal.MutableGlobalState
import io.getstream.chat.android.test.TestCoroutineExtension
import io.getstream.chat.android.test.randomCID
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.test.runTest
import org.amshove.kluent.`should be equal to`
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.extension.RegisterExtension
import org.mockito.kotlin.doReturn
import org.mockito.kotlin.mock
import org.mockito.kotlin.whenever

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
    fun `When adding reaction Should add it to own and latest reactions`() = runTest {
        val cid = randomCID()
        val message = Message(cid = cid).apply {
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
        val result = channelState.rawMessages[message.id]
        result!!.ownReactions.size `should be equal to` myReactions.size + 1
        result.ownReactions.contains(reactionToAdd) `should be equal to` true
        result.latestReactions.size `should be equal to` myReactions.size + 1
        result.latestReactions.contains(reactionToAdd) `should be equal to` true
    }

    @Test
    fun `When adding reaction with enforce unique Should remove current user's other reactions`() = runTest {
        val cid = randomCID()
        val message = Message(cid = cid).apply {
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
        val result = channelState.rawMessages[message.id]
        result!!.ownReactions.size `should be equal to` 1
        result.ownReactions.first() `should be equal to` reactionToAdd
        result.latestReactions.size `should be equal to` 1
        result.latestReactions.contains(reactionToAdd) `should be equal to` true
    }

    @Test
    fun `When adding reaction with enforce unique Should properly update reactions count`() = runTest {
        val cid = randomCID()
        val message = Message(cid = cid).apply {
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
        val result = channelState.rawMessages[message.id]
        result!!.reactionCounts[newReaction.type] `should be equal to` 1
    }

    @Test
    fun `When adding reaction with enforce unique Should properly update reactions score`() = runTest {
        val cid = randomCID()
        val message = Message(cid = cid).apply {
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
        val result = channelState.rawMessages[message.id]
        result!!.reactionScores[newReaction.type] `should be equal to` newReaction.score
    }

    private class Fixture(scope: CoroutineScope, user: User) {

        private val stateRegistry = StateRegistry.create(
            job = mock(),
            scope = scope,
            userStateFlow = MutableStateFlow(user),
            messageRepository = mock(),
            latestUsers = MutableStateFlow(emptyMap()),
        )

        private val client = mock<ChatClient>()

        private var repos = mock<RepositoryFacade>()
        private val globalState = mock<MutableGlobalState>()
        private val clientState = mock<ClientState>()
        private val logicRegistry = LogicRegistry.create(
            stateRegistry = stateRegistry,
            globalState = globalState,
            clientState = clientState,
            userPresence = false,
            repos = repos,
            client = client,
            coroutineScope = testCoroutines.scope
        )

        suspend fun givenMessageWithReactions(message: Message): Fixture = apply {
            whenever(repos.selectMessage(message.id)) doReturn message
        }

        fun get(): Pair<SendReactionListener, StateRegistry> =
            SendReactionListenerImpl(logicRegistry, clientState, repos) to stateRegistry
    }
}
