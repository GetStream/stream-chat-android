package io.getstream.chat.android.livedata

import android.content.Context
import androidx.annotation.VisibleForTesting
import androidx.lifecycle.LiveData
import androidx.lifecycle.asLiveData
import io.getstream.chat.android.client.ChatClient
import io.getstream.chat.android.client.api.models.FilterObject
import io.getstream.chat.android.client.api.models.QuerySort
import io.getstream.chat.android.client.call.Call
import io.getstream.chat.android.client.errors.ChatError
import io.getstream.chat.android.client.events.ChatEvent
import io.getstream.chat.android.client.models.Channel
import io.getstream.chat.android.client.models.Config
import io.getstream.chat.android.client.models.Message
import io.getstream.chat.android.client.models.Mute
import io.getstream.chat.android.client.models.Reaction
import io.getstream.chat.android.client.models.TypingEvent
import io.getstream.chat.android.client.models.User
import io.getstream.chat.android.client.parser.StreamGson
import io.getstream.chat.android.client.utils.Result
import io.getstream.chat.android.core.internal.coroutines.DispatcherProvider
import io.getstream.chat.android.livedata.controller.ChannelControllerImpl
import io.getstream.chat.android.livedata.controller.QueryChannelsControllerImpl
import io.getstream.chat.android.livedata.model.SyncState
import io.getstream.chat.android.livedata.repository.RepositoryFacade
import io.getstream.chat.android.livedata.repository.database.ChatDatabase
import io.getstream.chat.android.livedata.request.QueryChannelPaginationRequest
import io.getstream.chat.android.livedata.request.QueryChannelsPaginationRequest
import io.getstream.chat.android.livedata.usecase.UseCaseHelper
import io.getstream.chat.android.livedata.utils.Event
import io.getstream.chat.android.livedata.utils.RetryPolicy
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Deferred
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.flow.filterNotNull
import io.getstream.chat.android.offline.ChatDomainImpl as NewChatDomainImpl

private val CHANNEL_CID_REGEX = Regex("^!?[\\w-]+:!?[\\w-]+$")

internal val gson = StreamGson.gson

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
internal class ChatDomainImpl internal constructor(private val delegate: NewChatDomainImpl) : ChatDomain {

    internal var client: ChatClient = delegate.client

    internal val appContext: Context
        get() = delegate.appContext

    override var offlineEnabled: Boolean
        get() = delegate.offlineEnabled
        set(value) {
            delegate.offlineEnabled = value
        }

    override var userPresence: Boolean
        get() = delegate.userPresence
        set(value) {
            delegate.userPresence = value
        }

    override var currentUser: User
        get() = delegate.currentUser
        set(value) {
            delegate.currentUser = value
        }

    val database: ChatDatabase
        get() = delegate.database

    /** a helper object which lists all the initialized use cases for the chat domain */
    override val useCases: UseCaseHelper = UseCaseHelper(this)

    @VisibleForTesting
    val defaultConfig: Config
        get() = delegate.defaultConfig

    /** if the client connection has been initialized */
    override val initialized: LiveData<Boolean> = delegate.initialized.asLiveData()

    /**
     * LiveData<Boolean> that indicates if we are currently online
     */
    override val online: LiveData<Boolean> = delegate.online.asLiveData()

    /**
     * The total unread message count for the current user.
     * Depending on your app you'll want to show this or the channelUnreadCount
     */
    override val totalUnreadCount: LiveData<Int> = delegate.totalUnreadCount.asLiveData()

    /**
     * the number of unread channels for the current user
     */
    override val channelUnreadCount: LiveData<Int> = delegate.channelUnreadCount.asLiveData()

    /**
     * list of users that you've muted
     */
    override val muted: LiveData<List<Mute>> = delegate.muted.asLiveData()

    /**
     * if the current user is banned or not
     */
    override val banned: LiveData<Boolean> = delegate.banned.asLiveData()

    /**
     * The error event livedata object is triggered when errors in the underlying components occur.
     * The following example shows how to observe these errors
     *
     *  channelController.errorEvent.observe(this) {
     *       // create a toast
     *   })
     *
     */
    override val errorEvents: LiveData<Event<ChatError>> = delegate.errorEvents.filterNotNull().asLiveData()

    override val typingUpdates: LiveData<TypingEvent> = delegate.typingUpdates.asLiveData()

    internal val eventHandler: EventHandlerImpl
        get() = delegate.eventHandler

    internal lateinit var repos: RepositoryFacade
    internal var initJob: Deferred<SyncState?>? = null

    /** The retry policy for retrying failed requests */
    override var retryPolicy: RetryPolicy
        get() = delegate.retryPolicy
        set(value) {
            delegate.retryPolicy = value
        }

    internal fun setUser(user: User) = delegate.setUser(user)

