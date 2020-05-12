package io.getstream.chat.android.livedata

import android.content.Context
import android.os.Handler
import android.os.Looper
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.room.Room
import com.google.gson.Gson
import io.getstream.chat.android.client.BuildConfig
import io.getstream.chat.android.client.ChatClient
import io.getstream.chat.android.client.api.models.QueryChannelsRequest
import io.getstream.chat.android.client.api.models.QuerySort
import io.getstream.chat.android.client.api.models.WatchChannelRequest
import io.getstream.chat.android.client.call.Call
import io.getstream.chat.android.client.errors.ChatError
import io.getstream.chat.android.client.events.ChatEvent
import io.getstream.chat.android.client.logger.ChatLogger
import io.getstream.chat.android.client.models.*
import io.getstream.chat.android.client.models.Filters.`in`
import io.getstream.chat.android.client.utils.FilterObject
import io.getstream.chat.android.client.utils.Result
import io.getstream.chat.android.client.utils.SyncStatus
import io.getstream.chat.android.client.utils.observable.Subscription
import io.getstream.chat.android.livedata.controller.ChannelControllerImpl
import io.getstream.chat.android.livedata.controller.QueryChannelsControllerImpl
import io.getstream.chat.android.livedata.entity.*
import io.getstream.chat.android.livedata.repository.RepositoryHelper
import io.getstream.chat.android.livedata.request.AnyChannelPaginationRequest
import io.getstream.chat.android.livedata.request.QueryChannelPaginationRequest
import io.getstream.chat.android.livedata.request.QueryChannelsPaginationRequest
import io.getstream.chat.android.livedata.usecase.*
import io.getstream.chat.android.livedata.utils.DefaultRetryPolicy
import io.getstream.chat.android.livedata.utils.Event
import io.getstream.chat.android.livedata.utils.RetryPolicy
import java.lang.Thread.sleep
import java.util.*
import java.util.concurrent.ConcurrentHashMap
import kotlinx.coroutines.*

/**
 * The Chat Repository exposes livedata objects to make it easier to build your chat UI.
 * It intercepts the various low level events to ensure data stays in sync.
 * Offline storage is handled using Room
 *
 * A different Room database is used for different users. That's why it's mandatory to specify the user id when
 * initializing the ChatRepository
 *
 * repo.channel(type, id) returns a repo object with channel specific livedata object
 * repo.queryChannels(query) returns a livedata object for the specific queryChannels query
 *
 * repo.online livedata object indicates if you're online or not
 * repo.totalUnreadCount livedata object returns the current unread count for this user
 * repo.channelUnreadCount livedata object returns the number of unread channels for this user
 * repo.errorEvents events for errors that happen while interacting with the chat
 *
 */
