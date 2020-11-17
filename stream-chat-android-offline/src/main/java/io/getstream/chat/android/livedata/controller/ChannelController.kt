package io.getstream.chat.android.livedata.controller

import androidx.lifecycle.LiveData
import io.getstream.chat.android.client.models.Channel
import io.getstream.chat.android.client.models.ChannelUserRead
import io.getstream.chat.android.client.models.Member
import io.getstream.chat.android.client.models.Message
import io.getstream.chat.android.client.models.TypeEvent
import io.getstream.chat.android.client.models.User
import io.getstream.chat.android.livedata.ChannelData

/**
 * The Channel Controller exposes convenient livedata objects to build your chat interface
 * It automatically handles the incoming events and keeps users, messages, reactions, channel information up to date automatically
 * Offline storage is also handled using Room
 *
 * The most commonly used livedata objects are
 *
 * - .messages (the livedata for the list of messages)
 * - .channelData (livedata object with the channel name, image, etc.)
 * - .members (livedata object with the members of this channel)
 * - .watchers (the people currently watching this channel)
 * - .typing (who is currently typing)
 *
 */
public interface ChannelController {
    public val channelType: String
    public val channelId: String
    /** a list of messages sorted by message.createdAt */
    public val messages: LiveData<List<Message>>
    /** Old messages loaded from history of conversation */
    public val oldMessages: LiveData<List<Message>>
    /** the number of people currently watching the channel */
    public val watcherCount: LiveData<Int>
    /** the list of users currently watching this channel */
    public val watchers: LiveData<List<User>>
    /** who is currently typing (current user is excluded from this) */
    public val typing: LiveData<TypeEvent>
    /** how far every user in this channel has read */
    public val reads: LiveData<List<ChannelUserRead>>
    /** read status for the current user */
    public val read: LiveData<ChannelUserRead>
    /**
     * unread count for this channel, calculated based on read state (this works even if you're offline)
     */
    public val unreadCount: LiveData<Int>
    /** the list of members of this channel */
    public val members: LiveData<List<Member>>
    /** if we are currently loading */
    public val loading: LiveData<Boolean>
    /** if we are currently loading older messages */
    public val loadingOlderMessages: LiveData<Boolean>
    /** if we are currently loading newer messages */
    public val loadingNewerMessages: LiveData<Boolean>
    /** set to true if there are no more older messages to load */
    public val endOfOlderMessages: LiveData<Boolean>
    /** set to true if there are no more newer messages to load */
    public val endOfNewerMessages: LiveData<Boolean>
    public val recoveryNeeded: Boolean
    public val cid: String
    /** LiveData object with the channel data */
    public val channelData: LiveData<ChannelData>

    public fun clean()
    public fun toChannel(): Channel
    public fun getMessage(messageId: String): Message?

    public val hidden: LiveData<Boolean>
    public val muted: LiveData<Boolean>
}
