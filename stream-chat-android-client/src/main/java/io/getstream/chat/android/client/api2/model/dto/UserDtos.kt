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
 * See [io.getstream.chat.android.client.parser2.adapters.UpstreamUserDtoAdapter] for
 * special [extraData] handling.
 */
@StreamHandsOff(
    reason = "Field names can't be changed because [CustomObjectDtoAdapter] class uses reflections to add/remove " +
        "content of [extraData] map",
)
@JsonClass(generateAdapter = true)
internal data class UpstreamUserDto(
    val banned: Boolean,
    val id: String,
    val name: String,
    val image: String,
    val invisible: Boolean,
    val privacy_settings: PrivacySettingsDto?,
    val language: String,
    val role: String,
    val devices: List<DeviceDto>,
    val teams: List<String>,
    val teams_role: Map<String, String>?,

    val extraData: Map<String, Any>,
) : ExtraDataDto

/**
 * See [io.getstream.chat.android.client.parser2.adapters.DownstreamUserDtoAdapter] for
 * special [extraData] handling.
 */
@StreamHandsOff(
    reason = "Field names can't be changed because [CustomObjectDtoAdapter] class uses reflections to add/remove " +
        "content of [extraData] map",
)
@JsonClass(generateAdapter = true)
internal data class DownstreamUserDto(
    val id: String,
    val name: String?,
    val image: String?,
    val role: String,
    val invisible: Boolean? = false,
    val privacy_settings: PrivacySettingsDto?,
    val language: String?,
    val banned: Boolean,
    val devices: List<DeviceDto>?,
    val online: Boolean,
    val created_at: Date?,
    val deactivated_at: Date?,
    val updated_at: Date?,
    val last_active: Date?,
    val total_unread_count: Int = 0,
    val unread_channels: Int = 0,
    val unread_count: Int = 0,
    val unread_threads: Int = 0,
    val mutes: List<DownstreamMuteDto>?,
    val teams: List<String> = emptyList(),
    val teams_role: Map<String, String>?,
    val channel_mutes: List<DownstreamChannelMuteDto>?,
    val blocked_user_ids: List<String>?,

    val extraData: Map<String, Any>,
) : ExtraDataDto

@JsonClass(generateAdapter = true)
internal data class PartialUpdateUserDto(
    val id: String,
    val set: Map<String, Any>,
    val unset: List<String>,
)

@JsonClass(generateAdapter = true)
internal data class DownstreamUserBlockDto(
    val user_id: String,
    val blocked_user_id: String,
    val created_at: Date,
)
