package io.getstream.chat.android.offline.plugin.listener.internal

import io.getstream.chat.android.client.setup.state.ClientState
import io.getstream.chat.android.client.test.randomMessage
import io.getstream.chat.android.client.utils.SyncStatus
import io.getstream.chat.android.offline.plugin.logic.channel.internal.ChannelLogic
import io.getstream.chat.android.offline.plugin.logic.channel.internal.ChannelStateLogic
import io.getstream.chat.android.offline.plugin.logic.channel.thread.internal.ThreadLogic
import io.getstream.chat.android.offline.plugin.logic.channel.thread.internal.ThreadStateLogic
import io.getstream.chat.android.offline.plugin.logic.internal.LogicRegistry
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

        verify(channelStateLogic).upsertMessage(argThat { message ->
            message.id == testMessage.id
        })
        verify(threadStateLogic).upsertMessage(argThat { message ->
            message.id == testMessage.id
        })
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

        verify(channelStateLogic).upsertMessage(argThat { message ->
            message.id == testMessage.id && message.syncStatus == SyncStatus.IN_PROGRESS
        })
        verify(threadStateLogic).upsertMessage(argThat { message ->
            message.id == testMessage.id && message.syncStatus == SyncStatus.IN_PROGRESS
        })
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

        verify(channelStateLogic).upsertMessage(argThat { message ->
            message.id == testMessage.id && message.syncStatus == SyncStatus.SYNC_NEEDED
        })
        verify(threadStateLogic).upsertMessage(argThat { message ->
            message.id == testMessage.id && message.syncStatus == SyncStatus.SYNC_NEEDED
        })
    }


}
