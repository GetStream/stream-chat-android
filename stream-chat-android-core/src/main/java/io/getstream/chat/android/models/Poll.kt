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
import io.getstream.chat.android.core.internal.InternalStreamChatApi
import io.getstream.chat.android.models.querysort.ComparableFieldProvider
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
 * @property voteCount The total number of votes cast in the poll.
 * @property voteCountsByOption The number of votes for each option.
 * @property votes The list of votes.
 * @property ownVotes The list of votes cast by the current user.
 * @property createdAt The creation date of the poll.
 * @property updatedAt The last update date of the poll.
 * @property closed If set to true, the poll is closed and no more votes can be cast.
 * @property answersCount The total number of answers in the poll.
 * @property answers The list of poll answers.
 * @property createdBy The user who created the poll. This property is optional and might be null.
 * @property extraData Any additional data associated with the poll.
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
    val voteCount: Int,
    val voteCountsByOption: Map<String, Int>,
    val votes: List<Vote>,
    val ownVotes: List<Vote>,
    val createdAt: Date,
    val updatedAt: Date,
    val closed: Boolean,
    val answersCount: Int,
    val answers: List<Answer> = emptyList(),
    val createdBy: User?,
    val extraData: Map<String, Any> = emptyMap(),
) : ComparableFieldProvider {

    /**
     * Get the votes for a specific option.
     *
     * @param option The option to get the votes for.
     * @return The list of votes for the option.
     */
    public fun getVotes(option: Option): List<Vote> = votes.filter { it.optionId == option.id }

    override fun getComparableField(fieldName: String): Comparable<*>? = when (fieldName) {
        "id" -> id
        "name" -> name
        "created_at", "createdAt" -> createdAt
        "updated_at", "updatedAt" -> updatedAt
        "is_closed", "isClosed" -> closed
        else -> null
    }
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
 * @property extraData Any additional data associated with the option.
 */
@Immutable
public data class Option(
    val id: String,
    val text: String,
    val extraData: Map<String, Any> = emptyMap(),
)

/**
 * The PollConfig object is used to configure a poll.
 *
 * @property name The name of the poll.
 * @property description The description of the poll.
 * @property votingVisibility The visibility of the votes.
 * If set to [VotingVisibility.ANONYMOUS], the votes will be anonymous.
 * @property enforceUniqueVote If set to true, a user can only vote once.
 * @property maxVotesAllowed The maximum number of votes a user can cast.
 * @property allowUserSuggestedOptions If set to true, users can suggest new options.
 * @property allowAnswers If set to true, users can send answers.
 * @property extraData Any additional data associated with the poll.
 * @property optionsWithExtraData The list of options for the poll.
 */
public data class PollConfig internal constructor(
    val name: String,
    val description: String,
    val votingVisibility: VotingVisibility,
    val enforceUniqueVote: Boolean,
    val maxVotesAllowed: Int,
    val allowUserSuggestedOptions: Boolean,
    val allowAnswers: Boolean,
    val extraData: Map<String, Any>,
    @InternalStreamChatApi
    val optionsWithExtraData: List<PollOption>,
) {

    /**
     * Constructor to create a PollConfig with a list of option texts.
     *
     * @param name The name of the poll.
     * @param options The list of option texts for the poll.
     * @param description The description of the poll. Default: empty.
     * @param votingVisibility The visibility of the votes. Default: [VotingVisibility.PUBLIC].
     * @param enforceUniqueVote If set to true, a user can only vote once. Default: true.
     * @param maxVotesAllowed The maximum number of votes a user can cast. Default: 1.
     * @param allowUserSuggestedOptions If set to true, users can suggest new options. Default: false.
     * @param allowAnswers If set to true, users can send answers. Default: false.
     */
    @Deprecated(
        "This constructor doesn't allow passing extra data for options. " +
            "Use the constructor with List<PollOption> instead.",
    )
    public constructor(
        name: String,
        options: List<String>,
        description: String = "",
        votingVisibility: VotingVisibility = VotingVisibility.PUBLIC,
        enforceUniqueVote: Boolean = true,
        maxVotesAllowed: Int = 1,
        allowUserSuggestedOptions: Boolean = false,
        allowAnswers: Boolean = false,
    ) : this(
        name = name,
        optionsWithExtraData = options.map { PollOption(it) },
        description = description,
        votingVisibility = votingVisibility,
        enforceUniqueVote = enforceUniqueVote,
        maxVotesAllowed = maxVotesAllowed,
        allowUserSuggestedOptions = allowUserSuggestedOptions,
        allowAnswers = allowAnswers,
        extraData = emptyMap(),
    )

    /**
     * Alternative constructor to create a PollConfig with a list of options with extra data.
     *
     * @param name The name of the poll.
     * @param options The list of options (with optional extra data) for the poll.
     * @param description The description of the poll. Default: empty.
     * @param votingVisibility The visibility of the votes. Default: [VotingVisibility.PUBLIC].
     * @param enforceUniqueVote If set to true, a user can only vote once. Default: true.
     * @param maxVotesAllowed The maximum number of votes a user can cast. Default: 1.
     * @param allowUserSuggestedOptions If set to true, users can suggest new options. Default: false.
     * @param allowAnswers If set to true, users can send answers. Default: false.
     * @param extraData Any additional data associated with the poll.
     */
    public constructor(
        name: String,
        options: List<PollOption>,
        description: String = "",
        votingVisibility: VotingVisibility = VotingVisibility.PUBLIC,
        enforceUniqueVote: Boolean = true,
        maxVotesAllowed: Int = 1,
        allowUserSuggestedOptions: Boolean = false,
        allowAnswers: Boolean = false,
        extraData: Map<String, Any> = emptyMap(),
    ) : this(
        name = name,
        optionsWithExtraData = options,
        description = description,
        votingVisibility = votingVisibility,
        enforceUniqueVote = enforceUniqueVote,
        maxVotesAllowed = maxVotesAllowed,
        allowUserSuggestedOptions = allowUserSuggestedOptions,
        allowAnswers = allowAnswers,
        extraData = extraData,
    )

    /**
     * The list of option texts for the poll.
     */
    public val options: List<String>
        get() = optionsWithExtraData.map { it.text }
}

/**
 * Model representing the input required to create a poll option.
 *
 * @property text The text of the option.
 * @property extraData Any additional data associated with the option.
 */
public data class PollOption(
    val text: String,
    val extraData: Map<String, Any> = emptyMap(),
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
) : ComparableFieldProvider {

    override fun getComparableField(fieldName: String): Comparable<*>? = when (fieldName) {
        "created_at", "createdAt" -> createdAt
        else -> null
    }
}

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
