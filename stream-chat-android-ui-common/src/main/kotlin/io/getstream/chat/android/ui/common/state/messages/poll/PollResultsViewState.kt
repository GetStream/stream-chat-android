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

package io.getstream.chat.android.ui.common.state.messages.poll

import io.getstream.chat.android.models.Poll

/**
 * Represents the state of the poll results view.
 *
 * This sealed interface is used to model the different states that the poll results view
 * can be in, such as loading, displaying content, or showing an error.
 */
public sealed interface PollResultsViewState {

    /**
     * The original poll (with limited votes).
     */
    public val poll: Poll

    /**
     * Represents the loading state of the poll results view.
     *
     * @param poll The original poll (with limited votes).
     */
    public data class Loading(
        override val poll: Poll,
    ) : PollResultsViewState

    /**
     * Represents the content state of the poll results view.
     *
     * @param poll The poll with votes fetched so far.
     * @param canLoadMore True if there are more votes to be loaded. Defaults to true.
     * @param isLoadingMore True if the loading of the next page is in progress. Defaults to false.
     */
    public data class Content(
        override val poll: Poll,
        val canLoadMore: Boolean = true,
        val isLoadingMore: Boolean = false,
    ) : PollResultsViewState

    /**
     * Represents the error state of the poll results view.
     *
     * This state is used when an error occurs while loading the votes.
     *
     * @param poll The original poll (with limited votes).
     * @param message The error message to be displayed.
     */
    public data class Error(
        override val poll: Poll,
        val message: String,
    ) : PollResultsViewState
}
