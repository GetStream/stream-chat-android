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

package io.getstream.chat.android.client.extensions.internal

import io.getstream.chat.android.client.events.AnswerCastedEvent
import io.getstream.chat.android.client.events.PollClosedEvent
import io.getstream.chat.android.client.events.PollUpdatedEvent
import io.getstream.chat.android.client.events.VoteCastedEvent
import io.getstream.chat.android.client.events.VoteChangedEvent
import io.getstream.chat.android.client.events.VoteRemovedEvent
import io.getstream.chat.android.core.internal.InternalStreamChatApi
import io.getstream.chat.android.models.Option
import io.getstream.chat.android.models.Poll
import io.getstream.chat.android.models.Vote
import io.getstream.chat.android.models.VotingVisibility

/**
 * Processes the [VoteChangedEvent] and updates the poll with the new vote.
 *
 * @param currentUserId The current user ID.
 * @param getOldPoll A function to get the old poll by its ID.
 */
@InternalStreamChatApi
public fun VoteChangedEvent.processPoll(
    currentUserId: String?,
    getOldPoll: (String) -> Poll?,
): Poll {
    val oldPoll = getOldPoll(poll.id)
    val ownVotes = newVote.takeIf { it.user?.id == currentUserId }?.let { listOf(it) }
        ?: oldPoll?.ownVotes
    return poll.copy(
        ownVotes = ownVotes ?: emptyList(),
        answers = oldPoll?.answers ?: poll.answers,
    )
}

/**
 * Processes the [VoteCastedEvent] and updates the poll with the new vote.
 *
 * @param currentUserId The current user ID.
 * @param getOldPoll A function to get the old poll by its ID.
 */
@InternalStreamChatApi
public fun VoteCastedEvent.processPoll(
    currentUserId: String?,
    getOldPoll: (String) -> Poll?,
): Poll {
    val oldPoll = getOldPoll(poll.id)
    val ownVotes = (
        oldPoll?.ownVotes?.associateBy { it.id }
            ?: emptyMap()
        ) +
        listOfNotNull(newVote.takeIf { it.user?.id == currentUserId }).associateBy { it.id }
    return poll.copy(
        ownVotes = ownVotes.values.toList(),
        answers = oldPoll?.answers ?: poll.answers,
    )
}

/**
 * Processes the [VoteRemovedEvent] and updates the poll by removing the vote.
 *
 * @param getOldPoll A function to get the old poll by its ID.
 */
@InternalStreamChatApi
public fun VoteRemovedEvent.processPoll(
    getOldPoll: (String) -> Poll?,
): Poll {
    val oldPoll = getOldPoll(poll.id)
    val ownVotes = (oldPoll?.ownVotes?.associateBy { it.id } ?: emptyMap()) - removedVote.id
    return poll.copy(
        ownVotes = ownVotes.values.toList(),
        answers = oldPoll?.answers ?: poll.answers,
    )
}

/**
 * Processes the [AnswerCastedEvent] and updates the poll with the new answer.
 *
 * @param getOldPoll A function to get the old poll by its ID.
 */
@InternalStreamChatApi
public fun AnswerCastedEvent.processPoll(
    getOldPoll: (String) -> Poll?,
): Poll {
    val oldPoll = getOldPoll(poll.id)
    val answers = (
        oldPoll?.answers?.associateBy { it.id }
            ?: emptyMap()
        ) + (newAnswer.id to newAnswer)
    return poll.copy(
        answers = answers.values.toList(),
        ownVotes = oldPoll?.ownVotes ?: poll.ownVotes,
    )
}

/**
 * Processes the [PollClosedEvent] and updates the poll by marking it as closed.
 */
@InternalStreamChatApi
public fun PollClosedEvent.processPoll(
    getOldPoll: (String) -> Poll?,
): Poll = getOldPoll(poll.id)?.copy(closed = true) ?: poll

/**
 * Processes the [PollUpdatedEvent] and updates the poll with the new data.
 *
 * @param getOldPoll A function to get the old poll by its ID.
 */
@InternalStreamChatApi
public fun PollUpdatedEvent.processPoll(
    getOldPoll: (String) -> Poll?,
): Poll {
    val oldPoll = getOldPoll(poll.id)
    return poll.copy(
        ownVotes = oldPoll?.ownVotes ?: poll.ownVotes,
        answers = oldPoll?.answers ?: poll.answers,
    )
}

/**
 * Returns the votes for a specific option in the poll. If the poll is anonymous, it returns an empty list.
 */
@InternalStreamChatApi
public fun Poll.getVotesUnlessAnonymous(option: Option): List<Vote> = getVotes(option).takeUnless { votingVisibility == VotingVisibility.ANONYMOUS }
    ?: emptyList()

/**
 * Returns the unique winner of the poll.
 */
@InternalStreamChatApi
public fun Poll.getWinner(): Option? = options
    .maxByOrNull { voteCountsByOption[it.id] ?: 0 }
    ?.takeIf { option ->
        val maxVotes = voteCountsByOption[option.id] ?: 0
        maxVotes > 0 && // Ensure there are votes
            options.count { (voteCountsByOption[it.id] ?: 0) == maxVotes } == 1 // Ensure the winner is unique
    }
