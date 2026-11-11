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

package io.getstream.chat.android.client.internal.state.plugin.listener.internal

import io.getstream.chat.android.client.internal.state.plugin.state.global.internal.MutableGlobalState
import io.getstream.chat.android.models.DraftMessage
import io.getstream.chat.android.models.QueryDraftsResult
import io.getstream.chat.android.randomChannel
import io.getstream.chat.android.randomDraftMessage
import io.getstream.chat.android.randomInt
import io.getstream.chat.android.randomString
import io.getstream.result.Error
import io.getstream.result.Result
import kotlinx.coroutines.test.runTest
import org.amshove.kluent.shouldBeEqualTo
import org.amshove.kluent.shouldBeNull
import org.junit.jupiter.api.Test
import org.mockito.kotlin.mock
import java.util.Date

internal class DraftMessageListenerStateTest {

    private val mutableGlobalState = MutableGlobalState(userId = randomString())
    private val listener = DraftMessageListenerState(mutableGlobalState)

    private val channel = randomChannel()
    private val serverCopy = randomDraftMessage(cid = channel.cid, parentId = null, replyMessage = null)
    private val localDraft = serverCopy.copy(text = randomString())

    @Test
    fun `onCreateDraftMessageRequest should update state before the request completes`() = runTest {
        listener.onCreateDraftMessageRequest(channel.type, channel.id, localDraft)

        channelDraft() shouldBeEqualTo localDraft
    }

    @Test
    fun `an older server copy should not replace a draft saved while the request is pending`() = runTest {
        listener.onCreateDraftMessageRequest(channel.type, channel.id, localDraft)

        mutableGlobalState.updateChannelDrafts(listOf(channel.copy(draftMessage = serverCopy)))

        channelDraft() shouldBeEqualTo localDraft
    }

    @Test
    fun `an older server copy should not replace a draft whose request failed for a temporary reason`() = runTest {
        listener.onCreateDraftMessageRequest(channel.type, channel.id, localDraft)
        listener.onCreateDraftMessageResult(Result.Failure(temporaryError()), channel.type, channel.id, localDraft)

        mutableGlobalState.updateChannelDrafts(listOf(channel.copy(draftMessage = serverCopy)))

        channelDraft() shouldBeEqualTo localDraft
    }

    @Test
    fun `onCreateDraftMessageResult should show the server copy and accept later copies on success`() = runTest {
        val savedCopy = localDraft.copy(createdAt = Date())
        val laterCopy = serverCopy.copy(text = randomString())
        listener.onCreateDraftMessageRequest(channel.type, channel.id, localDraft)

        listener.onCreateDraftMessageResult(Result.Success(savedCopy), channel.type, channel.id, localDraft)
        channelDraft() shouldBeEqualTo savedCopy
        mutableGlobalState.updateChannelDrafts(listOf(channel.copy(draftMessage = laterCopy)))

        channelDraft() shouldBeEqualTo laterCopy
    }

    @Test
    fun `onCreateDraftMessageResult should keep the local draft and accept later copies on a permanent error`() =
        runTest {
            listener.onCreateDraftMessageRequest(channel.type, channel.id, localDraft)

            listener.onCreateDraftMessageResult(Result.Failure(permanentError()), channel.type, channel.id, localDraft)
            channelDraft() shouldBeEqualTo localDraft
            mutableGlobalState.updateChannelDrafts(listOf(channel.copy(draftMessage = serverCopy)))

            channelDraft() shouldBeEqualTo serverCopy
        }

    @Test
    fun `onDeleteDraftMessagesRequest should remove the draft from state before the request completes`() = runTest {
        mutableGlobalState.updateDraftMessage(serverCopy)

        listener.onDeleteDraftMessagesRequest(channel.type, channel.id, serverCopy)

        channelDraft().shouldBeNull()
    }

    @Test
    fun `a server copy should not bring back a draft deleted while the request is pending`() = runTest {
        mutableGlobalState.updateDraftMessage(serverCopy)
        listener.onDeleteDraftMessagesRequest(channel.type, channel.id, serverCopy)
        listener.onDeleteDraftMessagesResult(Result.Failure(temporaryError()), channel.type, channel.id, serverCopy)

        mutableGlobalState.updateChannelDrafts(listOf(channel.copy(draftMessage = serverCopy)))

        channelDraft().shouldBeNull()
    }

    @Test
    fun `a server copy should not bring back a thread draft deleted while the request is pending`() = runTest {
        val threadDraft = randomDraftMessage(cid = channel.cid, replyMessage = null)
        mutableGlobalState.updateDraftMessage(threadDraft)
        listener.onDeleteDraftMessagesRequest(channel.type, channel.id, threadDraft)

        mutableGlobalState.updateDraftMessage(threadDraft)

        mutableGlobalState.threadDraftMessages.value[threadDraft.parentId].shouldBeNull()
    }

    @Test
    fun `onDeleteDraftMessagesResult should accept later server copies once the deletion reached the server`() =
        runTest {
            val laterCopy = serverCopy.copy(text = randomString())
            mutableGlobalState.updateDraftMessage(serverCopy)
            listener.onDeleteDraftMessagesRequest(channel.type, channel.id, serverCopy)

            listener.onDeleteDraftMessagesResult(Result.Success(Unit), channel.type, channel.id, serverCopy)
            mutableGlobalState.updateChannelDrafts(listOf(channel.copy(draftMessage = laterCopy)))

            channelDraft() shouldBeEqualTo laterCopy
        }

    @Test
    fun `onDeleteDraftMessagesResult should end the deletion when the server has no draft`() = runTest {
        mutableGlobalState.updateDraftMessage(serverCopy)
        listener.onDeleteDraftMessagesRequest(channel.type, channel.id, serverCopy)

        listener.onDeleteDraftMessagesResult(Result.Failure(permanentError()), channel.type, channel.id, serverCopy)
        mutableGlobalState.updateChannelDrafts(listOf(channel.copy(draftMessage = serverCopy)))

        channelDraft() shouldBeEqualTo serverCopy
    }

    @Test
    fun `onQueryDraftMessagesResult should update state with all messages on success`() = runTest {
        val draftMessages = listOf(
            randomDraftMessage(parentId = null, replyMessage = null),
            randomDraftMessage(replyMessage = null),
        )

        listener.onQueryDraftMessagesResult(
            result = Result.Success(QueryDraftsResult(drafts = draftMessages, next = null)),
            filter = mock(),
            limit = randomInt(),
            next = null,
            sort = mock(),
        )

        mutableGlobalState.channelDraftMessages.value[draftMessages[0].cid] shouldBeEqualTo draftMessages[0]
        mutableGlobalState.threadDraftMessages.value[draftMessages[1].parentId] shouldBeEqualTo draftMessages[1]
    }

    @Test
    fun `onQueryDraftMessagesResult should not update state on error`() = runTest {
        listener.onQueryDraftMessagesResult(
            result = Result.Failure(Error.GenericError("")),
            filter = mock(),
            limit = randomInt(),
            next = null,
            sort = mock(),
        )

        mutableGlobalState.channelDraftMessages.value shouldBeEqualTo emptyMap()
        mutableGlobalState.threadDraftMessages.value shouldBeEqualTo emptyMap()
    }

    private fun channelDraft(): DraftMessage? = mutableGlobalState.channelDraftMessages.value[channel.cid]

    private fun permanentError() = Error.NetworkError(message = randomString(), serverErrorCode = 4, statusCode = 404)

    private fun temporaryError() = Error.NetworkError(message = randomString(), serverErrorCode = -1, statusCode = 500)
}
