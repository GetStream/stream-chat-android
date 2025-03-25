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

package io.getstream.chat.android.ui.common.state.mentions

import io.getstream.chat.android.ui.common.model.MessageResult

/**
 * The state of the mention list on which the UI should be rendered.
 *
 * @param isLoading True if the initial loading is in progress.
 * @param messages The list of messages to be displayed.
 * @param nextPage The next page token to be loaded *(Internal usage only)*
 * @param canLoadMore True if there are more messages to be loaded.
 * @param isLoadingMore True if the loading of the next page is in progress.
 */
public data class MentionListState(
    val isLoading: Boolean,
    val messages: List<MessageResult>,
    internal val nextPage: String?,
    val canLoadMore: Boolean,
    val isLoadingMore: Boolean,
)
