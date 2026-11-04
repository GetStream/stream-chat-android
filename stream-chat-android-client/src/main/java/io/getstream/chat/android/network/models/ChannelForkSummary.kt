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
 * A fork of an agent channel
 */
@com.squareup.moshi.JsonClass(generateAdapter = true)
internal data class ChannelForkSummary(
    @Json(name = "channel_cid")
    internal val channelCid: String,

    @Json(name = "copied_message_count")
    internal val copiedMessageCount: Int,

    @Json(name = "created_at")
    internal val createdAt: java.util.Date,

    @Json(name = "source_channel_cid")
    internal val sourceChannelCid: String,

    @Json(name = "source_message_id")
    internal val sourceMessageId: String,

    @Json(name = "status")
    internal val status: String,

    @Json(name = "failed_reason")
    internal val failedReason: String? = null,

    @Json(name = "oldest_source_message_id")
    internal val oldestSourceMessageId: String? = null,

    @Json(name = "task_id")
    internal val taskId: String? = null,

    @Json(name = "truncated")
    internal val truncated: Boolean? = null,
)
