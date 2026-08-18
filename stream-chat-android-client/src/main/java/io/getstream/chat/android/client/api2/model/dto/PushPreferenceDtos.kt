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
import java.util.Date

/**
 * Downstream DTO for receiving push notification preferences.
 *
 * @param chat_level The chat level preference ("all", "default", "mentions" or "none").
 * @param disabled_until Timestamp until which notifications are disabled.
 * @param chat_preferences Per-category toggles when set instead of [chat_level].
 */
@JsonClass(generateAdapter = true)
internal data class DownstreamPushPreferenceDto(
    val chat_level: String?,
    val disabled_until: Date?,
    val chat_preferences: DownstreamChatPreferencesDto? = null,
)

@JsonClass(generateAdapter = true)
internal data class DownstreamChatPreferencesDto(
    val direct_mentions: String? = null,
    val role_mentions: String? = null,
    val group_mentions: String? = null,
    val here_mentions: String? = null,
    val channel_mentions: String? = null,
    val thread_replies: String? = null,
    val default_preference: String? = null,
)
