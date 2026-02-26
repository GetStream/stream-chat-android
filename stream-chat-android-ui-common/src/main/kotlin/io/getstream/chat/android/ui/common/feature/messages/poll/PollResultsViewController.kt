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

package io.getstream.chat.android.ui.common.feature.messages.poll

import io.getstream.chat.android.client.extensions.internal.getVotesUnlessAnonymous
import io.getstream.chat.android.client.extensions.internal.getWinner
import io.getstream.chat.android.core.internal.InternalStreamChatApi
import io.getstream.chat.android.models.Poll
import io.getstream.chat.android.ui.common.state.messages.poll.PollResultsViewState
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow

/**
 * Controller responsible for managing the state and events related to poll results.
 *
 * This controller processes poll data to create a view state that displays all poll options, with
 * each option showing up to [MAX_VOTES_TO_SHOW] votes and a "Show All" button when there are more
 * votes available. The controller does not fetch votes from the API; it uses votes already present
 * in the poll object.
 *
 * @param poll The poll containing the votes to display.
 */
@OptIn(ExperimentalCoroutinesApi::class)
@InternalStreamChatApi
public class PollResultsViewController(
    private val poll: Poll,
) {

    private val _state = MutableStateFlow(
        run {
            val winner = poll.getWinner()
            PollResultsViewState(
                pollName = poll.name,
                results = poll.options.map { option ->
                    val votes = poll.getVotesUnlessAnonymous(option)
                        .take(MAX_VOTES_TO_SHOW)
                    val voteCount = poll.voteCountsByOption[option.id] ?: 0
                    PollResultsViewState.ResultItem(
                        option = option,
                        isWinner = winner == option,
                        voteCount = voteCount,
                        votes = votes,
                        showAllButton = voteCount > MAX_VOTES_TO_SHOW,
                    )
                },
            )
        },
    )

    /**
     * The current state of the poll results view.
     */
    public val state: StateFlow<PollResultsViewState> = _state.asStateFlow()
}

private const val MAX_VOTES_TO_SHOW = 5
