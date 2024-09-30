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

import io.getstream.chat.android.models.Message
import java.util.Date

/**
 * Represents the pinned message list state, used to render the required UI.
 *
 * @param canLoadMore Indicator if we've reached the end of messages, to stop triggering pagination.
 * @param results The messages to render.
 * @param isLoading Indicator if we're currently loading data (initial load).
 * @param nextDate Date used to fetch next page of the messages.
 */
public data class PinnedMessageListState(
    val canLoadMore: Boolean,
    val results: List<Message>,
    val isLoading: Boolean,
    val nextDate: Date,
)
