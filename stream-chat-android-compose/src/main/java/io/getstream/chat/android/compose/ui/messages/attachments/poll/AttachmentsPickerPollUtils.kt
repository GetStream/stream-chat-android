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

package io.getstream.chat.android.compose.ui.messages.attachments.poll

import io.getstream.chat.android.models.PollConfig
import io.getstream.chat.android.models.PollOption
import io.getstream.chat.android.models.VotingVisibility
import io.getstream.chat.android.ui.common.utils.PollsConstants

/**
 * Builds a [PollConfig] from the provided poll creation data.
 *
 * @param pollQuestion The question of the poll.
 * @param pollOptions The list of poll options.
 * @param state The current poll creation view state.
 */
internal fun pollConfigFrom(
    pollQuestion: String,
    pollOptions: List<PollOptionItem>,
    state: CreatePollViewState,
): PollConfig {
    val options = pollOptions
        .filter { it.title.isNotEmpty() }
        .map { it.title }
    val votingVisibility = if (state.anonymousPollEnabled) {
        VotingVisibility.ANONYMOUS
    } else {
        VotingVisibility.PUBLIC
    }
    val maxVotesAllowed = when {
        !state.multipleVotesEnabled -> 1
        !state.limitVotesEnabled -> null
        else -> state.maxVotesPerPersonText.toIntOrNull()
            ?.coerceIn(PollsConstants.MULTIPLE_ANSWERS_RANGE)
            ?: PollsConstants.MULTIPLE_ANSWERS_RANGE.first
    }
    return PollConfig(
        name = pollQuestion,
        options = options.map { text -> PollOption(text = text) },
        allowUserSuggestedOptions = state.suggestAnOptionEnabled,
        allowAnswers = state.allowCommentsEnabled,
        votingVisibility = votingVisibility,
        maxVotesAllowed = maxVotesAllowed,
        enforceUniqueVote = !state.multipleVotesEnabled,
    )
}
