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

package io.getstream.chat.android.ui.common.state.mentions

import io.getstream.chat.android.ui.common.model.MessageResult

/**
 * The state of the mention list on which the UI should be rendered.
 *
 * @param isLoading True if the initial loading is in progress. Defaults to true.
 * @param results The list of mentions to be displayed. Defaults to an empty list.
 * @param nextPage The next page token to be loaded *(Internal usage only)*. Defaults to null.
 * @param canLoadMore True if there are more mentions to be loaded. Defaults to true.
 * @param isLoadingMore True if the loading of the next page is in progress. Defaults to false.
 * @param isRefreshing True if the mention list is being refreshed. Defaults to false.
 */
public data class MentionListState(
    val isLoading: Boolean = true,
    val results: List<MessageResult> = emptyList(),
    internal val nextPage: String? = null,
    val canLoadMore: Boolean = true,
    val isLoadingMore: Boolean = false,
    val isRefreshing: Boolean = false,
)
