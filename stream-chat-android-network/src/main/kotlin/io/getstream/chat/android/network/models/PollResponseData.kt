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

data class PollResponseData (
    @Json(name = "allow_answers")
    val allowAnswers: kotlin.Boolean,

    @Json(name = "allow_user_suggested_options")
    val allowUserSuggestedOptions: kotlin.Boolean,

    @Json(name = "answers_count")
    val answersCount: kotlin.Int,

    @Json(name = "created_at")
    val createdAt: org.threeten.bp.OffsetDateTime,

    @Json(name = "created_by_id")
    val createdById: kotlin.String,

    @Json(name = "description")
    val description: kotlin.String,

    @Json(name = "enforce_unique_vote")
    val enforceUniqueVote: kotlin.Boolean,

    @Json(name = "id")
    val id: kotlin.String,

    @Json(name = "name")
    val name: kotlin.String,

    @Json(name = "updated_at")
    val updatedAt: org.threeten.bp.OffsetDateTime,

    @Json(name = "vote_count")
    val voteCount: kotlin.Int,

    @Json(name = "voting_visibility")
    val votingVisibility: kotlin.String,

    @Json(name = "latest_answers")
    val latestAnswers: kotlin.collections.List<io.getstream.chat.android.network.models.PollVoteResponseData> = emptyList(),

    @Json(name = "options")
    val options: kotlin.collections.List<io.getstream.chat.android.network.models.PollOptionResponseData> = emptyList(),

    @Json(name = "own_votes")
    val ownVotes: kotlin.collections.List<io.getstream.chat.android.network.models.PollVoteResponseData> = emptyList(),

    @Json(name = "custom")
    val custom: kotlin.collections.Map<kotlin.String, Any?> = emptyMap(),

    @Json(name = "latest_votes_by_option")
    val latestVotesByOption: kotlin.collections.Map<kotlin.String, kotlin.collections.List<io.getstream.chat.android.network.models.PollVoteResponseData>> = emptyMap(),

    @Json(name = "vote_counts_by_option")
    val voteCountsByOption: kotlin.collections.Map<kotlin.String, kotlin.Int> = emptyMap(),

    @Json(name = "is_closed")
    val isClosed: kotlin.Boolean? = null,

    @Json(name = "max_votes_allowed")
    val maxVotesAllowed: kotlin.Int? = null,

    @Json(name = "created_by")
    val createdBy: io.getstream.chat.android.network.models.UserResponse? = null
)
