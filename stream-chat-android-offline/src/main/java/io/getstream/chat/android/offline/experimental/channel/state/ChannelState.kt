package io.getstream.chat.android.offline.experimental.channel.state

import io.getstream.chat.android.client.models.ChannelUserRead
import io.getstream.chat.android.client.models.Member
import io.getstream.chat.android.client.models.Message
import io.getstream.chat.android.client.models.TypingEvent
import io.getstream.chat.android.client.models.User
import io.getstream.chat.android.core.ExperimentalStreamChatApi
import io.getstream.chat.android.offline.channel.ChannelData
import kotlinx.coroutines.flow.StateFlow

@ExperimentalStreamChatApi
public interface ChannelState {
    public val channelType: String
    public val channelId: String
    public val cid: String
    public val repliedMessage: StateFlow<Message?>
    public val messages: StateFlow<List<Message>>
    public val messagesState: StateFlow<MessagesState>
    public val oldMessages: StateFlow<List<Message>>
    /** the number of people currently watching the channel */
    public val watcherCount: StateFlow<Int>
    /** the list of users currently watching this channel */
    public val watchers: StateFlow<List<User>>
    /** who is currently typing (current user is excluded from this) */
    public val typing: StateFlow<TypingEvent>
    /** how far every user in this channel has read */
    public val reads: StateFlow<List<ChannelUserRead>>
    /** read status for the current user */
    public val read: StateFlow<ChannelUserRead?>
    /** unread count for this channel, calculated based on read state (this works even if you're offline)*/
    public val unreadCount: StateFlow<Int?>
    /** the list of members of this channel */
    public val members: StateFlow<List<Member>>
    /** StateFlow object with the channel data */
    public val channelData: StateFlow<ChannelData>
    /** if the channel is currently hidden */
    public val hidden: StateFlow<Boolean>
    /** if the channel is currently muted */
    public val muted: StateFlow<Boolean>
    /** if we are currently loading */
    public val loading: StateFlow<Boolean>
    /** if we are currently loading older messages */
    public val loadingOlderMessages: StateFlow<Boolean>
    /** if we are currently loading newer messages */
    public val loadingNewerMessages: StateFlow<Boolean>
    /** set to true if there are no more older messages to load */
    public val endOfOlderMessages: StateFlow<Boolean>
    /** set to true if there are no more newer messages to load */
    public val endOfNewerMessages: StateFlow<Boolean>
    public val recoveryNeeded: Boolean
}
