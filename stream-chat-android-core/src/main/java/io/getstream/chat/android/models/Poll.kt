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

package io.getstream.chat.android.models

import androidx.compose.runtime.Immutable
import java.util.Date

/**
 * The Poll object represents a poll in a channel.
 *
 * @property id The unique identifier of the poll.
 * @property name The name of the poll.
 * @property description The description of the poll.
 * @property options The list of options for the poll.
 * @property votingVisibility The visibility of the votes.
 * If set to [VotingVisibility.ANONYMOUS], the votes will be anonymous.
 * @property enforceUniqueVote If set to true, a user can only vote once.
 * @property maxVotesAllowed The maximum number of votes a user can cast.
 * @property allowUserSuggestedOptions If set to true, users can suggest new options.
 * @property allowAnswers If set to true, users can vote.
 * @property voteCountsByOption The number of votes for each option.
 * @property votes The list of votes.
 * @property ownVotes The list of votes cast by the current user.
 * @property createdAt The creation date of the poll.
 * @property updatedAt The last update date of the poll.
 * @property closed If set to true, the poll is closed and no more votes can be cast.
 * @property answers The list of poll answers.
 */
@Immutable
public data class Poll(
    val id: String,
    val name: String,
    val description: String,
    val options: List<Option>,
    val votingVisibility: VotingVisibility,
    val enforceUniqueVote: Boolean,
    val maxVotesAllowed: Int,
    val allowUserSuggestedOptions: Boolean,
    val allowAnswers: Boolean,
    val voteCountsByOption: Map<String, Int>,
    val votes: List<Vote>,
    val ownVotes: List<Vote>,
    val createdAt: Date,
    val updatedAt: Date,
    val closed: Boolean,
    val answers: List<Answer> = emptyList(),
) {

    /**
     * Get the votes for a specific option.
     *
     * @param option The option to get the votes for.
     * @return The list of votes for the option.
     */
    public fun getVotes(option: Option): List<Vote> = votes.filter { it.optionId == option.id }
}

/**
 * The Answer object represents an answer in a poll.
 *
 * @property id The unique identifier of the answer.
 * @property pollId The unique identifier of the poll.
 * @property text The text of the answer.
 * @property createdAt The creation date of the answer.
 * @property updatedAt The last update date of the answer.
 * @property user The user who sent the answer.
 */
@Immutable
public data class Answer(
    val id: String,
    val pollId: String,
    val text: String,
    val createdAt: Date,
    val updatedAt: Date,
    val user: User?,
)

/**
 * The Option object represents an answer option in a poll.
 *
 * @property id The unique identifier of the option.
 * @property text The text of the option.
 */
@Immutable
public data class Option(
    val id: String,
    val text: String,
)

/**
 * The PollConfig object is used to configure a poll.
 *
 * @property name The name of the poll.
 * @property options The list of options for the poll.
 * @property description The description of the poll.
 * @property votingVisibility The visibility of the votes. Default is [VotingVisibility.PUBLIC].
 * If set to [VotingVisibility.ANONYMOUS], the votes will be anonymous.
 * @property enforceUniqueVote If set to true, a user can only vote once. Default is true.
 * @property maxVotesAllowed The maximum number of votes a user can cast. Default is 1.
 * @property allowUserSuggestedOptions If set to true, users can suggest new options. Default is false.
 * @property allowAnswers If set to true, users can send answers. Default is false.
 */
public data class PollConfig(
    val name: String,
    val options: List<String>,
    val description: String = "",
    val votingVisibility: VotingVisibility = VotingVisibility.PUBLIC,
    val enforceUniqueVote: Boolean = true,
    val maxVotesAllowed: Int = 1,
    val allowUserSuggestedOptions: Boolean = false,
    val allowAnswers: Boolean = false,
)

/**
 * The Vote object represents a vote in a poll.
 *
 * @property id The unique identifier of the vote.
 * @property pollId The unique identifier of the poll.
 * @property optionId The unique identifier of the voted option.
 * @property createdAt The creation date of the vote.
 * @property updatedAt The last update date of the vote.
 * @property user The user who cast the vote.
 */
@Immutable
public data class Vote(
    val id: String,
    val pollId: String,
    val optionId: String,
    val createdAt: Date,
    val updatedAt: Date,
    val user: User?,
)

/**
 * Represents the visibility of the votes.
 */
@Immutable
public enum class VotingVisibility {
    /** Votes are public. */
    PUBLIC,

    /** Votes are anonymous. */
    ANONYMOUS,
}
