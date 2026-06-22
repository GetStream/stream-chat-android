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

package io.getstream.chat.android.client.api2.model.dto

import com.squareup.moshi.JsonClass
import io.getstream.chat.android.network.models.UnreadCountsChannel as UnreadChannelDto
import io.getstream.chat.android.network.models.UnreadCountsChannelType as UnreadChannelByTypeDto
import io.getstream.chat.android.network.models.UnreadCountsThread as UnreadThreadDto

@JsonClass(generateAdapter = true)
internal data class UnreadDto(
    val total_unread_count: Int = 0,
    val total_unread_threads_count: Int = 0,
    val total_unread_count_by_team: Map<String, Int>? = emptyMap(),
    val channels: List<UnreadChannelDto> = emptyList(),
    val threads: List<UnreadThreadDto> = emptyList(),
    val channel_type: List<UnreadChannelByTypeDto> = emptyList(),
)
