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

package io.getstream.chat.android.offline.plugin.state.global

import io.getstream.chat.android.client.errors.ChatError
import io.getstream.chat.android.client.models.ChannelMute
import io.getstream.chat.android.client.models.Mute
import io.getstream.chat.android.client.models.TypingEvent
import io.getstream.chat.android.client.models.User
import io.getstream.chat.android.offline.model.connection.ConnectionState
import io.getstream.chat.android.offline.utils.Event
import io.getstream.chat.android.state.plugin.internal.StatePlugin
import kotlinx.coroutines.flow.StateFlow

/**
 * Global state of [StatePlugin].
 */
public interface GlobalState {

    /**
     * The current user in the StatePlugin state.
     */
    public val user: StateFlow<User?>

    /**
     * If the client connection has been initialized.
     */
    public val initialized: StateFlow<Boolean>

    /**
     * StateFlow<ConnectionState> that indicates if we are currently online, connecting or offline.
     */
    public val connectionState: StateFlow<ConnectionState>

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
     * The error event state flow object is triggered when errors in the underlying components occur.
     * The following example shows how to observe these errors
     *
     *  repo.errorEvent.collect {
     *       // create a toast
     *   }
     */
    public val errorEvents: StateFlow<Event<ChatError>>

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
     * Updates about currently typing users in active channels. See [TypingEvent].
     */
    public val typingUpdates: StateFlow<TypingEvent>

    /**
     * If the user is online or not.
     *
     * @return True if the user is online otherwise False.
     */
    public fun isOnline(): Boolean

    /**
     * If the user is offline or not.
     *
     * @return True if the user is offline otherwise False.
     */
    public fun isOffline(): Boolean

    /**
     * If connection is in connecting state.
     *
     * @return True if the connection is in connecting state.
     */
    public fun isConnecting(): Boolean

    /**
     * If domain state is initialized or not.
     *
     * @return True if initialized otherwise False.
     */
    public fun isInitialized(): Boolean

    /**
     * Clears the state of [GlobalState].
     */
    public fun clearState()
}
