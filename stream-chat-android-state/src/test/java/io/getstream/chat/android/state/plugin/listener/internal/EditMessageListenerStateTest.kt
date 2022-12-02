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

import io.getstream.chat.android.client.errors.ChatError
import io.getstream.chat.android.client.setup.state.ClientState
import io.getstream.chat.android.client.test.randomMessage
import io.getstream.chat.android.client.utils.Result
import io.getstream.chat.android.models.SyncStatus
import io.getstream.chat.android.state.plugin.logic.channel.internal.ChannelLogic
import io.getstream.chat.android.state.plugin.logic.channel.internal.ChannelStateLogic
import io.getstream.chat.android.state.plugin.logic.channel.thread.internal.ThreadLogic
import io.getstream.chat.android.state.plugin.logic.channel.thread.internal.ThreadStateLogic
import io.getstream.chat.android.state.plugin.logic.internal.LogicRegistry
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
internal class EditMessageListenerStateTest {

    private val logicRegistry: LogicRegistry = mock()
    private val clientState: ClientState = mock()
    private val editMessageListenerState: EditMessageListenerState =
        EditMessageListenerState(logicRegistry, clientState)

    @Test
    fun `when messages are in channel and threads, they are upserted in edit message`() = runTest {
        val channelStateLogic: ChannelStateLogic = mock()
        val channelLogic: ChannelLogic = mock {
            on(it.stateLogic()) doReturn channelStateLogic
        }

        val threadStateLogic: ThreadStateLogic = mock()
        val threadLogic: ThreadLogic = mock {
            on(it.stateLogic()) doReturn threadStateLogic
        }

        whenever(logicRegistry.channelFromMessage(any())) doReturn channelLogic
        whenever(logicRegistry.threadFromMessage(any())) doReturn threadLogic

        val testMessage = randomMessage()
        editMessageListenerState.onMessageEditRequest(testMessage)

        verify(channelStateLogic).upsertMessage(
            argThat { message ->
                message.id == testMessage.id
            },
            any()
        )
        verify(threadStateLogic).upsertMessage(
            argThat { message ->
                message.id == testMessage.id
            }
        )
    }

    @Test
    fun `when messages edit is requested, sync status is updated correctly when online`() = runTest {
        val channelStateLogic: ChannelStateLogic = mock()
        val channelLogic: ChannelLogic = mock {
            on(it.stateLogic()) doReturn channelStateLogic
        }

        val threadStateLogic: ThreadStateLogic = mock()
        val threadLogic: ThreadLogic = mock {
            on(it.stateLogic()) doReturn threadStateLogic
        }

        whenever(logicRegistry.channelFromMessage(any())) doReturn channelLogic
        whenever(logicRegistry.threadFromMessage(any())) doReturn threadLogic
        whenever(clientState.isNetworkAvailable) doReturn true

        val testMessage = randomMessage()
        editMessageListenerState.onMessageEditRequest(testMessage)

        verify(channelStateLogic).upsertMessage(
            argThat { message ->
                message.id == testMessage.id && message.syncStatus == SyncStatus.IN_PROGRESS
            },
            any()
        )
        verify(threadStateLogic).upsertMessage(
            argThat { message ->
                message.id == testMessage.id && message.syncStatus == SyncStatus.IN_PROGRESS
            }
        )
    }

    @Test
    fun `when messages edit is requested, sync status is updated correctly when offline`() = runTest {
        val channelStateLogic: ChannelStateLogic = mock()
        val channelLogic: ChannelLogic = mock {
            on(it.stateLogic()) doReturn channelStateLogic
        }

        val threadStateLogic: ThreadStateLogic = mock()
        val threadLogic: ThreadLogic = mock {
            on(it.stateLogic()) doReturn threadStateLogic
        }

        whenever(logicRegistry.channelFromMessage(any())) doReturn channelLogic
        whenever(logicRegistry.threadFromMessage(any())) doReturn threadLogic
        whenever(clientState.isNetworkAvailable) doReturn false

        val testMessage = randomMessage()
        editMessageListenerState.onMessageEditRequest(testMessage)

        verify(channelStateLogic).upsertMessage(
            argThat { message ->
                message.id == testMessage.id && message.syncStatus == SyncStatus.SYNC_NEEDED
            },
            any()
        )
        verify(threadStateLogic).upsertMessage(
            argThat { message ->
                message.id == testMessage.id && message.syncStatus == SyncStatus.SYNC_NEEDED
            }
        )
    }

    @Test
    fun `when messages edit completes, sync status is updated correctly when successful`() = runTest {
        val channelStateLogic: ChannelStateLogic = mock()
        val channelLogic: ChannelLogic = mock {
            on(it.stateLogic()) doReturn channelStateLogic
        }

        val threadStateLogic: ThreadStateLogic = mock()
        val threadLogic: ThreadLogic = mock {
            on(it.stateLogic()) doReturn threadStateLogic
        }

        whenever(logicRegistry.channelFromMessage(any())) doReturn channelLogic
        whenever(logicRegistry.threadFromMessage(any())) doReturn threadLogic

        val testMessage = randomMessage()

        editMessageListenerState.onMessageEditResult(testMessage, Result.Success(testMessage))

        verify(channelStateLogic).upsertMessage(
            argThat { message ->
                message.id == testMessage.id && message.syncStatus == SyncStatus.COMPLETED
            },
            any()
        )
        verify(threadStateLogic).upsertMessage(
            argThat { message ->
                message.id == testMessage.id && message.syncStatus == SyncStatus.COMPLETED
            }
        )
    }

    @Test
    fun `when messages edit completes, sync status is updated correctly when failing`() = runTest {
        val channelStateLogic: ChannelStateLogic = mock()
        val channelLogic: ChannelLogic = mock {
            on(it.stateLogic()) doReturn channelStateLogic
        }

        val threadStateLogic: ThreadStateLogic = mock()
        val threadLogic: ThreadLogic = mock {
            on(it.stateLogic()) doReturn threadStateLogic
        }

        whenever(logicRegistry.channelFromMessage(any())) doReturn channelLogic
        whenever(logicRegistry.threadFromMessage(any())) doReturn threadLogic

        val testMessage = randomMessage()

        editMessageListenerState.onMessageEditResult(testMessage, Result.Failure(ChatError.GenericError("")))

        verify(channelStateLogic).upsertMessage(
            argThat { message ->
                message.id == testMessage.id && message.syncStatus == SyncStatus.SYNC_NEEDED
            },
            any()
        )
        verify(threadStateLogic).upsertMessage(
            argThat { message ->
                message.id == testMessage.id && message.syncStatus == SyncStatus.SYNC_NEEDED
            }
        )
    }
}
