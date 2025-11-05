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

package io.getstream.chat.android.state.plugin.listener.internal

import io.getstream.chat.android.models.SyncStatus
import io.getstream.chat.android.randomMessage
import io.getstream.chat.android.randomString
import io.getstream.chat.android.state.plugin.logic.channel.internal.ChannelLogic
import io.getstream.chat.android.state.plugin.logic.channel.thread.internal.ThreadLogic
import io.getstream.chat.android.state.plugin.logic.internal.LogicRegistry
import io.getstream.chat.android.state.plugin.logic.querythreads.internal.QueryThreadsLogic
import io.getstream.result.Error
import io.getstream.result.Result
import kotlinx.coroutines.test.runTest
import org.junit.jupiter.api.Test
import org.mockito.kotlin.any
import org.mockito.kotlin.argThat
import org.mockito.kotlin.doReturn
import org.mockito.kotlin.mock
import org.mockito.kotlin.never
import org.mockito.kotlin.verify
import org.mockito.kotlin.whenever
import java.util.Date

internal class SendMessageListenerStateTest {

    private val channelLogic: ChannelLogic = mock()
    private val threadsLogic: QueryThreadsLogic = mock()
    private val activeThreadsLogic = listOf(threadsLogic)
    private val threadLogic: ThreadLogic = mock()
    private val logicRegistry: LogicRegistry = mock {
        on(it.channelFromMessage(any())) doReturn channelLogic
        on(it.threadFromMessage(any())) doReturn threadLogic
        on(it.getActiveQueryThreadsLogic()) doReturn activeThreadsLogic
        on(it.getMessageById(any())) doReturn null
    }

    private val sendMessageListener = SendMessageListenerState(logicRegistry)

    @Test
    fun `when message is already in state, it should not be upserted again`() = runTest {
        whenever(logicRegistry.getMessageById(any())) doReturn randomMessage(syncStatus = SyncStatus.COMPLETED)

        val testMessage = randomMessage(syncStatus = SyncStatus.SYNC_NEEDED)

        sendMessageListener.onMessageSendResult(
            result = Result.Success(testMessage),
            channelType = randomString(),
            channelId = randomString(),
            message = testMessage,
        )

        verify(channelLogic, never()).upsertMessage(
            argThat { message ->
                message.id == testMessage.id && message.syncStatus == SyncStatus.COMPLETED
            },
        )
        verify(threadsLogic, never()).upsertMessage(
            argThat { message ->
                message.id == testMessage.id && message.syncStatus == SyncStatus.COMPLETED
            },
        )
        verify(threadLogic, never()).upsertMessage(
            argThat { message ->
                message.id == testMessage.id && message.syncStatus == SyncStatus.COMPLETED
            },
        )
    }

    @Test
    fun `when old message exists with createdLocallyAt, it should be preserved on success`() = runTest {
        val originalCreatedLocallyAt = Date()
        val oldMessage = randomMessage(
            syncStatus = SyncStatus.SYNC_NEEDED,
            createdLocallyAt = originalCreatedLocallyAt,
        )
        whenever(logicRegistry.getMessageById(oldMessage.id)) doReturn oldMessage

        val testMessage = oldMessage.copy(
            createdLocallyAt = Date(), // Different timestamp
        )

        sendMessageListener.onMessageSendResult(
            result = Result.Success(testMessage),
            channelType = randomString(),
            channelId = randomString(),
            message = testMessage,
        )

        verify(channelLogic).upsertMessage(
            argThat { message ->
                message.id == testMessage.id &&
                    message.syncStatus == SyncStatus.COMPLETED &&
                    message.createdLocallyAt == originalCreatedLocallyAt
            },
        )
        verify(threadsLogic).upsertMessage(
            argThat { message ->
                message.id == testMessage.id &&
                    message.syncStatus == SyncStatus.COMPLETED &&
                    message.createdLocallyAt == originalCreatedLocallyAt
            },
        )
        verify(threadLogic).upsertMessage(
            argThat { message ->
                message.id == testMessage.id &&
                    message.syncStatus == SyncStatus.COMPLETED &&
                    message.createdLocallyAt == originalCreatedLocallyAt
            },
        )
    }

