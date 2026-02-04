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

package io.getstream.chat.android.ui.common.state.messages.poll

import io.getstream.chat.android.models.Option
import io.getstream.chat.android.models.Vote

/**
 * Represents the state of the poll results view.
 *
 * Contains the poll name and a list of result items, where each item represents one poll option
 * with its vote count, winner status, preview of votes, and whether to show the "Show All" button.
 *
 * @param pollName The name of the poll.
 * @param results The list of poll result items, typically sorted by vote count in descending order.
 */
public data class PollResultsViewState(
    val pollName: String,
    val results: List<ResultItem>,
) {

    /**
     * Represents a single poll result item for one option.
     *
     * Contains the option details, vote count, winner status, a preview of votes (typically
     * limited to a few votes), and a flag indicating whether to show the "Show All" button
     * to navigate to the detailed view.
     *
     * @param option The poll option associated with this result.
     * @param isWinner True if this option has the highest vote count and is the winner.
     * @param voteCount The total number of votes for this option.
     * @param votes The preview list of votes for this option (typically limited to a few items).
     * @param showAllButton True if the "Show All" button should be displayed to view all votes.
     */
    public data class ResultItem(
        val option: Option,
        val isWinner: Boolean,
        val voteCount: Int,
        val votes: List<Vote>,
        val showAllButton: Boolean = false,
    )
}
