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

@file:Suppress(
    "ArrayInDataClass",
    "EnumEntryName",
    "RemoveRedundantQualifierName",
    "UnusedImport",
)

package io.getstream.chat.android.network.models

import com.squareup.moshi.Json

/**
 * Channel configuration overrides
 */
@com.squareup.moshi.JsonClass(generateAdapter = true)
internal data class ConfigOverridesRequest(
    @Json(name = "blocklist")
    internal val blocklist: String? = null,

    @Json(name = "blocklist_behavior")
    internal val blocklistBehavior: io.getstream.chat.android.network.models.BlocklistBehavior? = null,

    @Json(name = "count_messages")
    internal val countMessages: Boolean? = null,

    @Json(name = "max_message_length")
    internal val maxMessageLength: Int? = null,

    @Json(name = "push_level")
    internal val pushLevel: io.getstream.chat.android.network.models.PushLevel? = null,

    @Json(name = "quotes")
    internal val quotes: Boolean? = null,

    @Json(name = "reactions")
    internal val reactions: Boolean? = null,

    @Json(name = "replies")
    internal val replies: Boolean? = null,

    @Json(name = "shared_locations")
    internal val sharedLocations: Boolean? = null,

    @Json(name = "typing_events")
    internal val typingEvents: Boolean? = null,

    @Json(name = "uploads")
    internal val uploads: Boolean? = null,

    @Json(name = "url_enrichment")
    internal val urlEnrichment: Boolean? = null,

    @Json(name = "user_message_reminders")
    internal val userMessageReminders: Boolean? = null,

    @Json(name = "commands")
    internal val commands: List<String>? = null,

    @Json(name = "chat_preferences")
    internal val chatPreferences: io.getstream.chat.android.network.models.ChatPreferences? = null,

    @Json(name = "grants")
    internal val grants: Map<String, List<String>>? = null,
)
