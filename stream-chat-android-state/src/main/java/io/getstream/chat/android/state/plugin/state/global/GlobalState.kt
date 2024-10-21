/*
 * Copyright (c) 2014-2022 Stream.io Inc. All rights reserved.
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

package io.getstream.chat.android.state.plugin.state.global

import io.getstream.chat.android.models.Channel
import io.getstream.chat.android.models.ChannelMute
import io.getstream.chat.android.models.Mute
import io.getstream.chat.android.models.TypingEvent
import io.getstream.chat.android.state.plugin.internal.StatePlugin
import kotlinx.coroutines.flow.StateFlow

/**
 * Global state of [StatePlugin].
 */
public interface GlobalState {

    /**
     * The total unread message count for the current user.
     * Depending on your app you'll want to show this or the channelUnreadCount.
     */
    public val totalUnreadCount: StateFlow<Int>

    /**
     * the number of unread channels for the current user.
     */
    public val channelUnreadCount: StateFlow<Int>

    /**
     * The number of unread threads for the current user.
     */
    public val unreadThreadsCount: StateFlow<Int>

    /**
     * list of users that you've muted.
     */
    public val muted: StateFlow<List<Mute>>

    /**
     * List of channels you've muted.
     */
    public val channelMutes: StateFlow<List<ChannelMute>>

    /**
     * if the current user is banned or not.
     */
    public val banned: StateFlow<Boolean>

    /**
     * Map of typing users in all active channel.
     * Use [Channel.cid] to access events for a particular channel.
     *
     * @see [TypingEvent]
     */
    public val typingChannels: StateFlow<Map<String, TypingEvent>>
}
