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

import io.getstream.chat.android.client.errors.isPermanent
import io.getstream.chat.android.client.persistance.repository.MessageRepository
import io.getstream.chat.android.client.plugin.listeners.DraftMessageListener
import io.getstream.chat.android.models.DraftMessage
import io.getstream.chat.android.models.DraftsSort
import io.getstream.chat.android.models.FilterObject
import io.getstream.chat.android.models.QueryDraftsResult
import io.getstream.chat.android.models.querysort.QuerySorter
import io.getstream.result.Result
import io.getstream.result.onSuccessSuspend

internal class DraftMessageListenerDatabase(
    private val messageRepository: MessageRepository,
) : DraftMessageListener {

    /**
     * Method called before the request to create a draft message in the API is launched. Persists the draft upfront so
     * it is not lost if the process dies while the request is in flight, and keeps it pending until the request
     * succeeds, so it is sent again when the connection comes back.
     *
     * @param channelType The type of the channel
     * @param channelId The id of the channel
     * @param message The draft message to be created
     */
    override suspend fun onCreateDraftMessageRequest(
        channelType: String,
        channelId: String,
        message: DraftMessage,
    ) {
        messageRepository.insertPendingDraftMessage(message)
    }

    /**
     * Method called when a request to create a draft message in the API happens. Replaces the draft persisted by
     * [onCreateDraftMessageRequest] with the server copy. On a failure the server will never accept, the local draft
     * stops being pending; on any other failure it stays pending.
     *
     * @param result The result of the create draft message request
     * @param channelType The type of the channel
     * @param channelId The id of the channel
     * @param message The draft message to be created
     */
    override suspend fun onCreateDraftMessageResult(
        result: Result<DraftMessage>,
        channelType: String,
        channelId: String,
        message: DraftMessage,
    ) {
        when (result) {
            is Result.Success -> messageRepository.insertDraftMessage(result.value)
            is Result.Failure -> if (result.value.isPermanent()) messageRepository.insertDraftMessage(message)
        }
    }

    /**
     * Method called before the request to delete draft messages in the API is launched. Hides the draft upfront so it
     * stays deleted if the process dies while the request is in flight, and keeps the deletion pending until the
     * request succeeds, so it is sent again when the connection comes back.
     *
     * @param channelType The type of the channel
     * @param channelId The id of the channel
     * @param message The draft message to be deleted
     */
    override suspend fun onDeleteDraftMessagesRequest(
        channelType: String,
        channelId: String,
        message: DraftMessage,
    ) {
        messageRepository.markDraftMessageDeleted(message)
    }

    /**
     * Method called when a request to delete draft messages in the API happens. Removes the draft hidden by
     * [onDeleteDraftMessagesRequest] once the server has no draft any more, or on a failure the server will never
     * accept. On any other failure the deletion stays pending.
     *
     * @param result The result of the delete draft messages request
     * @param channelType The type of the channel
     * @param channelId The id of the channel
     * @param message The draft message to be deleted
     */
    override suspend fun onDeleteDraftMessagesResult(
        result: Result<Unit>,
        channelType: String,
        channelId: String,
        message: DraftMessage,
    ) {
        when (result) {
            is Result.Success -> messageRepository.deleteDraftMessage(message)
            // A missing draft is a permanent failure too, so a deletion that already landed is not sent forever.
            is Result.Failure -> if (result.value.isPermanent()) messageRepository.deleteDraftMessage(message)
        }
    }

    /**
     * Method called when a request to query draft messages in the API happens
     *
     * @param result The result of the query draft messages request
     * @param filter The filter object used in the query
     * @param limit The limit of the query
     * @param next The next page token
     * @param sort The sorter used in the query
     */
    override suspend fun onQueryDraftMessagesResult(
        result: Result<QueryDraftsResult>,
        filter: FilterObject,
        limit: Int,
        next: String?,
        sort: QuerySorter<DraftsSort>,
    ) {
        result.onSuccessSuspend {
            it.drafts.forEach { draftMessage ->
                messageRepository.insertDraftMessage(draftMessage)
            }
        }
    }
}
