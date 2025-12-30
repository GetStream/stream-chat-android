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

package io.getstream.chat.android.internal.offline.plugin.listener.internal

import io.getstream.chat.android.client.persistance.repository.MessageRepository
import io.getstream.chat.android.client.persistance.repository.UserRepository
import io.getstream.chat.android.client.setup.state.ClientState
import io.getstream.chat.android.models.SyncStatus
import io.getstream.chat.android.randomCID
import io.getstream.chat.android.randomMessage
import io.getstream.result.Error
import io.getstream.result.Result
import kotlinx.coroutines.test.runTest
import org.junit.jupiter.api.Test
import org.mockito.kotlin.any
import org.mockito.kotlin.argThat
import org.mockito.kotlin.doReturn
import org.mockito.kotlin.mock
import org.mockito.kotlin.verify
import org.mockito.kotlin.whenever

internal class DeleteMessageListenerDatabaseTest {

    private val clientState: ClientState = mock()

    private val messageRepository: MessageRepository = mock()
    private val userRepository: UserRepository = mock()

    private val deleteMessageListenerState: DeleteMessageListenerDatabase =
        DeleteMessageListenerDatabase(clientState, messageRepository, userRepository)

    @Test
    fun `when internet is available, the message should be updated as in progress before the request`() = runTest {
        val testMessage = randomMessage(
            cid = randomCID(),
            syncStatus = SyncStatus.SYNC_NEEDED,
        )

        whenever(clientState.isNetworkAvailable) doReturn true
        whenever(messageRepository.selectMessage(any())) doReturn testMessage

        deleteMessageListenerState.onMessageDeleteRequest(randomCID())

        verify(messageRepository).insertMessage(
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
        whenever(messageRepository.selectMessage(any())) doReturn testMessage

        deleteMessageListenerState.onMessageDeleteRequest(randomCID())

        verify(messageRepository).insertMessage(
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
            syncStatus = SyncStatus.SYNC_NEEDED,
        )

        whenever(clientState.isNetworkAvailable) doReturn false
        whenever(messageRepository.selectMessage(any())) doReturn testMessage

        deleteMessageListenerState.onMessageDeleteResult(randomCID(), Result.Success(testMessage))

        verify(messageRepository).insertMessage(
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

        whenever(clientState.isNetworkAvailable) doReturn false
        whenever(messageRepository.selectMessage(any())) doReturn testMessage

        deleteMessageListenerState.onMessageDeleteResult(randomCID(), Result.Failure(Error.GenericError("")))

        verify(messageRepository).insertMessage(
            argThat { message ->
                // The same ID, but not the status was correctly updated
                message.id == testMessage.id && message.syncStatus == SyncStatus.SYNC_NEEDED
            },
        )
    }
}
