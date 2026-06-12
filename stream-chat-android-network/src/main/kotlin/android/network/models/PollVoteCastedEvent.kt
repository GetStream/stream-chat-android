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
 * Emitted when a vote is cast on a poll.
 */

data class PollVoteCastedEvent (
    @Json(name = "created_at")
    val createdAt: org.threeten.bp.OffsetDateTime,

    @Json(name = "custom")
    val custom: kotlin.collections.Map<kotlin.String, Any?> = emptyMap(),

    @Json(name = "poll")
    val poll: io.getstream.chat.android.network.models.PollResponseData,

    @Json(name = "poll_vote")
    val pollVote: io.getstream.chat.android.network.models.PollVoteResponseData,

    @Json(name = "type")
    val type: kotlin.String,

    @Json(name = "activity_id")
    val activityId: kotlin.String? = null,

    @Json(name = "cid")
    val cid: kotlin.String? = null,

    @Json(name = "message_id")
    val messageId: kotlin.String? = null,

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