    internal val job = SupervisorJob()
    internal var scope = CoroutineScope(job + DispatcherProvider.IO)

    internal suspend fun updateCurrentUser(me: User) {
        delegate.updateCurrentUser(me)
    }

    internal suspend fun storeSyncState(): SyncState? {
        return delegate.storeSyncState()
    }

    override suspend fun disconnect() = delegate.disconnect()

    override fun getVersion(): String = delegate.getVersion()

    suspend fun <T : Any> runAndRetry(runnable: () -> Call<T>): Result<T> = delegate.runAndRetry(runnable)

    suspend fun createChannel(c: Channel): Result<Channel> = delegate.createChannel(c)

    fun addError(error: ChatError) = delegate.addError(error)

    fun isActiveChannel(cid: String): Boolean = delegate.isActiveChannel(cid)

    fun setChannelUnreadCount(newCount: Int) = delegate.setChannelUnreadCount(newCount)

    fun setBanned(newBanned: Boolean) = delegate.setBanned(newBanned)

    fun setTotalUnreadCount(newCount: Int) = delegate.setTotalUnreadCount(newCount)

    internal fun channel(c: Channel): ChannelControllerImpl = channel(c.type, c.id)

    internal fun channel(cid: String): ChannelControllerImpl {
        if (!CHANNEL_CID_REGEX.matches(cid)) {
            throw IllegalArgumentException("Received invalid cid, expected format messaging:123, got $cid")
        }
        val parts = cid.split(":")
        return channel(parts[0], parts[1])
    }

    internal fun channel(
        channelType: String,
        channelId: String,
    ): ChannelControllerImpl {
        return ChannelControllerImpl(delegate.channel(channelType, channelId))
    }

    internal fun allActiveChannels(): List<ChannelControllerImpl> {
        return delegate.allActiveChannels().map { ChannelControllerImpl(it) }
    }

    fun generateMessageId(): String = delegate.generateMessageId()

    internal fun setOffline() = delegate.setOffline()

    internal fun setOnline() = delegate.setOnline()

    internal fun setInitialized() = delegate.setInitialized()

    override fun isOnline(): Boolean = delegate.isOnline()

    override fun isOffline(): Boolean = delegate.isOffline()

    override fun isInitialized(): Boolean = delegate.isInitialized()

    override fun getActiveQueries(): List<QueryChannelsControllerImpl> {
        return delegate.getActiveQueries().map { QueryChannelsControllerImpl(it) }
    }

    /**
     * queryChannels
     * - first read the current results from Room
     * - if we are online make the API call to update results
     */
    fun queryChannels(
        filter: FilterObject,
        sort: QuerySort<Channel>,
    ): QueryChannelsControllerImpl {
        return QueryChannelsControllerImpl(delegate.queryChannels(filter, sort))
    }

    /**
     * replay events for all active channels
     * ensures that the cid you provide is active
     *
     * @param cid ensures that the channel with this id is active
     */
    suspend fun replayEventsForActiveChannels(cid: String? = null): Result<List<ChatEvent>> {
        return delegate.replayEventsForActiveChannels(cid)
    }

    internal suspend fun replayEventsForChannels(cids: List<String>): Result<List<ChatEvent>> {
        return delegate.replayEventsForChannels(cids)
    }

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
    suspend fun connectionRecovered(recoverAll: Boolean = false) = delegate.connectionRecovered(recoverAll)

    internal suspend fun retryFailedEntities() = delegate.retryFailedEntities()

    @VisibleForTesting
    internal suspend fun retryChannels(): List<Channel> = delegate.retryChannels()

    @VisibleForTesting
    internal suspend fun retryMessages(): List<Message> = delegate.retryMessages()

    @VisibleForTesting
    internal suspend fun retryReactions(): List<Reaction> = delegate.retryReactions()

    suspend fun storeStateForChannel(channel: Channel) = delegate.storeStateForChannel(channel)

    suspend fun storeStateForChannels(channelsResponse: Collection<Channel>) {
        return delegate.storeStateForChannels(channelsResponse)
    }

    suspend fun selectAndEnrichChannel(
        channelId: String,
        pagination: QueryChannelPaginationRequest,
    ): Channel? {
        return delegate.selectAndEnrichChannel(channelId, pagination)
    }

    suspend fun selectAndEnrichChannel(
        channelId: String,
        pagination: QueryChannelsPaginationRequest,
    ): Channel? {
        return delegate.selectAndEnrichChannel(channelId, pagination)
    }

    suspend fun selectAndEnrichChannels(
        channelIds: List<String>,
        pagination: QueryChannelsPaginationRequest,
    ): List<Channel> {
        return delegate.selectAndEnrichChannels(channelIds, pagination)
    }

    override fun clean() = delegate.clean()

    override fun getChannelConfig(channelType: String): Config = delegate.getChannelConfig(channelType)
}
