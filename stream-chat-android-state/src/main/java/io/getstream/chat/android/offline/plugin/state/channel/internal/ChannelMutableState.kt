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

import io.getstream.chat.android.client.events.TypingStartEvent
import io.getstream.chat.android.client.models.ChannelUserRead
import io.getstream.chat.android.client.models.Config
import io.getstream.chat.android.client.models.Member
import io.getstream.chat.android.client.models.Message
import io.getstream.chat.android.client.models.TypingEvent
import io.getstream.chat.android.client.models.User
import io.getstream.chat.android.offline.model.channel.ChannelData
import io.getstream.chat.android.offline.plugin.state.channel.ChannelState
import kotlinx.coroutines.flow.StateFlow
import java.util.Date

@Suppress("VariableNaming", "TooManyFunctions")
/** State container with mutable data of a channel.*/
internal interface ChannelMutableState : ChannelState {

    /** Sorted version of messages. */
    val sortedMessages: StateFlow<List<Message>>

    /** The message collection of this channel. */
    val messageList: StateFlow<List<Message>>

    /** raw version of messages. */
    var rawMessages: Map<String, Message>

    /** raw version of reads. */
    var rawReads: Map<String, ChannelUserRead>

    /** raw version of reads. */
    var rawMembers: Map<String, Member>

    /** raw version of old messages. */
    var rawOldMessages: Map<String, Message>

    /** raw version of old watchers. */
    var rawWatchers: Map<String, User>

    /** raw version of typing. */
    var rawTyping: Map<String, TypingStartEvent>

    /** the date of the last message */
    var lastMessageAt: Date?

    /** the data to hide messages before */
    var hideMessagesBefore: Date?

    /** The date of the last typing event. */
    var lastStartTypingEvent: Date?

    /** If we need to recover state when connection established again. */
    override var recoveryNeeded: Boolean

    /**
     * Sets the channel to be loading older messages.
     *
     * @param isLoading Boolean.
     */
    fun setLoadingOlderMessages(isLoading: Boolean)

    /**
     * Sets the channel to be loading newer messages.
     *
     * @param isLoading Boolean.
     */
    fun setLoadingNewerMessages(isLoading: Boolean)

    /**
     * Sets the watchers counter the this channel.
     *
     * @param count Int.
     */
    fun setWatcherCount(count: Int)

    /**
     * Sets the read information for this channel.
     *
     * @param channelUserRead [ChannelUserRead]
     */
    fun setRead(channelUserRead: ChannelUserRead?)

    /** Sets the end for newer messages. */
    fun setEndOfNewerMessages(isEnd: Boolean)

    /**
     * Sets the end for older messages.
     *
     * @param isEnd Boolean
     */
    fun setEndOfOlderMessages(isEnd: Boolean)

    /**
     * Sets loadings.
     *
     * @param isLoading Boolean.
     */
    fun setLoading(isLoading: Boolean)

    /**
     * Sets hidden.
     *
     * @param isHidden Boolean
     */
    fun setHidden(isHidden: Boolean)

    /**
     * Sets muted.
     *
     * @param isMuted Boolean.
     */
    fun setMuted(isMuted: Boolean)

    /** Sets [ChannelData]. */
    fun setChannelData(channelData: ChannelData)

    /**
     * Sets replied message.
     *
     * @param repliedMessage [Message]
     */
    fun setRepliedMessage(repliedMessage: Message?)

    /**
     * Sets unread count.
     *
     * @param count Int.
     */
    fun setUnreadCount(count: Int)

    /**
     *  Sets member count.
     *
     * @param count Int.
     */
    fun setMembersCount(count: Int)

    /** Sets inside search. This must be set when a search is started in the channel and the
     * user is looks sort a older message using the search functionality.
     *
     * @param isInsideSearch Boolean.
     * */
    fun setInsideSearch(isInsideSearch: Boolean)

    /**
     * Set channel config
     *
     * @param channelConfig [Config]
     */
    fun setChannelConfig(channelConfig: Config)

    /**
     * Updates StateFlows related to typing updates.
     */
    fun updateTypingEvents(eventsMap: Map<String, TypingStartEvent>, typingEvent: TypingEvent)
}
