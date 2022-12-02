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

package io.getstream.chat.android.state.plugin.state.global.internal

import io.getstream.chat.android.client.setup.state.ClientState
import io.getstream.chat.android.models.ChannelMute
import io.getstream.chat.android.models.Mute
import io.getstream.chat.android.models.TypingEvent
import io.getstream.chat.android.models.User
import io.getstream.chat.android.state.plugin.internal.StatePlugin
import io.getstream.chat.android.state.plugin.state.global.GlobalState

/**
 * Mutable global state of [StatePlugin].
 */
internal interface MutableGlobalState : GlobalState {

    val clientState: ClientState

    fun setUser(user: User)

    fun setTotalUnreadCount(totalUnreadCount: Int)

    fun setChannelUnreadCount(channelUnreadCount: Int)

    fun setBanned(banned: Boolean)

    fun setChannelMutes(channelMutes: List<ChannelMute>)

    fun setMutedUsers(mutedUsers: List<Mute>)

    /**
     * Tries emit typing event for a particular channel.
     *
     * @param cid The full channel id, i.e. "messaging:123" to which the message with reaction belongs.
     * @param typingEvent [TypingEvent] with information about typing users. Current user is excluded.
     */
    fun tryEmitTypingEvent(cid: String, typingEvent: TypingEvent)
}
