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

package io.getstream.chat.android.compose.previewdata

import io.getstream.chat.android.models.Option
import io.getstream.chat.android.models.Poll
import io.getstream.chat.android.models.Vote
import io.getstream.chat.android.models.VotingVisibility
import java.util.Date
import java.util.UUID

/**
 * Provides sample poll that will be used to render previews.
 */
internal object PreviewPollData {

    private val option1 = Option(
        id = UUID.randomUUID().toString(),
        text = "option1",
    )

    private val option2 = Option(
        id = UUID.randomUUID().toString(),
        text = "option2",
    )

    private val option3 = Option(
        id = UUID.randomUUID().toString(),
        text = "option3",
    )

    val poll1 = Poll(
        id = UUID.randomUUID().toString(),
        name = "Vote an option!",
        description = "This is a poll",
        options = listOf(option1, option2, option3),
        votingVisibility = VotingVisibility.PUBLIC,
        enforceUniqueVote = true,
        maxVotesAllowed = 1,
        allowUserSuggestedOptions = false,
        allowAnswers = true,
        voteCountsByOption = mapOf(
            option1.id to 3,
            option2.id to 1,
            option3.id to 1,
        ),
        votes = listOf(
            Vote(
                id = UUID.randomUUID().toString(),
                pollId = UUID.randomUUID().toString(),
                optionId = option1.id,
                createdAt = Date(),
                updatedAt = Date(),
                user = null,
            ),
            Vote(
                id = UUID.randomUUID().toString(),
                pollId = UUID.randomUUID().toString(),
                optionId = option1.id,
                createdAt = Date(),
                updatedAt = Date(),
                user = null,
            ),
            Vote(
                id = UUID.randomUUID().toString(),
                pollId = UUID.randomUUID().toString(),
                optionId = option1.id,
                createdAt = Date(),
                updatedAt = Date(),
                user = null,
            ),
            Vote(
                id = UUID.randomUUID().toString(),
                pollId = UUID.randomUUID().toString(),
                optionId = option2.id,
                createdAt = Date(),
                updatedAt = Date(),
                user = null,
            ),
            Vote(
                id = UUID.randomUUID().toString(),
                pollId = UUID.randomUUID().toString(),
                optionId = option3.id,
                createdAt = Date(),
                updatedAt = Date(),
                user = null,
            ),
        ),
        ownVotes = listOf(),
        createdAt = Date(),
        updatedAt = Date(),
        closed = false,
    )
}
