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
import io.getstream.chat.android.models.Answer

/**
 * Represents the state of the paginated list of a poll's answers (comments), newest first.
 *
 * @param isLoading True if the initial load is in progress.
 * @param results The answers fetched so far, plus those received live.
 * @param canLoadMore True if there are more answers available to load.
 * @param isLoadingMore True if a pagination request is in progress.
 */
@InternalStreamChatApi
public data class PollAnswersViewState(
    val isLoading: Boolean = true,
    val results: List<Answer> = emptyList(),
    val canLoadMore: Boolean = true,
    val isLoadingMore: Boolean = false,
)
