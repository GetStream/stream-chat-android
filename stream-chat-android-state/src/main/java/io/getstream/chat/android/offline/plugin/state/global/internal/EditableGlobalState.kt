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

package io.getstream.chat.android.offline.plugin.state.global.internal

import io.getstream.chat.android.client.errors.ChatError
import io.getstream.chat.android.client.models.ChannelMute
import io.getstream.chat.android.client.models.Mute
import io.getstream.chat.android.client.models.User
import io.getstream.chat.android.core.internal.InternalStreamChatApi
import io.getstream.chat.android.offline.model.connection.ConnectionState
import io.getstream.chat.android.offline.plugin.state.global.GlobalState
import io.getstream.chat.android.offline.utils.Event
import io.getstream.chat.android.state.plugin.internal.StatePlugin

/**
 * Editable global state of [StatePlugin].
 */
@InternalStreamChatApi
public interface EditableGlobalState : GlobalState {

    @InternalStreamChatApi
    public fun setErrorEvent(errorEvent: Event<ChatError>)

    @InternalStreamChatApi
    public fun setUser(user: User)

    @InternalStreamChatApi
    public fun setConnectionState(connectionState: ConnectionState)

    @InternalStreamChatApi
    public fun setInitialized(initialized: Boolean)

    @InternalStreamChatApi
    public fun setTotalUnreadCount(totalUnreadCount: Int)

    @InternalStreamChatApi
    public fun setChannelUnreadCount(channelUnreadCount: Int)

    @InternalStreamChatApi
    public fun setBanned(banned: Boolean)

    @InternalStreamChatApi
    public fun setChannelMutes(channelMutes: List<ChannelMute>)

    @InternalStreamChatApi
    public fun setMutedUsers(mutedUsers: List<Mute>)
}
