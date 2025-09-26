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

package io.getstream.chat.android.client.query.request

import io.getstream.chat.android.client.ChatClient
import io.getstream.chat.android.client.api.models.QueryChannelsRequest
import io.getstream.chat.android.core.internal.InternalStreamChatApi
import io.getstream.chat.android.models.Channel
import io.getstream.chat.android.models.FilterObject
import io.getstream.result.Result

@InternalStreamChatApi
public object ChannelFilterRequest {

    /**
     *  Default filter to include FilterObject in a channel by its cid
     *
     * @param filter - the filter to be included.
     * @param offset - the offset to be included with the filter.
     * @param limit - the maximum number of results to return.
     */
    public suspend fun ChatClient.filterWithOffset(
        filter: FilterObject,
        offset: Int,
        limit: Int,
    ): List<Channel> {
        val request = QueryChannelsRequest(
            filter = filter,
            offset = offset,
            limit = limit,
            messageLimit = 0,
            memberLimit = 0,
        )
        return queryChannelsInternal(request).await().let { result ->
            when (result) {
                is Result.Success -> result.value
                is Result.Failure -> emptyList()
            }
        }
    }
}
