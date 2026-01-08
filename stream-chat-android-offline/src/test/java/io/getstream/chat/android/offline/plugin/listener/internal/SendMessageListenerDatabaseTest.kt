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

package io.getstream.chat.android.offline.plugin.listener.internal

import io.getstream.chat.android.client.extensions.internal.users
import io.getstream.chat.android.client.persistance.repository.MessageRepository
import io.getstream.chat.android.client.persistance.repository.UserRepository
import io.getstream.chat.android.models.SyncStatus
import io.getstream.chat.android.randomMessage
import io.getstream.chat.android.randomString
import io.getstream.result.Error
import io.getstream.result.Result
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.test.runTest
import org.junit.jupiter.api.Test
import org.mockito.kotlin.any
import org.mockito.kotlin.argThat
import org.mockito.kotlin.doReturn
import org.mockito.kotlin.mock
import org.mockito.kotlin.never
import org.mockito.kotlin.verify
import org.mockito.kotlin.whenever

@OptIn(ExperimentalCoroutinesApi::class)
internal class SendMessageListenerDatabaseTest {

    private val userRepository: UserRepository = mock()
    private val messageRepository: MessageRepository = mock()

    private val sendMessageListenerDatabase = SendMessageListenerDatabase(userRepository, messageRepository)

    @Test
    fun `when request to send messages is successful, the message should be upserted with correct status`() = runTest {
        whenever(messageRepository.selectMessage(any())) doReturn null

        val testMessage = randomMessage(syncStatus = SyncStatus.IN_PROGRESS)

        sendMessageListenerDatabase.onMessageSendResult(
            result = Result.Success(testMessage),
            channelType = randomString(),
            channelId = randomString(),
            message = testMessage,
        )

        verify(userRepository).insertUsers(testMessage.users())
        verify(messageRepository).insertMessage(
            argThat { message ->
                message.id == testMessage.id && message.syncStatus == SyncStatus.COMPLETED
            },
        )
    }

    @Test
    fun `when request to send messages fails, the message should be upserted with correct status`() = runTest {
        whenever(messageRepository.selectMessage(any())) doReturn null

        val testMessage = randomMessage(syncStatus = SyncStatus.IN_PROGRESS)

        sendMessageListenerDatabase.onMessageSendResult(
            result = Result.Failure(Error.GenericError("")),
            channelType = randomString(),
            channelId = randomString(),
            message = testMessage,
        )

        verify(userRepository).insertUsers(testMessage.users())
        verify(messageRepository).insertMessage(
            argThat { message ->
                message.id == testMessage.id && message.syncStatus == SyncStatus.SYNC_NEEDED
            },
        )
    }

    @Test
    fun `when message is already in database and completed, it should not be inserted again`() = runTest {
        whenever(messageRepository.selectMessage(any())) doReturn randomMessage(syncStatus = SyncStatus.COMPLETED)

        val testMessage = randomMessage(syncStatus = SyncStatus.IN_PROGRESS)

        sendMessageListenerDatabase.onMessageSendResult(
            result = Result.Failure(Error.GenericError("")),
            channelType = randomString(),
            channelId = randomString(),
            message = testMessage,
        )

        verify(userRepository, never()).insertUsers(testMessage.users())
        verify(messageRepository, never()).insertMessage(
            argThat { message -> message.id == testMessage.id },
        )
    }
}
