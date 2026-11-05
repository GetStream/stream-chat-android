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
internal data class MarkReadResponseEvent(
    @Json(name = "channel_id")
    internal val channelId: String,

    @Json(name = "channel_type")
    internal val channelType: String,

    @Json(name = "cid")
    internal val cid: String,

    @Json(name = "created_at")
    internal val createdAt: java.util.Date,

    @Json(name = "type")
    internal val type: String,

    @Json(name = "channel_last_message_at")
    internal val channelLastMessageAt: java.util.Date? = null,

    @Json(name = "last_read_message_id")
    internal val lastReadMessageId: String? = null,

    @Json(name = "team")
    internal val team: String? = null,

    @Json(name = "channel")
    internal val channel: io.getstream.chat.android.network.models.ChannelResponse? = null,

    @Json(name = "thread")
    internal val thread: io.getstream.chat.android.network.models.ThreadResponse? = null,

    @Json(name = "user")
    internal val user: io.getstream.chat.android.network.models.UserResponseCommonFields? = null,
)
