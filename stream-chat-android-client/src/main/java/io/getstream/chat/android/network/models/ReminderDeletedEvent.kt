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
 * Emitted when a reminder is deleted.
 */
@com.squareup.moshi.JsonClass(generateAdapter = true)
internal data class ReminderDeletedEvent(
    @Json(name = "cid")
    internal val cid: String,

    @Json(name = "created_at")
    internal val createdAt: io.getstream.chat.android.network.infrastructure.ExactDate,

    @Json(name = "message_id")
    internal val messageId: String,

    @Json(name = "user_id")
    internal val userId: String,

    @Json(name = "custom")
    internal val custom: Map<String, Any?> = emptyMap(),

    @Json(name = "reminder")
    internal val reminder: io.getstream.chat.android.network.models.ReminderResponseData,

    @Json(name = "type")
    internal val type: String = "reminder.deleted",

    @Json(name = "parent_id")
    internal val parentId: String? = null,

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
