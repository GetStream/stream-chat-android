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
import io.getstream.chat.android.models.MessageType
import io.getstream.chat.android.models.ModerationAction
import io.getstream.chat.android.models.SyncStatus
import io.getstream.chat.android.randomCID
import io.getstream.chat.android.randomMessage
import io.getstream.chat.android.randomModeration
import io.getstream.chat.android.randomUser
import io.getstream.chat.android.state.plugin.logic.channel.internal.ChannelLogic
import io.getstream.chat.android.state.plugin.logic.channel.internal.ChannelStateLogic
import io.getstream.chat.android.state.plugin.logic.internal.LogicRegistry
import io.getstream.chat.android.state.plugin.logic.querythreads.internal.QueryThreadsLogic
import io.getstream.chat.android.state.plugin.state.global.GlobalState
import io.getstream.result.Error
import io.getstream.result.Result
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.test.runTest
import org.junit.jupiter.api.Assertions.assertTrue
import org.junit.jupiter.api.Test
import org.mockito.kotlin.any
import org.mockito.kotlin.argThat
import org.mockito.kotlin.doReturn
import org.mockito.kotlin.mock
import org.mockito.kotlin.verify
import org.mockito.kotlin.whenever

internal class DeleteMessageListenerStateTest {

    private val channelStateLogic: ChannelStateLogic = mock()
    private val channelLogic: ChannelLogic = mock {
        on(it.stateLogic) doReturn channelStateLogic
    }
    private val threadsLogic: QueryThreadsLogic = mock()
    private val activeThreadsLogic = listOf(threadsLogic)

    private val clientState: ClientState = mock {
        on(it.user) doReturn MutableStateFlow(randomUser())
    }
    private val globalState: GlobalState = mock()
    private val logicRegistry: LogicRegistry = mock {
        on(it.channel(any(), any())) doReturn channelLogic
        on(it.channelFromMessageId(any())) doReturn channelLogic
        on(it.channelFromMessage(any())) doReturn channelLogic
        on(it.getActiveQueryThreadsLogic()) doReturn activeThreadsLogic
    }

    private val deleteMessageListenerState: DeleteMessageListenerState =
        DeleteMessageListenerState(logicRegistry, clientState, globalState)

    @Test
    fun `when internet is available, the message should be updated as in progress before the request`() = runTest {
        val testMessage = randomMessage(
            cid = randomCID(),
            syncStatus = SyncStatus.SYNC_NEEDED,
        )

        whenever(clientState.isNetworkAvailable) doReturn true
        whenever(channelLogic.getMessage(any())) doReturn testMessage
        whenever(threadsLogic.getMessage(any())) doReturn testMessage

        deleteMessageListenerState.onMessageDeleteRequest(testMessage.id)

        verify(channelLogic).upsertMessage(
            argThat { message ->
                // The same ID, but not the status was correctly updated
                message.id == testMessage.id && message.syncStatus == SyncStatus.IN_PROGRESS
            },
        )
        verify(threadsLogic).upsertMessage(
            argThat { message ->
                // The same ID, but not the status was correctly updated
                message.id == testMessage.id && message.syncStatus == SyncStatus.IN_PROGRESS
            },
        )
    }

    @Test
    fun `when internet is not available, the message should be updated as sync needed before the request`() = runTest {
        val testMessage = randomMessage(
            cid = randomCID(),
            syncStatus = SyncStatus.IN_PROGRESS,
        )

        whenever(clientState.isNetworkAvailable) doReturn false
        whenever(channelLogic.getMessage(any())) doReturn testMessage
        whenever(threadsLogic.getMessage(any())) doReturn testMessage

        deleteMessageListenerState.onMessageDeleteRequest(testMessage.id)

        verify(channelLogic).upsertMessage(
            argThat { message ->
                // The same ID, but not the status was correctly updated
                message.id == testMessage.id && message.syncStatus == SyncStatus.SYNC_NEEDED
            },
        )
        verify(threadsLogic).upsertMessage(
            argThat { message ->
                // The same ID, but not the status was correctly updated
                message.id == testMessage.id && message.syncStatus == SyncStatus.SYNC_NEEDED
            },
        )
    }

