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
 *
 */
@com.squareup.moshi.JsonClass(generateAdapter = true)
internal data class ChannelConfigWithInfo(
    @Json(name = "automod")
    internal val automod: io.getstream.chat.android.network.models.AutomodMode,

    @Json(name = "automod_behavior")
    internal val automodBehavior: io.getstream.chat.android.network.models.ChannelModerationBehavior,

    @Json(name = "connect_events")
    internal val connectEvents: Boolean,

    @Json(name = "count_messages")
    internal val countMessages: Boolean,

    @Json(name = "created_at")
    internal val createdAt: java.util.Date,

    @Json(name = "custom_events")
    internal val customEvents: Boolean,

    @Json(name = "delivery_events")
    internal val deliveryEvents: Boolean,

    @Json(name = "mark_messages_pending")
    internal val markMessagesPending: Boolean,

    @Json(name = "max_message_length")
    internal val maxMessageLength: Int,

    @Json(name = "message_retention")
    internal val messageRetention: String,

    @Json(name = "mutes")
    internal val mutes: Boolean,

    @Json(name = "name")
    internal val name: String,

    @Json(name = "polls")
    internal val polls: Boolean,

    @Json(name = "push_notifications")
    internal val pushNotifications: Boolean,

    @Json(name = "quotes")
    internal val quotes: Boolean,

    @Json(name = "reactions")
    internal val reactions: Boolean,

    @Json(name = "read_events")
    internal val readEvents: Boolean,

    @Json(name = "reminders")
    internal val reminders: Boolean,

    @Json(name = "replies")
    internal val replies: Boolean,

    @Json(name = "search")
    internal val search: Boolean,

    @Json(name = "shared_locations")
    internal val sharedLocations: Boolean,

    @Json(name = "skip_last_msg_update_for_system_msgs")
    internal val skipLastMsgUpdateForSystemMsgs: Boolean,

    @Json(name = "typing_events")
    internal val typingEvents: Boolean,

    @Json(name = "updated_at")
    internal val updatedAt: java.util.Date,

    @Json(name = "uploads")
    internal val uploads: Boolean,

    @Json(name = "url_enrichment")
    internal val urlEnrichment: Boolean,

    @Json(name = "user_message_reminders")
    internal val userMessageReminders: Boolean,

    @Json(name = "commands")
    internal val commands: List<io.getstream.chat.android.network.models.Command> = emptyList(),

    @Json(name = "blocklist")
    internal val blocklist: String? = null,

    @Json(name = "blocklist_behavior")
    internal val blocklistBehavior: io.getstream.chat.android.network.models.ChannelModerationBehavior? = null,

    @Json(name = "partition_size")
    internal val partitionSize: Int? = null,

    @Json(name = "partition_ttl")
    internal val partitionTtl: String? = null,

    @Json(name = "push_level")
    internal val pushLevel: io.getstream.chat.android.network.models.PushLevel? = null,

    @Json(name = "scheduled_messages_max_lead_time_seconds")
    internal val scheduledMessagesMaxLeadTimeSeconds: Int? = null,

    @Json(name = "scheduled_messages_max_per_channel")
    internal val scheduledMessagesMaxPerChannel: Int? = null,

    @Json(name = "scheduled_messages_max_per_user")
    internal val scheduledMessagesMaxPerUser: Int? = null,

    @Json(name = "scheduled_messages_min_lead_time_seconds")
    internal val scheduledMessagesMinLeadTimeSeconds: Int? = null,

    @Json(name = "allowed_flag_reasons")
    internal val allowedFlagReasons: List<String>? = null,

    @Json(name = "blocklists")
    internal val blocklists: List<io.getstream.chat.android.network.models.BlockListOptions>? = null,

    @Json(name = "automod_thresholds")
    internal val automodThresholds: io.getstream.chat.android.network.models.Thresholds? = null,

    @Json(name = "chat_preferences")
    internal val chatPreferences: io.getstream.chat.android.network.models.ChatPreferences? = null,

    @Json(name = "grants")
    internal val grants: Map<String, List<String>>? = null,
)
