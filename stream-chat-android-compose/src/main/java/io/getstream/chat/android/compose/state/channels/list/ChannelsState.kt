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

package io.getstream.chat.android.compose.state.channels.list

/**
 * Represents the Channels screen state, used to render the required UI.
 *
 * @param isLoading If we're currently loading data (initial load).
 * @param isLoadingMore If we're loading more items (pagination).
 * @param endOfChannels If we've reached the end of channels, to stop triggering pagination.
 * @param channelItems The channel items to represent in the list.
 * @param searchQuery The current search query.
 */
public data class ChannelsState(
    val isLoading: Boolean = true,
    val isLoadingMore: Boolean = false,
    val endOfChannels: Boolean = false,
    val channelItems: List<ItemState> = emptyList(),
    val searchQuery: SearchQuery = SearchQuery.Empty,
)
