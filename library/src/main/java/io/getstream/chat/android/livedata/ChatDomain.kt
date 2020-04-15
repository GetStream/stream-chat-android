package io.getstream.chat.android.livedata

import android.content.Context
import android.os.Handler
import android.os.Looper
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.room.Room
import com.google.gson.Gson
import io.getstream.chat.android.client.ChatClient
import io.getstream.chat.android.client.api.models.QueryChannelsRequest
import io.getstream.chat.android.client.api.models.QuerySort
import io.getstream.chat.android.client.call.Call
import io.getstream.chat.android.client.errors.ChatError
import io.getstream.chat.android.client.logger.ChatLogger
import io.getstream.chat.android.client.models.*
import io.getstream.chat.android.client.models.Filters.`in`
import io.getstream.chat.android.client.utils.FilterObject
import io.getstream.chat.android.client.utils.Result
import io.getstream.chat.android.client.utils.SyncStatus
import io.getstream.chat.android.client.utils.observable.Subscription
import io.getstream.chat.android.livedata.entity.*
import io.getstream.chat.android.livedata.repository.RepositoryHelper
import io.getstream.chat.android.livedata.request.AnyChannelPaginationRequest
import io.getstream.chat.android.livedata.request.QueryChannelPaginationRequest
import io.getstream.chat.android.livedata.request.QueryChannelsPaginationRequest
import io.getstream.chat.android.livedata.usecase.*
import kotlinx.coroutines.*
import java.util.*
import java.util.concurrent.ConcurrentHashMap


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
class ChatDomain private constructor(var context: Context, var client: ChatClient, var currentUser: User, var offlineEnabled: Boolean = false, var userPresence: Boolean = false) {
    lateinit var useCases: UseCaseHelper
    internal lateinit var eventHandler: EventHandlerImpl
    private lateinit var mainHandler: Handler
    private var baseLogger: ChatLogger = ChatLogger.instance
    private var logger = ChatLogger.get("Repo")
    private val cleanTask = object : Runnable {
        override fun run() {
            clean()
            mainHandler.postDelayed(this, 1000)
        }
    }

    val job = SupervisorJob()
    val scope = CoroutineScope(Dispatchers.IO + job)

    internal lateinit var repos: RepositoryHelper

    /** The retry policy for retrying failed requests */
    var retryPolicy: RetryPolicy = DefaultRetryPolicy()

    internal constructor(context: Context, client: ChatClient, currentUser: User, offlineEnabled: Boolean = true, userPresence: Boolean = true, db: ChatDatabase? = null) : this(context, client, currentUser, offlineEnabled, userPresence) {
        val chatDatabase = db ?: createDatabase()
        repos = RepositoryHelper( client, chatDatabase)


        // load channel configs from Room into memory
        scope.launch(Dispatchers.IO) {
            repos.configs.load()
        }

        useCases = UseCaseHelper(
            CreateChannel(this),
            DeleteMessage(this),
            DeleteReaction(this),
            EditMessage(this),
            Keystroke(this),
            SendMessage(this),
            SendReaction(this),
            StopTyping(this),
            WatchChannel(this)
        )

        // verify that you're not connecting 2 different users
        if (client.getCurrentUser() != null && client.getCurrentUser()?.id != currentUser.id) {
            throw IllegalArgumentException("client.getCurrentUser() returns ${client.getCurrentUser()} which is not equal to the user passed to this repo ${currentUser.id} ")
        }

        // start listening for events
        eventHandler = EventHandlerImpl(this)
        startListening()
        initClean()
    }

