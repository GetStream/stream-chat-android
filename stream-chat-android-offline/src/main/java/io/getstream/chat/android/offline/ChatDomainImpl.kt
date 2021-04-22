package io.getstream.chat.android.offline

import android.content.Context
import android.os.Build
import android.os.Handler
import androidx.annotation.VisibleForTesting
import io.getstream.chat.android.client.BuildConfig.STREAM_CHAT_VERSION
import io.getstream.chat.android.client.ChatClient
import io.getstream.chat.android.client.api.models.FilterObject
import io.getstream.chat.android.client.api.models.QueryChannelsRequest
import io.getstream.chat.android.client.api.models.QuerySort
import io.getstream.chat.android.client.call.Call
import io.getstream.chat.android.client.call.CoroutineCall
import io.getstream.chat.android.client.call.await
import io.getstream.chat.android.client.errors.ChatError
import io.getstream.chat.android.client.events.ChatEvent
import io.getstream.chat.android.client.events.ConnectedEvent
import io.getstream.chat.android.client.events.MarkAllReadEvent
import io.getstream.chat.android.client.extensions.enrichWithCid
import io.getstream.chat.android.client.logger.ChatLogger
import io.getstream.chat.android.client.models.Attachment
import io.getstream.chat.android.client.models.Channel
import io.getstream.chat.android.client.models.Config
import io.getstream.chat.android.client.models.Filters.`in`
import io.getstream.chat.android.client.models.Member
import io.getstream.chat.android.client.models.Message
import io.getstream.chat.android.client.models.Mute
import io.getstream.chat.android.client.models.Reaction
import io.getstream.chat.android.client.models.TypingEvent
import io.getstream.chat.android.client.models.User
import io.getstream.chat.android.client.models.UserEntity
import io.getstream.chat.android.client.parser.StreamGson
import io.getstream.chat.android.client.socket.SocketListener
import io.getstream.chat.android.client.utils.Result
import io.getstream.chat.android.client.utils.SyncStatus
import io.getstream.chat.android.client.utils.observable.Disposable
import io.getstream.chat.android.core.internal.coroutines.DispatcherProvider
import io.getstream.chat.android.livedata.BuildConfig
import io.getstream.chat.android.livedata.service.sync.BackgroundSyncConfig
import io.getstream.chat.android.livedata.service.sync.SyncProvider
import io.getstream.chat.android.offline.channel.ChannelController
import io.getstream.chat.android.offline.event.EventHandlerImpl
import io.getstream.chat.android.offline.extensions.applyPagination
import io.getstream.chat.android.offline.extensions.isPermanent
import io.getstream.chat.android.offline.extensions.users
import io.getstream.chat.android.offline.model.ChannelConfig
import io.getstream.chat.android.offline.model.SyncState
import io.getstream.chat.android.offline.querychannels.QueryChannelsController
import io.getstream.chat.android.offline.repository.RepositoryFacade
import io.getstream.chat.android.offline.repository.builder.RepositoryFacadeBuilder
import io.getstream.chat.android.offline.repository.database.ChatDatabase
import io.getstream.chat.android.offline.request.AnyChannelPaginationRequest
import io.getstream.chat.android.offline.request.QueryChannelPaginationRequest
import io.getstream.chat.android.offline.request.QueryChannelsPaginationRequest
import io.getstream.chat.android.offline.request.toAnyChannelPaginationRequest
import io.getstream.chat.android.offline.thread.ThreadController
import io.getstream.chat.android.offline.usecase.CancelMessage
import io.getstream.chat.android.offline.usecase.CreateChannel
import io.getstream.chat.android.offline.usecase.DeleteChannel
import io.getstream.chat.android.offline.usecase.DeleteMessage
import io.getstream.chat.android.offline.usecase.DeleteReaction
import io.getstream.chat.android.offline.usecase.DownloadAttachment
import io.getstream.chat.android.offline.usecase.EditMessage
import io.getstream.chat.android.offline.usecase.GetChannelController
import io.getstream.chat.android.offline.usecase.GetThread
import io.getstream.chat.android.offline.usecase.HideChannel
import io.getstream.chat.android.offline.usecase.Keystroke
import io.getstream.chat.android.offline.usecase.LeaveChannel
import io.getstream.chat.android.offline.usecase.LoadMessageById
import io.getstream.chat.android.offline.usecase.LoadNewerMessages
import io.getstream.chat.android.offline.usecase.LoadOlderMessages
import io.getstream.chat.android.offline.usecase.MarkAllRead
import io.getstream.chat.android.offline.usecase.MarkRead
import io.getstream.chat.android.offline.usecase.QueryChannels
import io.getstream.chat.android.offline.usecase.QueryChannelsLoadMore
import io.getstream.chat.android.offline.usecase.QueryMembers
import io.getstream.chat.android.offline.usecase.ReplayEventsForActiveChannels
import io.getstream.chat.android.offline.usecase.SearchUsersByName
import io.getstream.chat.android.offline.usecase.SendGiphy
import io.getstream.chat.android.offline.usecase.SendMessage
import io.getstream.chat.android.offline.usecase.SendReaction
import io.getstream.chat.android.offline.usecase.SetMessageForReply
import io.getstream.chat.android.offline.usecase.ShowChannel
import io.getstream.chat.android.offline.usecase.ShuffleGiphy
import io.getstream.chat.android.offline.usecase.StopTyping
import io.getstream.chat.android.offline.usecase.ThreadLoadMore
import io.getstream.chat.android.offline.usecase.WatchChannel
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Deferred
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.async
import kotlinx.coroutines.cancelChildren
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.emitAll
import kotlinx.coroutines.flow.filterNotNull
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import java.io.File
import java.util.Date
import java.util.InputMismatchException
import java.util.UUID
import java.util.concurrent.ConcurrentHashMap

