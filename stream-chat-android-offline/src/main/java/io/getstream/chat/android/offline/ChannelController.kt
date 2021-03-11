package io.getstream.chat.android.offline

import io.getstream.chat.android.client.models.Channel
import io.getstream.chat.android.client.models.ChannelUserRead
import io.getstream.chat.android.client.models.Member
import io.getstream.chat.android.client.models.Message
import io.getstream.chat.android.client.models.TypingEvent
import io.getstream.chat.android.client.models.User
import io.getstream.chat.android.livedata.ChannelData
import io.getstream.chat.android.livedata.controller.ChannelController
import io.getstream.chat.android.livedata.usecase.SetMessageForReply
import kotlinx.coroutines.flow.StateFlow

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
    public val messages: StateFlow<List<Message>>

    /**
     * Similar to the messages field, but returns the a MessagesState object
     * This sealed class makes it easier to verify that you've implemented all possible error/no result states
     *
     * @see MessagesState
     */
    public val messagesState: StateFlow<ChannelController.MessagesState>

    /** Old messages loaded from history of conversation */
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

    /**
     * unread count for this channel, calculated based on read state (this works even if you're offline)
     */
    public val unreadCount: StateFlow<Int?>

    /** the list of members of this channel */
    public val members: StateFlow<List<Member>>

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
    public val cid: String

    /** LiveData object with the channel data */
    public val channelData: StateFlow<ChannelData>

    /**
     * Contains the Message that is selected to be replied to in this channel,
     * or null if no such selection exists.
     *
     * See [SetMessageForReply].
     */
    public val repliedMessage: StateFlow<Message?>

    public fun clean()
    public fun toChannel(): Channel
    public fun getMessage(messageId: String): Message?

    public val hidden: StateFlow<Boolean>
    public val muted: StateFlow<Boolean>
}
