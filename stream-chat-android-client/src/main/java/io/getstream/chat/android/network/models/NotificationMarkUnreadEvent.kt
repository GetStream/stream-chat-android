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
 * Emitted when a channel/thread is marked as unread.
 */
@com.squareup.moshi.JsonClass(generateAdapter = true)
internal data class NotificationMarkUnreadEvent(
    @Json(name = "created_at")
    internal val createdAt: io.getstream.chat.android.network.infrastructure.ExactDate,

    @Json(name = "custom")
    internal val custom: Map<String, Any?> = emptyMap(),

    @Json(name = "type")
    internal val type: String = "notification.mark_unread",

    @Json(name = "channel_id")
    internal val channelId: String? = null,

    @Json(name = "channel_member_count")
    internal val channelMemberCount: Int? = null,

    @Json(name = "channel_message_count")
    internal val channelMessageCount: Int? = null,

    @Json(name = "channel_type")
    internal val channelType: String? = null,

    @Json(name = "cid")
    internal val cid: String? = null,

    @Json(name = "first_unread_message_id")
    internal val firstUnreadMessageId: String? = null,

    @Json(name = "last_read_at")
    internal val lastReadAt: java.util.Date? = null,

    @Json(name = "last_read_message_id")
    internal val lastReadMessageId: String? = null,

    @Json(name = "received_at")
    internal val receivedAt: java.util.Date? = null,

    @Json(name = "team")
    internal val team: String? = null,

    @Json(name = "thread_id")
    internal val threadId: String? = null,

    @Json(name = "total_unread_count")
    internal val totalUnreadCount: Int? = null,

    @Json(name = "unread_channels")
    internal val unreadChannels: Int? = null,

    @Json(name = "unread_count")
    internal val unreadCount: Int? = null,

    @Json(name = "unread_messages")
    internal val unreadMessages: Int? = null,

    @Json(name = "unread_thread_messages")
    internal val unreadThreadMessages: Int? = null,

    @Json(name = "unread_threads")
    internal val unreadThreads: Int? = null,

    @Json(name = "channel")
    internal val channel: io.getstream.chat.android.network.models.ChannelResponse? = null,

    @Json(name = "channel_custom")
    internal val channelCustom: Map<String, Any?>? = null,

    @Json(name = "grouped_unread_channels")
    internal val groupedUnreadChannels: Map<String, Int>? = null,

    @Json(name = "user")
    internal val user: io.getstream.chat.android.network.models.UserResponseCommonFields? = null,
) :
    io.getstream.chat.android.network.models.WSClientEvent, io.getstream.chat.android.network.models.WSEvent {

    override fun getWSClientEventType(): String {
        return type
    }

    override fun getWSEventType(): String {
        return type
    }
}
