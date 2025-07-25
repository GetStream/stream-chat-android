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

package io.getstream.chat.android.previewdata

import io.getstream.chat.android.models.Option
import io.getstream.chat.android.models.Poll
import io.getstream.chat.android.models.Vote
import io.getstream.chat.android.models.VotingVisibility
import java.util.Calendar
import java.util.UUID

/**
 * Provides sample poll that will be used to render previews.
 */
public object PreviewPollData {

    public val option1: Option = Option(
        id = UUID.randomUUID().toString(),
        text = "option1",
    )

    public val option2: Option = Option(
        id = UUID.randomUUID().toString(),
        text = "option2",
    )

    public val option3: Option = Option(
        id = UUID.randomUUID().toString(),
        text = "option3",
    )

    public val poll1: Poll = Poll(
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
                createdAt = Now,
                updatedAt = Now,
                user = PreviewUserData.user1,
            ),
            Vote(
                id = UUID.randomUUID().toString(),
                pollId = UUID.randomUUID().toString(),
                optionId = option1.id,
                createdAt = Now,
                updatedAt = Now,
                user = PreviewUserData.user2,
            ),
            Vote(
                id = UUID.randomUUID().toString(),
                pollId = UUID.randomUUID().toString(),
                optionId = option1.id,
                createdAt = Now,
                updatedAt = Now,
                user = PreviewUserData.user3,
            ),
            Vote(
                id = UUID.randomUUID().toString(),
                pollId = UUID.randomUUID().toString(),
                optionId = option2.id,
                createdAt = Now,
                updatedAt = Now,
                user = PreviewUserData.user4,
            ),
            Vote(
                id = UUID.randomUUID().toString(),
                pollId = UUID.randomUUID().toString(),
                optionId = option3.id,
                createdAt = Now,
                updatedAt = Now,
                user = PreviewUserData.user5,
            ),
        ),
        ownVotes = listOf(),
        createdAt = Now,
        updatedAt = Now,
        closed = false,
    )
}

@Suppress("MagicNumber")
private val Now = Calendar.getInstance().run {
    set(Calendar.HOUR_OF_DAY, 8)
    set(Calendar.MINUTE, 32)
    set(Calendar.SECOND, 0)
    time
}
