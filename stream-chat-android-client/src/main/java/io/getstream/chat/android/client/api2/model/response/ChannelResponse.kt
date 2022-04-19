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

package io.getstream.chat.android.client.api2.model.response

import com.squareup.moshi.Json
import com.squareup.moshi.JsonClass
import io.getstream.chat.android.client.api2.model.dto.DownstreamChannelDto
import io.getstream.chat.android.client.api2.model.dto.DownstreamChannelUserRead
import io.getstream.chat.android.client.api2.model.dto.DownstreamMemberDto
import io.getstream.chat.android.client.api2.model.dto.DownstreamMessageDto
import io.getstream.chat.android.client.api2.model.dto.DownstreamUserDto
import java.util.Date

@JsonClass(generateAdapter = true)
internal data class ChannelResponse(
    @Json(name = "channel") val channel: DownstreamChannelDto,
    @Json(name = "messages") val messages: List<DownstreamMessageDto> = emptyList(),
    @Json(name = "members") val members: List<DownstreamMemberDto> = emptyList(),
    @Json(name = "watchers") val watchers: List<DownstreamUserDto> = emptyList(),
    @Json(name = "read") val read: List<DownstreamChannelUserRead> = emptyList(),
    @Json(name = "watcher_count") val watcherCount: Int = 0,
    @Json(name = "hidden") val hidden: Boolean?,
    @Json(name = "hide_messages_before") val hideMessagesBefore: Date?,
)
