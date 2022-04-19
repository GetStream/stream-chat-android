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

import com.squareup.moshi.Json
import com.squareup.moshi.JsonClass
import java.util.Date

@JsonClass(generateAdapter = true)
internal data class UpstreamMemberDto(
    @Json(name = "user") val user: UpstreamUserDto,
    @Json(name = "role") val role: String?,
    @Json(name = "created_at") val createdAt: Date?,
    @Json(name = "updated_at") val updatedAt: Date?,
    @Json(name = "invited") val invited: Boolean?,
    @Json(name = "invite_accepted_at") val inviteAcceptedAt: Date?,
    @Json(name = "invite_rejected_at") val inviteRejectedAt: Date?,
    @Json(name = "shadow_banned") val shadowBanned: Boolean,
    @Json(name = "banned") val banned: Boolean = false,
    @Json(name = "channel_role") val channelRole: String?,
)

@JsonClass(generateAdapter = true)
internal data class DownstreamMemberDto(
    @Json(name = "user") val user: DownstreamUserDto,
    @Json(name = "role") val role: String?,
    @Json(name = "created_at") val createdAt: Date?,
    @Json(name = "updated_at") val updatedAt: Date?,
    @Json(name = "invited") val invited: Boolean?,
    @Json(name = "invite_accepted_at") val inviteAcceptedAt: Date?,
    @Json(name = "invite_rejected_at") val inviteRejectedAt: Date?,
    @Json(name = "shadow_banned") val shadowBanned: Boolean = false,
    @Json(name = "banned") val banned: Boolean = false,
    @Json(name = "channel_role") val channelRole: String?,
)
