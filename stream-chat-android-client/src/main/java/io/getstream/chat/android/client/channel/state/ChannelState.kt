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

package io.getstream.chat.android.client.channel.state

import io.getstream.chat.android.models.Channel
import io.getstream.chat.android.models.ChannelData
import io.getstream.chat.android.models.ChannelUserRead
import io.getstream.chat.android.models.Config
import io.getstream.chat.android.models.Location
import io.getstream.chat.android.models.Member
import io.getstream.chat.android.models.Message
import io.getstream.chat.android.models.MessagesState
import io.getstream.chat.android.models.TypingEvent
import io.getstream.chat.android.models.User
import kotlinx.coroutines.flow.StateFlow
import java.util.Date

/** State container with reactive data of a channel.*/
public interface ChannelState {
    /** Type of this channel.*/
    public val channelType: String

    /** Id of this channel.*/
    public val channelId: String

    /** CID of this channel. It's 'channelType:channelId'.*/
    public val cid: String

    /** A replied message state in this channel. By default is null. There is a value if you're replying some message.*/
    public val repliedMessage: StateFlow<Message?>

    /**
     * A map containing all the ids that quoted a message. Using the id of a message you can get the IDs of the
     * messages that quoted it. This can be used to reflected changes of the original message to the messages that
     * quoted it.
     */
    public val quotedMessagesMap: StateFlow<Map<String, List<String>>>

    /** The message collection of this channel. */
    public val messages: StateFlow<List<Message>>

    /** The pinned message collection of this channel. */
    public val pinnedMessages: StateFlow<List<Message>>

    /** Strong typed state of message collection of this channel. See [MessagesState] for more details.*/
    public val messagesState: StateFlow<MessagesState>

    /** The collection of messages from previous pages of data.*/
    @Deprecated("This property is not used anymore and will be removed in future versions.")
    public val oldMessages: StateFlow<List<Message>>

    /** The number of people currently watching the channel.*/
    public val watcherCount: StateFlow<Int>

    /** The list of users currently watching this channel.*/
    public val watchers: StateFlow<List<User>>

    /** Who is currently typing. Current user is excluded from this. */
    public val typing: StateFlow<TypingEvent>

    /** How far every user in this channel has read. */
    public val reads: StateFlow<List<ChannelUserRead>>

    /** Read status for the current user. */
    public val read: StateFlow<ChannelUserRead?>

    /** Unread count for this channel, calculated based on read state. This works even if you're offline. */
    public val unreadCount: StateFlow<Int>

    /** The list of members of this channel. */
    public val members: StateFlow<List<Member>>

    /** Number of all members of this channel. */
    public val membersCount: StateFlow<Int>

    /** StateFlow object with the channel data. */
    public val channelData: StateFlow<ChannelData>

    /** If the channel is currently hidden. */
    public val hidden: StateFlow<Boolean>

    /** If the channel is currently muted. */
    public val muted: StateFlow<Boolean>

    /** If we are currently loading. */
    public val loading: StateFlow<Boolean>

    /** If we are currently loading older messages. */
    public val loadingOlderMessages: StateFlow<Boolean>

    /** If we are currently loading newer messages. */
    public val loadingNewerMessages: StateFlow<Boolean>

    /** If there are no more older messages to load. */
    public val endOfOlderMessages: StateFlow<Boolean>

    /** If there are no more newer messages to load. */
    public val endOfNewerMessages: StateFlow<Boolean>

    /** If we need to recover state when connection established again. */
    public val recoveryNeeded: Boolean

    /** Channel config data */
    public val channelConfig: StateFlow<Config>

    public val insideSearch: StateFlow<Boolean>

    /** The date of the last message sent by the current user. */
    public val lastSentMessageDate: StateFlow<Date?>

    /** Live locations that are currently active in this channel. */
    public val activeLiveLocations: StateFlow<List<Location>>

    /** Number of messages in the channel. */
    public val messageCount: StateFlow<Int?>

    /** Function that builds a channel based on data from StateFlows. */
    public fun toChannel(): Channel

    /**
     * Gets message using the ID. This is a more performant way to request one message than request the whole list
     * of messages
     */
    public fun getMessageById(id: String): Message?
}
