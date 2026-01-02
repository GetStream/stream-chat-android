/*
 * Copyright (c) 2014-2025 Stream.io Inc. All rights reserved.
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

import io.getstream.chat.android.client.persistance.repository.MessageRepository
import io.getstream.chat.android.client.setup.state.ClientState
import io.getstream.chat.android.models.Message
import io.getstream.chat.android.models.SyncStatus
import io.getstream.chat.android.randomMessage
import io.getstream.result.Result
import kotlinx.coroutines.test.runTest
import org.junit.Test
import org.mockito.kotlin.any
import org.mockito.kotlin.doReturn
import org.mockito.kotlin.mock
import org.mockito.kotlin.never
import org.mockito.kotlin.verifyBlocking
import org.mockito.kotlin.whenever
import org.mockito.kotlin.wheneverBlocking

internal class DeleteMessageForMeListenerDatabaseTest {

    @Test
    fun `on request, when message found and network available, should insert message`() = runTest {
        val message = randomMessage()
        val fixture = Fixture()
            .givenNetworkAvailable(true)
            .givenSelectMessage(message)
        val sut = fixture.get()

        sut.onDeleteMessageForMeRequest(message.id)

        val updatedMessage = message.copy(deletedForMe = true, syncStatus = SyncStatus.IN_PROGRESS)
        fixture.verifyInsertMessage(updatedMessage)
    }

    @Test
    fun `on request, when message found and network unavailable, should insert message`() = runTest {
        val message = randomMessage()
        val fixture = Fixture()
            .givenNetworkAvailable(false)
            .givenSelectMessage(message)
        val sut = fixture.get()

        sut.onDeleteMessageForMeRequest(message.id)

        val updatedMessage = message.copy(deletedForMe = true, syncStatus = SyncStatus.SYNC_NEEDED)
        fixture.verifyInsertMessage(updatedMessage)
    }

    @Test
    fun `on request, when message not found, should never insert`() = runTest {
        val message = randomMessage()
        val fixture = Fixture()
        val sut = fixture.get()

        sut.onDeleteMessageForMeRequest(message.id)

        fixture.verifyNeverInserted()
    }

    @Test
    fun `on result success, should insert message`() = runTest {
        val message = randomMessage()
        val fixture = Fixture()
        val sut = fixture.get()

        sut.onDeleteMessageForMeResult(message.id, Result.Success(message))

        val updatedMessage = message.copy(syncStatus = SyncStatus.COMPLETED)
        fixture.verifyInsertMessage(updatedMessage)
    }

    @Test
    fun `on result failure, when message found, should insert message`() = runTest {
        val message = randomMessage()
        val fixture = Fixture()
            .givenSelectMessage(message)
        val sut = fixture.get()

        sut.onDeleteMessageForMeResult(message.id, Result.Failure(mock()))

        val updatedMessage = message.copy(deletedForMe = true, syncStatus = SyncStatus.SYNC_NEEDED)
        fixture.verifyInsertMessage(updatedMessage)
    }

    @Test
    fun `on result failure, when message not found, should never insert`() = runTest {
        val message = randomMessage()
        val fixture = Fixture()
        val sut = fixture.get()

        sut.onDeleteMessageForMeResult(message.id, Result.Failure(mock()))

        fixture.verifyNeverInserted()
    }

    private class Fixture {

        private val clientState: ClientState = mock()
        private val messageRepository: MessageRepository = mock()

        fun givenNetworkAvailable(isNetworkAvailable: Boolean) = apply {
            whenever(clientState.isNetworkAvailable) doReturn isNetworkAvailable
        }

        fun givenSelectMessage(message: Message) = apply {
            wheneverBlocking { messageRepository.selectMessage(message.id) } doReturn message
        }

        fun verifyInsertMessage(message: Message) = apply {
            verifyBlocking(messageRepository) { insertMessage(message) }
        }

        fun verifyNeverInserted() = apply {
            verifyBlocking(messageRepository, never()) { insertMessage(any()) }
        }

        fun get() = DeleteMessageForMeListenerDatabase(
            clientState = clientState,
            messageRepository = messageRepository,
        )
    }
}
