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
internal data class PollResponseData(
    @Json(name = "allow_answers")
    internal val allowAnswers: Boolean,

    @Json(name = "allow_user_suggested_options")
    internal val allowUserSuggestedOptions: Boolean,

    @Json(name = "answers_count")
    internal val answersCount: Int,

    @Json(name = "created_at")
    internal val createdAt: java.util.Date,

    @Json(name = "created_by_id")
    internal val createdById: String,

    @Json(name = "description")
    internal val description: String,

    @Json(name = "enforce_unique_vote")
    internal val enforceUniqueVote: Boolean,

    @Json(name = "id")
    internal val id: String,

    @Json(name = "name")
    internal val name: String,

    @Json(name = "updated_at")
    internal val updatedAt: java.util.Date,

    @Json(name = "vote_count")
    internal val voteCount: Int,

    @Json(name = "voting_visibility")
    internal val votingVisibility: String,

    @Json(name = "latest_answers")
    internal val latestAnswers: List<PollVoteResponseData> = emptyList(),

    @Json(name = "options")
    internal val options: List<PollOptionResponseData> = emptyList(),

    @Json(name = "own_votes")
    internal val ownVotes: List<PollVoteResponseData> = emptyList(),

    @Json(name = "custom")
    internal val custom: Map<String, Any?> = emptyMap(),

    @Json(name = "latest_votes_by_option")
    internal val latestVotesByOption: Map<String, List<PollVoteResponseData>> = emptyMap(),

    @Json(name = "vote_counts_by_option")
    internal val voteCountsByOption: Map<String, Int> = emptyMap(),

    @Json(name = "is_closed")
    internal val isClosed: Boolean? = null,

    @Json(name = "max_votes_allowed")
    internal val maxVotesAllowed: Int? = null,

    @Json(name = "created_by")
    internal val createdBy: UserResponse? = null,
)
