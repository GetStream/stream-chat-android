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

package io.getstream.chat.android.client.api2.model.dto

import com.squareup.moshi.JsonClass
import io.getstream.chat.android.core.internal.StreamHandsOff
import java.util.Date

@StreamHandsOff(
    reason = "Field names can't be changed because [CustomObjectDtoAdapter] class uses reflections to add/remove " +
        "content of [extraData] map"
)
@JsonClass(generateAdapter = true)
internal data class UpstreamChannelDto(
    val cid: String,
    val id: String,
    val type: String,
    val name: String,
    val image: String,
    val watcher_count: Int,
    val frozen: Boolean,
    val last_message_at: Date?,
    val created_at: Date?,
    val deleted_at: Date?,
    val updated_at: Date?,
    val member_count: Int,
    val messages: List<UpstreamMessageDto>,
    val members: List<UpstreamMemberDto>,
    val watchers: List<UpstreamUserDto>,
    val read: List<UpstreamChannelUserRead>,
    val config: ConfigDto,
    val created_by: UpstreamUserDto,
    val team: String,
    val cooldown: Int,
    val pinned_messages: List<UpstreamMessageDto>,

    val extraData: Map<String, Any>,
) : ExtraDataDto

@JsonClass(generateAdapter = true)
internal data class DownstreamChannelDto(
    val cid: String,
    val id: String,
    val type: String,
    val name: String?,
    val image: String?,
    val watcher_count: Int = 0,
    val frozen: Boolean,
    val last_message_at: Date?,
    val created_at: Date?,
    val deleted_at: Date?,
    val updated_at: Date?,
    val member_count: Int = 0,
    val messages: List<DownstreamMessageDto> = emptyList(),
    val members: List<DownstreamMemberDto> = emptyList(),
    val watchers: List<DownstreamUserDto> = emptyList(),
    val read: List<DownstreamChannelUserRead> = emptyList(),
    val config: ConfigDto,
    val created_by: DownstreamUserDto?,
    val team: String = "",
    val cooldown: Int = 0,
    val pinned_messages: List<DownstreamMessageDto> = emptyList(),
    val own_capabilities: List<String> = emptyList(),

    val extraData: Map<String, Any>,
) : ExtraDataDto
