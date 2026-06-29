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
import io.getstream.chat.android.network.models.DeviceResponse as DeviceDto
import io.getstream.chat.android.network.models.PrivacySettingsResponse as PrivacySettingsDto

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

internal typealias DownstreamUserDto = io.getstream.chat.android.network.models.UserResponse

@JsonClass(generateAdapter = true)
internal data class DownstreamUserBlockDto(
    val user_id: String,
    val blocked_user_id: String,
    val created_at: Date,
)
