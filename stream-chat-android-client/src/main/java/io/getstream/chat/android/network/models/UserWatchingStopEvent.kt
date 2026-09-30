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
 * This event is sent when a user stops watching a channel. The event contains information about the user that stopped watching the channel.
 */
@com.squareup.moshi.JsonClass(generateAdapter = true)
internal data class UserWatchingStopEvent(
    @Json(name = "created_at")
    internal val createdAt: io.getstream.chat.android.network.infrastructure.ExactDate,

    @Json(name = "watcher_count")
    internal val watcherCount: Int,

    @Json(name = "custom")
    internal val custom: Map<String, Any?> = emptyMap(),

    @Json(name = "user")
    internal val user: io.getstream.chat.android.network.models.UserResponseCommonFields,

    @Json(name = "type")
    internal val type: String = "user.watching.stop",

    @Json(name = "channel_id")
    internal val channelId: String? = null,

    @Json(name = "channel_type")
    internal val channelType: String? = null,

    @Json(name = "cid")
    internal val cid: String? = null,

    @Json(name = "received_at")
    internal val receivedAt: java.util.Date? = null,
) :
    io.getstream.chat.android.network.models.WSClientEvent, io.getstream.chat.android.network.models.WSEvent {

    override fun getWSClientEventType(): String {
        return type
    }

    override fun getWSEventType(): String {
        return type
    }
}