private val CHANNEL_CID_REGEX = Regex("^!?[\\w-]+:!?[\\w-]+$")
private const val MESSAGE_LIMIT = 30
private const val MEMBER_LIMIT = 30
private const val INITIAL_CHANNEL_OFFSET = 0
private const val CHANNEL_LIMIT = 30

internal val gson = StreamGson.gson

/**
 * The Chat Domain exposes StateFlow objects to make it easier to build your chat UI.
 * It intercepts the various low level events to ensure data stays in sync.
 * Offline storage is handled using Room
 *
 * A different Room database is used for different users. That's why it's mandatory to specify the user id when
 * initializing the ChatRepository
 *
 * chatDomain.channel(type, id) returns a controller object with channel specific state flow objects
 * chatDomain.queryChannels(query) returns a state flow object for the specific queryChannels query
 *
 * chatDomain.online state flow object indicates if you're online or not
 * chatDomain.totalUnreadCount state flow object returns the current unread count for this user
 * chatDomain.muted the list of muted users
 * chatDomain.banned if the current user is banned or not
 * chatDomain.channelUnreadCount state flow object returns the number of unread channels for this user
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
) : ChatDomain {
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

    internal val job = SupervisorJob()
    internal var scope = CoroutineScope(job + DispatcherProvider.IO)
    @VisibleForTesting
    val defaultConfig: Config = Config(isConnectEvents = true, isMutes = true)
    internal var repos: RepositoryFacade = createNoOpRepos()

    private val _initialized = MutableStateFlow(false)
    private val _online = MutableStateFlow(false)
    private val _totalUnreadCount = MutableStateFlow(0)
    private val _channelUnreadCount = MutableStateFlow(0)
    private val _errorEvent = MutableStateFlow<Event<ChatError>?>(null)
    private val _banned = MutableStateFlow(false)

    private val _mutedUsers = MutableStateFlow<List<Mute>>(emptyList())
    private val _typingChannels = MutableStateFlow<TypingEvent>(TypingEvent("", emptyList()))

    override lateinit var currentUser: User

    private val syncModule by lazy { SyncProvider(appContext) }

    /** if the client connection has been initialized */
    override val initialized: StateFlow<Boolean> = _initialized

    /**
     * StateFlow<Boolean> that indicates if we are currently online
     */
    override val online: StateFlow<Boolean> = _online

    /**
     * The total unread message count for the current user.
     * Depending on your app you'll want to show this or the channelUnreadCount
     */
    override val totalUnreadCount: StateFlow<Int> = _totalUnreadCount

    /**
     * the number of unread channels for the current user
     */
    override val channelUnreadCount: StateFlow<Int> = _channelUnreadCount

    /**
     * list of users that you've muted
     */
    override val muted: StateFlow<List<Mute>> = _mutedUsers

    /**
     * if the current user is banned or not
     */
    override val banned: StateFlow<Boolean> = _banned

    /**
     * The error event state flow object is triggered when errors in the underlying components occur.
     */
    override val errorEvents: StateFlow<Event<ChatError>> =
        _errorEvent.filterNotNull().stateIn(scope, SharingStarted.Eagerly, Event(ChatError()))

    /** the event subscription */
    private var eventSubscription: Disposable = EMPTY_DISPOSABLE

    /** stores the mapping from cid to ChannelController */
    private val activeChannelMapImpl: ConcurrentHashMap<String, ChannelController> = ConcurrentHashMap()

    override val typingUpdates: StateFlow<TypingEvent> = _typingChannels

    private val activeQueryMapImpl: ConcurrentHashMap<String, QueryChannelsController> = ConcurrentHashMap()

    @VisibleForTesting
    internal var eventHandler: EventHandlerImpl = EventHandlerImpl(this)
    private var logger = ChatLogger.get("Domain")

    private val cleanTask = object : Runnable {
        override fun run() {
            clean()
            mainHandler.postDelayed(this, 1000)
        }
    }
    private val syncStateFlow: MutableStateFlow<SyncState?> = MutableStateFlow(null)
    internal var initJob: Deferred<SyncState?>? = null

    /** The retry policy for retrying failed requests */
    override var retryPolicy: RetryPolicy = DefaultRetryPolicy()

    private fun clearState() {
        _initialized.value = false
        _online.value = false
        _totalUnreadCount.value = 0
        _channelUnreadCount.value = 0
        _banned.value = false
        _mutedUsers.value = emptyList()
        activeChannelMapImpl.clear()
        activeQueryMapImpl.clear()
    }

    private fun isTestRunner(): Boolean {
        return Build.FINGERPRINT.toLowerCase().contains("robolectric")
    }

    internal fun setUser(user: User) {
        clearState()

        currentUser = user

        repos = RepositoryFacadeBuilder {
            context(appContext)
            database(db)
            currentUser(currentUser)
            scope(scope)
            defaultConfig(defaultConfig)
            setOfflineEnabled(offlineEnabled)
        }
            .build()

        // load channel configs from Room into memory
        initJob = scope.async {
            // fetch the configs for channels
            repos.cacheChannelConfigs()

            // load the current user from the db
            val syncState = repos.selectSyncState(currentUser.id) ?: SyncState(currentUser.id)
            // set active channels and recover
            // restore channels
            syncState.activeChannelIds.forEach(::channel)
            // restore queries
            repos.selectQueriesChannelsByIds(syncState.activeQueryIds)
                .forEach { spec -> queryChannels(spec.filter, spec.sort) }

            // retrieve the last time the user marked all as read and handle it as an event
            syncState.markedAllReadAt
                ?.let { MarkAllReadEvent(user = currentUser, createdAt = it) }
                ?.let { eventHandler.handleEvent(it) }

            syncState.also { syncStateFlow.value = it }
        }

        if (client.isSocketConnected()) {
            setOnline()
        }
        startListening()
        initClean()
    }

    init {
        logger.logI("Initializing ChatDomain with version " + getVersion())

        // if the user is already defined, just call setUser ourselves
        val current = userOverwrite ?: client.getCurrentUser()
        if (current != null) {
            setUser(current)
        }
        // past behaviour was to set the user on the chat domain
        // the new syntax is to automatically pick up changes from the client
        if (userOverwrite == null) {
            // listen to future user changes
            client.preSetUserListeners.add {
                setUser(it)
            }
        }
        // disconnect if the low level client disconnects
        client.disconnectListeners.add {
            scope.launch {
                disconnect()
            }
        }
        storeBgSyncDataWhenUserConnects()
    }

    internal suspend fun updateCurrentUser(me: User) {
        if (me.id != currentUser.id) {
            throw InputMismatchException("received connect event for user with id ${me.id} while chat domain is configured for user with id ${currentUser.id}. create a new chatdomain when connecting a different user.")
        }
        currentUser = me
        repos.insertCurrentUser(me)
        _mutedUsers.value = me.mutes
        setTotalUnreadCount(me.totalUnreadCount)
        setChannelUnreadCount(me.unreadChannels)

        setBanned(me.banned)
    }

    internal suspend fun storeSyncState(): SyncState? {
        syncStateFlow.value?.let { _syncState ->
            val newSyncState = _syncState.copy(
                activeChannelIds = activeChannelMapImpl.keys().toList(),
                activeQueryIds = activeQueryMapImpl.values.map { it.queryChannelsSpec.id }
            )
            repos.insertSyncState(newSyncState)
            syncStateFlow.value = newSyncState
        }

        return syncStateFlow.value
    }

    override suspend fun disconnect() {
        storeSyncState()
        job.cancelChildren()
        stopListening()
        stopClean()
        clearState()
    }

    override fun getVersion(): String {
        return STREAM_CHAT_VERSION + "-" + BuildConfig.BUILD_TYPE
    }

    private fun stopClean() {
        mainHandler.removeCallbacks(cleanTask)
    }

    private fun initClean() {
        mainHandler.postDelayed(cleanTask, 5000)
    }

    suspend fun <T : Any> runAndRetry(runnable: () -> Call<T>): Result<T> {
        var attempt = 1
        var result: Result<T>

        while (true) {
            result = runnable().execute()
            if (result.isSuccess || result.error().isPermanent()) {
                break
            } else {
                // retry logic
                val shouldRetry = retryPolicy.shouldRetry(client, attempt, result.error())
                val timeout = retryPolicy.retryTimeout(client, attempt, result.error())

                if (shouldRetry) {
                    // temporary failure, continue
                    logger.logI("API call failed (attempt $attempt), retrying in $timeout seconds. Error was ${result.error()}")
                    delay(timeout.toLong())
                    attempt += 1
                } else {
                    logger.logI("API call failed (attempt $attempt). Giving up for now, will retry when connection recovers. Error was ${result.error()}")
                    break
                }
            }
        }
        // permanent failure case return
        return result
    }

    internal suspend fun createNewChannel(c: Channel): Result<Channel> =
        try {
            val online = isOnline()
            c.createdAt = c.createdAt ?: Date()
            c.syncStatus = if (online) {
                SyncStatus.IN_PROGRESS
            } else {
                SyncStatus.SYNC_NEEDED
            }
            if (c.createdBy != currentUser) {
                c.createdBy = currentUser
            }

            val channelController = channel(c.cid)
            channelController.updateDataFromChannel(c)

            // Update Room State
            repos.insertChannel(c)

            // Add to query controllers
            for (query in activeQueryMapImpl.values) {
                query.addChannelIfFilterMatches(c)
            }

            // make the API call and follow retry policy
            if (online) {
                val runnable = {
                    val members = c.members.map { it.getUserId() }
                    client.createChannel(c.type, c.id, members, c.extraData)
                }
                val result = runAndRetry(runnable)
                if (result.isSuccess) {
                    c.syncStatus = SyncStatus.COMPLETED
                    repos.insertChannel(c)
                    Result(result.data())
                } else {
                    if (result.error().isPermanent()) {
                        c.syncStatus = SyncStatus.FAILED_PERMANENTLY
                    } else {
                        c.syncStatus = SyncStatus.SYNC_NEEDED
                    }
                    repos.insertChannel(c)
                    Result(result.error())
                }
            } else {
                Result(c)
            }
        } catch (e: IllegalStateException) {
            Result(ChatError(cause = e))
        }

    fun addError(error: ChatError) {
        _errorEvent.value = Event(error)
    }

    fun isActiveChannel(cid: String): Boolean {
        return activeChannelMapImpl.containsKey(cid)
    }

    fun setChannelUnreadCount(newCount: Int) {
        _channelUnreadCount.value = newCount
    }

    fun setBanned(newBanned: Boolean) {
        _banned.value = newBanned
    }

    fun setTotalUnreadCount(newCount: Int) {
        _totalUnreadCount.value = newCount
    }

    override fun removeMembers(cid: String, vararg userIds: String): Call<Channel> {
        validateCid(cid)
        val channelController = channel(cid)
        return CoroutineCall(scope) {
            channelController.removeMembers(*userIds)
        }
    }

    private fun storeBgSyncDataWhenUserConnects() {
        client.addSocketListener(
            object : SocketListener() {
                override fun onConnected(event: ConnectedEvent) {
                    storeBgSyncData()
                    client.removeSocketListener(this)
                }
            }
        )
    }

    private fun storeBgSyncData() {
        if (backgroundSyncEnabled && !isTestRunner()) {
            val config = BackgroundSyncConfig(client.config.apiKey, currentUser.id, client.getCurrentToken() ?: "")
            if (config.isValid()) {
                syncModule.encryptedBackgroundSyncConfigStore.apply {
                    put(config)
                }
            }
        }
    }

    /**
     * Start listening to chat events and keep the room database in sync
     */
    private fun startListening() {
        if (eventSubscription.isDisposed) {
            eventSubscription = client.subscribe {
                eventHandler.handleEvents(listOf(it))
            }
        }
    }

    /**
     * Stop listening to chat events
     */
    private fun stopListening() {
        eventSubscription.dispose()
    }

    internal fun channel(c: Channel): ChannelController {
        return channel(c.type, c.id)
    }

    internal fun channel(cid: String): ChannelController {
        if (!CHANNEL_CID_REGEX.matches(cid)) {
            throw IllegalArgumentException("Received invalid cid, expected format messaging:123, got $cid")
        }
        val parts = cid.split(":")
        return channel(parts[0], parts[1])
    }

    internal fun channel(
        channelType: String,
        channelId: String,
    ): ChannelController {
        val cid = "%s:%s".format(channelType, channelId)
        if (!activeChannelMapImpl.containsKey(cid)) {
            val channelController =
                ChannelController(
                    channelType,
                    channelId,
                    client,
                    this
                )
            activeChannelMapImpl[cid] = channelController
            addTypingChannel(channelController)
        }
        return activeChannelMapImpl.getValue(cid)
    }

    internal fun allActiveChannels(): List<ChannelController> =
        activeChannelMapImpl.values.toList()

    fun generateMessageId(): String {
        return currentUser.id + "-" + UUID.randomUUID().toString()
    }

    private fun addTypingChannel(channelController: ChannelController) {
        scope.launch { _typingChannels.emitAll(channelController.typing) }
    }

    internal fun setOffline() {
        _online.value = false
    }

    internal fun setOnline() {
        _online.value = true
    }

    internal fun setInitialized() {
        _initialized.value = true
    }

    override fun isOnline(): Boolean = _online.value

    override fun isOffline(): Boolean = !_online.value

    override fun isInitialized(): Boolean {
        return _initialized.value
    }

    override fun getActiveQueries(): List<QueryChannelsController> {
        return activeQueryMapImpl.values.toList()
    }

    /**
     * queryChannels
     * - first read the current results from Room
     * - if we are online make the API call to update results
     */
    fun queryChannels(
        filter: FilterObject,
        sort: QuerySort<Channel>,
    ): QueryChannelsController =
        activeQueryMapImpl.getOrPut("${filter.hashCode()}-${sort.hashCode()}") {
            QueryChannelsController(
                filter,
                sort,
                client,
                this
            )
        }

    private fun queryEvents(cids: List<String>): Result<List<ChatEvent>> =
        client.getSyncHistory(cids, syncStateFlow.value?.lastSyncedAt ?: Date()).execute()

    /**
     * replay events for all active channels
     * ensures that the cid you provide is active
     *
     * @param cid ensures that the channel with this id is active
     */
    internal suspend fun replayEvents(cid: String? = null): Result<List<ChatEvent>> {
        // wait for the active channel info to load
        initJob?.join()
        // make a list of all channel ids
        val cids = activeChannelMapImpl.keys().toList().toMutableList()
        cid?.let {
            channel(it)
            cids.add(it)
        }

        return replayEventsForChannels(cids)
    }

    private suspend fun replayEventsForChannels(cids: List<String>): Result<List<ChatEvent>> {
        val now = Date()

        return if (cids.isNotEmpty()) {
            queryEvents(cids).also { resultChatEvent ->
                if (resultChatEvent.isSuccess) {
                    eventHandler.updateOfflineStorageFromEvents(resultChatEvent.data())
                    syncStateFlow.value?.let { syncStateFlow.value = it.copy(lastSyncedAt = now) }
                }
            }
        } else {
            Result(emptyList())
        }
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
    suspend fun connectionRecovered(recoverAll: Boolean = false) {
        // 0 ensure load is complete
        initJob?.join()

        // 1 update the results for queries that are actively being shown right now
        val updatedChannelIds = mutableSetOf<String>()
        val queriesToRetry = activeQueryMapImpl.values
            .toList()
            .filter { it.recoveryNeeded || recoverAll }
            .take(3)
        for (queryChannelController in queriesToRetry) {
            val pagination = QueryChannelsPaginationRequest(
                QuerySort<Channel>(),
                INITIAL_CHANNEL_OFFSET,
                CHANNEL_LIMIT,
                MESSAGE_LIMIT,
                MEMBER_LIMIT
            )
            val response = queryChannelController.runQueryOnline(pagination)
            if (response.isSuccess) {
                queryChannelController.updateChannelsAndQueryResults(response.data(), pagination.isFirstPage)
                updatedChannelIds.addAll(response.data().map { it.cid })
            }
        }
        // 2 update the data for all channels that are being show right now...
        // exclude ones we just updated
        val cids: List<String> = activeChannelMapImpl
            .entries
            .asSequence()
            .filter { it.value.recoveryNeeded || recoverAll }
            .filterNot { updatedChannelIds.contains(it.key) }
            .take(30)
            .map { it.key }
            .toList()

        val online = isOnline()
        logger.logI("recovery called: recoverAll: $recoverAll, online: $online retrying ${queriesToRetry.size} queries and ${cids.size} channels")

        var missingChannelIds = listOf<String>()
        if (cids.isNotEmpty() && online) {
            val filter = `in`("cid", cids)
            val request = QueryChannelsRequest(filter, 0, 30)
            val result = client.queryChannels(request).execute()
            if (result.isSuccess) {
                val channels = result.data()
                val foundChannelIds = channels.map { it.id }
                for (c in channels) {
                    val channelController = this.channel(c)
                    channelController.updateDataFromChannel(c)
                }
                missingChannelIds = cids.filterNot { foundChannelIds.contains(it) }
                storeStateForChannels(channels)
            }
            // create channels that are not present on the API
            for (c in missingChannelIds) {
                val channelController = this.channel(c)
                channelController.watch()
            }
            // 3 recover events
            replayEventsForChannels(cids)
        }

        // 4 retry any failed requests
        if (online) {
            retryFailedEntities()
        }
    }

    internal suspend fun retryFailedEntities() {
        delay(1000)
        // retry channels, messages and reactions in that order..
        val channels = retryChannels()
        val messages = retryMessages()
        val reactions = retryReactions()
        logger.logI("Retried ${channels.size} channel entities, ${messages.size} messages and ${reactions.size} reaction entities")
    }

    @VisibleForTesting
    internal suspend fun retryChannels(): List<Channel> {
        return repos.selectChannelsSyncNeeded().onEach { channel ->
            val result = client.createChannel(
                channel.type,
                channel.id,
                channel.members.map(UserEntity::getUserId),
                channel.extraData
            ).await()

            when {
                result.isSuccess -> {
                    channel.syncStatus = SyncStatus.COMPLETED
                    repos.insertChannel(channel)
                }
                result.isError && result.error().isPermanent() -> {
                    channel.syncStatus = SyncStatus.FAILED_PERMANENTLY
                    repos.insertChannel(channel)
                }
            }
        }
    }

    @VisibleForTesting
    internal suspend fun retryMessages(): List<Message> {
        val messages = repos.selectMessagesSyncNeeded()
        for (message in messages) {
            val channelClient = client.channel(message.cid)
            // support sending, deleting and editing messages here
            val result = when {
                message.deletedAt != null -> channelClient.deleteMessage(message.id).execute()
                message.updatedAt != null || message.updatedLocallyAt != null -> {
                    client.updateMessage(message).execute()
                }
                else -> channelClient.sendMessage(message).execute()
            }

            if (result.isSuccess) {
                // TODO: 1.1 image upload support
                repos.insertMessage(message.copy(syncStatus = SyncStatus.COMPLETED))
            } else if (result.isError && result.error().isPermanent()) {
                repos.insertMessage(message.copy(syncStatus = SyncStatus.FAILED_PERMANENTLY))
            }
        }

        return messages
    }

    @VisibleForTesting
    internal suspend fun retryReactions(): List<Reaction> {
        return repos.selectReactionsSyncNeeded().onEach { reaction ->
            val result = if (reaction.deletedAt != null) {
                client.deleteReaction(reaction.messageId, reaction.type).execute()
            } else {
                client.sendReaction(reaction, reaction.enforceUnique).execute()
            }

            if (result.isSuccess) {
                reaction.syncStatus = SyncStatus.COMPLETED
                repos.insertReaction(reaction)
            } else if (result.error().isPermanent()) {
                reaction.syncStatus = SyncStatus.FAILED_PERMANENTLY
                repos.insertReaction(reaction)
            }
        }
    }

    suspend fun storeStateForChannel(channel: Channel) {
        return storeStateForChannels(listOf(channel))
    }

    suspend fun storeStateForChannels(channelsResponse: Collection<Channel>) {
        val users = mutableMapOf<String, User>()
        val configs: MutableCollection<ChannelConfig> = mutableSetOf()
        // start by gathering all the users
        val messages = mutableListOf<Message>()
        for (channel in channelsResponse) {

            users.putAll(channel.users().associateBy { it.id })
            configs += ChannelConfig(channel.type, channel.config)

            channel.messages.forEach { message ->
                message.enrichWithCid(channel.cid)
                users.putAll(message.users().associateBy { it.id })
            }

            messages.addAll(channel.messages)
        }

        repos.storeStateForChannels(
            configs = configs,
            users = users.values.toList(),
            channels = channelsResponse,
            messages = messages
        )

        logger.logI("storeStateForChannels stored ${channelsResponse.size} channels, ${configs.size} configs, ${users.size} users and ${messages.size} messages")
    }

    suspend fun selectAndEnrichChannel(
        channelId: String,
        pagination: QueryChannelPaginationRequest,
    ): Channel? = selectAndEnrichChannels(listOf(channelId), pagination.toAnyChannelPaginationRequest()).getOrNull(0)

    suspend fun selectAndEnrichChannel(
        channelId: String,
        pagination: QueryChannelsPaginationRequest,
    ): Channel? = selectAndEnrichChannels(listOf(channelId), pagination.toAnyChannelPaginationRequest()).getOrNull(0)

    suspend fun selectAndEnrichChannels(
        channelIds: List<String>,
        pagination: QueryChannelsPaginationRequest,
    ): List<Channel> = selectAndEnrichChannels(channelIds, pagination.toAnyChannelPaginationRequest())

    private suspend fun selectAndEnrichChannels(
        channelIds: List<String>,
        pagination: AnyChannelPaginationRequest,
    ): List<Channel> = repos.selectChannels(channelIds, pagination).applyPagination(pagination)

    override fun clean() {
        for (channelController in activeChannelMapImpl.values.toList()) {
            channelController.clean()
        }
    }

    override fun getChannelConfig(channelType: String): Config =
        repos.selectChannelConfig(channelType)?.config ?: defaultConfig

    // region use-case functions
    override fun replayEventsForActiveChannels(cid: String): Call<List<ChatEvent>> =
        ReplayEventsForActiveChannels(this).invoke(cid)

    override fun getChannelController(cid: String): Call<ChannelController> = GetChannelController(this).invoke(cid)

    override fun watchChannel(cid: String, messageLimit: Int): Call<ChannelController> =
        WatchChannel(this).invoke(cid, messageLimit)

    override fun queryChannels(
        filter: FilterObject,
        sort: QuerySort<Channel>,
        limit: Int,
        messageLimit: Int,
    ): Call<QueryChannelsController> = QueryChannels(this).invoke(filter, sort, limit, messageLimit)

    override fun getThread(cid: String, parentId: String): Call<ThreadController> =
        GetThread(this).invoke(cid, parentId)

    override fun loadOlderMessages(cid: String, messageLimit: Int): Call<Channel> =
        LoadOlderMessages(this).invoke(cid, messageLimit)

    override fun loadNewerMessages(cid: String, messageLimit: Int): Call<Channel> =
        LoadNewerMessages(this).invoke(cid, messageLimit)

    override fun loadMessageById(
        cid: String,
        messageId: String,
        olderMessagesOffset: Int,
        newerMessagesOffset: Int,
    ): Call<Message> = LoadMessageById(this).invoke(cid, messageId, olderMessagesOffset, newerMessagesOffset)

    override fun queryChannelsLoadMore(
        filter: FilterObject,
        sort: QuerySort<Channel>,
        limit: Int,
        messageLimit: Int,
    ): Call<List<Channel>> = QueryChannelsLoadMore(this).invoke(filter, sort, limit, messageLimit)

    override fun queryChannelsLoadMore(
        filter: FilterObject,
        sort: QuerySort<Channel>,
        messageLimit: Int,
    ): Call<List<Channel>> = QueryChannelsLoadMore(this).invoke(filter, sort, messageLimit)

    override fun queryChannelsLoadMore(
        filter: FilterObject,
        sort: QuerySort<Channel>,
    ): Call<List<Channel>> = QueryChannelsLoadMore(this).invoke(filter, sort)

    override fun threadLoadMore(cid: String, parentId: String, messageLimit: Int): Call<List<Message>> =
        ThreadLoadMore(this).invoke(cid, parentId, messageLimit)

    override fun createChannel(channel: Channel): Call<Channel> = CreateChannel(this).invoke(channel)

    override fun sendMessage(message: Message): Call<Message> = SendMessage(this).invoke(message)

    override fun sendMessage(
        message: Message,
        attachmentTransformer: ((at: Attachment, file: File) -> Attachment)?,
    ): Call<Message> = SendMessage(this).invoke(message, attachmentTransformer)

    override fun cancelMessage(message: Message): Call<Boolean> = CancelMessage(this).invoke(message)

    override fun shuffleGiphy(message: Message): Call<Message> = ShuffleGiphy(this).invoke(message)

    override fun sendGiphy(message: Message): Call<Message> = SendGiphy(this).invoke(message)

    override fun editMessage(message: Message): Call<Message> = EditMessage(this).invoke(message)

    override fun deleteMessage(message: Message): Call<Message> = DeleteMessage(this).invoke(message)

    override fun sendReaction(cid: String, reaction: Reaction, enforceUnique: Boolean): Call<Reaction> =
        SendReaction(this).invoke(cid, reaction, enforceUnique)

    override fun deleteReaction(cid: String, reaction: Reaction): Call<Message> =
        DeleteReaction(this).invoke(cid, reaction)

    override fun keystroke(cid: String, parentId: String?): Call<Boolean> =
        Keystroke(this).invoke(cid, parentId)

    override fun stopTyping(cid: String, parentId: String?): Call<Boolean> =
        StopTyping(this).invoke(cid, parentId)

    override fun markRead(cid: String): Call<Boolean> = MarkRead(this).invoke(cid)

    override fun markAllRead(): Call<Boolean> = MarkAllRead(this).invoke()

    override fun hideChannel(cid: String, keepHistory: Boolean): Call<Unit> =
        HideChannel(this).invoke(cid, keepHistory)

    override fun showChannel(cid: String): Call<Unit> = ShowChannel(this).invoke(cid)

    override fun leaveChannel(cid: String): Call<Unit> = LeaveChannel(this).invoke(cid)

    override fun deleteChannel(cid: String): Call<Unit> = DeleteChannel(this).invoke(cid)

    override fun setMessageForReply(cid: String, message: Message?): Call<Unit> =
        SetMessageForReply(this).invoke(cid, message)

    override fun downloadAttachment(attachment: Attachment): Call<Unit> = DownloadAttachment(this).invoke(attachment)

    override fun searchUsersByName(
        querySearch: String,
        offset: Int,
        userLimit: Int,
        userPresence: Boolean,
    ): Call<List<User>> = SearchUsersByName(this).invoke(querySearch, offset, userLimit, userPresence)

    override fun queryMembers(
        cid: String,
        offset: Int,
        limit: Int,
        filter: FilterObject,
        sort: QuerySort<Member>,
        members: List<Member>,
    ): Call<List<Member>> = QueryMembers(this).invoke(cid, offset, limit, filter, sort, members)

    override fun createDistinctChannel(
        channelType: String,
        members: List<String>,
        extraData: Map<String, Any>,
    ): Call<Channel> {
        return CoroutineCall(scope) {
            client.createChannel(channelType, members, extraData).execute().also {
                if (it.isSuccess) {
                    repos.insertChannel(it.data())
                }
            }
        }
    }
    // end region

    private fun createNoOpRepos(): RepositoryFacade = RepositoryFacadeBuilder {
        context(appContext)
        scope(scope)
        defaultConfig(defaultConfig)
        setOfflineEnabled(false)
    }.build()

    companion object {
        val EMPTY_DISPOSABLE = object : Disposable {
            override val isDisposed: Boolean = true
            override fun dispose() {}
        }
    }
}
