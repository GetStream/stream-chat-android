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
 *
 */

@com.squareup.moshi.JsonClass(generateAdapter = true)
data class ChannelInput (
    @Json(name = "auto_translation_enabled")
    val autoTranslationEnabled: kotlin.Boolean? = null,

    @Json(name = "auto_translation_language")
    val autoTranslationLanguage: kotlin.String? = null,

    @Json(name = "created_by_id")
    val createdById: kotlin.String? = null,

    @Json(name = "disabled")
    val disabled: kotlin.Boolean? = null,

    @Json(name = "frozen")
    val frozen: kotlin.Boolean? = null,

    @Json(name = "team")
    val team: kotlin.String? = null,

    @Json(name = "truncated_by_id")
    val truncatedById: kotlin.String? = null,

    @Json(name = "filter_tags")
    val filterTags: kotlin.collections.List<kotlin.String>? = emptyList(),

    @Json(name = "invites")
    val invites: kotlin.collections.List<io.getstream.chat.android.network.models.ChannelMemberRequest>? = emptyList(),

    @Json(name = "members")
    val members: kotlin.collections.List<io.getstream.chat.android.network.models.ChannelMemberRequest>? = emptyList(),

    @Json(name = "config_overrides")
    val configOverrides: io.getstream.chat.android.network.models.ChannelConfig? = null,

    @Json(name = "created_by")
    val createdBy: io.getstream.chat.android.network.models.UserRequest? = null,

    @Json(name = "custom")
    val custom: kotlin.collections.Map<kotlin.String, Any?>? = emptyMap()
)
