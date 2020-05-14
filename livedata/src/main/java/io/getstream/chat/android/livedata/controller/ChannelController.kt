package io.getstream.chat.android.livedata.controller

import androidx.lifecycle.LiveData
import io.getstream.chat.android.client.models.Channel
import io.getstream.chat.android.client.models.ChannelUserRead
import io.getstream.chat.android.client.models.Member
import io.getstream.chat.android.client.models.Message
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
interface ChannelController {
    var channelType: String
    var channelId: String
    /** a list of messages sorted by message.createdAt */
    val messages: LiveData<List<Message>>
    /** the number of people currently watching the channel */
    val watcherCount: LiveData<Int>
    /** the list of users currently watching this channel */
    val watchers: LiveData<List<User>>
    /** who is currently typing (current user is excluded from this) */
    val typing: LiveData<List<User>>
    /** how far every user in this channel has read */
    val reads: LiveData<List<ChannelUserRead>>
    /** read status for the current user */
    val read: LiveData<ChannelUserRead>
    /**
     * unread count for this channel, calculated based on read state (this works even if you're offline)
     */
    val unreadCount: LiveData<Int>
    /** the list of members of this channel */
    val members: LiveData<List<Member>>
    /** if we are currently loading */
    val loading: LiveData<Boolean>
    /** if we are currently loading older messages */
    val loadingOlderMessages: LiveData<Boolean>
    /** if we are currently loading newer messages */
    val loadingNewerMessages: LiveData<Boolean>
    /** set to true if there are no more older messages to load */
    val endOfOlderMessages: LiveData<Boolean>
    /** set to true if there are no more newer messages to load */
    val endOfNewerMessages: LiveData<Boolean>
    var recoveryNeeded: Boolean
    val cid: String
    /** LiveData object with the channel data */
    val channelData: LiveData<ChannelData>

    fun clean()
    fun toChannel(): Channel
    fun getMessage(messageId: String): Message?
    // This one needs to be public for flows such as running a message action
    // TODO: this is for handling actions, think we should expose this in a different way
    fun upsertMessage(message: Message)

    val hidden: LiveData<Boolean>
    val muted: LiveData<Boolean>
}
