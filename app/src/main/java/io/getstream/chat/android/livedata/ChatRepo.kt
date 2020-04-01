package io.getstream.chat.android.livedata

import android.content.Context
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.liveData
import com.google.gson.Gson
import io.getstream.chat.android.client.ChatClient
import io.getstream.chat.android.client.errors.ChatError
import io.getstream.chat.android.client.events.*
import io.getstream.chat.android.client.logger.ChatLogger
import io.getstream.chat.android.client.models.*
import io.getstream.chat.android.client.utils.SyncStatus
import io.getstream.chat.android.client.utils.observable.Subscription
import io.getstream.chat.android.livedata.dao.*
import io.getstream.chat.android.livedata.entity.*
import io.getstream.chat.android.livedata.entity.UserEntity
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.launch
import java.lang.Thread.sleep
import java.util.*


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
class StreamChatRepository(
    var client: ChatClient,
    var currentUser: User
) {

    private lateinit var channelQueryDao: ChannelQueryDao
    private lateinit var userDao: UserDao
    private lateinit var reactionDao: ReactionDao
    private lateinit var messageDao: MessageDao
    private lateinit var channelStateDao: ChannelStateDao
    private lateinit var channelConfigDao: ChannelConfigDao
    private val logger = ChatLogger.get("ChatRepo")

    constructor(client: ChatClient, currentUser: User, database: ChatDatabase) : this(client, currentUser) {
        channelQueryDao = database.queryChannelsQDao()
        userDao = database.userDao()
        reactionDao = database.reactionDao()
        messageDao = database.messageDao()
        channelStateDao = database.channelStateDao()
        channelConfigDao = database.channelConfigDao()

        // load channel configs from Room into memory
        GlobalScope.launch {
            loadConfigs()
        }

        // start listening for events
        startListening()
    }

    // TODO: make this more dry

    constructor(context: Context, currentUser: User, client: ChatClient) : this(client, currentUser) {
        val database = ChatDatabase.getDatabase(context, currentUser.id)
        channelQueryDao = database.queryChannelsQDao()
        userDao = database.userDao()
        reactionDao = database.reactionDao()
        messageDao = database.messageDao()
        channelStateDao = database.channelStateDao()
        channelConfigDao = database.channelConfigDao()

        // load channel configs from Room into memory
        GlobalScope.launch {
            loadConfigs()
        }

        // start listening for events
        startListening()

    }

    private suspend fun loadConfigs() {
        val configEntities = channelConfigDao.selectAll()
        for (configEntity in configEntities) {
            channelConfigs[configEntity.channelType] = configEntity.config
        }
    }

    private val _initialized = MutableLiveData<Boolean>(false)
    val initialized: LiveData<Boolean> = _initialized

    private val _online = MutableLiveData<Boolean>(false)
    /**
     * LiveData<Boolean> that indicates if we are currently online
     */
    val online: LiveData<Boolean> = _online
    // TODO: in 1.1 we should accelerate online/offline detection

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


    // TODO: implement retry policy

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
        _errorEvent.value = io.getstream.chat.android.livedata.Event(error)
    }

    /** if we should enable offline storage or not */
    var offlineEnabled = true // TODO we will implement support for disabling offline support in 1.1

    /** the event subscription, cancel using repo.stopListening */
    private var eventSubscription: Subscription? = null
    /** stores the mapping from cid to channelRepository */
    var activeChannelMap: MutableMap<String, io.getstream.chat.android.livedata.ChannelRepo> =
        mutableMapOf()

    /** stores the mapping from cid to channelRepository */
    var activeQueryMap: MutableMap<QueryChannelsEntity, ChatQueryChannelRepo> =
        mutableMapOf()

    var channelConfigs: MutableMap<String, Config> = mutableMapOf()

    suspend fun handleEvent(event : ChatEvent) {
        // keep the data in Room updated based on the various events..
        // TODO: cache users, messages and channels to reduce number of Room queries


        // any event can have channel and unread count information
        event.unreadChannels?.let { setChannelUnreadCount(it) }
        event.totalUnreadCount?.let { setTotalUnreadCount(it) }

        // if this is a channel level event, let the channel repo handle it
        if (!event.cid.isNullOrEmpty()) {
            val cid = event.cid!!
            if (activeChannelMap.containsKey(cid)) {
                val channelRepo = activeChannelMap.get(cid)!!
                channelRepo.handleEvent(event)
            }
        }

        // connection events
        when (event) {
            is DisconnectedEvent -> {
                _online.value = false

            }
            is ConnectedEvent -> {
                _online.value = true
                _initialized.value = true
            }
        }

        // notifications of messages on channels you're a member of but not watching
        if (event is NotificationAddedToChannelEvent) {
            // this one is trickier. we need to insert the message
            // we also need to add the channel to the query which is tricker...
            for ((_, queryRepo) in activeQueryMap) {
                queryRepo.handleMessageNotification(event)
            }
        }

        if (offlineEnabled) {

            when (event) {
                is NewMessageEvent, is MessageDeletedEvent, is MessageUpdatedEvent -> {
                    insertMessage(event.message)
                }
                is MessageReadEvent -> {
                    // get the channel, update reads, write the channel
                    val channel = channelStateDao.select(event.cid)
                    val read = ChannelUserRead()
                    read.user = event.user!!
                    read.lastRead = event.createdAt
                    channel?.let {
                        it.updateReads(read)
                        insertChannelStateEntity(it)
                    }
                }
                is ReactionNewEvent -> {
                    // get the message, update the reaction data, update the message
                    // TODO: should we do this or update based the message based on reaction?
                    val message = selectMessageEntity(event.reaction!!.messageId)
                    message?.let {
                        val userId = event.reaction!!.user!!.id
                        it.addReaction(event.reaction!!, currentUser.id == userId)
                        insertMessageEntity(it)
                    }

                }
                is ReactionDeletedEvent -> {
                    // get the message, update the reaction data, update the message
                    insertMessage(event.message)
                    // TODO: this isn't right, use the same approach as with newReaction
                }
                is MemberAddedEvent, is MemberRemovedEvent, is MemberUpdatedEvent -> {
                    // get the channel, update members, write the channel
                    val channelEntity = selectChannelEntity(event.cid!!)
                    if (channelEntity != null) {
                        var member = event.member
                        val userId = event.member!!.user.id
                        if (event is MemberRemovedEvent) {
                            member = null
                        }
                        channelEntity.setMember(userId, member)
                        insertChannelStateEntity(channelEntity)
                    }


                }
                is ChannelUpdatedEvent, is ChannelHiddenEvent, is ChannelDeletedEvent -> {
                    // get the channel, update members, write the channel
                    event.channel?.let {
                        insertChannel(it)
                    }
                }

            }
        }
    }

    private fun setChannelUnreadCount(newCount: Int) {
        val currentCount = _channelUnreadCount.value ?: 0
        if (currentCount != newCount) {
            _channelUnreadCount.value = newCount
        }
    }

    private fun setTotalUnreadCount(newCount: Int) {
        val currentCount = _totalUnreadCount.value ?: 0
        if (currentCount != newCount) {
            _totalUnreadCount.value = newCount
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
            GlobalScope.launch(Dispatchers.IO) {
                handleEvent(it)
            }
        }
    }

    /**
     * Stop listening to chat events
     */
    fun stopListening() {
        eventSubscription?.let { it.unsubscribe() }
    }

    /**
     * repo.channel("messaging", "12") return a ChatChannelRepository
     */
    fun channel(
        channelType: String,
        channelId: String
    ): io.getstream.chat.android.livedata.ChannelRepo {
        val cid = "%s:%s".format(channelType, channelId)
        if (!activeChannelMap.containsKey(cid)) {
            val channelRepo =
                io.getstream.chat.android.livedata.ChannelRepo(
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
        checkNotNull(client.getCurrentUser()) { "client.getCurrentUser() must be available to generate a message id" }
        return client.getCurrentUser()!!.getUserId() + "-" + UUID.randomUUID().toString()
    }

    suspend fun selectMessageEntity(messageId: String): MessageEntity? {
        return messageDao.select(messageId)
    }


    fun setOffline() {
        _online.value = false
    }

    fun setOnline() {
        _online.value = true
    }

    fun isOnline(): Boolean {
        val online = _online.value!!
        sleep(100000)
        return online
    }

    fun isOffline(): Boolean {
        return !_online.value!!
    }

    /**
     * queryChannels
     * - first read the current results from Room
     * - if we are online make the API call to update results
     */
    fun queryChannels(
        queryChannelsEntity: QueryChannelsEntity
    ): ChatQueryChannelRepo {
        // mark this query as active
        val queryRepo = ChatQueryChannelRepo(queryChannelsEntity, client, this)
        activeQueryMap[queryChannelsEntity] = queryRepo
        return queryRepo
    }

    suspend fun insertConfigs(configs: MutableMap<String, Config>) {
        val configEntities = mutableListOf<ChannelConfigEntity>()
        for ((channelType, config) in configs) {
            val entity = ChannelConfigEntity(channelType)
            entity.config = config
        }
        channelConfigDao.insertMany(configEntities)
    }


    fun messagesForChannel(
        cid: String,
        limit: Int = 100,
        offset: Int = 0
    ): LiveData<List<MessageEntity>> {
        var messagesLiveData = liveData(Dispatchers.IO) {
            val messages = messageDao.messagesForChannelLive(cid, limit, offset)
            emitSource(messages)
        }
        return messagesLiveData
    }

    suspend fun selectMessagesForChannel(
        cid: String,
        limit: Int = 100,
        offset: Int = 0
    ): List<MessageEntity> {

        return messageDao.messagesForChannel(cid, limit, offset)
    }


    suspend fun insertUser(user: User) {
            userDao.insert(UserEntity(user))
    }

    suspend fun insertChannel(channel: Channel) {
        var channelEntity = ChannelStateEntity(channel)

        channelStateDao.insert(ChannelStateEntity(channel))
    }

    suspend fun insertChannelStateEntity(channelStateEntity: ChannelStateEntity) {

        channelStateDao.insert(channelStateEntity)
    }

    suspend fun insertChannels(channels: List<Channel>) {
        var entities = mutableListOf<ChannelStateEntity>()
        for (channel in channels) {
            entities.add(ChannelStateEntity(channel))
        }

        channelStateDao.insertMany(entities)
    }


    suspend fun insertReactionEntity(reactionEntity: ReactionEntity) {
        reactionDao.insert(reactionEntity)
    }

    suspend fun insertQuery(queryChannelsEntity: QueryChannelsEntity) {
        channelQueryDao.insert(queryChannelsEntity)
    }

    suspend fun insertUsers(users: List<User>) {
        val userEntities = mutableListOf<UserEntity>()
        for (user in users) {
            userEntities.add(UserEntity(user))
        }
        userDao.insertMany(userEntities)
    }

    suspend fun insertMessages(messages: List<Message>) {
        val messageEntities = mutableListOf<MessageEntity>()
        for (message in messages) {
            messageEntities.add(MessageEntity(message))
        }
        messageDao.insertMany(messageEntities)
    }

    suspend fun insertMessage(message: Message) {
        val messageEntity = MessageEntity(message)
        messageDao.insert(messageEntity)
    }

    suspend fun insertMessageEntity(messageEntity: MessageEntity) {
        messageDao.insert(messageEntity)
    }

    suspend fun connectionRecovered() {
        // update the results for queries that are actively being shown right now
        for (query in activeQueryMap) {
            // TODO: talk about this with tommaso
        }
        // update the data for all channels that are being show right now...
        for (channel in activeChannelMap) {
            // TODO: talk about this with tommaso
        }
        // retry any failed requests
        retryFailedEntities()
    }

    suspend fun retryMessages(): List<MessageEntity> {

        val user = client.getCurrentUser()!!
        val userMap: Map<String, User> = mutableMapOf(user.id to user)
        val messageEntities = messageDao.selectSyncNeeded()
        if (isOnline()) {
            for (messageEntity in messageEntities) {
                // TODO: remove this as soon as the low level client has a better solution
                val parts = messageEntity.cid.split(":")
                val channel = client.channel(parts[0], parts[1])
                channel.sendMessage(messageEntity.toMessage(userMap))
            }
        }
        return messageEntities
    }

    suspend fun retryFailedEntities() {
        // fake the user map for enriching objects
        val user = client.getCurrentUser()!!
        val userMap: Map<String, User> = mutableMapOf(user.id to user)

        val reactionEntities = reactionDao.selectSyncNeeded()
        for (reactionEntity in reactionEntities) {
            client.sendReaction(reactionEntity.toReaction(userMap))
        }


        retryMessages()
        // TODO: support channels in version 1.1
    }


    suspend fun selectChannelEntity(cid: String): ChannelStateEntity? {
        return channelStateDao.select(cid)
    }

    suspend fun selectQuery(id: String): QueryChannelsEntity? {
        return channelQueryDao.select(id)
    }

    suspend fun selectChannelEntities(channelCIDs: List<String>): List<ChannelStateEntity> {
        return channelStateDao.select(channelCIDs)
    }

    suspend fun selectUsers(userIds: List<String>): List<UserEntity> {
        return userDao.select(userIds)
    }

    suspend fun storeStateForChannel(channel: Channel) {
        return storeStateForChannels(listOf(channel))
    }


    suspend fun selectUserMap(userIds: List<String>): MutableMap<String, User> {
        val userEntities = selectUsers(userIds.toList())
        val userMap = mutableMapOf<String, User>()
        for (userEntity in userEntities) {
            userMap[userEntity.id] = userEntity.toUser()
        }
        client.getCurrentUser()?.let {
            userMap[it.id] = it
        }

        return userMap
    }

    suspend fun storeStateForChannels(channelsResponse: List<Channel>) {
        val users = mutableSetOf<User>()
        val configs: MutableMap<String, Config> = mutableMapOf()
        // start by gathering all the users
        val messages = mutableListOf<Message>()
        for (channel in channelsResponse) {

            activeChannelMap.get(channel.cid)?.let {
                it.setWatchers(channel.watchers)
                it.setWatcherCount(channel.watcherCount)
            }

            users.add(channel.createdBy)
            configs[channel.type] = channel.config
            for (member in channel.members) {
                users.add(member.user)
            }
            for (read in channel.read) {
                users.add(read.user)
            }
            messages.addAll(channel.messages)

            for (message in channel.messages) {
                users.add(message.user)
                for (reaction in message.latestReactions) {
                    reaction.user?.let { users.add(it) }
                }
            }

        }

        // store the channel configs
        // TODO: only store if the data changed
        insertConfigs(configs)
        // store the users
        insertUsers(users.toList())
        // store the channel data
        insertChannels(channelsResponse)
        // store the messages
        insertMessages(messages)
    }

    suspend fun selectAndEnrichChannel(channelId: String, messageLimit: Int = 0): Channel? {
        val channelStates = selectAndEnrichChannels(listOf(channelId), messageLimit)
        return channelStates.getOrNull(0)
    }

    // TODO: Chat repo should raise a big fat error if the currentUser != client.currentUser

    suspend fun selectAndEnrichChannels(
        channelIds: List<String>,
        messageLimit: Int = 0
    ): List<Channel> {

        // fetch the channel entities from room
        val channelEntities = selectChannelEntities(channelIds)

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
            if (messageLimit > 0) {
                val messages = selectMessagesForChannel(channelEntity.cid, messageLimit)
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
        val userMap = selectUserMap(userIds.toList())

        // convert the channels
        val channels = mutableListOf<Channel>()
        for (channelEntity in channelEntities) {
            val channel = channelEntity.toChannel(userMap)
            // get the config we have stored offline
            channelConfigs.get(channel.type)?.let {
                channel.config = it
            }

            if (messageLimit > 0) {
                val messageEntities = channelMessagesMap[channel.cid] ?: emptyList()
                val messages = messageEntities.map { it.toMessage(userMap) }
                channel.messages = messages
            }

            channels.add(channel)
        }
        return channels.toList()
    }


}

var gson = Gson()