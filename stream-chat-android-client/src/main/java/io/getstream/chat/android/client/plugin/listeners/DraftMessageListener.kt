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

package io.getstream.chat.android.client.plugin.listeners

import io.getstream.chat.android.models.DraftMessage
import io.getstream.chat.android.models.DraftsSort
import io.getstream.chat.android.models.FilterObject
import io.getstream.chat.android.models.QueryDraftsResult
import io.getstream.chat.android.models.querysort.QuerySorter
import io.getstream.result.Result

/**
 * Listener for draft message.
 */
public interface DraftMessageListener {

    /**
     * Side effect to be invoked when the original request is completed with a response.
     *
     * @param result [Result] response from the original request.
     * @param channelType The type of the channel in which message is created.
     * @param channelId The id of the the channel in which message is created.
     * @param message [DraftMessage] to be created.
     */
    public suspend fun onCreateDraftMessageResult(
        result: Result<DraftMessage>,
        channelType: String,
        channelId: String,
        message: DraftMessage,
    )

    /**
     * Side effect to be invoked when the original request is completed with a response.
     *
     * @param result [Result] response from the original request.
     * @param channelType The type of the channel in which message is updated.
     * @param channelId The id of the the channel in which message is updated.
     * @param message [DraftMessage] to be updated.
     */
    public suspend fun onDeleteDraftMessagesResult(
        result: Result<Unit>,
        channelType: String,
        channelId: String,
        message: DraftMessage,
    )

    /**
     * Side effect to be invoked when the original request is completed with a response.
     *
     * @param result [Result] response from the original request.
     * @param offset The offset of the query.
     * @param limit The limit of the query.
     */
    @Deprecated(
        message = "The offset param in the onQueryDraftMessagesResult method is not used. Use the " +
            "onQueryDraftMessagesResult(Result, FilterObject, Int, String?, QuerySorter) method instead.",
    )
    public suspend fun onQueryDraftMessagesResult(
        result: Result<List<DraftMessage>>,
        offset: Int?,
        limit: Int?,
    )

    /**
     * Side effect to be invoked when the original request is completed with a response.
     *
     * @param result [Result] response from the original request.
     * @param filter The filter object used in the query.
     * @param limit The limit of the query.
     * @param next The next page token.
     * @param sort The sorter used in the query.
     */
    public suspend fun onQueryDraftMessagesResult(
        result: Result<QueryDraftsResult>,
        filter: FilterObject,
        limit: Int,
        next: String?,
        sort: QuerySorter<DraftsSort>,
    )
}
