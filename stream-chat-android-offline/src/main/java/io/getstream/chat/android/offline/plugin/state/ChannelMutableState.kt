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

package io.getstream.chat.android.offline.plugin.state

import io.getstream.chat.android.client.events.ChatEvent
import io.getstream.chat.android.client.models.ChannelUserRead
import io.getstream.chat.android.client.models.Config
import io.getstream.chat.android.client.models.Member
import io.getstream.chat.android.client.models.Message
import io.getstream.chat.android.client.models.User
import io.getstream.chat.android.offline.model.channel.ChannelData
import io.getstream.chat.android.offline.plugin.state.channel.ChannelState
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import java.util.Date

@Suppress("VariableNaming")
public interface ChannelMutableState : ChannelState {

    public val _messages: MutableStateFlow<Map<String, Message>>
    public val _watcherCount: MutableStateFlow<Int>
    public val _typing: MutableStateFlow<Map<String, ChatEvent>>
    public val _reads: MutableStateFlow<Map<String, ChannelUserRead>>
    public val _read: MutableStateFlow<ChannelUserRead?>
    public val _endOfNewerMessages: MutableStateFlow<Boolean>
    public val _endOfOlderMessages: MutableStateFlow<Boolean>
    public val _loading: MutableStateFlow<Boolean>
    public val _hidden: MutableStateFlow<Boolean>
    public val _muted: MutableStateFlow<Boolean>
    public val _watchers: MutableStateFlow<Map<String, User>>
    public val _members: MutableStateFlow<Map<String, Member>>
    public val _loadingOlderMessages: MutableStateFlow<Boolean>
    public val _loadingNewerMessages: MutableStateFlow<Boolean>
    public val _channelData: MutableStateFlow<ChannelData?>
    public val _oldMessages: MutableStateFlow<Map<String, Message>>
    public val lastMessageAt: MutableStateFlow<Date?>
    public val _repliedMessage: MutableStateFlow<Message?>
    public val _unreadCount: MutableStateFlow<Int>
    public val _membersCount: MutableStateFlow<Int>
    public val _insideSearch: MutableStateFlow<Boolean>

    /** Channel config data. */
    public val _channelConfig: MutableStateFlow<Config>

    public var hideMessagesBefore: Date?

    public val messageList: StateFlow<List<Message>>

    /** If we need to recover state when connection established again. */
    override var recoveryNeeded: Boolean
}
