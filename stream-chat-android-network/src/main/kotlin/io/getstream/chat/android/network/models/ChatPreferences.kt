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
data class ChatPreferences (
    @Json(name = "channel_mentions")
    val channelMentions: kotlin.String? = null,

    @Json(name = "default_preference")
    val defaultPreference: kotlin.String? = null,

    @Json(name = "direct_mentions")
    val directMentions: kotlin.String? = null,

    @Json(name = "distinct_channel_messages")
    val distinctChannelMessages: kotlin.String? = null,

    @Json(name = "group_mentions")
    val groupMentions: kotlin.String? = null,

    @Json(name = "here_mentions")
    val hereMentions: kotlin.String? = null,

    @Json(name = "role_mentions")
    val roleMentions: kotlin.String? = null,

    @Json(name = "thread_replies")
    val threadReplies: kotlin.String? = null
)
