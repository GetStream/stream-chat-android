package io.getstream.chat.android.livedata

import androidx.lifecycle.LiveData
import androidx.lifecycle.asLiveData
import io.getstream.chat.android.client.api.models.FilterObject
import io.getstream.chat.android.client.api.models.QuerySort
import io.getstream.chat.android.client.call.Call
import io.getstream.chat.android.client.call.map
import io.getstream.chat.android.client.errors.ChatError
import io.getstream.chat.android.client.events.ChatEvent
import io.getstream.chat.android.client.models.Attachment
import io.getstream.chat.android.client.models.Channel
import io.getstream.chat.android.client.models.ChannelMute
import io.getstream.chat.android.client.models.Config
import io.getstream.chat.android.client.models.Member
import io.getstream.chat.android.client.models.Message
import io.getstream.chat.android.client.models.Mute
import io.getstream.chat.android.client.models.Reaction
import io.getstream.chat.android.client.models.TypingEvent
import io.getstream.chat.android.client.models.User
import io.getstream.chat.android.livedata.controller.ChannelController
import io.getstream.chat.android.livedata.controller.ChannelControllerImpl
import io.getstream.chat.android.livedata.controller.QueryChannelsController
import io.getstream.chat.android.livedata.controller.QueryChannelsControllerImpl
import io.getstream.chat.android.livedata.controller.ThreadController
import io.getstream.chat.android.livedata.controller.ThreadControllerImpl
import io.getstream.chat.android.livedata.utils.Event
import io.getstream.chat.android.livedata.utils.RetryPolicy
import io.getstream.chat.android.livedata.utils.toLiveDataRetryPolicy
import io.getstream.chat.android.offline.model.ConnectionState
import kotlinx.coroutines.flow.map
import io.getstream.chat.android.offline.ChatDomain as ChatDomainStateFlow

/**
 * The Chat Domain exposes livedata objects to make it easier to build your chat UI.
 * It intercepts the various low level events to ensure data stays in sync.
 * Offline storage is handled using Room
 *
 * A different Room database is used for different users. That's why it's mandatory to specify the user id when
 * initializing the ChatRepository
 *
 * chatDomain.channel(type, id) returns a controller object with channel specific livedata objects
 * chatDomain.queryChannels(query) returns a livedata object for the specific queryChannels query
 *
 * chatDomain.online livedata object indicates if you're online or not
 * chatDomain.totalUnreadCount livedata object returns the current unread count for this user
 * chatDomain.muted the list of muted users
 * chatDomain.banned if the current user is banned or not
 * chatDomain.channelUnreadCount livedata object returns the number of unread channels for this user
 * chatDomain.errorEvents events for errors that happen while interacting with the chat
 *
 */
