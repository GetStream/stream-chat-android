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
 * 
 */

@com.squareup.moshi.JsonClass(generateAdapter = true)
data class ChannelGetOrCreateRequest (
    @Json(name = "hide_for_creator")
    val hideForCreator: kotlin.Boolean? = null,

    @Json(name = "presence")
    val presence: kotlin.Boolean? = null,

    @Json(name = "state")
    val state: kotlin.Boolean? = null,

    @Json(name = "thread_unread_counts")
    val threadUnreadCounts: kotlin.Boolean? = null,

    @Json(name = "watch")
    val watch: kotlin.Boolean? = null,

    @Json(name = "data")
    val data: io.getstream.chat.android.network.models.ChannelInput? = null,

    @Json(name = "members")
    val members: io.getstream.chat.android.network.models.PaginationParams? = null,

    @Json(name = "messages")
    val messages: io.getstream.chat.android.network.models.MessagePaginationParams? = null,

    @Json(name = "watchers")
    val watchers: io.getstream.chat.android.network.models.PaginationParams? = null
)
