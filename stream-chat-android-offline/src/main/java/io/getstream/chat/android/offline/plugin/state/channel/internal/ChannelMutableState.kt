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

package io.getstream.chat.android.offline.plugin.state.channel.internal

import io.getstream.chat.android.client.events.ChatEvent
import io.getstream.chat.android.client.models.ChannelUserRead
import io.getstream.chat.android.client.models.Config
import io.getstream.chat.android.client.models.Member
import io.getstream.chat.android.client.models.Message
import io.getstream.chat.android.client.models.User
import io.getstream.chat.android.offline.model.channel.ChannelData
import io.getstream.chat.android.offline.plugin.state.channel.ChannelState
import kotlinx.coroutines.flow.StateFlow
import java.util.Date

@Suppress("VariableNaming", "TooManyFunctions")
internal interface ChannelMutableState : ChannelState {

    val sortedMessages: StateFlow<List<Message>>
    val messageList: StateFlow<List<Message>>

    var rawMessages: Map<String, Message>
    var rawReads: Map<String, ChannelUserRead>
    var rawMembers: Map<String, Member>
    var rawOldMessages: Map<String, Message>
    var rawWatchers: Map<String, User>
    var rawTyping: Map<String, ChatEvent>

    var lastMessageAt: Date?
    var hideMessagesBefore: Date?
    var lastStartTypingEvent: Date?

    /** If we need to recover state when connection established again. */
    override var recoveryNeeded: Boolean

    fun setLoadingOlderMessages(isLoading: Boolean)
    fun setLoadingNewerMessages(isLoading: Boolean)
    fun setWatcherCount(count: Int)
    fun setRead(channelUserRead: ChannelUserRead?)
    fun setEndOfNewerMessages(isEnd: Boolean)
    fun setEndOfOlderMessages(isEnd: Boolean)
    fun setLoading(isLoading: Boolean)
    fun setHidden(isHidden: Boolean)
    fun setMuted(isMuted: Boolean)
    fun setChannelData(channelData: ChannelData)
    fun setRepliedMessage(repliedMessage: Message?)
    fun setUnreadCount(count: Int)
    fun setMembersCount(count: Int)
    fun setInsideSearch(isInsideSearch: Boolean)
    fun setChannelConfig(channelConfig: Config)
}
