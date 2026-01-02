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

import io.getstream.chat.android.models.Channel
import io.getstream.chat.android.models.querysort.QuerySorter

internal data class QueryChannelsPaginationRequest(
    val sort: QuerySorter<Channel>,
    val channelOffset: Int = 0,
    val channelLimit: Int = 30,
    val messageLimit: Int = 10,
    val memberLimit: Int,
) {

    val isFirstPage: Boolean
        get() = channelOffset == 0
}
