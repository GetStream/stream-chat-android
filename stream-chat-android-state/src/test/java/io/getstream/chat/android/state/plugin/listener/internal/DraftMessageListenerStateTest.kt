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

package io.getstream.chat.android.state.plugin.listener.internal

import io.getstream.chat.android.randomDraftMessage
import io.getstream.chat.android.randomInt
import io.getstream.chat.android.randomString
import io.getstream.chat.android.state.plugin.state.global.internal.MutableGlobalState
import io.getstream.result.Error
import io.getstream.result.Result
import kotlinx.coroutines.test.runTest
import org.junit.jupiter.api.Test
import org.mockito.kotlin.any
import org.mockito.kotlin.mock
import org.mockito.kotlin.never
import org.mockito.kotlin.verify

internal class DraftMessageListenerStateTest {

    private val mutableGlobalState: MutableGlobalState = mock()
    private val listener = DraftMessageListenerState(mutableGlobalState)

    @Test
    fun `onCreateDraftMessageResult should update state on success`() = runTest {
        val draftMessage = randomDraftMessage()

        listener.onCreateDraftMessageResult(
            result = Result.Success(draftMessage),
            channelType = randomString(),
            channelId = randomString(),
            message = draftMessage,
        )

        verify(mutableGlobalState).updateDraftMessage(draftMessage)
    }

    @Test
    fun `onCreateDraftMessageResult should not update state on error`() = runTest {
        listener.onCreateDraftMessageResult(
            result = Result.Failure(Error.GenericError("")),
            channelType = randomString(),
            channelId = randomString(),
            message = randomDraftMessage(),
        )

        verify(mutableGlobalState, never()).updateDraftMessage(any())
    }

    @Test
    fun `onDeleteDraftMessagesResult should remove message from state on success`() = runTest {
        val draftMessage = randomDraftMessage()

        listener.onDeleteDraftMessagesResult(
            result = Result.Success(Unit),
            channelType = randomString(),
            channelId = randomString(),
            message = draftMessage,
        )

        verify(mutableGlobalState).removeDraftMessage(draftMessage)
    }

    @Test
    fun `onDeleteDraftMessagesResult should not remove message from state on error`() = runTest {
        listener.onDeleteDraftMessagesResult(
            result = Result.Failure(Error.GenericError(message = "")),
            channelType = randomString(),
            channelId = randomString(),
            message = randomDraftMessage(),
        )

        verify(mutableGlobalState, never()).removeDraftMessage(any())
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
            verify(mutableGlobalState).updateDraftMessage(message)
        }
    }

    @Test
    fun `onQueryDraftMessagesResult should not update state on error`() = runTest {
        listener.onQueryDraftMessagesResult(
            Result.Failure(Error.GenericError("")),
            randomInt(),
            randomInt(),
        )

        verify(mutableGlobalState, never()).updateDraftMessage(any())
    }
}
