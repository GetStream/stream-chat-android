package io.getstream.chat.android.livedata

import android.content.Context
import android.os.Handler
import androidx.annotation.VisibleForTesting
import androidx.lifecycle.LiveData
import androidx.lifecycle.asLiveData
import io.getstream.chat.android.client.ChatClient
import io.getstream.chat.android.client.api.models.FilterObject
import io.getstream.chat.android.client.api.models.QuerySort
import io.getstream.chat.android.client.call.Call
import io.getstream.chat.android.client.call.map
import io.getstream.chat.android.client.errors.ChatError
import io.getstream.chat.android.client.events.ChatEvent
import io.getstream.chat.android.client.models.Attachment
import io.getstream.chat.android.client.models.Channel
import io.getstream.chat.android.client.models.Config
import io.getstream.chat.android.client.models.Member
import io.getstream.chat.android.client.models.Message
import io.getstream.chat.android.client.models.Mute
import io.getstream.chat.android.client.models.Reaction
import io.getstream.chat.android.client.models.TypingEvent
import io.getstream.chat.android.client.models.User
import io.getstream.chat.android.client.utils.Result
import io.getstream.chat.android.livedata.controller.ChannelController
import io.getstream.chat.android.livedata.controller.ChannelControllerImpl
import io.getstream.chat.android.livedata.controller.QueryChannelsController
import io.getstream.chat.android.livedata.controller.QueryChannelsControllerImpl
import io.getstream.chat.android.livedata.controller.ThreadController
import io.getstream.chat.android.livedata.controller.ThreadControllerImpl
import io.getstream.chat.android.livedata.model.SyncState
import io.getstream.chat.android.livedata.repository.RepositoryFacade
import io.getstream.chat.android.livedata.repository.database.ChatDatabase
import io.getstream.chat.android.livedata.usecase.UseCaseHelper
import io.getstream.chat.android.livedata.utils.Event
import io.getstream.chat.android.livedata.utils.RetryPolicy
import kotlinx.coroutines.Deferred
import java.io.File
import io.getstream.chat.android.offline.ChatDomainImpl as ChatDomainStateFlowImpl
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
internal class ChatDomainImpl internal constructor(
    internal var client: ChatClient,
    // the new behaviour for ChatDomain is to follow the ChatClient.setUser
    // the userOverwrite field is here for backwards compatibility
    internal var userOverwrite: User? = null,
    internal var db: ChatDatabase? = null,
    private val mainHandler: Handler,
    override var offlineEnabled: Boolean = true,
    internal var recoveryEnabled: Boolean = true,
    override var userPresence: Boolean = false,
    internal var backgroundSyncEnabled: Boolean = false,
    internal var appContext: Context,
) :
    ChatDomain {
    internal constructor(
        client: ChatClient,
        handler: Handler,
        offlineEnabled: Boolean,
        recoveryEnabled: Boolean,
        userPresence: Boolean,
        backgroundSyncEnabled: Boolean,
        appContext: Context,
    ) : this(
        client,
        null,
        null,
        handler,
        offlineEnabled,
        recoveryEnabled,
        userPresence,
        backgroundSyncEnabled,
        appContext
    )

    internal val chatDomainStateFlowImpl: ChatDomainStateFlowImpl = ChatDomainStateFlowImpl(
        client,
        userOverwrite,
        db,
        mainHandler,
        offlineEnabled,
        recoveryEnabled,
        userPresence,
        backgroundSyncEnabled,
        appContext
    )

    override var currentUser: User
        get() = chatDomainStateFlowImpl.currentUser
        set(value) {
            chatDomainStateFlowImpl.currentUser = value
        }

    lateinit var database: ChatDatabase

    /** a helper object which lists all the initialized use cases for the chat domain */
    override val useCases: UseCaseHelper = UseCaseHelper(chatDomainStateFlowImpl.useCases)

    @VisibleForTesting
    val defaultConfig: Config = Config(isConnectEvents = true, isMutes = true)

    /** if the client connection has been initialized */
    override val initialized: LiveData<Boolean> = chatDomainStateFlowImpl.initialized.asLiveData()

    /**
     * LiveData<Boolean> that indicates if we are currently online
     */
    override val online: LiveData<Boolean> = chatDomainStateFlowImpl.online.asLiveData()

    /**
     * The total unread message count for the current user.
     * Depending on your app you'll want to show this or the channelUnreadCount
     */
    override val totalUnreadCount: LiveData<Int> = chatDomainStateFlowImpl.totalUnreadCount.asLiveData()

    /**
     * the number of unread channels for the current user
     */
    override val channelUnreadCount: LiveData<Int> = chatDomainStateFlowImpl.channelUnreadCount.asLiveData()

    /**
     * list of users that you've muted
     */
    override val muted: LiveData<List<Mute>> = chatDomainStateFlowImpl.muted.asLiveData()

    /**
     * if the current user is banned or not
     */
    override val banned: LiveData<Boolean> = chatDomainStateFlowImpl.banned.asLiveData()

    /**
     * The error event livedata object is triggered when errors in the underlying components occur.
     * The following example shows how to observe these errors
     *
     *  channelController.errorEvent.observe(this) {
     *       // create a toast
     *   })
     *
     */
    override val errorEvents: LiveData<Event<ChatError>> = chatDomainStateFlowImpl.errorEvents.asLiveData()
    override val typingUpdates: LiveData<TypingEvent> = chatDomainStateFlowImpl.typingUpdates.asLiveData()

    internal var repos: RepositoryFacade
        get() = chatDomainStateFlowImpl.repos
        set(value) {
            chatDomainStateFlowImpl.repos = value
        }
    internal val initJob: Deferred<SyncState?>?
        get() = chatDomainStateFlowImpl.initJob

    /** The retry policy for retrying failed requests */
    override var retryPolicy: RetryPolicy
        get() = chatDomainStateFlowImpl.retryPolicy
        set(value) {
            chatDomainStateFlowImpl.retryPolicy = value
        }

    internal fun setUser(user: User) = chatDomainStateFlowImpl.setUser(user)

    internal val job = chatDomainStateFlowImpl.job
    internal var scope
        get() = chatDomainStateFlowImpl.scope
        set(value) {
            chatDomainStateFlowImpl.scope = value
        }

    internal suspend fun updateCurrentUser(me: User) = chatDomainStateFlowImpl.updateCurrentUser(me)
    internal suspend fun storeSyncState(): SyncState? = chatDomainStateFlowImpl.storeSyncState()
    override suspend fun disconnect() = chatDomainStateFlowImpl.disconnect()
    override fun getVersion(): String = chatDomainStateFlowImpl.getVersion()

    suspend fun <T : Any> runAndRetry(runnable: () -> Call<T>): Result<T> =
        chatDomainStateFlowImpl.runAndRetry(runnable)

    internal suspend fun createNewChannel(c: Channel): Result<Channel> = chatDomainStateFlowImpl.createNewChannel(c)

    fun addError(error: ChatError) = chatDomainStateFlowImpl.addError(error)

    fun isActiveChannel(cid: String): Boolean = chatDomainStateFlowImpl.isActiveChannel(cid)

    fun setChannelUnreadCount(newCount: Int) = chatDomainStateFlowImpl.setChannelUnreadCount(newCount)

    fun setBanned(newBanned: Boolean) = chatDomainStateFlowImpl.setBanned(newBanned)

    fun setTotalUnreadCount(newCount: Int) = chatDomainStateFlowImpl.setTotalUnreadCount(newCount)

    override fun removeMembers(cid: String, vararg userIds: String): Call<Channel> =
        chatDomainStateFlowImpl.removeMembers(cid, *userIds)

    internal fun channel(c: Channel): ChannelControllerImpl = ChannelControllerImpl(chatDomainStateFlowImpl.channel(c))

    internal fun channel(cid: String): ChannelControllerImpl =
        ChannelControllerImpl(chatDomainStateFlowImpl.channel(cid))

    internal fun channel(
        channelType: String,
        channelId: String,
    ): ChannelControllerImpl = ChannelControllerImpl(chatDomainStateFlowImpl.channel(channelType, channelId))

    internal fun allActiveChannels(): List<ChannelControllerImpl> =
        chatDomainStateFlowImpl.allActiveChannels().map(::ChannelControllerImpl)

    fun generateMessageId(): String = chatDomainStateFlowImpl.generateMessageId()

    internal fun setOffline() = chatDomainStateFlowImpl.setOffline()

    internal fun setOnline() = chatDomainStateFlowImpl.setOnline()

    internal fun setInitialized() = chatDomainStateFlowImpl.setInitialized()

    override fun isOnline(): Boolean = chatDomainStateFlowImpl.isOnline()

    override fun isOffline(): Boolean = chatDomainStateFlowImpl.isOffline()

    override fun isInitialized(): Boolean = chatDomainStateFlowImpl.isInitialized()

    override fun getActiveQueries(): List<QueryChannelsControllerImpl> =
        chatDomainStateFlowImpl.getActiveQueries().map(::QueryChannelsControllerImpl)

    /**
     * queryChannels
     * - first read the current results from Room
     * - if we are online make the API call to update results
     */
    fun queryChannels(
        filter: FilterObject,
        sort: QuerySort<Channel>,
    ): QueryChannelsControllerImpl = QueryChannelsControllerImpl(chatDomainStateFlowImpl.queryChannels(filter, sort))

    /**
     * replay events for all active channels
     * ensures that the cid you provide is active
     *
     * @param cid ensures that the channel with this id is active
     */
    internal suspend fun replayEvents(cid: String? = null): Result<List<ChatEvent>> =
        chatDomainStateFlowImpl.replayEvents(cid)

    /**
     * There are several scenarios in which we need to recover events
     * - Connection is lost and comes back (everything should be considered stale, so use recover all)
     * - App goes to the background and comes back (everything should be considered stale, so use recover all)
     * - We run a queryChannels or channel.watch call and encounter an offline state/or API error (should recover just that query or channel)
     * - A reaction, message or channel fails to be created. We should retry this every health check (30 seconds or so)
     *
     * Calling connectionRecovered triggers:
     * - queryChannels for the active query (at most 3) that need recovery
     * - queryChannels for any channels that need recovery
     * - channel.watch for channels that are not returned by the server
     * - event recovery for those channels
     * - API calls to create local channels, messages and reactions
     */
    suspend fun connectionRecovered(recoverAll: Boolean = false) =
        chatDomainStateFlowImpl.connectionRecovered(recoverAll)

    internal suspend fun retryFailedEntities() = chatDomainStateFlowImpl.retryFailedEntities()

    @VisibleForTesting
    internal suspend fun retryChannels(): List<Channel> = chatDomainStateFlowImpl.retryChannels()

    override fun clean() = chatDomainStateFlowImpl.clean()

    override fun getChannelConfig(channelType: String): Config = chatDomainStateFlowImpl.getChannelConfig(channelType)

    // region use-case functions
    override fun replayEventsForActiveChannels(cid: String): Call<List<ChatEvent>> =
        chatDomainStateFlowImpl.replayEventsForActiveChannels(cid)

    override fun getChannelController(cid: String): Call<ChannelController> =
        chatDomainStateFlowImpl.getChannelController(cid).map(::ChannelControllerImpl)

    override fun watchChannel(cid: String, messageLimit: Int): Call<ChannelController> =
        chatDomainStateFlowImpl.watchChannel(cid, messageLimit).map(::ChannelControllerImpl)

    override fun queryChannels(
        filter: FilterObject,
        sort: QuerySort<Channel>,
        limit: Int,
        messageLimit: Int,
    ): Call<QueryChannelsController> =
        chatDomainStateFlowImpl.queryChannels(filter, sort, limit, messageLimit).map(::QueryChannelsControllerImpl)

    override fun getThread(cid: String, parentId: String): Call<ThreadController> =
        chatDomainStateFlowImpl.getThread(cid, parentId).map(::ThreadControllerImpl)

    override fun loadOlderMessages(cid: String, messageLimit: Int): Call<Channel> =
        chatDomainStateFlowImpl.loadOlderMessages(cid, messageLimit)

    override fun loadNewerMessages(cid: String, messageLimit: Int): Call<Channel> =
        chatDomainStateFlowImpl.loadNewerMessages(cid, messageLimit)

    override fun loadMessageById(
        cid: String,
        messageId: String,
        olderMessagesOffset: Int,
        newerMessagesOffset: Int,
    ): Call<Message> = chatDomainStateFlowImpl.loadMessageById(cid, messageId, olderMessagesOffset, newerMessagesOffset)

    override fun queryChannelsLoadMore(
        filter: FilterObject,
        sort: QuerySort<Channel>,
        limit: Int,
        messageLimit: Int,
    ): Call<List<Channel>> = chatDomainStateFlowImpl.queryChannelsLoadMore(filter, sort, limit, messageLimit)

    override fun queryChannelsLoadMore(
        filter: FilterObject,
        sort: QuerySort<Channel>,
        messageLimit: Int,
    ): Call<List<Channel>> = chatDomainStateFlowImpl.queryChannelsLoadMore(filter, sort, messageLimit)

    override fun queryChannelsLoadMore(
        filter: FilterObject,
        sort: QuerySort<Channel>,
    ): Call<List<Channel>> = chatDomainStateFlowImpl.queryChannelsLoadMore(filter, sort)

    override fun threadLoadMore(cid: String, parentId: String, messageLimit: Int): Call<List<Message>> =
        chatDomainStateFlowImpl.threadLoadMore(cid, parentId, messageLimit)

    override fun createChannel(channel: Channel): Call<Channel> = chatDomainStateFlowImpl.createChannel(channel)

    override fun sendMessage(message: Message): Call<Message> = chatDomainStateFlowImpl.sendMessage(message)

    override fun sendMessage(
        message: Message,
        attachmentTransformer: ((at: Attachment, file: File) -> Attachment)?,
    ): Call<Message> = chatDomainStateFlowImpl.sendMessage(message, attachmentTransformer)

    override fun cancelMessage(message: Message): Call<Boolean> = chatDomainStateFlowImpl.cancelMessage(message)

    override fun shuffleGiphy(message: Message): Call<Message> = chatDomainStateFlowImpl.shuffleGiphy(message)

    override fun sendGiphy(message: Message): Call<Message> = chatDomainStateFlowImpl.sendGiphy(message)

    override fun editMessage(message: Message): Call<Message> = chatDomainStateFlowImpl.editMessage(message)

    override fun deleteMessage(message: Message): Call<Message> = chatDomainStateFlowImpl.deleteMessage(message)

    override fun sendReaction(cid: String, reaction: Reaction, enforceUnique: Boolean): Call<Reaction> =
        chatDomainStateFlowImpl.sendReaction(cid, reaction, enforceUnique)

    override fun deleteReaction(cid: String, reaction: Reaction): Call<Message> =
        chatDomainStateFlowImpl.deleteReaction(cid, reaction)

    override fun keystroke(cid: String, parentId: String?): Call<Boolean> =
        chatDomainStateFlowImpl.keystroke(cid, parentId)

    override fun stopTyping(cid: String, parentId: String?): Call<Boolean> =
        chatDomainStateFlowImpl.stopTyping(cid, parentId)

    override fun markRead(cid: String): Call<Boolean> = chatDomainStateFlowImpl.markRead(cid)

    override fun markAllRead(): Call<Boolean> = chatDomainStateFlowImpl.markAllRead()

    override fun hideChannel(cid: String, keepHistory: Boolean): Call<Unit> =
        chatDomainStateFlowImpl.hideChannel(cid, keepHistory)

    override fun showChannel(cid: String): Call<Unit> = chatDomainStateFlowImpl.showChannel(cid)

    override fun leaveChannel(cid: String): Call<Unit> = chatDomainStateFlowImpl.leaveChannel(cid)

    override fun deleteChannel(cid: String): Call<Unit> = chatDomainStateFlowImpl.deleteChannel(cid)

    override fun setMessageForReply(cid: String, message: Message?): Call<Unit> =
        chatDomainStateFlowImpl.setMessageForReply(cid, message)

    override fun downloadAttachment(attachment: Attachment): Call<Unit> =
        chatDomainStateFlowImpl.downloadAttachment(attachment)

    override fun searchUsersByName(
        querySearch: String,
        offset: Int,
        userLimit: Int,
        userPresence: Boolean,
    ): Call<List<User>> = chatDomainStateFlowImpl.searchUsersByName(querySearch, offset, userLimit, userPresence)

    override fun queryMembers(
        cid: String,
        offset: Int,
        limit: Int,
        filter: FilterObject,
        sort: QuerySort<Member>,
        members: List<Member>,
    ): Call<List<Member>> = chatDomainStateFlowImpl.queryMembers(cid, offset, limit, filter, sort, members)

    override fun createDistinctChannel(
        channelType: String,
        members: List<String>,
        extraData: Map<String, Any>,
    ): Call<Channel> = chatDomainStateFlowImpl.createDistinctChannel(channelType, members, extraData)
    // end region
}
