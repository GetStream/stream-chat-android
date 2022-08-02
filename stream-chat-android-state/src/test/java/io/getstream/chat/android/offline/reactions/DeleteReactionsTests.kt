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
import io.getstream.chat.android.client.errors.ChatError
import io.getstream.chat.android.client.errors.ChatNetworkError
import io.getstream.chat.android.client.extensions.cidToTypeAndId
import io.getstream.chat.android.client.extensions.internal.addMyReaction
import io.getstream.chat.android.client.models.Message
import io.getstream.chat.android.client.models.Reaction
import io.getstream.chat.android.client.models.User
import io.getstream.chat.android.client.persistance.repository.RepositoryFacade
import io.getstream.chat.android.client.setup.state.ClientState
import io.getstream.chat.android.client.test.randomMessage
import io.getstream.chat.android.client.utils.Result
import io.getstream.chat.android.client.utils.SyncStatus
import io.getstream.chat.android.offline.plugin.listener.internal.DeleteReactionListenerImpl
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
import org.mockito.kotlin.argThat
import org.mockito.kotlin.doReturn
import org.mockito.kotlin.mock
import org.mockito.kotlin.verify
import org.mockito.kotlin.whenever

@ExperimentalCoroutinesApi
internal class DeleteReactionsTests {

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
    fun `When deleting reaction Should remove it from own and latest reactions`() = runTest {
        val cid = randomCID()
        val message = Message(cid = cid).apply {
            myReactions.forEach(::addMyReaction)
        }
        val (sut, stateRegistry) = Fixture(testCoroutines.scope, currentUser)
            .givenMessageWithReactions(message)
            .get()

        val deletedReaction = myReactions.first()

        sut.onDeleteReactionRequest(
            cid = cid,
            messageId = deletedReaction.messageId,
            reactionType = deletedReaction.type,
            currentUser = currentUser,
        )

        val (channelType, channelId) = cid.cidToTypeAndId()
        val channelState = stateRegistry.channel(channelType = channelType, channelId = channelId).toMutableState()
        val result = channelState.rawMessages[message.id]
        result!!.ownReactions.size `should be equal to` myReactions.size - 1
        result.ownReactions.contains(deletedReaction) `should be equal to` false
        result.latestReactions.contains(deletedReaction) `should be equal to` false
    }

    @Test
    fun `Given offline state When deleting a reaction Should insert a reaction with proper status`() =
        runTest {
            val cid = randomCID()
            val repos = mock<RepositoryFacade>()
            val (sut, _) = Fixture(testCoroutines.scope, currentUser)
                .givenMockedRepos(repos)
                .givenOfflineState()
                .get()

            sut.onDeleteReactionRequest(
                cid = cid,
                messageId = mockReaction.messageId,
                reactionType = mockReaction.type,
                currentUser = currentUser,
            )

            verify(repos).insertReaction(
                argThat {
                    syncStatus == SyncStatus.SYNC_NEEDED
                }
            )
        }

    @Test
    fun `When deleting reaction FAILED Should insert the reaction with proper sync status`() =
        runTest {
            val cid = randomCID()
            val repos = mock<RepositoryFacade>()
            val (sut, _) = Fixture(testCoroutines.scope, currentUser)
                .givenMockedRepos(repos)
                .givenCachedReaction(newReaction)
                .get()

            sut.onDeleteReactionResult(
                cid = cid,
                messageId = newReaction.messageId,
                reactionType = newReaction.type,
                currentUser = currentUser,
                result = Result.error(ChatError()),
            )

            verify(repos).insertReaction(
                argThat {
                    syncStatus == SyncStatus.SYNC_NEEDED
                }
            )
        }

    @Test
    fun `When deleting reaction failed PERMANENTLY Should insert the reaction with proper sync status`() =
        runTest {
            val chatError = ChatNetworkError.create(
                description = "error",
                streamCode = 401,
                statusCode = 401
            )

            val cid = randomCID()
            val repos = mock<RepositoryFacade>()
            val (sut, _) = Fixture(testCoroutines.scope, currentUser)
                .givenMockedRepos(repos)
                .givenCachedReaction(newReaction)
                .get()

            sut.onDeleteReactionResult(
                cid = cid,
                messageId = newReaction.messageId,
                reactionType = newReaction.type,
                currentUser = currentUser,
                result = Result.error(chatError),
            )

            verify(repos).insertReaction(
                argThat {
                    syncStatus == SyncStatus.FAILED_PERMANENTLY
                }
            )
        }

    @Test
    fun `When deleting a reaction SUCCESSFULLY Should insert the reaction with proper sync status`() =
        runTest {
            val cid = randomCID()
            val repos = mock<RepositoryFacade>()
            val (sut, _) = Fixture(testCoroutines.scope, currentUser)
                .givenMockedRepos(repos)
                .givenCachedReaction(newReaction)
                .get()

            sut.onDeleteReactionResult(
                cid = cid,
                messageId = newReaction.messageId,
                reactionType = newReaction.type,
                currentUser = currentUser,
                result = Result.success(randomMessage()),
            )

            verify(repos).insertReaction(
                argThat {
                    syncStatus == SyncStatus.COMPLETED
                }
            )
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
        private val logicRegistry =
            LogicRegistry.create(stateRegistry, globalState, clientState, false, repos, client, testCoroutines.scope)

        fun givenMockedRepos(repos: RepositoryFacade): Fixture = apply {
            this.repos = repos
        }

        suspend fun givenCachedReaction(reaction: Reaction): Fixture = apply {
            whenever(
                repos.selectUserReactionToMessage(
                    reactionType = reaction.type,
                    messageId = reaction.messageId,
                    userId = reaction.userId,
                ),
            ) doReturn reaction
        }

        suspend fun givenMessageWithReactions(message: Message): Fixture = apply {
            whenever(repos.selectMessage(message.id)) doReturn message
        }

        fun givenOfflineState(): Fixture = apply {
            whenever(clientState.isOnline) doReturn false
        }

        fun get(): Pair<DeleteReactionListenerImpl, StateRegistry> =
            DeleteReactionListenerImpl(logicRegistry, clientState, repos) to stateRegistry
    }
}