    @Test
    fun `when request is successful, the message should be updated as completed after request`() = runTest {
        val testMessage = randomMessage(
            cid = randomCID(),
            syncStatus = SyncStatus.IN_PROGRESS,
        )

        whenever(clientState.isNetworkAvailable) doReturn true
        whenever(channelLogic.getMessage(any())) doReturn testMessage
        whenever(threadsLogic.getMessage(any())) doReturn testMessage

        deleteMessageListenerState.onMessageDeleteResult(testMessage.id, Result.Success(testMessage))

        verify(channelLogic).upsertMessage(
            argThat { message ->
                // The same ID, but not the status was correctly updated
                message.id == testMessage.id && message.syncStatus == SyncStatus.COMPLETED
            },
        )
        verify(threadsLogic).upsertMessage(
            argThat { message ->
                // The same ID, but not the status was correctly updated
                message.id == testMessage.id && message.syncStatus == SyncStatus.COMPLETED
            },
        )
    }

    @Test
    fun `when request fails, the message should be updated as sync needed after request`() = runTest {
        val testMessage = randomMessage(
            cid = randomCID(),
            syncStatus = SyncStatus.IN_PROGRESS,
        )

        whenever(clientState.isNetworkAvailable) doReturn true
        whenever(channelLogic.getMessage(any())) doReturn testMessage
        whenever(threadsLogic.getMessage(any())) doReturn testMessage

        deleteMessageListenerState.onMessageDeleteResult(testMessage.id, Result.Failure(Error.GenericError("")))

        verify(channelLogic).upsertMessage(
            argThat { message ->
                // The same ID, but not the status was correctly updated
                message.id == testMessage.id && message.syncStatus == SyncStatus.SYNC_NEEDED
            },
        )
        verify(threadsLogic).upsertMessage(
            argThat { message ->
                // The same ID, but not the status was correctly updated
                message.id == testMessage.id && message.syncStatus == SyncStatus.SYNC_NEEDED
            },
        )
    }

    @Test
    fun `onMessageDeletePrecondition when message not found locally should return Success`() = runTest {
        whenever(logicRegistry.channelFromMessageId(any())) doReturn null

        val result = deleteMessageListenerState.onMessageDeletePrecondition("unknown-message-id")

        assertTrue(result is Result.Success)
    }

    @Test
    fun `onMessageDeletePrecondition when message has moderation bounce should return Failure and delete locally`() =
        runTest {
            val currentUser = randomUser()
            val testMessage = randomMessage(
                id = "msg-1",
                cid = randomCID(),
                user = currentUser,
                type = MessageType.ERROR,
                moderation = randomModeration(action = ModerationAction.bounce),
            )

            whenever(clientState.user) doReturn MutableStateFlow(currentUser)
            whenever(logicRegistry.channelFromMessageId(any())) doReturn channelLogic
            whenever(logicRegistry.channelFromMessage(any())) doReturn channelLogic
            whenever(logicRegistry.getActiveQueryThreadsLogic()) doReturn activeThreadsLogic
            whenever(logicRegistry.threadFromMessage(any())) doReturn null
            whenever(channelLogic.getMessage(any())) doReturn testMessage

            val result = deleteMessageListenerState.onMessageDeletePrecondition(testMessage.id)

            assertTrue(result is Result.Failure)
            verify(channelLogic).deleteMessage(argThat { id == testMessage.id })
            verify(threadsLogic).deleteMessage(argThat { id == testMessage.id })
        }

