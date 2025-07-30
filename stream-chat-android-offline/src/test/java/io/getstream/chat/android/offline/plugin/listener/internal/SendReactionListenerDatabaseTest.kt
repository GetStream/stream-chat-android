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

package io.getstream.chat.android.offline.plugin.listener.internal

import io.getstream.chat.android.client.persistance.repository.MessageRepository
import io.getstream.chat.android.client.persistance.repository.ReactionRepository
import io.getstream.chat.android.client.persistance.repository.UserRepository
import io.getstream.chat.android.client.setup.state.ClientState
import io.getstream.chat.android.models.SyncStatus
import io.getstream.chat.android.models.User
import io.getstream.chat.android.randomCID
import io.getstream.chat.android.randomMessage
import io.getstream.chat.android.randomReaction
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

@ExperimentalCoroutinesApi
internal class SendReactionListenerDatabaseTest {

    private val currentUser = User()
    private val defaultMessage = randomMessage()

    private val clientState = mock<ClientState>()
    private val reactionsRepository: ReactionRepository = mock()
    private val userRepository: UserRepository = mock()
    private val messageRepository: MessageRepository = mock()

    private val sendReactionListenerState = SendReactionListenerDatabase(
        clientState = clientState,
        reactionsRepository = reactionsRepository,
        messageRepository = messageRepository,
        userRepository = userRepository,
        ignoredChannelTypes = emptySet(),
    )

    @Test
    fun `when sending reactions, messages with reactions and reactions should be saved before request`() = runTest {
        val testReaction = randomReaction(user = randomUser(), syncStatus = SyncStatus.SYNC_NEEDED)

        whenever(messageRepository.selectMessage(any())) doReturn defaultMessage
        whenever(clientState.isNetworkAvailable) doReturn true

        sendReactionListenerState.onSendReactionRequest(randomCID(), testReaction, false, currentUser)

        verify(reactionsRepository).insertReaction(
            argThat { reaction ->
                reaction.messageId == testReaction.messageId
            },
        )

        verify(userRepository).insertUser(testReaction.user!!)

        verify(messageRepository).insertMessage(
            argThat { message ->
                message.latestReactions.any { reaction ->
                    reaction.messageId == testReaction.messageId && reaction.syncStatus == SyncStatus.IN_PROGRESS
                } && message.ownReactions.any { reaction ->
                    reaction.messageId == testReaction.messageId && reaction.syncStatus == SyncStatus.IN_PROGRESS
                }
            },
        )
    }

    @Test
    fun `when there is not connection, the message should be saved with correct sync status`() = runTest {
        val testReaction = randomReaction(user = randomUser(), syncStatus = SyncStatus.IN_PROGRESS)

        whenever(messageRepository.selectMessage(any())) doReturn defaultMessage
        whenever(clientState.isNetworkAvailable) doReturn false

        sendReactionListenerState.onSendReactionRequest(randomCID(), testReaction, false, currentUser)

        verify(reactionsRepository).insertReaction(
            argThat { reaction ->
                reaction.messageId == testReaction.messageId
            },
        )

        verify(userRepository).insertUser(testReaction.user!!)

        verify(messageRepository).insertMessage(
            argThat { message ->
                message.latestReactions.any { reaction ->
                    reaction.messageId == testReaction.messageId && reaction.syncStatus == SyncStatus.SYNC_NEEDED
                } && message.ownReactions.any { reaction ->
                    reaction.messageId == testReaction.messageId && reaction.syncStatus == SyncStatus.SYNC_NEEDED
                }
            },
        )
    }

    @Test
    fun `when sending reactions, the message should be saved in database after a successful request`() = runTest {
        val testReaction = randomReaction(user = randomUser(), syncStatus = SyncStatus.IN_PROGRESS)
        whenever(reactionsRepository.selectUserReactionToMessage(any(), any(), any())) doReturn testReaction

        sendReactionListenerState.onSendReactionResult(
            randomCID(),
            testReaction,
            false,
            currentUser,
            Result.Success(testReaction),
        )

        verify(reactionsRepository).insertReaction(testReaction.copy(syncStatus = SyncStatus.COMPLETED))
    }

    @Test
    fun `when sending reactions, the message should be saved in database after a failure request`() = runTest {
        val testReaction = randomReaction(user = randomUser(), syncStatus = SyncStatus.IN_PROGRESS)
        whenever(reactionsRepository.selectUserReactionToMessage(any(), any(), any())) doReturn testReaction

        sendReactionListenerState.onSendReactionResult(
            randomCID(),
            testReaction,
            false,
            currentUser,
            Result.Failure(Error.GenericError("")),
        )

        verify(reactionsRepository).insertReaction(testReaction.copy(syncStatus = SyncStatus.SYNC_NEEDED))
    }
}
