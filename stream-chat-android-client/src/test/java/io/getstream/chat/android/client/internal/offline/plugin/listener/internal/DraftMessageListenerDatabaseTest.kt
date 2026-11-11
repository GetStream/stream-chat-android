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

import io.getstream.chat.android.client.persistance.repository.MessageRepository
import io.getstream.chat.android.models.Filters
import io.getstream.chat.android.models.QueryDraftsResult
import io.getstream.chat.android.models.querysort.QuerySortByField
import io.getstream.chat.android.randomDraftMessage
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
import java.util.Date

internal class DraftMessageListenerDatabaseTest {

    private val messageRepository: MessageRepository = mock()
    private val listener = DraftMessageListenerDatabase(messageRepository)

    @BeforeEach
    fun setup() {
        Mockito.reset(messageRepository)
    }

    @Test
    fun `onCreateDraftMessageRequest should persist the draft as pending before the request completes`() = runTest {
        val draftMessage = randomDraftMessage()

        listener.onCreateDraftMessageRequest(
            channelType = randomString(),
            channelId = randomString(),
            message = draftMessage,
        )

        verify(messageRepository).insertPendingDraftMessage(draftMessage)
    }

    @Test
    fun `onCreateDraftMessageResult should store the server copy as synced on success`() = runTest {
        val draftMessage = randomDraftMessage()
        val serverCopy = draftMessage.copy(createdAt = Date())

        listener.onCreateDraftMessageResult(
            result = Result.Success(serverCopy),
            channelType = randomString(),
            channelId = randomString(),
            message = draftMessage,
        )

        verify(messageRepository).insertDraftMessage(serverCopy)
    }

    @Test
    fun `onCreateDraftMessageResult should keep the draft pending on a temporary error`() = runTest {
        val draftMessage = randomDraftMessage()

        listener.onCreateDraftMessageResult(
            result = Result.Failure(temporaryError()),
            channelType = randomString(),
            channelId = randomString(),
            message = draftMessage,
        )

        verify(messageRepository, never()).insertDraftMessage(any())
    }

    @Test
    fun `onCreateDraftMessageResult should store the local draft as synced on a permanent error`() = runTest {
        val draftMessage = randomDraftMessage()

        listener.onCreateDraftMessageResult(
            result = Result.Failure(permanentError()),
            channelType = randomString(),
            channelId = randomString(),
            message = draftMessage,
        )

        verify(messageRepository).insertDraftMessage(draftMessage)
    }

    @Test
    fun `onDeleteDraftMessagesRequest should hide the draft before the request completes`() = runTest {
        val draftMessage = randomDraftMessage()

        listener.onDeleteDraftMessagesRequest(
            channelType = randomString(),
            channelId = randomString(),
            message = draftMessage,
        )

        verify(messageRepository).markDraftMessageDeleted(draftMessage)
    }

    @Test
    fun `onDeleteDraftMessagesResult should delete the hidden draft on success`() = runTest {
        val draftMessage = randomDraftMessage()

        listener.onDeleteDraftMessagesResult(
            result = Result.Success(Unit),
            channelType = randomString(),
            channelId = randomString(),
            message = draftMessage,
        )

        verify(messageRepository).deleteDraftMessage(draftMessage)
    }

    @Test
    fun `onDeleteDraftMessagesResult should delete the hidden draft when the server has no draft`() = runTest {
        val draftMessage = randomDraftMessage()

        listener.onDeleteDraftMessagesResult(
            result = Result.Failure(permanentError()),
            channelType = randomString(),
            channelId = randomString(),
            message = draftMessage,
        )

        verify(messageRepository).deleteDraftMessage(draftMessage)
    }

    @Test
    fun `onDeleteDraftMessagesResult should keep the deletion pending on a temporary error`() = runTest {
        val draftMessage = randomDraftMessage()

        listener.onDeleteDraftMessagesResult(
            result = Result.Failure(temporaryError()),
            channelType = randomString(),
            channelId = randomString(),
            message = draftMessage,
        )

        verify(messageRepository, never()).deleteDraftMessage(any())
    }

    @Test
    fun `onQueryDraftMessagesResult should update state with all messages on success`() = runTest {
        val draftMessages = listOf(randomDraftMessage(), randomDraftMessage())

        listener.onQueryDraftMessagesResult(
            Result.Success(QueryDraftsResult(draftMessages, null)),
            Filters.neutral(),
            2,
            null,
            QuerySortByField.ascByName("created_at"),
        )

        draftMessages.forEach { message ->
            verify(messageRepository).insertDraftMessage(message)
        }
    }

    @Test
    fun `onQueryDraftMessagesResult should not update state on error`() = runTest {
        listener.onQueryDraftMessagesResult(
            Result.Failure(Error.GenericError("")),
            Filters.neutral(),
            2,
            null,
            QuerySortByField.ascByName("created_at"),
        )

        verify(messageRepository, never()).insertDraftMessage(any())
    }

    private fun permanentError() = Error.NetworkError(message = randomString(), serverErrorCode = 4, statusCode = 404)

    private fun temporaryError() = Error.NetworkError(message = randomString(), serverErrorCode = -1, statusCode = 500)
}
