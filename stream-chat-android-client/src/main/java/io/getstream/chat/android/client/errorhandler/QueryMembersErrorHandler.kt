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

package io.getstream.chat.android.client.errorhandler

import io.getstream.chat.android.core.internal.InternalStreamChatApi
import io.getstream.chat.android.models.FilterObject
import io.getstream.chat.android.models.Member
import io.getstream.chat.android.models.querysort.QuerySorter
import io.getstream.result.Result
import io.getstream.result.call.Call
import io.getstream.result.call.ReturnOnErrorCall

/**
 * Error handler for [io.getstream.chat.android.client.ChatClient.queryMembers] calls.
 */
@InternalStreamChatApi
public interface QueryMembersErrorHandler {

    /**
     * Returns a [Result] from this side effect when original request is failed.
     *
     * @param originalCall The original call.
     * @param cid The full channel id, i.e. "messaging:123".
     * @param messageId The id of the message to which reaction belongs.
     *
     * @return result The replacement for the original result.
     */
    @Suppress("LongParameterList")
    public fun onQueryMembersError(
        originalCall: Call<List<Member>>,
        channelType: String,
        channelId: String,
        offset: Int,
        limit: Int,
        filter: FilterObject,
        sort: QuerySorter<Member>,
        members: List<Member>,
    ): ReturnOnErrorCall<List<Member>>
}

@Suppress("LongParameterList")
internal fun Call<List<Member>>.onQueryMembersError(
    errorHandlers: List<QueryMembersErrorHandler>,
    channelType: String,
    channelId: String,
    offset: Int,
    limit: Int,
    filter: FilterObject,
    sort: QuerySorter<Member>,
    members: List<Member>,
): Call<List<Member>> = errorHandlers.fold(this) { messageCall, errorHandler ->
    errorHandler.onQueryMembersError(messageCall, channelType, channelId, offset, limit, filter, sort, members)
}
