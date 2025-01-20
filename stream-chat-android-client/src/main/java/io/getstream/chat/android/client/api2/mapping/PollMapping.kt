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

package io.getstream.chat.android.client.api2.mapping

import io.getstream.chat.android.client.api2.model.dto.DownstreamOptionDto
import io.getstream.chat.android.client.api2.model.dto.DownstreamPollDto
import io.getstream.chat.android.client.api2.model.dto.DownstreamVoteDto
import io.getstream.chat.android.models.Answer
import io.getstream.chat.android.models.ChannelTransformer
import io.getstream.chat.android.models.MessageTransformer
import io.getstream.chat.android.models.Option
import io.getstream.chat.android.models.Poll
import io.getstream.chat.android.models.UserId
import io.getstream.chat.android.models.Vote
import io.getstream.chat.android.models.VotingVisibility

/**
 * Transforms DownstreamPollDto to Poll
 *
 * @param currentUserId the current user id.
 * @param channelTransformer the channel transformer to transform the channel.
 * @param messageTransformer the message transformer to transform the channel's messages.
 *
 * @return Poll
 */
internal fun DownstreamPollDto.toDomain(
    currentUserId: UserId?,
    channelTransformer: ChannelTransformer,
    messageTransformer: MessageTransformer,
): Poll {
    val ownUserId = currentUserId ?: own_votes.firstOrNull()?.user?.id
    val votes = latest_votes_by_option
        ?.values
        ?.flatten()
        ?.filter { it.is_answer != true }
        ?.map {
            it.toDomain(
                currentUserId = currentUserId,
                channelTransformer = channelTransformer,
                messageTransformer = messageTransformer,
            )
        } ?: emptyList()
    val ownVotes = (
        own_votes
            .filter { it.is_answer != true }
            .map {
                it.toDomain(
                    currentUserId = currentUserId,
                    channelTransformer = channelTransformer,
                    messageTransformer = messageTransformer,
                )
            } +
            votes.filter { it.user?.id == ownUserId }
        )
        .associateBy { it.id }
        .values
        .toList()

    val answer = latest_answers?.map {
        it.toAnswerDomain(
            currentUserId = currentUserId,
            channelTransformer = channelTransformer,
            messageTransformer = messageTransformer,
        )
    } ?: emptyList()

    return Poll(
        id = id,
        name = name,
        description = description,
        options = options.map { it.toDomain() },
        votingVisibility = voting_visibility.toVotingVisibility(),
        enforceUniqueVote = enforce_unique_vote,
        maxVotesAllowed = max_votes_allowed ?: 1,
        allowUserSuggestedOptions = allow_user_suggested_options,
        allowAnswers = allow_answers,
        voteCountsByOption = vote_counts_by_option ?: emptyMap(),
        votes = votes,
        ownVotes = ownVotes,
        createdAt = created_at,
        updatedAt = updated_at,
        closed = is_closed,
        answers = answer,
    )
}

/**
 * Transforms DownstreamOptionDto to Option
 *
 * @return Option
 */
internal fun DownstreamOptionDto.toDomain(): Option = Option(
    id = id,
    text = text,
)

/**
 * Transforms DownstreamVoteDto to Vote
 *
 * @param currentUserId the current user id.
 * @param channelTransformer the channel transformer to transform the channel.
 * @param messageTransformer the message transformer to transform the channel's messages.
 *
 * @return Vote
 */
internal fun DownstreamVoteDto.toDomain(
    currentUserId: UserId?,
    channelTransformer: ChannelTransformer,
    messageTransformer: MessageTransformer,
): Vote = Vote(
    id = id,
    pollId = poll_id,
    optionId = option_id,
    createdAt = created_at,
    updatedAt = updated_at,
    user = user?.toDomain(
        currentUserId = currentUserId,
        channelTransformer = channelTransformer,
        messageTransformer = messageTransformer,
    ),
)

/**
 * Transforms DownstreamVoteDto to Answer
 *
 * @param currentUserId the current user id.
 * @param channelTransformer the channel transformer to transform the channel.
 * @param messageTransformer the message transformer to transform the channel's messages.
 *
 * @return Answer
 */
internal fun DownstreamVoteDto.toAnswerDomain(
    currentUserId: UserId?,
    channelTransformer: ChannelTransformer,
    messageTransformer: MessageTransformer,
): Answer = Answer(
    id = id,
    pollId = poll_id,
    text = answer_text ?: "",
    createdAt = created_at,
    updatedAt = updated_at,
    user = user?.toDomain(
        currentUserId = currentUserId,
        channelTransformer = channelTransformer,
        messageTransformer = messageTransformer,
    ),
)

/**
 * Transforms String to VotingVisibility
 *
 * @return VotingVisibility
 */
private fun String?.toVotingVisibility(): VotingVisibility = when (this) {
    null,
    "public",
    -> VotingVisibility.PUBLIC
    "anonymous" -> VotingVisibility.ANONYMOUS
    else -> throw IllegalArgumentException("Unknown voting visibility: $this")
}
