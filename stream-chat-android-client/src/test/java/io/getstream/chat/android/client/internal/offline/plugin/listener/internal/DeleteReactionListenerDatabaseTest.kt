/*
 * Copyright (c) 2014-2026 Stream.io Inc. All rights reserved.
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

package io.getstream.chat.android.client.internal.offline.plugin.listener.internal

import io.getstream.chat.android.client.persistance.repository.MessageRepository
import io.getstream.chat.android.client.persistance.repository.ReactionRepository
import io.getstream.chat.android.client.setup.state.ClientState
import io.getstream.chat.android.models.SyncStatus
import io.getstream.chat.android.randomCID
import io.getstream.chat.android.randomMessage
import io.getstream.chat.android.randomReaction
import io.getstream.chat.android.randomString
import io.getstream.chat.android.randomUser
import io.getstream.result.Error
import io.getstream.result.Result
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.test.runTest
import org.junit.jupiter.api.Test
import org.mockito.kotlin.any
import org.mockito.kotlin.argThat
import org.mockito.kotlin.doReturn
import org.mockito.kotlin.mock
import org.mockito.kotlin.verify
import org.mockito.kotlin.whenever

@OptIn(ExperimentalCoroutinesApi::class)
internal class DeleteReactionListenerDatabaseTest {

    private val clientState = mock<ClientState>()
    private val reactionRepository = mock<ReactionRepository>()
    private val messageRepository = mock<MessageRepository>()

    private val deleteReactionListenerDatabase = DeleteReactionListenerDatabase(
        clientState,
        reactionRepository,
        messageRepository,
    )

    @Test
    fun `when deleting reactions, the reactions repository should be updated with correct information`() = runTest {
        whenever(clientState.isNetworkAvailable) doReturn true

        deleteReactionListenerDatabase.onDeleteReactionRequest(
            randomCID(),
            randomString(),
            randomString(),
            randomUser(),
        )

        verify(reactionRepository).insertReaction(
            argThat { reaction ->
                reaction.deletedAt != null && reaction.syncStatus == SyncStatus.IN_PROGRESS
            },
        )

        whenever(clientState.isNetworkAvailable) doReturn false

        deleteReactionListenerDatabase.onDeleteReactionRequest(
            randomCID(),
            randomString(),
            randomString(),
            randomUser(),
        )

        verify(reactionRepository).insertReaction(
            argThat { reaction ->
                reaction.deletedAt != null && reaction.syncStatus == SyncStatus.SYNC_NEEDED
            },
        )
    }

    @Test
    fun `when deleting reactions, the messages repository should be updated with correct information`() = runTest {
        val testUser = randomUser()
        val testReaction = randomReaction(
            user = testUser,
            userId = testUser.id,
        )
        val testMessage = randomMessage(
            latestReactions = mutableListOf(testReaction),
            ownReactions = mutableListOf(testReaction),
            user = testUser,
        )

        whenever(clientState.isNetworkAvailable) doReturn true
        whenever(messageRepository.selectMessage(any())) doReturn testMessage

        deleteReactionListenerDatabase.onDeleteReactionRequest(
            cid = randomCID(),
            messageId = testMessage.id,
            reactionType = testReaction.type,
            currentUser = testUser,
        )

        verify(messageRepository).insertMessage(
            argThat { message ->
                message.id == testMessage.id &&
                    message.ownReactions.isEmpty() &&
                    message.latestReactions.isEmpty()
            },
        )
    }

    @Test
    fun `when deleting message, the correct sync status should be insert in the database when request is successful`() =
        runTest {
            val testUser = randomUser()
            val testReaction = randomReaction(
                user = testUser,
                userId = testUser.id,
                syncStatus = SyncStatus.SYNC_NEEDED,
            )
            val testMessage = randomMessage(
                latestReactions = mutableListOf(testReaction),
                ownReactions = mutableListOf(testReaction),
                user = testUser,
            )

            whenever(reactionRepository.selectUserReactionToMessage(any(), any(), any())) doReturn testReaction

            deleteReactionListenerDatabase.onDeleteReactionResult(
                cid = randomCID(),
                messageId = testMessage.id,
                reactionType = testReaction.type,
                currentUser = testUser,
                Result.Success(testMessage),
            )

            verify(reactionRepository).insertReaction(
                argThat { reaction ->
                    reaction.messageId == testReaction.messageId &&
                        reaction.userId == testReaction.userId &&
                        reaction.syncStatus == SyncStatus.COMPLETED
                },
            )
        }

    @Test
    fun `when deleting message, the correct sync status should be insert in the database when request fails`() =
        runTest {
            val testUser = randomUser()
            val testReaction = randomReaction(
                user = testUser,
                userId = testUser.id,
                syncStatus = SyncStatus.IN_PROGRESS,
            )
            val testMessage = randomMessage(
                latestReactions = mutableListOf(testReaction),
                ownReactions = mutableListOf(testReaction),
                user = testUser,
            )

            whenever(reactionRepository.selectUserReactionToMessage(any(), any(), any())) doReturn testReaction

            deleteReactionListenerDatabase.onDeleteReactionResult(
                cid = randomCID(),
                messageId = testMessage.id,
                reactionType = testReaction.type,
                currentUser = testUser,
                Result.Failure(Error.GenericError("")),
            )

            verify(reactionRepository).insertReaction(
                argThat { reaction ->
                    reaction.messageId == testReaction.messageId &&
                        reaction.userId == testReaction.userId &&
                        reaction.syncStatus == SyncStatus.SYNC_NEEDED
                },
            )
        }
}
