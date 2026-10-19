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

package io.getstream.chat.android.client.internal.offline.plugin.listener.internal

import io.getstream.chat.android.client.extensions.internal.users
import io.getstream.chat.android.client.persistance.repository.MessageRepository
import io.getstream.chat.android.client.persistance.repository.UserRepository
import io.getstream.chat.android.models.MessageType
import io.getstream.chat.android.models.SyncStatus
import io.getstream.chat.android.randomCID
import io.getstream.chat.android.randomMessage
import io.getstream.result.Error
import io.getstream.result.Result
import kotlinx.coroutines.test.runTest
import org.junit.jupiter.api.Test
import org.mockito.kotlin.any
import org.mockito.kotlin.doReturn
import org.mockito.kotlin.mock
import org.mockito.kotlin.never
import org.mockito.kotlin.verify
import org.mockito.kotlin.whenever

internal class SendGiphyListenerDatabaseTest {

    private val userRepository: UserRepository = mock()
    private val messageRepository: MessageRepository = mock()

    private val sendGiphyListenerDatabase = SendGiphyListenerDatabase(
        userRepository = userRepository,
        messageRepository = messageRepository,
    )

    @Test
    fun `when sending giphy succeeds, the sent message should replace the stored preview`() = runTest {
        val sentMessage = randomMessage(type = MessageType.REGULAR, syncStatus = SyncStatus.IN_PROGRESS)
        whenever(messageRepository.selectMessage(sentMessage.id)) doReturn
            sentMessage.copy(type = MessageType.EPHEMERAL, syncStatus = SyncStatus.COMPLETED)

        sendGiphyListenerDatabase.onGiphySendResult(sentMessage.cid, Result.Success(sentMessage))

        verify(userRepository).insertUsers(sentMessage.users())
        verify(messageRepository).insertMessage(sentMessage.copy(syncStatus = SyncStatus.COMPLETED))
    }

    @Test
    fun `when sending giphy succeeds and nothing is stored, the sent message should be inserted`() = runTest {
        val sentMessage = randomMessage(type = MessageType.REGULAR)
        whenever(messageRepository.selectMessage(sentMessage.id)) doReturn null

        sendGiphyListenerDatabase.onGiphySendResult(sentMessage.cid, Result.Success(sentMessage))

        verify(messageRepository).insertMessage(sentMessage.copy(syncStatus = SyncStatus.COMPLETED))
    }

    @Test
    fun `when sending giphy succeeds after its server copy is stored, the stored copy should be kept`() = runTest {
        val sentMessage = randomMessage(type = MessageType.REGULAR)
        whenever(messageRepository.selectMessage(sentMessage.id)) doReturn sentMessage.copy(text = "newer")

        sendGiphyListenerDatabase.onGiphySendResult(sentMessage.cid, Result.Success(sentMessage))

        verify(messageRepository, never()).insertMessage(any())
    }

    @Test
    fun `when sending giphy and request fails, nothing should be inserted in database`() = runTest {
        sendGiphyListenerDatabase.onGiphySendResult(randomCID(), Result.Failure(Error.GenericError("")))

        verify(userRepository, never()).insertUsers(any())
        verify(messageRepository, never()).insertMessage(any())
    }
}
