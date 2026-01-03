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

package io.getstream.chat.android.state.model.querychannels.pagination.internal

import io.getstream.chat.android.client.api.models.QueryChannelRequest
import io.getstream.chat.android.client.api.models.QueryChannelsRequest
import io.getstream.chat.android.client.query.pagination.AnyChannelPaginationRequest
import io.getstream.chat.android.models.FilterObject

internal fun QueryChannelsPaginationRequest.toAnyChannelPaginationRequest(): AnyChannelPaginationRequest {
    val originalRequest = this
    return AnyChannelPaginationRequest().apply {
        this.channelLimit = originalRequest.channelLimit
        this.channelOffset = originalRequest.channelOffset
        this.messageLimit = originalRequest.messageLimit
        this.sort = originalRequest.sort
    }
}

internal fun QueryChannelsPaginationRequest.toQueryChannelsRequest(filter: FilterObject, userPresence: Boolean): QueryChannelsRequest {
    var request = QueryChannelsRequest(filter, channelOffset, channelLimit, sort, messageLimit, memberLimit)
    if (userPresence) {
        request = request.withPresence()
    }
    return request.withWatch()
}

internal fun QueryChannelRequest.toAnyChannelPaginationRequest(): AnyChannelPaginationRequest {
    val originalRequest = this
    val paginationAndValue = pagination()
    return AnyChannelPaginationRequest().apply {
        this.messageLimit = originalRequest.messagesLimit()
        this.messageFilterDirection = paginationAndValue?.first
        this.messageFilterValue = paginationAndValue?.second ?: ""
        this.memberLimit = originalRequest.membersLimit()
        this.memberOffset = originalRequest.membersOffset()
        this.watcherLimit = originalRequest.watchersLimit()
        this.watcherOffset = originalRequest.watchersOffset()
        this.channelLimit = 1
    }
}