    private fun disconnect() {
        stopListening()
        stopClean()
        client.disconnect()
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

    suspend fun runAndRetry(runnable: () -> Call<Any>): Result<Any> {
        var attempt = 1
        var result: Result<Any>

        while (true) {
            result = runnable().execute()
            if (result.isSuccess) {
                return result
            } else {
                // retry logic
                val shouldRetry = retryPolicy.shouldRetry(client, attempt, result.error())
                val timeout = retryPolicy.retryTimeout(client, attempt, result.error())

                if (shouldRetry) {
                    // temporary failure, continue
                    logger.logI("API call failed (attempt ${attempt}), retrying in ${timeout} seconds")
                    if (timeout != null) {
                        delay(timeout.toLong())
                    }
                    attempt += 1
                } else {
                    logger.logI("API call failed (attempt ${attempt}). Giving up for now, will retry when connection recovers.")
                    break
                }
            }
        }
        // permanent failure case return
        return result
    }

    suspend fun createChannel(c: Channel): Result<Channel> {
        c.createdAt = c.createdAt ?: Date()
        c.syncStatus = SyncStatus.SYNC_NEEDED

        // update livedata
        val channelRepo = channel(c.cid)
        channelRepo.updateChannel(c)
        val channelController = client.channel(c.type, c.id)

        // Update Room State
        repos.channels.insert(c)

        // make the API call and follow retry policy
        if (isOnline()) {
            val runnable = {
                channelController.watch() as Call<Any>
            }
            val result = runAndRetry(runnable)
            if (result.isSuccess) {
                c.syncStatus = SyncStatus.SYNCED
                repos.channels.insert(c)
            }
            return Result(result.data() as Channel, result.error())
        } else {
            return Result(c, null)
        }

    }



    private val _initialized = MutableLiveData<Boolean>(false)
    val initialized: LiveData<Boolean> = _initialized

    private val _online = MutableLiveData<Boolean>(false)
    /**
     * LiveData<Boolean> that indicates if we are currently online
     */
    val online: LiveData<Boolean> = _online
    // TODO 1.1: We should accelerate online/offline detection

    private val _totalUnreadCount = MutableLiveData<Int>()

    /**
     * The total unread message count for the current user.
     * Depending on your app you'll want to show this or the channelUnreadCount
     */
    val totalUnreadCount: LiveData<Int> = _totalUnreadCount

    private val _channelUnreadCount = MutableLiveData<Int>()

    /**
     * the number of unread channels for the current user
     */
    val channelUnreadCount: LiveData<Int> = _channelUnreadCount

    private val _errorEvent = MutableLiveData<io.getstream.chat.android.livedata.Event<ChatError>>()
    /**
     * The error event livedata object is triggered when errors in the underlying components occure.
     * The following example shows how to observe these errors
     *
     *  repo.errorEvent.observe(this, EventObserver {
     *       // create a toast
     *   })
     *
     */
    val errorEvents: LiveData<io.getstream.chat.android.livedata.Event<ChatError>> = _errorEvent

    fun addError(error: ChatError) {
        _errorEvent.postValue(io.getstream.chat.android.livedata.Event(error))
    }


    /** the event subscription, cancel using repo.stopListening */
    private var eventSubscription: Subscription? = null
    /** stores the mapping from cid to channelRepository */
    var activeChannelMap: ConcurrentHashMap<String, ChannelController> = ConcurrentHashMap()


    /** stores the mapping from cid to channelRepository */
    var activeQueryMap: ConcurrentHashMap<QueryChannelsEntity, QueryChannelsController> = ConcurrentHashMap()

    fun isActiveChannel(cid: String): Boolean {
        return activeChannelMap.containsKey(cid)
    }

    fun setChannelUnreadCount(newCount: Int) {
        val currentCount = _channelUnreadCount.value ?: 0
        if (currentCount != newCount) {
            _channelUnreadCount.postValue(newCount)
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

    fun channel(c: Channel): ChannelController {
        return channel(c.type, c.id)
    }

    fun channel(cid: String): ChannelController {
        val parts = cid.split(":")
        check(parts.size == 2) { "Received invalid cid, expected format messaging:123, got ${cid}" }
        return channel(parts[0], parts[1])
    }

    /**
     * repo.channel("messaging", "12") return a ChatChannelRepository
     */
    fun channel(
            channelType: String,
            channelId: String
    ): io.getstream.chat.android.livedata.ChannelController {
        val cid = "%s:%s".format(channelType, channelId)
        if (!activeChannelMap.containsKey(cid)) {
            val channelRepo =
                    io.getstream.chat.android.livedata.ChannelController(
                            channelType,
                            channelId,
                            client,
                            this
                    )
            activeChannelMap.put(cid, channelRepo)
        }
        return activeChannelMap.getValue(cid)
    }

    fun generateMessageId(): String {
        return currentUser.getUserId() + "-" + UUID.randomUUID().toString()
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

    fun isOnline(): Boolean {
        val online = _online.value!!
        return online
    }

    fun isOffline(): Boolean {
        return !_online.value!!
    }

    fun isInitialized(): Boolean {
        return _initialized.value ?: false
    }

    fun getActiveQueries(): List<QueryChannelsController> {
        return activeQueryMap.values.toList()
    }


    /**
     * queryChannels
     * - first read the current results from Room
     * - if we are online make the API call to update results
     */
    fun queryChannels(
            filter: FilterObject,
            sort: QuerySort? = null
    ): QueryChannelsController {
        // mark this query as active
        val queryChannelsEntity = QueryChannelsEntity(filter, sort)
        val queryRepo = QueryChannelsController(queryChannelsEntity, client, this)
        activeQueryMap[queryChannelsEntity] = queryRepo
        return queryRepo
    }

    suspend fun connectionRecovered(recoveryNeeded: Boolean = false) {
        /*
         * client.recoverEvents(channelIDs, {limit: 100, since: last_time_online, [offset: $offset_returned_by_previous_call ]})
         */
        // 1 update the results for queries that are actively being shown right now
        val updatedChannelIds = mutableSetOf<String>()
        val queriesToRetry =  activeQueryMap.values.toList().filter { it.recoveryNeeded || recoveryNeeded }.take(3)
        for (queryRepo in queriesToRetry) {
            val response = queryRepo.runQueryOnline(QueryChannelsPaginationRequest(0, 30, 30))
            if (response.isSuccess) {
                updatedChannelIds.addAll(response.data().map { it.cid })
            }
        }
        // 2 update the data for all channels that are being show right now...
        // exclude ones we just updated
        val cids = activeChannelMap.entries.toList().filter { it.value.recoveryNeeded || recoveryNeeded }.filterNot { updatedChannelIds.contains(it.key) }.take(30)



        logger.logI("connection established: recoveryNeeded= ${recoveryNeeded}, retrying ${queriesToRetry.size} queries and ${cids.size} channels")

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
        retryFailedEntities()
    }

    suspend fun retryMessages(): List<MessageEntity> {
        val userMap: Map<String, User> = mutableMapOf(currentUser.id to currentUser)

        val messageEntities = repos.messages.selectSyncNeeded()
        if (isOnline()) {
            for (messageEntity in messageEntities) {
                val channel = client.channel(messageEntity.cid)
                // support sending, deleting and editing messages here
                val result = when {
                    messageEntity.deletedAt != null -> {
                        channel.deleteMessage(messageEntity.id).execute()
                    }
                    messageEntity.sendMessageCompletedAt != null -> {
                        client.updateMessage(messageEntity.toMessage(userMap)).execute()
                    }
                    else -> {
                        channel.sendMessage(messageEntity.toMessage(userMap)).execute()
                    }
                }

                if (result.isSuccess) {
                    // TODO: 1.1 image upload support
                    messageEntity.syncStatus = SyncStatus.SYNCED
                    messageEntity.sendMessageCompletedAt = messageEntity.sendMessageCompletedAt
                            ?: Date()
                    repos.messages.insert(messageEntity)
                }
            }
        }
        return messageEntities
    }

    suspend fun retryReactions(): List<ReactionEntity> {
        val userMap: Map<String, User> = mutableMapOf(currentUser.id to currentUser)

        val reactionEntities = repos.reactions.selectSyncNeeded()
        for (reactionEntity in reactionEntities) {
            val reaction = reactionEntity.toReaction(userMap)
            reaction.user = null
            val result = if (reactionEntity.deletedAt != null) {
                client.deleteReaction(reaction.messageId, reaction.type).execute()
            } else {
                client.sendReaction(reaction).execute()
            }

            if (result.isSuccess) {
                reactionEntity.syncStatus = SyncStatus.SYNCED
                repos.reactions.insert(reactionEntity)
            } else {
                addError(result.error())
            }
        }
        return reactionEntities
    }


    suspend fun retryChannels(): List<ChannelEntity> {
        val userMap: Map<String, User> = mutableMapOf(currentUser.id to currentUser)
        val channelEntities = repos.channels.selectSyncNeeded()

        for (channelEntity in channelEntities) {
            val channel = channelEntity.toChannel(userMap)
            val controller = client.channel(channelEntity.type, channelEntity.type)
            val result = controller.watch().execute()
            if (result.isSuccess) {
                channelEntity.syncStatus = SyncStatus.SYNCED
                repos.channels.insert(channelEntity)
            }
            // TODO: 1.1 support hiding channels

        }
        return channelEntities
    }

    suspend fun retryFailedEntities() {
        // retry channels, messages and reactions in that order..
        val channelEntities = retryChannels()
        val messageEntities = retryMessages()
        val reactionEntities = retryReactions()
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
        repos.users.insertMany(users.values.toList())
        // store the channel data
        repos.channels.insert(channelsResponse)
        // store the messages
        repos.messages.insertMessages(messages)

        logger.logI("stored ${channelsResponse.size} channels, ${configs.size} configs, ${users.size} users and ${messages.size} messages")
    }

    suspend fun selectAndEnrichChannel(channelId: String, pagination: QueryChannelPaginationRequest): Channel? {
        val channelStates = selectAndEnrichChannels(listOf(channelId), pagination.toAnyChannelPaginationRequest())
        return channelStates.getOrNull(0)
    }

    suspend fun selectAndEnrichChannel(channelId: String, pagination: QueryChannelsPaginationRequest): Channel? {
        val channelStates = selectAndEnrichChannels(listOf(channelId), pagination.toAnyChannelPaginationRequest())
        return channelStates.getOrNull(0)
    }

    suspend fun selectAndEnrichChannels(
            channelIds: List<String>,
            pagination: QueryChannelsPaginationRequest
    ): List<Channel> {
        return selectAndEnrichChannels(channelIds, pagination.toAnyChannelPaginationRequest())
    }

    internal suspend fun selectAndEnrichChannels(
            channelIds: List<String>,
            pagination: AnyChannelPaginationRequest
    ): List<Channel> {

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
        val channels = mutableListOf<Channel>()
        for (channelEntity in channelEntities) {
            val channel = channelEntity.toChannel(userMap)
            // get the config we have stored offline
            channel.config = getChannelConfig(channel.type)

            if (pagination.messageLimit > 0) {
                val messageEntities = channelMessagesMap[channel.cid] ?: emptyList()
                val messages = messageEntities.map { it.toMessage(userMap) }
                channel.messages = messages
            }

            channels.add(channel)
        }
        return channels.toList()
    }



    fun clean() {
        for (channelRepo in activeChannelMap.values.toList()) {
            channelRepo.clean()
        }
    }

    fun getChannelConfig(channelType: String): Config {
        val config = repos.configs.select(channelType)
        checkNotNull(config) { "Missing channel config for channel type $channelType" }
        return config
    }

    fun postInitialized() {
        _initialized.postValue(true)
    }

    data class Builder(
            private var appContext: Context, private var client: ChatClient, private var user: User
    ) {

        private var database: ChatDatabase? = null

        private var userPresence: Boolean = false
        private var offlineEnabled: Boolean = true

        fun database(db: ChatDatabase): Builder {
            this.database = db
            return this
        }

        fun offlineEnabled(): Builder {
            this.offlineEnabled = true
            return this
        }

        fun offlineDisabled(): Builder {
            this.offlineEnabled = false
            return this
        }

        fun userPresenceEnabled(): Builder {
            this.userPresence = true
            return this
        }

        fun userPresenceDisabled(): Builder {
            this.userPresence = false
            return this
        }

        fun build(): ChatDomain {
            val chatRepo = ChatDomain(appContext, client, user, offlineEnabled, userPresence, database)

            ChatDomain.instance = chatRepo

            return chatRepo
        }
    }

    companion object {

        private lateinit var instance: ChatDomain

        @JvmStatic
        fun instance(): ChatDomain {
            return instance
        }


    }
}

var gson = Gson()