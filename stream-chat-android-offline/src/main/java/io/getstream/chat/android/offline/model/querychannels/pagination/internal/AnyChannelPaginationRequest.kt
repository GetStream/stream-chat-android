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
 
package io.getstream.chat.android.offline.model.querychannels.pagination.internal

import io.getstream.chat.android.client.api.models.Pagination
import io.getstream.chat.android.client.api.models.QuerySort
import io.getstream.chat.android.client.models.Channel

internal class AnyChannelPaginationRequest(var messageLimit: Int = 30) {
    var messageFilterDirection: Pagination? = null
    var messageFilterValue: String = ""
    var sort: QuerySort<Channel> = QuerySort()

    var channelLimit: Int = 30
    var channelOffset: Int = 0

    var memberLimit: Int = 30
    var memberOffset: Int = 0

    var watcherLimit: Int = 30
    var watcherOffset: Int = 0
}

internal fun AnyChannelPaginationRequest.isFirstPage(): Boolean {
    return channelOffset == 0
}

internal fun AnyChannelPaginationRequest.isRequestingMoreThanLastMessage(): Boolean {
    return (isFirstPage() && messageLimit > 1) || (isNotFirstPage() && messageLimit > 0)
}

internal fun AnyChannelPaginationRequest.isNotFirstPage(): Boolean = isFirstPage().not()
