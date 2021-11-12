package io.getstream.chat.android.livedata.controller

import androidx.lifecycle.LiveData
import io.getstream.chat.android.client.models.Channel
import io.getstream.chat.android.client.models.ChannelUserRead
import io.getstream.chat.android.client.models.Member
import io.getstream.chat.android.client.models.Message
import io.getstream.chat.android.client.models.TypingEvent
import io.getstream.chat.android.client.models.User
import io.getstream.chat.android.livedata.ChatDomainImpl
import io.getstream.chat.android.offline.channel.ChannelData

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
public sealed interface ChannelController {
    public val channelType: String
    public val channelId: String

    /** a list of messages sorted by message.createdAt */
    public val messages: LiveData<List<Message>>

    /**
     * Similar to the messages field, but returns the a MessagesState object
     * This sealed class makes it easier to verify that you've implemented all possible error/no result states
     *
     * @see MessagesState
     */
    public val messagesState: LiveData<MessagesState>

    /** Old messages loaded from history of conversation */
    public val oldMessages: LiveData<List<Message>>

    /** the number of people currently watching the channel */
    public val watcherCount: LiveData<Int>

    /** the list of users currently watching this channel */
    public val watchers: LiveData<List<User>>

    /** who is currently typing (current user is excluded from this) */
    public val typing: LiveData<TypingEvent>

    /** how far every user in this channel has read */
    public val reads: LiveData<List<ChannelUserRead>>

    /** read status for the current user */
    public val read: LiveData<ChannelUserRead?>

    /**
     * unread count for this channel, calculated based on read state (this works even if you're offline)
     */
    public val unreadCount: LiveData<Int?>

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

    public val offlineChannelData: LiveData<ChannelData>

    /**
     * Contains the Message that is selected to be replied to in this channel,
     * or null if no such selection exists.
     */
    public val repliedMessage: LiveData<Message?>

    public fun clean()
    public fun toChannel(): Channel
    public fun getMessage(messageId: String): Message?

    public val hidden: LiveData<Boolean>
    public val muted: LiveData<Boolean>

    public sealed class MessagesState {
        /** The ChannelController is initialized but no query is currently running.
         * If you know that a query will be started you typically want to display a loading icon.
         */
        public object NoQueryActive : MessagesState()

        /** Indicates we are loading the first page of results.
         * We are in this state if ChannelController.loading is true
         * For seeing if we're loading more results have a look at loadingNewerMessages and loadingOlderMessages
         *
         * @see loading
         * @see loadingNewerMessages
         * @see loadingOlderMessages
         */
        public object Loading : MessagesState()

        /** If we are offline and don't have channels stored in offline storage, typically displayed as an error condition. */
        public object OfflineNoResults : MessagesState()

        /** The list of messages, loaded either from offline storage or an API call.
         * Observe chatDomain.online to know if results are currently up to date
         * @see ChatDomainImpl.connectionState
         */
        public data class Result(val messages: List<Message>) : MessagesState()
    }
}
