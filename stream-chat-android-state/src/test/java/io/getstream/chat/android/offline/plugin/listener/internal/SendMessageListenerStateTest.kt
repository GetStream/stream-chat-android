package io.getstream.chat.android.offline.plugin.listener.internal

import io.getstream.chat.android.client.errors.ChatError
import io.getstream.chat.android.client.test.randomMessage
import io.getstream.chat.android.client.utils.Result
import io.getstream.chat.android.client.utils.SyncStatus
import io.getstream.chat.android.offline.plugin.logic.channel.internal.ChannelLogic
import io.getstream.chat.android.offline.plugin.logic.channel.thread.internal.ThreadLogic
import io.getstream.chat.android.offline.plugin.logic.internal.LogicRegistry
import io.getstream.chat.android.test.randomString
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.test.runTest
import org.junit.jupiter.api.Test
import org.mockito.kotlin.any
import org.mockito.kotlin.argThat
import org.mockito.kotlin.doReturn
import org.mockito.kotlin.mock
import org.mockito.kotlin.verify

@OptIn(ExperimentalCoroutinesApi::class)
internal class SendMessageListenerStateTest {

    private val channelLogic: ChannelLogic = mock()
    private val threadLogic: ThreadLogic = mock()
    private val logicRegistry: LogicRegistry = mock {
        on(it.channelFromMessage(any())) doReturn channelLogic
        on(it.threadFromMessage(any())) doReturn threadLogic
    }

    private val sendMessageListener = SendMessageListenerState(logicRegistry)

    @Test
    fun `when request to send messages is successful, the message should be upserted with correct status`() = runTest {
        val testMessage = randomMessage(syncStatus = SyncStatus.SYNC_NEEDED)

        sendMessageListener.onMessageSendResult(
            result = Result.success(testMessage),
            channelType = randomString(),
            channelId = randomString(),
            message = testMessage,
        )

        verify(channelLogic).upsertMessage(argThat { message ->
            message.id == testMessage.id && message.syncStatus == SyncStatus.COMPLETED
        })
        verify(threadLogic).upsertMessage(argThat { message ->
            message.id == testMessage.id && message.syncStatus == SyncStatus.COMPLETED
        })
    }

    @Test
    fun `when request to send messages fails, the message should be upserted with correct status`() = runTest {
        val testMessage = randomMessage(syncStatus = SyncStatus.SYNC_NEEDED)

        sendMessageListener.onMessageSendResult(
            result = Result.error(ChatError()),
            channelType = randomString(),
            channelId = randomString(),
            message = testMessage,
        )

        verify(channelLogic).upsertMessage(argThat { message ->
            message.id == testMessage.id && message.syncStatus == SyncStatus.SYNC_NEEDED
        })
        verify(threadLogic).upsertMessage(argThat { message ->
            message.id == testMessage.id && message.syncStatus == SyncStatus.SYNC_NEEDED
        })
    }
}
