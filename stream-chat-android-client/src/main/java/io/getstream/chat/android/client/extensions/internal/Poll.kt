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
import io.getstream.chat.android.client.events.VoteCastedEvent
import io.getstream.chat.android.client.events.VoteChangedEvent
import io.getstream.chat.android.client.events.VoteRemovedEvent
import io.getstream.chat.android.core.internal.InternalStreamChatApi
import io.getstream.chat.android.models.Poll

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
