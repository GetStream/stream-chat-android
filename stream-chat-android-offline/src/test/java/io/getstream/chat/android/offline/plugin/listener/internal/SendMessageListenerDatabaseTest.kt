package io.getstream.chat.android.offline.plugin.listener.internal

import io.getstream.chat.android.client.errors.ChatError
import io.getstream.chat.android.client.extensions.internal.users
import io.getstream.chat.android.client.persistance.repository.MessageRepository
import io.getstream.chat.android.client.persistance.repository.UserRepository
import io.getstream.chat.android.client.test.randomMessage
import io.getstream.chat.android.client.utils.Result
import io.getstream.chat.android.client.utils.SyncStatus
import io.getstream.chat.android.test.randomString
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.test.runTest
import org.junit.jupiter.api.Test
import org.mockito.kotlin.argThat
import org.mockito.kotlin.eq
import org.mockito.kotlin.mock
import org.mockito.kotlin.reset
import org.mockito.kotlin.verify

@OptIn(ExperimentalCoroutinesApi::class)
internal class SendMessageListenerDatabaseTest {

    private val userRepository: UserRepository = mock()
    private val messageRepository: MessageRepository = mock()

    private val sendMessageListenerDatabase = SendMessageListenerDatabase(userRepository, messageRepository)

    @Test
    fun `when request to send messages is successful, the message should be upserted with correct status`() = runTest {
        val testMessage = randomMessage(syncStatus = SyncStatus.IN_PROGRESS)

        sendMessageListenerDatabase.onMessageSendResult(
            result = Result.success(testMessage),
            channelType = randomString(),
            channelId = randomString(),
            message = testMessage,
        )

        verify(userRepository).insertUsers(testMessage.users())
        verify(messageRepository).insertMessage(
            argThat { message ->
                message.id == testMessage.id && message.syncStatus == SyncStatus.COMPLETED
            },
            eq(false)
        )

    }

    @Test
    fun `when request to send messages fails, the message should be upserted with correct status`() = runTest {
        val testMessage = randomMessage(syncStatus = SyncStatus.IN_PROGRESS)

        sendMessageListenerDatabase.onMessageSendResult(
            result = Result.error(ChatError()),
            channelType = randomString(),
            channelId = randomString(),
            message = testMessage,
        )

        verify(userRepository).insertUsers(testMessage.users())
        verify(messageRepository).insertMessage(
            argThat { message ->
                message.id == testMessage.id && message.syncStatus == SyncStatus.SYNC_NEEDED
            },
            eq(false)
        )
    }
}
