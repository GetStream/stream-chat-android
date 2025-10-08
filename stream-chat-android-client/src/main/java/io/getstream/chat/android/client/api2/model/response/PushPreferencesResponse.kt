/*
 * Copyright (c) 2014-2025 Stream.io Inc. All rights reserved.
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

import com.squareup.moshi.JsonClass
import io.getstream.chat.android.client.api2.model.dto.DownstreamPushPreferenceDto

/**
 * Response DTO for upserting push notification preferences.
 *
 * @param user_channel_preferences Map of userId to a map of channelId to their respective channel push preferences.
 * @param user_preferences Map of userId to their overall push preferences.
 */
@JsonClass(generateAdapter = true)
internal data class PushPreferencesResponse(
    val user_channel_preferences: Map<String, Map<String, DownstreamPushPreferenceDto>>,
    val user_preferences: Map<String, DownstreamPushPreferenceDto?>,
)

/**
 * Extension function to get a user's overall push preference from the [PushPreferencesResponse].
 *
 * @param userId The ID of the user whose preference is to be retrieved.
 */
internal fun PushPreferencesResponse.getUserPreference(userId: String): DownstreamPushPreferenceDto? =
    user_preferences[userId]

/**
 * Extension function to get a user's channel-specific push preference from the [PushPreferencesResponse].
 *
 * @param userId The ID of the user whose channel preference is to be retrieved.
 * @param cid The channel ID (in the format "type:id") for which the preference is to be retrieved.
 */
internal fun PushPreferencesResponse.getUserChannelPreference(
    userId: String,
    cid: String,
): DownstreamPushPreferenceDto? = user_channel_preferences[userId]?.get(cid)
