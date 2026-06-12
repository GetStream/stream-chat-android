/*
 * Copyright (c) 2014-2026 Stream.io Inc. All rights reserved.
 *
 * Licensed under the Stream License;
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *    https://github.com/GetStream/stream-video-android/blob/main/LICENSE
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
    "UnusedImport"
)

package io.getstream.chat.android.network.models

import kotlin.collections.List
import kotlin.collections.Map
import kotlin.collections.*
import kotlin.io.*
import com.squareup.moshi.FromJson
import com.squareup.moshi.Json
import com.squareup.moshi.JsonAdapter
import com.squareup.moshi.JsonReader
import com.squareup.moshi.JsonWriter
import com.squareup.moshi.ToJson

/**
 * This event is sent when a user gets deleted. The event contains information about the user that was deleted and the deletion options that were used.
 */

data class UserDeletedEvent (
    @Json(name = "created_at")
    val createdAt: org.threeten.bp.OffsetDateTime,

    @Json(name = "delete_conversation")
    val deleteConversation: kotlin.String,

    @Json(name = "delete_conversation_channels")
    val deleteConversationChannels: kotlin.Boolean,

    @Json(name = "delete_messages")
    val deleteMessages: kotlin.String,

    @Json(name = "delete_user")
    val deleteUser: kotlin.String,

    @Json(name = "hard_delete")
    val hardDelete: kotlin.Boolean,

    @Json(name = "mark_messages_deleted")
    val markMessagesDeleted: kotlin.Boolean,

    @Json(name = "custom")
    val custom: kotlin.collections.Map<kotlin.String, Any?> = emptyMap(),

    @Json(name = "user")
    val user: io.getstream.chat.android.network.models.UserResponseCommonFields,

    @Json(name = "type")
    val type: kotlin.String,

    @Json(name = "received_at")
    val receivedAt: org.threeten.bp.OffsetDateTime? = null
)
: io.getstream.chat.android.network.models.WSClientEvent(), io.getstream.chat.android.network.models.WSEvent()
{
    
    override fun getWSClientEventType(): kotlin.String {
        return type
    }

    override fun getWSEventType(): kotlin.String {
        return type
    }    
}
