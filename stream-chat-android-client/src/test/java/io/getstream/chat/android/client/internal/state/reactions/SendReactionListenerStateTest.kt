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

package io.getstream.chat.android.client.internal.state.reactions

import io.getstream.chat.android.client.internal.state.plugin.listener.internal.SendReactionListenerState
import io.getstream.chat.android.client.internal.state.plugin.logic.channel.internal.ChannelLogic
import io.getstream.chat.android.client.internal.state.plugin.logic.internal.LogicRegistry
import io.getstream.chat.android.client.internal.state.plugin.logic.querythreads.internal.QueryThreadsLogic
import io.getstream.chat.android.client.setup.state.ClientState
import io.getstream.chat.android.models.Reaction
import io.getstream.chat.android.models.SyncStatus
import io.getstream.chat.android.models.User
import io.getstream.chat.android.randomCID
import io.getstream.chat.android.randomMessage
import io.getstream.chat.android.randomReaction
import io.getstream.chat.android.test.TestCoroutineExtension
import io.getstream.result.Result
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.test.runTest
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.extension.RegisterExtension
import org.mockito.kotlin.any
import org.mockito.kotlin.argThat
import org.mockito.kotlin.doReturn
import org.mockito.kotlin.mock
import org.mockito.kotlin.verify
import org.mockito.kotlin.whenever

@ExperimentalCoroutinesApi
internal class SendReactionListenerStateTest {

    companion object {
        @JvmField
        @RegisterExtension
        val testCoroutines = TestCoroutineExtension()
    }

    private val currentUser = User()

    private val myReactions: List<Reaction> = listOf(
        randomReaction(),
        Reaction(
            userId = currentUser.id,
            type = "type1",
            score = 123,
            syncStatus = SyncStatus.SYNC_NEEDED,
        ),
        Reaction(
            userId = currentUser.id,
            type = "type2",
            score = 234,
            syncStatus = SyncStatus.SYNC_NEEDED,
        ),
    )

    private val clientState = mock<ClientState>()
    private val defaultMessage = randomMessage(syncStatus = SyncStatus.SYNC_NEEDED)
    private val channelLogic: ChannelLogic = mock {
        on(it.getMessage(any())) doReturn defaultMessage
    }
    private val threadsLogic: QueryThreadsLogic = mock {
        on(it.getMessage(any())) doReturn defaultMessage
    }
    private val activeThreadsLogic = listOf(threadsLogic)

    private val logicRegistry: LogicRegistry = mock {
        on(it.channelFromMessageId(any())) doReturn channelLogic
        on(it.getActiveQueryThreadsLogic()) doReturn activeThreadsLogic
    }

    private val sendReactionListenerState = SendReactionListenerState(logicRegistry, clientState)

    @Test
    fun `when sending reactions, messages with reactions should be upserted before request`() = runTest {
        val testReaction = myReactions[0]

        whenever(clientState.isNetworkAvailable) doReturn true

        sendReactionListenerState.onSendReactionRequest(randomCID(), testReaction, false, currentUser)

        verify(channelLogic).upsertMessage(
            argThat { message ->
                message.latestReactions.any { reaction ->
                    reaction.messageId == testReaction.messageId && reaction.syncStatus == SyncStatus.IN_PROGRESS
                } && message.ownReactions.any { reaction ->
                    reaction.messageId == testReaction.messageId && reaction.syncStatus == SyncStatus.IN_PROGRESS
                }
            },
        )
        verify(threadsLogic).upsertMessage(
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
    fun `when sending reactions, messages with reactions should be upserted with correct sync status after request`() =
        runTest {
            val testReaction = myReactions[0]

            val testMessage = randomMessage(
                ownReactions = mutableListOf(testReaction),
                latestReactions = mutableListOf(testReaction),
            )

            whenever(channelLogic.getMessage(any())) doReturn testMessage
            whenever(threadsLogic.getMessage(any())) doReturn testMessage

            sendReactionListenerState.onSendReactionResult(
                randomCID(),
                testReaction,
                false,
                currentUser,
                Result.Success(testReaction),
            )

            verify(channelLogic).upsertMessage(
                argThat { message ->
                    message.latestReactions.any { reaction ->
                        reaction.messageId == testReaction.messageId && reaction.syncStatus == SyncStatus.COMPLETED
                    } && message.ownReactions.any { reaction ->
                        reaction.messageId == testReaction.messageId && reaction.syncStatus == SyncStatus.COMPLETED
                    }
                },
            )
            verify(threadsLogic).upsertMessage(
                argThat { message ->
                    message.latestReactions.any { reaction ->
                        reaction.messageId == testReaction.messageId && reaction.syncStatus == SyncStatus.COMPLETED
                    } && message.ownReactions.any { reaction ->
                        reaction.messageId == testReaction.messageId && reaction.syncStatus == SyncStatus.COMPLETED
                    }
                },
            )
        }
}
