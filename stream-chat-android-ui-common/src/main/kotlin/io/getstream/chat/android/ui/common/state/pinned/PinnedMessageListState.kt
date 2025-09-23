/*
 * Copyright (c) 2014-2024 Stream.io Inc. All rights reserved.
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

package io.getstream.chat.android.ui.common.state.pinned

import io.getstream.chat.android.ui.common.model.MessageResult
import java.util.Date

/**
 * Represents the pinned message list state, used to render the required UI.
 *
 * @param canLoadMore Indicator if we've reached the end of messages, to stop triggering pagination. Defaults to true.
 * @param results The messages to render. Defaults to an empty list.
 * @param isLoading Indicator if we're currently loading data (initial load). Defaults to true.
 * @param nextDate Date used to fetch next page of the messages. Defaults to current date.
 */
public data class PinnedMessageListState(
    val canLoadMore: Boolean = true,
    val results: List<MessageResult> = emptyList(),
    val isLoading: Boolean = true,
    val nextDate: Date = Date(),
)
