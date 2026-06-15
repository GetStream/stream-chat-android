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
 * Configuration for user deletion action
 */

@com.squareup.moshi.JsonClass(generateAdapter = true)
data class DeleteUserRequestPayload (
    @Json(name = "delete_conversation_channels")
    val deleteConversationChannels: kotlin.Boolean? = null,

    @Json(name = "delete_feeds_content")
    val deleteFeedsContent: kotlin.Boolean? = null,

    @Json(name = "entity_id")
    val entityId: kotlin.String? = null,

    @Json(name = "entity_type")
    val entityType: kotlin.String? = null,

    @Json(name = "hard_delete")
    val hardDelete: kotlin.Boolean? = null,

    @Json(name = "mark_messages_deleted")
    val markMessagesDeleted: kotlin.Boolean? = null,

    @Json(name = "reason")
    val reason: kotlin.String? = null
)
