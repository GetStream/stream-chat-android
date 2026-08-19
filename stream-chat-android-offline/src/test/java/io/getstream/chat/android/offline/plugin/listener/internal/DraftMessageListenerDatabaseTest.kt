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

import io.getstream.chat.android.client.persistance.repository.MessageRepository
import io.getstream.chat.android.randomDraftMessage
import io.getstream.chat.android.randomInt
import io.getstream.chat.android.randomString
import io.getstream.result.Error
import io.getstream.result.Result
import kotlinx.coroutines.test.runTest
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.mockito.Mockito
import org.mockito.kotlin.any
import org.mockito.kotlin.mock
import org.mockito.kotlin.never
import org.mockito.kotlin.verify

internal class DraftMessageListenerDatabaseTest {

    private val messageRepository: MessageRepository = mock()
    private val listener = DraftMessageListenerDatabase(messageRepository)

    @BeforeEach
    fun setup() {
        Mockito.reset(messageRepository)
    }

    @Test
    fun `onCreateDraftMessageRequest should persist the draft before the request completes`() = runTest {
        val draftMessage = randomDraftMessage()

        listener.onCreateDraftMessageRequest(
            channelType = randomString(),
            channelId = randomString(),
            message = draftMessage,
        )

        verify(messageRepository).insertDraftMessage(draftMessage)
    }

    @Test
    fun `onCreateDraftMessageResult should update state on success`() = runTest {
        val draftMessage = randomDraftMessage()

        listener.onCreateDraftMessageResult(
            result = Result.Success(draftMessage),
            channelType = randomString(),
            channelId = randomString(),
            message = draftMessage,
        )

        verify(messageRepository).insertDraftMessage(draftMessage)
    }

    @Test
    fun `onCreateDraftMessageResult should keep the persisted draft on any error`() = runTest {
        val draftMessage = randomDraftMessage()

        listener.onCreateDraftMessageResult(
            result = Result.Failure(Error.NetworkError(message = randomString(), 404)),
            channelType = randomString(),
            channelId = randomString(),
            message = draftMessage,
        )

        verify(messageRepository, never()).deleteDraftMessage(any())
        verify(messageRepository, never()).insertDraftMessage(any())
    }

    @Test
    fun `onDeleteDraftMessagesRequest should remove the draft before the request completes`() = runTest {
        val draftMessage = randomDraftMessage()

        listener.onDeleteDraftMessagesRequest(
            channelType = randomString(),
            channelId = randomString(),
            message = draftMessage,
        )

        verify(messageRepository).deleteDraftMessage(draftMessage)
    }

    @Test
    fun `onDeleteDraftMessagesResult should not touch storage on any outcome`() = runTest {
        val draftMessage = randomDraftMessage()

        listener.onDeleteDraftMessagesResult(
            result = Result.Failure(Error.NetworkError(message = randomString(), 404)),
            channelType = randomString(),
            channelId = randomString(),
            message = draftMessage,
        )
        listener.onDeleteDraftMessagesResult(
            result = Result.Success(Unit),
            channelType = randomString(),
            channelId = randomString(),
            message = draftMessage,
        )

        verify(messageRepository, never()).deleteDraftMessage(any())
        verify(messageRepository, never()).insertDraftMessage(any())
    }

    @Test
    fun `onQueryDraftMessagesResult should update state with all messages on success`() = runTest {
        val draftMessages = listOf(randomDraftMessage(), randomDraftMessage())

        listener.onQueryDraftMessagesResult(
            Result.Success(draftMessages),
            randomInt(),
            randomInt(),
        )

        draftMessages.forEach { message ->
            verify(messageRepository).insertDraftMessage(message)
        }
    }

    @Test
    fun `onQueryDraftMessagesResult should not update state on error`() = runTest {
        listener.onQueryDraftMessagesResult(
            Result.Failure(Error.GenericError("")),
            randomInt(),
            randomInt(),
        )

        verify(messageRepository, never()).insertDraftMessage(any())
    }
}