internal class ChatDomainImpl internal constructor(internal val chatDomainStateFlow: ChatDomainStateFlow) :
    ChatDomain {

    override var offlineEnabled: Boolean
        get() = chatDomainStateFlow.offlineEnabled
        set(value) {
            chatDomainStateFlow.offlineEnabled = value
        }
    override var userPresence: Boolean
        get() = chatDomainStateFlow.userPresence
        set(value) {
            chatDomainStateFlow.userPresence = value
        }

    override val user: LiveData<User?> = chatDomainStateFlow.user.asLiveData()

    /** if the client connection has been initialized */
    override val initialized: LiveData<Boolean> = chatDomainStateFlow.initialized.asLiveData()

    /**
     * LiveData<Boolean> that indicates if we are currently online
     */
    override val connectionState: LiveData<ConnectionState> = chatDomainStateFlow.connectionState.asLiveData()

    /**
     * The total unread message count for the current user.
     * Depending on your app you'll want to show this or the channelUnreadCount.
     */
    override val totalUnreadCount: LiveData<Int> = chatDomainStateFlow.totalUnreadCount.asLiveData()

    /**
     * the number of unread channels for the current user
     */
    override val channelUnreadCount: LiveData<Int> = chatDomainStateFlow.channelUnreadCount.asLiveData()

    /**
     * list of users that you've muted
     */
    override val muted: LiveData<List<Mute>> = chatDomainStateFlow.muted.asLiveData()

    /**
     * List of channels you've muted
     */
    override val channelMutes: LiveData<List<ChannelMute>> = chatDomainStateFlow.channelMutes.asLiveData()

    /**
     * if the current user is banned or not
     */
    override val banned: LiveData<Boolean> = chatDomainStateFlow.banned.asLiveData()

    /**
     * The error event livedata object is triggered when errors in the underlying components occur.
     * The following example shows how to observe these errors
     *
     *  channelController.errorEvent.observe(this) {
     *       // create a toast
     *   })
     *
     */
    override val errorEvents: LiveData<Event<ChatError>> = chatDomainStateFlow.errorEvents.map(::Event).asLiveData()
    override val typingUpdates: LiveData<TypingEvent> = chatDomainStateFlow.typingUpdates.asLiveData()

    /** The retry policy for retrying failed requests */
    override val retryPolicy: RetryPolicy = chatDomainStateFlow.retryPolicy.toLiveDataRetryPolicy()

    override fun getVersion(): String = chatDomainStateFlow.getVersion()

    override fun isOnline(): Boolean = chatDomainStateFlow.isOnline()

    override fun isOffline(): Boolean = chatDomainStateFlow.isOffline()

    override fun isInitialized(): Boolean = chatDomainStateFlow.isInitialized()

    override fun getActiveQueries(): List<QueryChannelsControllerImpl> =
        chatDomainStateFlow.getActiveQueries().map(::QueryChannelsControllerImpl)

    override fun clean() = chatDomainStateFlow.clean()

    override fun getChannelConfig(channelType: String): Config = chatDomainStateFlow.getChannelConfig(channelType)

    // region use-case functions
    @Suppress("DEPRECATION_ERROR")
    override fun replayEventsForActiveChannels(cid: String): Call<List<ChatEvent>> =
        chatDomainStateFlow.replayEventsForActiveChannels(cid)

    override fun getChannelController(cid: String): Call<ChannelController> =
        chatDomainStateFlow.getChannelController(cid).map(::ChannelControllerImpl)

    override fun watchChannel(cid: String, messageLimit: Int): Call<ChannelController> =
        chatDomainStateFlow.watchChannel(cid, messageLimit).map(::ChannelControllerImpl)

    override fun queryChannels(
        filter: FilterObject,
        sort: QuerySort<Channel>,
        limit: Int,
        messageLimit: Int,
    ): Call<QueryChannelsController> =
        chatDomainStateFlow.queryChannels(filter, sort, limit, messageLimit).map(::QueryChannelsControllerImpl)

    override fun getThread(cid: String, parentId: String): Call<ThreadController> =
        chatDomainStateFlow.getThread(cid, parentId).map(::ThreadControllerImpl)

    @Suppress("DEPRECATION_ERROR")
    override fun loadOlderMessages(cid: String, messageLimit: Int): Call<Channel> =
        chatDomainStateFlow.loadOlderMessages(cid, messageLimit)

    override fun loadNewerMessages(cid: String, messageLimit: Int): Call<Channel> =
        chatDomainStateFlow.loadNewerMessages(cid, messageLimit)

    override fun loadMessageById(
        cid: String,
        messageId: String,
        olderMessagesOffset: Int,
        newerMessagesOffset: Int,
    ): Call<Message> = chatDomainStateFlow.loadMessageById(cid, messageId, olderMessagesOffset, newerMessagesOffset)

    override fun queryChannelsLoadMore(
        filter: FilterObject,
        sort: QuerySort<Channel>,
        limit: Int,
        messageLimit: Int,
        memberLimit: Int,
    ): Call<List<Channel>> = chatDomainStateFlow.queryChannelsLoadMore(
        filter = filter,
        sort = sort,
        limit = limit,
        messageLimit = messageLimit,
        memberLimit = memberLimit,
    )

    override fun queryChannelsLoadMore(
        filter: FilterObject,
        sort: QuerySort<Channel>,
        messageLimit: Int,
    ): Call<List<Channel>> =
        chatDomainStateFlow.queryChannelsLoadMore(filter = filter, sort = sort, messageLimit = messageLimit)

    override fun queryChannelsLoadMore(
        filter: FilterObject,
        sort: QuerySort<Channel>,
    ): Call<List<Channel>> = chatDomainStateFlow.queryChannelsLoadMore(filter = filter, sort = sort)

    override fun threadLoadMore(cid: String, parentId: String, messageLimit: Int): Call<List<Message>> =
        chatDomainStateFlow.threadLoadMore(cid, parentId, messageLimit)

    override fun createChannel(channel: Channel): Call<Channel> = chatDomainStateFlow.createChannel(channel)

    override fun sendMessage(message: Message): Call<Message> = chatDomainStateFlow.sendMessage(message)

    override fun cancelMessage(message: Message): Call<Boolean> = chatDomainStateFlow.cancelMessage(message)

    override fun shuffleGiphy(message: Message): Call<Message> = chatDomainStateFlow.shuffleGiphy(message)

    override fun sendGiphy(message: Message): Call<Message> = chatDomainStateFlow.sendGiphy(message)

    override fun editMessage(message: Message): Call<Message> = chatDomainStateFlow.editMessage(message)

    override fun deleteMessage(message: Message, hard: Boolean): Call<Message> =
        chatDomainStateFlow.deleteMessage(message, hard)

    override fun deleteMessage(message: Message): Call<Message> =
        chatDomainStateFlow.deleteMessage(message, false)

    override fun sendReaction(cid: String, reaction: Reaction, enforceUnique: Boolean): Call<Reaction> =
        chatDomainStateFlow.sendReaction(cid, reaction, enforceUnique)

    override fun deleteReaction(cid: String, reaction: Reaction): Call<Message> =
        chatDomainStateFlow.deleteReaction(cid, reaction)

    @Suppress("DEPRECATION_ERROR")
    override fun keystroke(cid: String, parentId: String?): Call<Boolean> =
        chatDomainStateFlow.keystroke(cid, parentId)

    @Suppress("DEPRECATION_ERROR")
    override fun stopTyping(cid: String, parentId: String?): Call<Boolean> =
        chatDomainStateFlow.stopTyping(cid, parentId)

    override fun markRead(cid: String): Call<Boolean> = chatDomainStateFlow.markRead(cid)

    override fun markAllRead(): Call<Boolean> = chatDomainStateFlow.markAllRead()

    override fun hideChannel(cid: String, keepHistory: Boolean): Call<Unit> =
        chatDomainStateFlow.hideChannel(cid, keepHistory)

    @Suppress("DEPRECATION_ERROR")
    override fun showChannel(cid: String): Call<Unit> = chatDomainStateFlow.showChannel(cid)

    override fun leaveChannel(cid: String): Call<Unit> = chatDomainStateFlow.leaveChannel(cid)

    override fun deleteChannel(cid: String): Call<Unit> = chatDomainStateFlow.deleteChannel(cid)

    @Suppress("DEPRECATION_ERROR")
    override fun setMessageForReply(cid: String, message: Message?): Call<Unit> =
        chatDomainStateFlow.setMessageForReply(cid, message)

    @Suppress("DEPRECATION_ERROR")
    override fun downloadAttachment(attachment: Attachment): Call<Unit> =
        chatDomainStateFlow.downloadAttachment(attachment)

    override fun searchUsersByName(
        querySearch: String,
        offset: Int,
        userLimit: Int,
        userPresence: Boolean,
    ): Call<List<User>> = chatDomainStateFlow.searchUsersByName(querySearch, offset, userLimit, userPresence)

    override fun queryMembers(
        cid: String,
        offset: Int,
        limit: Int,
        filter: FilterObject,
        sort: QuerySort<Member>,
        members: List<Member>,
    ): Call<List<Member>> = chatDomainStateFlow.queryMembers(cid, offset, limit, filter, sort, members)
    // end region
}
