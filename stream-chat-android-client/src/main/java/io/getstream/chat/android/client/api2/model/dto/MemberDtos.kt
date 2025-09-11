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

/**
 * See [io.getstream.chat.android.client.parser2.adapters.UpstreamMemberDtoAdapter] for
 * special [extraData] handling.
 */
@StreamHandsOff(
    reason = "Field names can't be changed because [CustomObjectDtoAdapter] class uses reflections to add/remove " +
        "content of [extraData] map",
)
@JsonClass(generateAdapter = true)
internal data class UpstreamMemberDto(
    val user: UpstreamUserDto,
    val created_at: Date?,
    val updated_at: Date?,
    val invited: Boolean?,
    val invite_accepted_at: Date?,
    val invite_rejected_at: Date?,
    val shadow_banned: Boolean?,
    val banned: Boolean? = false,
    val channel_role: String?,
    val notifications_muted: Boolean?,
    val status: String?,
    val ban_expires: Date?,
    val pinned_at: Date?,
    val archived_at: Date?,
    val extraData: Map<String, Any>,
) : ExtraDataDto

/**
 * See [io.getstream.chat.android.client.parser2.adapters.DownstreamMemberDtoAdapter] for
 * special [extraData] handling.
 */
@StreamHandsOff(
    reason = "Field names can't be changed because [CustomObjectDtoAdapter] class uses reflections to add/remove " +
        "content of [extraData] map",
)
@JsonClass(generateAdapter = true)
internal data class DownstreamMemberDto(
    val user: DownstreamUserDto,
    val created_at: Date?,
    val updated_at: Date?,
    val invited: Boolean?,
    val invite_accepted_at: Date?,
    val invite_rejected_at: Date?,
    val shadow_banned: Boolean? = false,
    val banned: Boolean? = false,
    val channel_role: String?,
    val notifications_muted: Boolean?,
    val status: String?,
    val ban_expires: Date?,
    val pinned_at: Date?,
    val archived_at: Date?,
    val extraData: Map<String, Any>,
) : ExtraDataDto

/**
 * DTO holding limited data about a channel member.
 *
 * @property channel_role The role of the member in the channel.
 */
@JsonClass(generateAdapter = true)
internal data class DownstreamMemberInfoDto(
    val channel_role: String?,
)