class ChatDomainImpl private constructor(
    internal var context: Context,
    internal var client: ChatClient,
    override var currentUser: User,
    override var offlineEnabled: Boolean = false,
    internal var recoveryEnabled: Boolean = true,
    override var userPresence: Boolean = false
) :
    ChatDomain {
    private val _initialized = MutableLiveData<Boolean>(false)
    private val _online = MutableLiveData<Boolean>(false)
    private val _totalUnreadCount = MutableLiveData<Int>()
    private val _channelUnreadCount = MutableLiveData<Int>()
    private val _errorEvent = MutableLiveData<Event<ChatError>>()
    private val _banned = MutableLiveData<Boolean>(false)
    private val _mutedUsers = MutableLiveData<List<Mute>>()

    /** a helper object which lists all the initialized use cases for the chat domain */
    override var useCases: UseCaseHelper = UseCaseHelper(this)

    /** if the client connection has been initialized */
    override val initialized: LiveData<Boolean> = _initialized

    /**
     * LiveData<Boolean> that indicates if we are currently online
     */
    override val online: LiveData<Boolean> = _online

    /**
     * The total unread message count for the current user.
     * Depending on your app you'll want to show this or the channelUnreadCount
     */
    override val totalUnreadCount: LiveData<Int> = _totalUnreadCount

    /**
     * the number of unread channels for the current user
     */
    override val channelUnreadCount: LiveData<Int> = _channelUnreadCount

    /**
     * list of users that you've muted
     */
    override val mutedUsers: LiveData<List<Mute>> = _mutedUsers

    /**
     * if the current user is banned or not
     */
    override val banned: LiveData<Boolean> = _banned

    /**
     * The error event livedata object is triggered when errors in the underlying components occure.
     * The following example shows how to observe these errors
     *
     *  repo.errorEvent.observe(this, EventObserver {
     *       // create a toast
     *   })
     *
     */
    override val errorEvents: LiveData<Event<ChatError>> = _errorEvent

    // TODO 1.1: We should accelerate online/offline detection

    internal lateinit var eventHandler: EventHandlerImpl
    private lateinit var mainHandler: Handler

    private var logger = ChatLogger.get("Domain")
    private val cleanTask = object : Runnable {
        override fun run() {
            clean()
            mainHandler.postDelayed(this, 1000)
        }
    }

    internal val job = SupervisorJob()
    internal val scope = CoroutineScope(Dispatchers.IO + job)

    internal lateinit var repos: RepositoryHelper
    internal var syncState: SyncStateEntity? = null
    internal lateinit var initJob: Deferred<SyncStateEntity?>

    /** The retry policy for retrying failed requests */
    override var retryPolicy: RetryPolicy =
        DefaultRetryPolicy()

    internal constructor(context: Context, client: ChatClient, currentUser: User, offlineEnabled: Boolean = true, userPresence: Boolean = true, recoveryEnabled: Boolean = true, db: ChatDatabase? = null) : this(context, client, currentUser, offlineEnabled, userPresence, recoveryEnabled) {
        logger.logI("Initializing ChatDomain with version " + getVersion())

        val chatDatabase = db ?: createDatabase()
        repos = RepositoryHelper(client, currentUser, chatDatabase)

        // load channel configs from Room into memory
        initJob = scope.async(scope.coroutineContext) {
            // fetch the configs for channels
            repos.configs.load()

            val me = repos.users.selectMe()
            me?.let { updateCurrentUser(it) }
            // load the current user from the db
            val initialSyncState = SyncStateEntity(currentUser.id)
            syncState = repos.syncState.select(currentUser.id) ?: initialSyncState
            // set active channels and recover
            syncState?.let {
                for (channelId in it.activeChannelIds) {
                    channel(channelId)
                }
                // queries
                val queries = repos.queryChannels.select(it.activeQueryIds)
                for (queryEntity in queries) {
                    queryChannels(queryEntity.filter, queryEntity.sort)
                }
            }
            syncState
        }

        useCases = UseCaseHelper(this)

        // verify that you're not connecting 2 different users
        if (client.getCurrentUser() != null && client.getCurrentUser()?.id != currentUser.id) {
            throw IllegalArgumentException("client.getCurrentUser() returns ${client.getCurrentUser()} which is not equal to the user passed to this repo ${currentUser.id} ")
        }

        if (client.isSocketConnected()) {
            setOnline()
        }

        // start listening for events
        eventHandler = EventHandlerImpl(this)
        startListening()
        initClean()
    }

    internal suspend fun updateCurrentUser(me: User) {
        if (me.id != currentUser.id) {
            throw InputMismatchException("received connect event for user with id ${me.id} while chat domain is configured for user with id ${currentUser.id}. create a new chatdomain when connecting a different user.")
        }
        currentUser = me
        repos.users.insertMe(me)
        _mutedUsers.postValue(me.mutes)

        setBanned(me.banned)
    }

    internal suspend fun storeSyncState(): SyncStateEntity? {
        syncState?.let {
            it.activeChannelIds = activeChannelMapImpl.keys().toList()
            it.activeQueryIds = activeQueryMapImpl.values.toList().map { it.queryEntity.id }
            repos.syncState.insert(it)
        }

        return syncState
    }

    override suspend fun disconnect() {
        storeSyncState()
        job.cancelChildren()
        stopListening()
        stopClean()
    }

    override fun getVersion(): String {
        return BuildConfig.VERSION_NAME + "-" + BuildConfig.BUILD_TYPE
    }

    private fun stopClean() {
        mainHandler.removeCallbacks(cleanTask)
    }

    private fun initClean() {
        mainHandler = Handler(Looper.getMainLooper())

        mainHandler.postDelayed(cleanTask, 5000)
    }

    private fun createDatabase(): ChatDatabase {
        val database = if (offlineEnabled) {
            ChatDatabase.getDatabase(context, currentUser.id)
        } else {
            Room.inMemoryDatabaseBuilder(
                    context, ChatDatabase::class.java
            ).build()
        }
        return database
    }

    suspend fun runAndRetry(runnable: () -> Call<*>): Result<*> {
        var attempt = 1
        var result: Result<*>

        while (true) {
            result = runnable().execute()
            if (result.isSuccess) {
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

    suspend fun createChannel(c: Channel): Result<Channel> {
        val online = isOnline()
        c.createdAt = c.createdAt ?: Date()
        c.syncStatus = if (online) { SyncStatus.IN_PROGRESS } else { SyncStatus.SYNC_NEEDED }

        // update livedata
        val channelRepo = channel(c.cid)
        channelRepo.updateLiveDataFromChannel(c)
        val channelController = client.channel(c.type, c.id)

        // Update Room State
        repos.channels.insertChannel(c)

        // make the API call and follow retry policy
        if (online) {
            val runnable = {
                // TODO: LLC is a bit broken when it comes to creating channels
                // this syntax is really confusing
                val data = c.extraData.toMutableMap()
                data["members"] = c.members
                val watchChannelRequest = WatchChannelRequest()
                watchChannelRequest.withData(c.extraData)
                channelController.watch(watchChannelRequest)
            }
            val result = runAndRetry(runnable)
            return if (result.isSuccess) {
                c.syncStatus = SyncStatus.COMPLETED
                repos.channels.insertChannel(c)
                Result(result.data() as Channel, null)
            } else {
                if (result.error().isPermanent()) {
                    c.syncStatus = SyncStatus.FAILED_PERMANENTLY
                } else {
                    c.syncStatus = SyncStatus.SYNC_NEEDED
                }
                repos.channels.insertChannel(c)
                Result(null, result.error())
            }
        } else {
            return Result(c, null)
        }
    }

    fun addError(error: ChatError) {
        _errorEvent.postValue(
            Event(
                error
            )
        )
    }

    /** the event subscription, cancel using repo.stopListening */
    private var eventSubscription: Subscription? = null
    /** stores the mapping from cid to channelRepository */
    var activeChannelMapImpl: ConcurrentHashMap<String, ChannelControllerImpl> = ConcurrentHashMap()

    /** stores the mapping from cid to channelRepository */
    var activeQueryMapImpl: ConcurrentHashMap<QueryChannelsEntity, QueryChannelsControllerImpl> = ConcurrentHashMap()

    fun isActiveChannel(cid: String): Boolean {
        return activeChannelMapImpl.containsKey(cid)
    }

    fun setChannelUnreadCount(newCount: Int) {
        val currentCount = _channelUnreadCount.value ?: 0
        if (currentCount != newCount) {
            _channelUnreadCount.postValue(newCount)
        }
    }

    fun setBanned(newBanned: Boolean) {
        val banned = _banned.value ?: false
        if (newBanned != banned) {
            _banned.postValue(newBanned)
        }
    }

    fun setTotalUnreadCount(newCount: Int) {
        val currentCount = _totalUnreadCount.value ?: 0
        if (currentCount != newCount) {
            _totalUnreadCount.postValue(newCount)
        }
    }

    /**
     * Start listening to chat events and keep the room database in sync
     */
    fun startListening() {
        if (eventSubscription != null) {
            return
        }
        eventSubscription = client.events().subscribe {
            eventHandler.handleEvents(listOf(it))
        }
    }

    /**
     * Stop listening to chat events
     */
    fun stopListening() {
        eventSubscription?.let { it.unsubscribe() }
    }

    fun channel(c: Channel): ChannelControllerImpl {
        return channel(c.type, c.id)
    }

    fun channel(cid: String): ChannelControllerImpl {
        val parts = cid.split(":")
        check(parts.size == 2) { "Received invalid cid, expected format messaging:123, got $cid" }
        return channel(parts[0], parts[1])
    }

    /**
     * repo.channel("messaging", "12") return a ChatChannelRepository
     */
    fun channel(
        channelType: String,
        channelId: String
    ): ChannelControllerImpl {
        val cid = "%s:%s".format(channelType, channelId)
        if (!activeChannelMapImpl.containsKey(cid)) {
            val channelRepo =
                ChannelControllerImpl(
                    channelType,
                    channelId,
                    client,
                    this
                )
            activeChannelMapImpl.put(cid, channelRepo)
        }
        return activeChannelMapImpl.getValue(cid)
    }

    fun generateMessageId(): String {
        return currentUser.id + "-" + UUID.randomUUID().toString()
    }

    fun setOffline() {
        _online.value = false
    }

    fun postOffline() {
        _online.postValue(false)
    }

    fun setOnline() {
        _online.value = true
    }

    fun postOnline() {
        _online.postValue(true)
    }

    override fun isOnline(): Boolean {
        val online = _online.value!!
        return online
    }

    override fun isOffline(): Boolean {
        return !_online.value!!
    }

    override fun isInitialized(): Boolean {
        return _initialized.value ?: false
    }

    override fun getActiveQueries(): List<QueryChannelsControllerImpl> {
        return activeQueryMapImpl.values.toList()
    }

    /**
     * queryChannels
     * - first read the current results from Room
     * - if we are online make the API call to update results
     */
    fun queryChannels(
        filter: FilterObject,
        sort: QuerySort? = null
    ): QueryChannelsControllerImpl {
        // mark this query as active
        val queryChannelsEntity = QueryChannelsEntity(filter, sort)
        if (!activeQueryMapImpl.containsKey(queryChannelsEntity)) {
            val queryRepo =
                QueryChannelsControllerImpl(
                    queryChannelsEntity,
                    queryChannelsEntity.filter,
                    queryChannelsEntity.sort,
                    client,
                    this
                )
            activeQueryMapImpl[queryChannelsEntity] = queryRepo
        }
        return activeQueryMapImpl[queryChannelsEntity]!!
    }

    suspend fun queryEvents(cids: List<String>): List<ChatEvent> {
        val limit = 100
        var offset = 0
        val maxEvents = 500
        val allEvents = mutableListOf<ChatEvent>()

        while (true) {
            val call = client.replayEvents(cids, syncState?.lastSyncedAt, limit, offset)
            val response = call.execute()
            if (response.isError) {
                // TODO: what is the best error type for this?
                throw Error(response.error())
            }
            val events = response.data()
            allEvents.addAll(events)
            if (events.size <= limit || offset >= maxEvents) {
                break
            }
            offset += limit
        }

        return allEvents
    }

    /**
     * replay events for all active channels
     * ensures that the cid you provide is active
     *
     * @param cid ensures that the channel with this id is active
     */
    suspend fun replayEventsForActiveChannels(cid: String? = null): Result<List<ChatEvent>> {
        // wait for the active channel info to load
        initJob.join()
        // make a list of all channel ids
        val cids = activeChannelMapImpl.keys().toList().toMutableList()
        cid?.let {
            channel(it)
            cids.add(it)
        }

        val now = Date()
        val events = queryEvents(cids)
        eventHandler.updateOfflineStorageFromEvents(events)

        syncState?.let { it.lastSyncedAt = now }

        return Result(events, null)
    }

    suspend fun connectionRecovered(recoveryNeeded: Boolean = false) {
        // 0 ensure load is complete
        initJob.join()

        // 1 update the results for queries that are actively being shown right now
        val updatedChannelIds = mutableSetOf<String>()
        val queriesToRetry = activeQueryMapImpl.values.toList().filter { it.recoveryNeeded || recoveryNeeded }.take(3)
        for (queryRepo in queriesToRetry) {
            val response = queryRepo.runQueryOnline(QueryChannelsPaginationRequest(0, 30, 30))
            if (response.isSuccess) {
                updatedChannelIds.addAll(response.data().map { it.cid })
            }
        }
        // 2 update the data for all channels that are being show right now...
        // exclude ones we just updated
        val cids = activeChannelMapImpl.entries.toList().filter { it.value.recoveryNeeded || recoveryNeeded }.filterNot { updatedChannelIds.contains(it.key) }.take(30)

        logger.logI("connection established: recoveryNeeded= $recoveryNeeded, retrying ${queriesToRetry.size} queries and ${cids.size} channels")

        if (cids.isNotEmpty()) {
            val filter = `in`("cid", cids)
            val request = QueryChannelsRequest(filter, 0, 30)
            val result = client.queryChannels(request).execute()
            if (result.isSuccess) {
                val channels = result.data()
                for (c in channels) {
                    val channelRepo = this.channel(c)
                    channelRepo.updateLiveDataFromChannel(c)
                }
                storeStateForChannels(channels)
            }
        }

        // 3 retry any failed requests
        if (isOnline()) {
            retryFailedEntities()
        }

        // 4 recover events
        if (isOnline()) {
            // TODO: reenable this when the endpoint goes live
            // replayEventsForActiveChannels()
        }
    }

    suspend fun retryFailedEntities() {
        sleep(1000)
        // retry channels, messages and reactions in that order..
        val channelEntities = repos.channels.retryChannels()
        val messageEntities = repos.messages.retryMessages()
        val reactionEntities = repos.reactions.retryReactions()
        logger.logI("Retried ${channelEntities.size} channel entities, ${messageEntities.size} message entities and ${reactionEntities.size} reaction entities")
    }

    suspend fun storeStateForChannel(channel: Channel) {
        return storeStateForChannels(listOf(channel))
    }

    suspend fun storeStateForChannels(channelsResponse: List<Channel>) {
        val users = mutableMapOf<String, User>()
        val configs: MutableMap<String, Config> = mutableMapOf()
        // start by gathering all the users
        val messages = mutableListOf<Message>()
        for (channel in channelsResponse) {

            users.putAll(channel.users().associateBy { it.id })
            configs[channel.type] = channel.config

            for (message in channel.messages) {
                message.cid = channel.cid
                users.putAll(message.users().associateBy { it.id })
            }

            messages.addAll(channel.messages)
        }

        // store the channel configs
        repos.configs.insertConfigs(configs)
        // store the users
        repos.users.insertManyUsers(users.values.toList())
        // store the channel data
        repos.channels.insertChannel(channelsResponse)
        // store the messages
        repos.messages.insertMessages(messages)

        logger.logI("stored ${channelsResponse.size} channels, ${configs.size} configs, ${users.size} users and ${messages.size} messages")
    }

    suspend fun selectAndEnrichChannel(channelId: String, pagination: QueryChannelPaginationRequest): ChannelEntityPair? {
        val channelStates = selectAndEnrichChannels(listOf(channelId), pagination.toAnyChannelPaginationRequest())
        return channelStates.getOrNull(0)
    }

    suspend fun selectAndEnrichChannel(channelId: String, pagination: QueryChannelsPaginationRequest): ChannelEntityPair? {
        val channelStates = selectAndEnrichChannels(listOf(channelId), pagination.toAnyChannelPaginationRequest())
        return channelStates.getOrNull(0)
    }

    suspend fun selectAndEnrichChannels(
        channelIds: List<String>,
        pagination: QueryChannelsPaginationRequest
    ): List<ChannelEntityPair> {
        return selectAndEnrichChannels(channelIds, pagination.toAnyChannelPaginationRequest())
    }

    internal suspend fun selectAndEnrichChannels(
        channelIds: List<String>,
        pagination: AnyChannelPaginationRequest
    ): List<ChannelEntityPair> {

        // fetch the channel entities from room
        val channelEntities = repos.channels.select(channelIds)

        // gather the user ids from channels, members and the last message
        val userIds = mutableSetOf<String>()
        val channelMessagesMap = mutableMapOf<String, List<MessageEntity>>()
        for (channelEntity in channelEntities) {
            channelEntity.createdByUserId?.let { userIds.add(it) }
            channelEntity.members.let {
                userIds.addAll(it.keys)
            }
            channelEntity.reads.let {
                userIds.addAll(it.keys)
            }
            if (pagination.messageLimit > 0) {
                val messages = repos.messages.selectMessagesForChannel(channelEntity.cid, pagination)
                for (message in messages) {
                    userIds.add(message.userId)
                    for (reaction in message.latestReactions) {
                        userIds.add(reaction.userId)
                    }
                }
                channelMessagesMap[channelEntity.cid] = messages
            }
        }

        // get a map with user id to User
        val userMap = repos.users.selectUserMap(userIds.toList())

        // convert the channels
        val channelPairs = mutableListOf<ChannelEntityPair>()
        for (channelEntity in channelEntities) {
            val channel = channelEntity.toChannel(userMap)
            // get the config we have stored offline
            channel.config = getChannelConfig(channel.type)

            if (pagination.messageLimit > 0) {
                val messageEntities = channelMessagesMap[channel.cid] ?: emptyList()
                val messages = messageEntities.map { it.toMessage(userMap) }
                channel.messages = messages
            }

            val channelPair = ChannelEntityPair(channel, channelEntity)

            channelPairs.add(channelPair)
        }
        return channelPairs.toList()
    }

    override fun clean() {
        for (channelRepo in activeChannelMapImpl.values.toList()) {
            channelRepo.clean()
        }
    }

    override fun getChannelConfig(channelType: String): Config {
        val config = repos.configs.select(channelType)
        checkNotNull(config) { "Missing channel config for channel type $channelType" }
        return config
    }

    fun postInitialized() {
        _initialized.postValue(true)
    }
}

var gson = Gson()
