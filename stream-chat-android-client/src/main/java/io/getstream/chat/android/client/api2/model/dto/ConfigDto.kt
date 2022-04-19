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
internal data class ConfigDto(
    @Json(name = "created_at") val createdAt: Date?,
    @Json(name = "updated_at") val updatedAt: Date?,
    @Json(name = "name") val name: String?,
    @Json(name = "typing_events") val typingEvents: Boolean,
    @Json(name = "read_events") val readEvents: Boolean,
    @Json(name = "connect_events") val connectEvents: Boolean,
    @Json(name = "search") val search: Boolean,
    @Json(name = "reactions") val reactions: Boolean,
    @Json(name = "replies") val replies: Boolean,
    @Json(name = "mutes") val mutes: Boolean,
    @Json(name = "uploads") val uploads: Boolean,
    @Json(name = "url_enrichment") val urlEnrichment: Boolean,
    @Json(name = "custom_events") val customEvents: Boolean,
    @Json(name = "push_notifications") val pushNotifications: Boolean,
    @Json(name = "message_retention") val messageRetention: String,
    @Json(name = "max_message_length") val maxMessageLength: Int,
    @Json(name = "automod") val automod: String,
    @Json(name = "automod_behavior") val automodBehavior: String,
    @Json(name = "blocklist_behavior") val blocklistBehavior: String?,
    @Json(name = "commands") val commands: List<CommandDto>,
)
