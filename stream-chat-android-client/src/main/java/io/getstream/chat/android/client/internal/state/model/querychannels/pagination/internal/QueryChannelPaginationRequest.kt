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

package io.getstream.chat.android.client.internal.state.model.querychannels.pagination.internal

import io.getstream.chat.android.client.api.models.Pagination
import io.getstream.chat.android.client.api.models.QueryChannelRequest
import io.getstream.chat.android.client.api.models.WatchChannelRequest
import io.getstream.chat.android.client.query.pagination.AnyChannelPaginationRequest

internal class QueryChannelPaginationRequest(var messageLimit: Int = 30) : QueryChannelRequest() {

    var messageFilterDirection: Pagination? = null
    var messageFilterValue: String = ""

    @Suppress("MagicNumber")
    var memberLimit: Int = 30

    @Suppress("MagicNumber")
    var memberOffset: Int = 0

    @Suppress("MagicNumber")
    var watcherLimit: Int = 30

    @Suppress("MagicNumber")
    var watcherOffset: Int = 0

    fun hasFilter(): Boolean {
        return messageFilterDirection != null
    }

    fun isFirstPage(): Boolean {
        return messageFilterDirection == null
    }

    internal fun toAnyChannelPaginationRequest(): AnyChannelPaginationRequest {
        val originalRequest = this
        return AnyChannelPaginationRequest().apply {
            this.messageLimit = originalRequest.messageLimit
            this.messageFilterDirection = originalRequest.messageFilterDirection
            this.memberLimit = originalRequest.memberLimit
            this.memberOffset = originalRequest.memberOffset
            this.watcherLimit = originalRequest.watcherLimit
            this.watcherOffset = originalRequest.watcherOffset
            this.channelLimit = 1
        }
    }

    fun toWatchChannelRequest(userPresence: Boolean): WatchChannelRequest = WatchChannelRequest().apply {
        withMessages(messageLimit)
        if (userPresence) {
            withPresence()
        }
        if (hasFilter()) {
            withMessages(messageFilterDirection!!, messageFilterValue, messageLimit)
        }
        withMembers(memberLimit, memberOffset)
        withWatchers(watcherLimit, watcherOffset)
    }
}
