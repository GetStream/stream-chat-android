/*
 * Copyright (c) 2014-2024 Stream.io Inc. All rights reserved.
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

package io.getstream.chat.android.client.api2.model.dto

import com.squareup.moshi.JsonClass
import java.util.Date

/**
 * Represents the DTO for the option of a poll.
 *
 * @property id The id of the option.
 * @property text The text of the option.
 */
@JsonClass(generateAdapter = true)
internal data class DownstreamOptionDto(
    val id: String,
    val text: String,
)

/**
 * Represents the DTO for a vote in a poll.
 *
 * @property id The id of the vote.
 * @property poll_id The id of the poll.
 * @property option_id The id of the option that was voted for.
 * @property created_at The date when the vote was created.
 * @property updated_at The date when the vote was last updated.
 * @property user The user who voted.
 * @property user_id The id of the user who voted.
 */
@JsonClass(generateAdapter = true)
internal data class DownstreamVoteDto(
    val id: String,
    val poll_id: String,
    val option_id: String,
    val created_at: Date,
    val updated_at: Date,
    val user: DownstreamUserDto?,
    val user_id: String,
)

/**
 * Represents the DTO for a poll.
 *
 * @property id The id of the poll.
 * @property name The name of the poll.
 * @property description The description of the poll.
 * @property voting_visibility The visibility of the votes in the poll. Can be "public" or "anonymous".
 * @property enforce_unique_vote Whether the poll enforces unique votes.
 * @property max_votes_allowed The maximum number of votes allowed in the poll.
 * @property allow_user_suggested_options Whether the poll allows user suggested options.
 * @property allow_answers Whether the poll allows answers.
 * @property options The options in the poll.
 * @property vote_counts_by_option The vote counts for each option in the poll.
 * @property latest_votes_by_option The latest votes for each option in the poll.
 * @property created_at The date when the poll was created.
 * @property created_by The user who created the poll.
 * @property created_by_id The id of the user who created the poll.
 * @property own_votes The votes of the user who requested the poll.
 * @property updated_at The date when the poll was last updated.
 * @property vote_count The total number of votes in the poll.
 * @property is_closed Whether the poll is closed.
 */
@JsonClass(generateAdapter = true)
internal data class DownstreamPollDto(
    val id: String,
    val name: String,
    val description: String,
    val voting_visibility: String,
    val enforce_unique_vote: Boolean,
    val max_votes_allowed: Int,
    val allow_user_suggested_options: Boolean,
    val allow_answers: Boolean,
    val options: List<DownstreamOptionDto>,
    val vote_counts_by_option: Map<String, Int>?,
    val latest_votes_by_option: Map<String, List<DownstreamVoteDto>>?,
    val created_at: Date,
    val created_by: DownstreamUserDto,
    val created_by_id: String,
    val own_votes: List<DownstreamVoteDto>,
    val updated_at: Date,
    val vote_count: Int,
    val is_closed: Boolean = false,
)
