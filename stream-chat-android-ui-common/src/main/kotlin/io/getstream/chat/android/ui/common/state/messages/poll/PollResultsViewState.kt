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
 * @param isLoading True if the initial load is in progress. Defaults to true.
 * @param poll The poll with votes fetched so far.
 * @param canLoadMore True if there are more votes to be loaded. Defaults to true.
 * @param isLoadingMore True if the loading of the next page is in progress. Defaults to false.
 */
public data class PollResultsViewState(
    val isLoading: Boolean = true,
    val poll: Poll,
    val canLoadMore: Boolean = true,
    val isLoadingMore: Boolean = false,
)