    @Test
    fun `onMessageDeletePrecondition when message is error type should return Failure and delete locally`() = runTest {
        val testMessage = randomMessage(
            id = "msg-1",
            cid = randomCID(),
            type = MessageType.ERROR,
            syncStatus = SyncStatus.COMPLETED,
        )

        whenever(logicRegistry.channelFromMessageId(any())) doReturn channelLogic
        whenever(logicRegistry.channelFromMessage(any())) doReturn channelLogic
        whenever(logicRegistry.getActiveQueryThreadsLogic()) doReturn activeThreadsLogic
        whenever(logicRegistry.threadFromMessage(any())) doReturn null
        whenever(channelLogic.getMessage(any())) doReturn testMessage

        val result = deleteMessageListenerState.onMessageDeletePrecondition(testMessage.id)

        assertTrue(result is Result.Failure)
        verify(channelLogic).deleteMessage(argThat { id == testMessage.id })
    }

    @Test
    fun `onMessageDeletePrecondition when message is ephemeral type should return Failure and delete locally`() =
        runTest {
            val testMessage = randomMessage(
                id = "msg-1",
                cid = randomCID(),
                type = MessageType.EPHEMERAL,
                syncStatus = SyncStatus.COMPLETED,
            )

            whenever(logicRegistry.channelFromMessageId(any())) doReturn channelLogic
            whenever(logicRegistry.channelFromMessage(any())) doReturn channelLogic
            whenever(logicRegistry.getActiveQueryThreadsLogic()) doReturn activeThreadsLogic
            whenever(logicRegistry.threadFromMessage(any())) doReturn null
            whenever(channelLogic.getMessage(any())) doReturn testMessage

            val result = deleteMessageListenerState.onMessageDeletePrecondition(testMessage.id)

            assertTrue(result is Result.Failure)
            verify(channelLogic).deleteMessage(argThat { id == testMessage.id })
        }

    @Test
    fun `onMessageDeletePrecondition when message has SYNC_NEEDED should return Failure and delete locally`() =
        runTest {
            val testMessage = randomMessage(
                id = "msg-1",
                cid = randomCID(),
                type = MessageType.REGULAR,
                syncStatus = SyncStatus.SYNC_NEEDED,
            )

            whenever(logicRegistry.channelFromMessageId(any())) doReturn channelLogic
            whenever(logicRegistry.channelFromMessage(any())) doReturn channelLogic
            whenever(logicRegistry.getActiveQueryThreadsLogic()) doReturn activeThreadsLogic
            whenever(logicRegistry.threadFromMessage(any())) doReturn null
            whenever(channelLogic.getMessage(any())) doReturn testMessage

            val result = deleteMessageListenerState.onMessageDeletePrecondition(testMessage.id)

            assertTrue(result is Result.Failure)
            verify(channelLogic).deleteMessage(argThat { id == testMessage.id })
        }

    @Test
    fun `onMessageDeletePrecondition when message has FAILED_PERMANENTLY should return Failure and delete locally`() =
        runTest {
            val testMessage = randomMessage(
                id = "msg-1",
                cid = randomCID(),
                type = MessageType.REGULAR,
                syncStatus = SyncStatus.FAILED_PERMANENTLY,
            )

            whenever(logicRegistry.channelFromMessageId(any())) doReturn channelLogic
            whenever(logicRegistry.channelFromMessage(any())) doReturn channelLogic
            whenever(logicRegistry.getActiveQueryThreadsLogic()) doReturn activeThreadsLogic
            whenever(logicRegistry.threadFromMessage(any())) doReturn null
            whenever(channelLogic.getMessage(any())) doReturn testMessage

            val result = deleteMessageListenerState.onMessageDeletePrecondition(testMessage.id)

            assertTrue(result is Result.Failure)
            verify(channelLogic).deleteMessage(argThat { id == testMessage.id })
        }

    @Test
    fun `onMessageDeletePrecondition when message is COMPLETED regular should return Success`() = runTest {
        val testMessage = randomMessage(
            id = "msg-1",
            cid = randomCID(),
            type = MessageType.REGULAR,
            syncStatus = SyncStatus.COMPLETED,
        )

        whenever(logicRegistry.channelFromMessageId(any())) doReturn channelLogic
        whenever(channelLogic.getMessage(any())) doReturn testMessage

        val result = deleteMessageListenerState.onMessageDeletePrecondition(testMessage.id)

        assertTrue(result is Result.Success)
    }
}
