/*
 * Copyright (c) 2014-2025 Stream.io Inc. All rights reserved.
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

import io.getstream.chat.android.compose.ui.messages.attachments.factory.AttachmentPickerPollCreation
import io.getstream.chat.android.models.PollConfig
import io.getstream.chat.android.models.PollOption
import io.getstream.chat.android.models.VotingVisibility

/**
 * Builds a [PollConfig] from the provided [AttachmentPickerPollCreation] data.
 *
 * @param pollQuestion The question of the poll.
 * @param pollOptions The list of poll options.
 * @param pollSwitches The list of poll switches.
 */
internal fun pollConfigFrom(
    pollQuestion: String,
    pollOptions: List<PollOptionItem>,
    pollSwitches: List<PollSwitchItem>,
): PollConfig {
    val options = pollOptions
        .filter { it.title.isNotEmpty() }
        .map { it.title }
    val allowUserSuggestedOptions = pollSwitches.any {
        it.key == PollSwitchItemKeys.ALLOW_USER_SUGGESTED_OPTIONS && it.enabled
    }
    val allowAnswers = pollSwitches.any {
        it.key == PollSwitchItemKeys.ALLOW_ANSWERS && it.enabled
    }
    val anonymousPoll = pollSwitches.any {
        it.key == PollSwitchItemKeys.VOTING_VISIBILITY && it.enabled
    }
    val votingVisibility = if (anonymousPoll) {
        VotingVisibility.ANONYMOUS
    } else {
        VotingVisibility.PUBLIC
    }
    val maxVotesEnabled = pollSwitches.any {
        it.key == PollSwitchItemKeys.MAX_VOTES_ALLOWED && it.enabled
    }
    val maxVotesAllowed = if (maxVotesEnabled) {
        pollSwitches.first { it.key == PollSwitchItemKeys.MAX_VOTES_ALLOWED }.pollSwitchInput?.value.toString().toInt()
    } else {
        1
    }
    val enforceUniqueVotes = pollSwitches.none {
        it.key == PollSwitchItemKeys.MAX_VOTES_ALLOWED && it.enabled
    }
    return PollConfig(
        name = pollQuestion,
        options = options.map(::PollOption),
        allowUserSuggestedOptions = allowUserSuggestedOptions,
        allowAnswers = allowAnswers,
        votingVisibility = votingVisibility,
        maxVotesAllowed = maxVotesAllowed,
        enforceUniqueVote = enforceUniqueVotes,
    )
}
