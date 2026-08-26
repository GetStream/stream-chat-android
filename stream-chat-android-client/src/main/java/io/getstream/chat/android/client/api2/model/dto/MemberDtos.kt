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
import io.getstream.chat.android.core.internal.StreamHandsOff
import java.util.Date

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
 * DTO holding limited data about a channel member, as attached to a message payload.
 *
 * See [io.getstream.chat.android.client.parser2.adapters.DownstreamMemberInfoDtoAdapter] for
 * special [extraData] handling.
 *
 * @property channel_role The role of the member in the channel.
 * @property notifications_muted If notifications are muted for the member in the channel.
 * @property custom The member custom data, in the shape API v2 returns it: nested.
 * @property extraData The member custom data, in the shape API v1 returns it: inlined next to [channel_role].
 */
@StreamHandsOff(
    reason = "Field names can't be changed because [CustomObjectDtoAdapter] class uses reflections to add/remove " +
        "content of [extraData] map",
)
@JsonClass(generateAdapter = true)
internal data class DownstreamMemberInfoDto(
    val channel_role: String?,
    val notifications_muted: Boolean? = null,
    val custom: Map<String, Any>? = null,
    val extraData: Map<String, Any> = emptyMap(),
) : ExtraDataDto
