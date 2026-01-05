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

package io.getstream.chat.android.offline.repository.domain.message.internal

import io.getstream.chat.android.models.Answer
import io.getstream.chat.android.models.Option
import io.getstream.chat.android.models.Poll
import io.getstream.chat.android.models.User
import io.getstream.chat.android.models.Vote
import io.getstream.chat.android.models.VotingVisibility

/**
 * Converts a [Poll] domain model to a [PollEntity] database entity.
 *
 * @return The converted [PollEntity].
 */
internal fun Poll.toEntity(): PollEntity = PollEntity(
    id = id,
    name = name,
    description = description,
    options = options.map { it.toEntity() },
    votes = votes.map { it.toEntity() },
    createdAt = createdAt,
    updatedAt = updatedAt,
    votingVisibility = votingVisibility.toEntity(),
    enforceUniqueVote = enforceUniqueVote,
    maxVotesAllowed = maxVotesAllowed,
    allowUserSuggestedOptions = allowUserSuggestedOptions,
    allowAnswers = allowAnswers,
    voteCount = voteCount,
    voteCountsByOption = voteCountsByOption,
    ownVotes = ownVotes.map { it.toEntity() },
    closed = closed,
    answersCount = answersCount,
    answers = answers.map { it.toEntity() },
    createdById = createdBy?.id,
    extraData = extraData,
)

/**
 * Converts a [PollEntity] database entity to a [Poll] domain model.
 *
 * @param getUser A suspend function to retrieve a user by ID.
 * @return The converted [Poll].
 */
internal suspend fun PollEntity.toModel(
    getUser: suspend (userId: String) -> User,
): Poll = Poll(
    id = id,
    name = name,
    description = description,
    options = options.map(OptionEntity::toModel),
    votes = votes.map { it.toModel(getUser) },
    createdAt = createdAt,
    updatedAt = updatedAt,
    votingVisibility = votingVisibility.toVotingVisibility(),
    enforceUniqueVote = enforceUniqueVote,
    maxVotesAllowed = maxVotesAllowed,
    allowUserSuggestedOptions = allowUserSuggestedOptions,
    allowAnswers = allowAnswers,
    voteCount = voteCount,
    voteCountsByOption = voteCountsByOption,
    ownVotes = ownVotes.map { it.toModel(getUser) },
    closed = closed,
    answersCount = answersCount,
    answers = answers.map { it.toModel(getUser) },
    createdBy = createdById?.let { getUser(it) },
    extraData = extraData,
)

/**
 * Converts an [Option] domain model to an [OptionEntity] database entity.
 *
 * @return The converted [OptionEntity].
 */
internal fun Option.toEntity(): OptionEntity = OptionEntity(
    id = id,
    text = text,
    extraData = extraData,
)

/**
 * Converts an [OptionEntity] database entity to an [Option] domain model.
 *
 * @return The converted [Option].
 */
internal fun OptionEntity.toModel(): Option = Option(
    id = id,
    text = text,
    extraData = extraData,
)

/**
 * Converts a [Vote] domain model to a [VoteEntity] database entity.
 *
 * @return The converted [VoteEntity].
 */
internal fun Vote.toEntity(): VoteEntity = VoteEntity(
    id = id,
    optionId = optionId,
    pollId = pollId,
    createdAt = createdAt,
    updatedAt = updatedAt,
    userId = user?.id,
)

/**
 * Converts a [VoteEntity] database entity to a [Vote] domain model.
 *
 * @param getUser A suspend function to retrieve a user by ID.
 * @return The converted [Vote].
 */
internal suspend fun VoteEntity.toModel(
    getUser: suspend (userId: String) -> User,
): Vote = Vote(
    id = id,
    optionId = optionId,
    pollId = pollId,
    createdAt = createdAt,
    updatedAt = updatedAt,
    user = userId?.let { getUser(it) },
)

/**
 * Converts an [Answer] domain model to an [AnswerEntity] database entity.
 *
 * @return The converted [AnswerEntity].
 */
internal fun Answer.toEntity(): AnswerEntity = AnswerEntity(
    id = id,
    pollId = pollId,
    text = text,
    createdAt = createdAt,
    updatedAt = updatedAt,
    userId = user?.id,
)

/**
 * Converts an [AnswerEntity] database entity to an [Answer] domain model.
 *
 * @param getUser A suspend function to retrieve a user by ID.
 * @return The converted [Answer].
 */
internal suspend fun AnswerEntity.toModel(
    getUser: suspend (userId: String) -> User,
): Answer = Answer(
    id = id,
    pollId = pollId,
    text = text,
    createdAt = createdAt,
    updatedAt = updatedAt,
    user = userId?.let { getUser(it) },
)

/**
 * Converts a [VotingVisibility] domain enum to its string representation.
 *
 * @return The string representation of the voting visibility ("public" or "anonymous").
 */
internal fun VotingVisibility.toEntity(): String = when (this) {
    VotingVisibility.ANONYMOUS -> "anonymous"
    VotingVisibility.PUBLIC -> "public"
}

/**
 * Converts a string representation to a [VotingVisibility] domain enum.
 *
 * @return The converted [VotingVisibility].
 * @throws IllegalArgumentException if the string is not a recognized voting visibility.
 */
internal fun String.toVotingVisibility(): VotingVisibility = when (this) {
    "public" -> VotingVisibility.PUBLIC
    "anonymous" -> VotingVisibility.ANONYMOUS
    else -> throw IllegalArgumentException("Unknown voting visibility: $this")
}
