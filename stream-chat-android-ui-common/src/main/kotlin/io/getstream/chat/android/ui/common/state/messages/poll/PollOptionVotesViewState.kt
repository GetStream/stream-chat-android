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

import io.getstream.chat.android.core.internal.InternalStreamChatApi
import io.getstream.chat.android.models.Option
import io.getstream.chat.android.models.Vote

/**
 * Represents the state of the poll option votes view for a specific poll option.
 *
 * This state is used when displaying all votes for a single poll option in a paginated list.
 * It tracks loading states, pagination status, and the list of votes fetched so far.
 *
 * @param option The poll option for which vote results are displayed.
 * @param voteCount The total number of votes for this option.
 * @param isWinner True if this option has the highest vote count and is the winner.
 * @param isLoading True if the initial load is in progress. Defaults to true.
 * @param results The list of votes fetched so far. Defaults to an empty list.
 * @param canLoadMore True if there are more votes available to load via pagination. Defaults to true.
 * @param isLoadingMore True if a pagination request is currently in progress. Defaults to false.
 */
@InternalStreamChatApi
public data class PollOptionVotesViewState(
    val option: Option,
    val voteCount: Int,
    val isWinner: Boolean,
    val isLoading: Boolean = true,
    val results: List<Vote> = emptyList(),
    val canLoadMore: Boolean = true,
    val isLoadingMore: Boolean = false,
)
