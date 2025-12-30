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

import io.getstream.chat.android.client.extensions.internal.users
import io.getstream.chat.android.client.persistance.repository.MessageRepository
import io.getstream.chat.android.client.persistance.repository.UserRepository
import io.getstream.chat.android.client.setup.state.ClientState
import io.getstream.chat.android.models.SyncStatus
import io.getstream.chat.android.randomMessage
import io.getstream.result.Error
import io.getstream.result.Result
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.test.runTest
import org.junit.jupiter.api.Test
import org.mockito.kotlin.argThat
import org.mockito.kotlin.doReturn
import org.mockito.kotlin.mock
import org.mockito.kotlin.verify
import org.mockito.kotlin.whenever

@OptIn(ExperimentalCoroutinesApi::class)
internal class EditMessageListenerDatabaseTest {

    private val userRepository: UserRepository = mock()
    private val messageRepository: MessageRepository = mock()
    private val clientState: ClientState = mock()

    private val editMessageListenerDatabase: EditMessageListenerDatabase =
        EditMessageListenerDatabase(userRepository, messageRepository, clientState)

    @Test
    fun `when messages edit is requested, sync status is updated correctly when online`() = runTest {
        whenever(clientState.isNetworkAvailable) doReturn true

        val testMessage = randomMessage()

        editMessageListenerDatabase.onMessageEditRequest(testMessage)

        verify(userRepository).insertUsers(testMessage.users())
        verify(messageRepository).insertMessage(
            argThat { message ->
                testMessage.id == message.id && message.syncStatus == SyncStatus.IN_PROGRESS
            },
        )
    }

    @Test
    fun `when messages edit is requested, sync status is updated correctly when offline`() = runTest {
        whenever(clientState.isNetworkAvailable) doReturn false

        val testMessage = randomMessage()

        editMessageListenerDatabase.onMessageEditRequest(testMessage)

        verify(userRepository).insertUsers(testMessage.users())
        verify(messageRepository).insertMessage(
            argThat { message ->
                testMessage.id == message.id && message.syncStatus == SyncStatus.SYNC_NEEDED
            },
        )
    }

    @Test
    fun `when messages edit is completed, sync status is updated correctly when successful`() = runTest {
        whenever(clientState.isNetworkAvailable) doReturn true

        val testMessage = randomMessage()

        editMessageListenerDatabase.onMessageEditResult(testMessage, Result.Success(testMessage))

        verify(userRepository).insertUsers(testMessage.users())
        verify(messageRepository).insertMessage(
            argThat { message ->
                testMessage.id == message.id && message.syncStatus == SyncStatus.COMPLETED
            },
        )
    }

    @Test
    fun `when messages edit is completed, sync status is updated correctly when offline`() = runTest {
        whenever(clientState.isNetworkAvailable) doReturn false

        val testMessage = randomMessage()

        editMessageListenerDatabase.onMessageEditResult(testMessage, Result.Failure(Error.GenericError("")))

        verify(userRepository).insertUsers(testMessage.users())
        verify(messageRepository).insertMessage(
            argThat { message ->
                testMessage.id == message.id && message.syncStatus == SyncStatus.SYNC_NEEDED
            },
        )
    }
}
