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
 * Represents a user that is participating in a thread.
 */
@com.squareup.moshi.JsonClass(generateAdapter = true)
internal data class ThreadParticipant(
    @Json(name = "channel_cid")
    internal val channelCid: String,

    @Json(name = "created_at")
    internal val createdAt: java.util.Date,

    @Json(name = "last_read_at")
    internal val lastReadAt: java.util.Date,

    @Json(name = "custom")
    internal val custom: Map<String, Any?> = emptyMap(),

    @Json(name = "last_thread_message_at")
    internal val lastThreadMessageAt: java.util.Date? = null,

    @Json(name = "left_thread_at")
    internal val leftThreadAt: java.util.Date? = null,

    @Json(name = "thread_id")
    internal val threadId: String? = null,

    @Json(name = "user_id")
    internal val userId: String? = null,

    @Json(name = "user")
    internal val user: io.getstream.chat.android.network.models.UserResponse? = null,
)
