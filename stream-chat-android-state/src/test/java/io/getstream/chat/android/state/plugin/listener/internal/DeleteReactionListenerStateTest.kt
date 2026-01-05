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

package io.getstream.chat.android.state.plugin.listener.internal

import io.getstream.chat.android.client.setup.state.ClientState
import io.getstream.chat.android.randomCID
import io.getstream.chat.android.randomMessage
import io.getstream.chat.android.randomReaction
import io.getstream.chat.android.randomUser
import io.getstream.chat.android.state.plugin.logic.channel.internal.ChannelLogic
import io.getstream.chat.android.state.plugin.logic.internal.LogicRegistry
import io.getstream.chat.android.state.plugin.logic.querythreads.internal.QueryThreadsLogic
import kotlinx.coroutines.test.runTest
import org.junit.jupiter.api.Test
import org.mockito.kotlin.any
import org.mockito.kotlin.argThat
import org.mockito.kotlin.doReturn
import org.mockito.kotlin.mock
import org.mockito.kotlin.verify
import org.mockito.kotlin.whenever

internal class DeleteReactionListenerStateTest {
    private val user = randomUser()
    private val defaultReaction = randomReaction(
        userId = user.id,
        user = user,
    )
    private val defaultMessage = randomMessage(
        ownReactions = mutableListOf(defaultReaction),
        latestReactions = mutableListOf(defaultReaction),
        user = user,
    )

    private val clientState = mock<ClientState>()
    private val channelLogic = mock<ChannelLogic> {
        on(it.getMessage(any())) doReturn defaultMessage
    }
    private val threadsLogic = mock<QueryThreadsLogic> {
        on(it.getMessage(any())) doReturn defaultMessage
    }
    private val activeThreadsLogic = listOf(threadsLogic)
    private val logicRegistry = mock<LogicRegistry> {
        on(it.channelFromMessageId(any())) doReturn channelLogic
        on(it.channel(any(), any())) doReturn channelLogic
        on(it.getActiveQueryThreadsLogic()) doReturn activeThreadsLogic
    }

    private val deleteReactionListenerDatabase = DeleteReactionListenerState(logicRegistry, clientState)

    @Test
    fun `when deleting reactions, reactions should be optimistically be deleted`() = runTest {
        whenever(clientState.isNetworkAvailable) doReturn true

        deleteReactionListenerDatabase.onDeleteReactionRequest(
            cid = randomCID(),
            messageId = defaultMessage.id,
            reactionType = defaultReaction.type,
            currentUser = user,
        )

        verify(channelLogic).upsertMessage(
            argThat { message ->
                message.ownReactions.isEmpty() && message.latestReactions.isEmpty()
            },
        )
        verify(threadsLogic).upsertMessage(
            argThat { message ->
                message.ownReactions.isEmpty() && message.latestReactions.isEmpty()
            },
        )
    }
}
