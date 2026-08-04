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
internal data class ChatPreferences(
    @Json(name = "channel_mentions")
    internal val channelMentions: String? = null,

    @Json(name = "default_preference")
    internal val defaultPreference: String? = null,

    @Json(name = "direct_mentions")
    internal val directMentions: String? = null,

    @Json(name = "distinct_channel_messages")
    internal val distinctChannelMessages: String? = null,

    @Json(name = "group_mentions")
    internal val groupMentions: String? = null,

    @Json(name = "here_mentions")
    internal val hereMentions: String? = null,

    @Json(name = "role_mentions")
    internal val roleMentions: String? = null,

    @Json(name = "thread_replies")
    internal val threadReplies: String? = null,
)