    @Test
    fun `when old message exists with createdLocallyAt, it should be preserved on failure`() = runTest {
        val originalCreatedLocallyAt = Date()
        val oldMessage = randomMessage(
            syncStatus = SyncStatus.SYNC_NEEDED,
            createdLocallyAt = originalCreatedLocallyAt,
        )
        whenever(logicRegistry.getMessageById(oldMessage.id)) doReturn oldMessage

        val testMessage = oldMessage.copy(
            createdLocallyAt = Date(), // Different timestamp
        )

        sendMessageListener.onMessageSendResult(
            result = Result.Failure(Error.GenericError("")),
            channelType = randomString(),
            channelId = randomString(),
            message = testMessage,
        )

        verify(channelLogic).upsertMessage(
            argThat { message ->
                message.id == testMessage.id &&
                    message.syncStatus == SyncStatus.SYNC_NEEDED &&
                    message.createdLocallyAt == originalCreatedLocallyAt
            },
        )
        verify(threadsLogic).upsertMessage(
            argThat { message ->
                message.id == testMessage.id &&
                    message.syncStatus == SyncStatus.SYNC_NEEDED &&
                    message.createdLocallyAt == originalCreatedLocallyAt
            },
        )
        verify(threadLogic).upsertMessage(
            argThat { message ->
                message.id == testMessage.id &&
                    message.syncStatus == SyncStatus.SYNC_NEEDED &&
                    message.createdLocallyAt == originalCreatedLocallyAt
            },
        )
    }

    @Test
    fun `when no old message exists, createdLocallyAt should be null on success`() = runTest {
        val testMessage = randomMessage(
            syncStatus = SyncStatus.SYNC_NEEDED,
            createdLocallyAt = Date(),
        )

        sendMessageListener.onMessageSendResult(
            result = Result.Success(testMessage),
            channelType = randomString(),
            channelId = randomString(),
            message = testMessage,
        )

        verify(channelLogic).upsertMessage(
            argThat { message ->
                message.id == testMessage.id &&
                    message.syncStatus == SyncStatus.COMPLETED &&
                    message.createdLocallyAt == null
            },
        )
        verify(threadsLogic).upsertMessage(
            argThat { message ->
                message.id == testMessage.id &&
                    message.syncStatus == SyncStatus.COMPLETED &&
                    message.createdLocallyAt == null
            },
        )
        verify(threadLogic).upsertMessage(
            argThat { message ->
                message.id == testMessage.id &&
                    message.syncStatus == SyncStatus.COMPLETED &&
                    message.createdLocallyAt == null
            },
        )
    }

    @Test
    fun `when no old message exists, createdLocallyAt should be null on failure`() = runTest {
        val testMessage = randomMessage(
            syncStatus = SyncStatus.SYNC_NEEDED,
            createdLocallyAt = Date(),
        )

        sendMessageListener.onMessageSendResult(
            result = Result.Failure(Error.GenericError("")),
            channelType = randomString(),
            channelId = randomString(),
            message = testMessage,
        )

        verify(channelLogic).upsertMessage(
            argThat { message ->
                message.id == testMessage.id &&
                    message.syncStatus == SyncStatus.SYNC_NEEDED &&
                    message.createdLocallyAt == null
            },
        )
        verify(threadsLogic).upsertMessage(
            argThat { message ->
                message.id == testMessage.id &&
                    message.syncStatus == SyncStatus.SYNC_NEEDED &&
                    message.createdLocallyAt == null
            },
        )
        verify(threadLogic).upsertMessage(
            argThat { message ->
                message.id == testMessage.id &&
                    message.syncStatus == SyncStatus.SYNC_NEEDED &&
                    message.createdLocallyAt == null
            },
        )
    }
}
