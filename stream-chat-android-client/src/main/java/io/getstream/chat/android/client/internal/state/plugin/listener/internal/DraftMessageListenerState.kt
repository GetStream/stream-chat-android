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

import io.getstream.chat.android.client.errors.isPermanent
import io.getstream.chat.android.client.internal.state.plugin.state.global.internal.MutableGlobalState
import io.getstream.chat.android.client.plugin.listeners.DraftMessageListener
import io.getstream.chat.android.models.DraftMessage
import io.getstream.chat.android.models.DraftsSort
import io.getstream.chat.android.models.FilterObject
import io.getstream.chat.android.models.QueryDraftsResult
import io.getstream.chat.android.models.querysort.QuerySorter
import io.getstream.result.Result

/**
 * Implementation for [DraftMessageListener] that handles state update.
 */
internal class DraftMessageListenerState(
    private val mutableGlobalState: MutableGlobalState,
) : DraftMessageListener {

    /**
     * Keeps a reference of the [DraftMessage] in the [MutableGlobalState] before the request to create a draft message
     * is launched, so it shows up right away. Drafts from the server do not replace it until the request succeeds.
     *
     * @param channelType The type of the channel in which message is created.
     * @param channelId The id of the the channel in which message is created.
     * @param message [DraftMessage] to be created.
     */
    override suspend fun onCreateDraftMessageRequest(
        channelType: String,
        channelId: String,
        message: DraftMessage,
    ) {
        mutableGlobalState.updatePendingDraftMessage(message)
    }

    /**
     * Replaces the reference of the [DraftMessage] in the [MutableGlobalState] with the server copy when the request to
     * create a draft message is successful. On a failure the server will never accept, the local draft stops being
     * pending; on any other failure it stays pending.
     *
     * @param result [Result] response from the original request.
     * @param channelType The type of the channel in which message is created.
     * @param channelId The id of the the channel in which message is created.
     * @param message [DraftMessage] to be created.
     */
    override suspend fun onCreateDraftMessageResult(
        result: Result<DraftMessage>,
        channelType: String,
        channelId: String,
        message: DraftMessage,
    ) {
        when (result) {
            is Result.Success -> {
                mutableGlobalState.completeDraftMessage(message)
                mutableGlobalState.updateDraftMessage(result.value)
            }
            is Result.Failure -> if (result.value.isPermanent()) mutableGlobalState.completeDraftMessage(message)
        }
    }

    /**
     * Removes the reference of the [DraftMessage] from the [MutableGlobalState] before the request to delete a draft
     * message is launched, so it disappears right away. Drafts from the server do not bring it back until the request
     * succeeds.
     *
     * @param channelType The type of the channel in which message is updated.
     * @param channelId The id of the the channel in which message is updated.
     * @param message [DraftMessage] to be deleted.
     */
    override suspend fun onDeleteDraftMessagesRequest(
        channelType: String,
        channelId: String,
        message: DraftMessage,
    ) {
        mutableGlobalState.removePendingDraftMessage(message)
    }

    /**
     * Method called when a request to delete draft messages in the API happens. Ends the pending deletion when the
     * request succeeds, or on a failure the server will never accept.
     *
     * @param result [Result] response from the original request.
     * @param channelType The type of the channel in which message is updated.
     * @param channelId The id of the the channel in which message is updated.
     * @param message [DraftMessage] to be updated.
     */
    override suspend fun onDeleteDraftMessagesResult(
        result: Result<Unit>,
        channelType: String,
        channelId: String,
        message: DraftMessage,
    ) {
        when (result) {
            is Result.Success -> mutableGlobalState.completeDraftMessage(message)
            is Result.Failure -> if (result.value.isPermanent()) mutableGlobalState.completeDraftMessage(message)
        }
    }

    /**
     * Updates the [MutableGlobalState] with the list of [DraftMessage] when the request to query draft messages
     * is successful.
     *
     * @param result [Result] response from the original request.
     * @param filter The filter object used to query draft messages.
     * @param limit The limit of the query.
     * @param next The next page token of the query.
     * @param sort The sorter used to query draft messages.
     */
    override suspend fun onQueryDraftMessagesResult(
        result: Result<QueryDraftsResult>,
        filter: FilterObject,
        limit: Int,
        next: String?,
        sort: QuerySorter<DraftsSort>,
    ) {
        result.onSuccess {
            it.drafts.forEach { draftMessage ->
                mutableGlobalState.updateDraftMessage(draftMessage)
            }
        }
    }
}
