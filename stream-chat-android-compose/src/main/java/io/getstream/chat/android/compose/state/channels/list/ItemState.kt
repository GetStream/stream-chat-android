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

import io.getstream.chat.android.models.Channel
import io.getstream.chat.android.models.DraftMessage
import io.getstream.chat.android.models.Message
import io.getstream.chat.android.models.User

/**
 * Represents each item we show in the list of channels.
 */
public sealed class ItemState {
    public abstract val key: String

    /**
     * Represents each channel item we show in the list of channels.
     *
     * @param channel The channel to show.
     * @param isMuted If the channel is muted for the current user.
     * @param isPinned If the channel is pinned for the current user.
     * @param typingUsers The list of users currently typing in the channel.
     * @param draftMessage The draft message for the current user in the channel.
     * @param isSelected Whether this channel is currently selected (e.g. via long-press context menu).
     */
    public data class ChannelItemState(
        val channel: Channel,
        val isMuted: Boolean = false,
        val isPinned: Boolean = false,
        val typingUsers: List<User> = emptyList(),
        val draftMessage: DraftMessage? = null,
        val isSelected: Boolean = false,
    ) : ItemState() {
        override val key: String = channel.cid
    }

    /**
     * Represents each search result item we show in the list of channels.
     *
     * @param message The message to show.
     * @param channel The channel where the message was sent.
     * It can be null if the channel was not found on the local cache.
     */
    public data class SearchResultItemState(
        val message: Message,
        val channel: Channel? = null,
    ) : ItemState() {
        override val key: String = message.id
    }
}
