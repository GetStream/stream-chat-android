package com.getstream.sdk.chat.livedata

import android.content.Context
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.liveData
import com.getstream.sdk.chat.livedata.dao.*
import com.getstream.sdk.chat.livedata.entity.*
import com.getstream.sdk.chat.livedata.entity.UserEntity
import com.google.gson.Gson
import io.getstream.chat.android.client.ChatClient
import io.getstream.chat.android.client.api.models.QueryChannelsRequest
import io.getstream.chat.android.client.errors.ChatError
import io.getstream.chat.android.client.events.*
import io.getstream.chat.android.client.logger.ChatLogger
import io.getstream.chat.android.client.models.*
import io.getstream.chat.android.client.utils.observable.Subscription
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.launch
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
    var client: ChatClient
) {

    lateinit var channelQueryDao: ChannelQueryDao
    lateinit var userDao: UserDao
    lateinit var reactionDao: ReactionDao
    lateinit var messageDao: MessageDao
    lateinit var channelStateDao: ChannelStateDao
    lateinit var channelConfigDao: ChannelConfigDao
    private val logger = ChatLogger.get("ChatRepo")

    constructor(context: Context, userId: String, client: ChatClient): this(client) {
        val database = ChatDatabase.getDatabase(context, userId)
        channelQueryDao = database.queryChannelsQDao()
        userDao = database.userDao()
        reactionDao = database.reactionDao()
        messageDao = database.messageDao()
        channelStateDao = database.channelStateDao()
        channelConfigDao = database.channelConfigDao()
    }

    init {
        startListening()
    }

    private val _online = MutableLiveData<Boolean>()
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
    val totalUnreadCount : LiveData<Int> = _totalUnreadCount

    private val _channelUnreadCount = MutableLiveData<Int>()

    /**
     * the number of unread channels for the current user
     */
    val channelUnreadCount : LiveData<Int> = _channelUnreadCount


    // TODO: implement retry policy

    private val _errorEvent = MutableLiveData<Event<ChatError>>()
    /**
     * The error event livedata object is triggered when errors in the underlying components occure.
     * The following example shows how to observe these errors
     *
     *  repo.errorEvent.observe(this, EventObserver {
     *       // create a toast
     *   })
     *
     */
    val errorEvents: LiveData<Event<ChatError>> = _errorEvent

    fun addError(error: ChatError) {
        _errorEvent.value = Event(error)
    }

    /** if we should enable offline storage or not */
    var offlineEnabled = true // TODO we will implement support for disabling offline support in 1.1

    /** the event subscription, cancel using repo.stopListening */
    private var eventSubscription: Subscription? = null
    /** stores the mapping from cid to channelRepository */
    var activeChannelMap: MutableMap<String, StreamChatChannelRepository> = mutableMapOf()

    /** stores the mapping from cid to channelRepository */
    var activeQueryMap: MutableMap<QueryChannelsEntity, StreamQueryChannelRepository> = mutableMapOf()

    /**
     * Start listening to chat events and keep the room database in sync
     */
    fun startListening() {
        if (eventSubscription != null) {
            return
        }
        eventSubscription = client.events().subscribe {
            // keep the data in Room updated based on the various events..
            // TODO: cache messages and channels to reduce number of Room queries
            GlobalScope.launch(Dispatchers.IO) {

                // any event can have channel and unread count information
                if (it.unreadChannels != null) {
                    _channelUnreadCount.value = it.unreadChannels
                }
                if (it.totalUnreadCount != null) {
                    // TODO: we should deduplicate livedata updates in case the values didn't change
                    _totalUnreadCount.value = it.totalUnreadCount
                }

                // the watchers and watcher count are only stored in memory (as they go stale when you go offline)
                if (!it.cid.isNullOrEmpty() && activeChannelMap.containsKey(it.cid)) {
                    val channel = activeChannelMap.get(it.cid)!!
                    it.channel?.watchers?.let {
                        channel.setWatchers(it)
                    }
                    it.channel?.watcherCount?.let {
                        channel.setWatcherCount(it)
                    }
                }

                // connection events
                when (it) {
                    is DisconnectedEvent -> {
                        _online.value = false
                    }
                    is ConnectedEvent -> {
                        _online.value = true
                    }
                }

                // notifications of messages on channels you're a member of but not watching
                if (it is NotificationAddedToChannelEvent) {
                    // this one is trickier. we need to insert the message
                    // we also need to add the channel to the query which is tricker...
                    if (offlineEnabled) insertMessage(it.message)
                    for ((_, queryRepo) in activeQueryMap) {
                        queryRepo.handleMessageNotification(it as NotificationAddedToChannelEvent)
                    }
                }

                if (offlineEnabled) {

                    when (it) {
                        is NewMessageEvent, is MessageDeletedEvent, is MessageUpdatedEvent -> {
                            insertMessage(it.message)
                        }
                        is MessageReadEvent -> {
                            // get the channel, update reads, write the channel
                            val channel = channelStateDao.select(it.cid)
                            val read = ChannelUserRead()
                            // TODO: cleanup the !!
                            read.user = it.user!!
                            read.lastRead = it.createdAt
                            channel?.let {
                                it.updateReads(read)
                                insertChannelStateEntity(channel)
                            }
                        }
                        is ReactionNewEvent -> {
                            // get the message, update the reaction data, update the message
                            insertMessage(it.message)
                        }
                        is ReactionDeletedEvent -> {
                            // get the message, update the reaction data, update the message
                            insertMessage(it.message)
                        }
                        is MemberAddedEvent, is MemberRemovedEvent, is MemberUpdatedEvent -> {
                            // get the channel, update members, write the channel
                            it.channel?.let {
                                insertChannel(it)
                            }
                        }
                        is ChannelUpdatedEvent, is ChannelHiddenEvent, is ChannelDeletedEvent -> {
                            // get the channel, update members, write the channel
                            it.channel?.let {
                                insertChannel(it)
                            }
                        }

                    }
                }
            }
        }
    }

    /**
     * Stop listening to chat events
     */
    fun stopListening() {
        eventSubscription.unsubscribe()
    }

    /**
     * repo.channel("messaging", "12") return a ChatChannelRepository
     */
    fun channel(channelType: String, channelId: String): StreamChatChannelRepository {
        val cid = "%s:%s".format(channelType, channelId)
        if (!activeChannelMap.containsKey(cid)) {
            val channelRepo = StreamChatChannelRepository(channelType, channelId, client, this)
            activeChannelMap.put(cid, channelRepo)
        }
        return activeChannelMap.getValue(cid)
    }




    fun generateMessageId(): String {
        checkNotNull(client.getCurrentUser()) {"client.getCurrentUser() must be available to generate a message id"}
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
        return _online.value!!
    }
    fun isOffline(): Boolean {
        return _online.value!!
    }




    /**
     * queryChannels
     * - first read the current results from Room
     * - if we are online make the API call to update results
     */
    fun queryChannels(
        queryChannelsEntity: QueryChannelsEntity,
        request: QueryChannelsRequest
    ): LiveData<MutableList<Channel>> {
        // mark this query as active
        activeQueryMap[queryChannelsEntity] = StreamQueryChannelRepository()
        // return a livedata object with the channels
        var channelsLiveData = liveData(Dispatchers.IO) {
            // start by getting the query results from offline storage
            val query = channelQueryDao.select(queryChannelsEntity.id)
            // TODO: we should use a transform so it's based on the livedata perhaps?
            if (query != null) {
                val channelEntities = channelStateDao.select(query.channelCIDs)


                // TODO: I should use sets for many of these
                // gather all the user ids
                val userIds = mutableListOf<String>()
                for (channelEntity in channelEntities) {
                    channelEntity.createdByUserId?.let { userIds.add(it) }
                    channelEntity.members?.let {
                        for (member in it) {
                            userIds.add(member.userId)
                        }
                    }
                    channelEntity.lastMessage?.let {
                        userIds.add(it.userId)
                    }
                }
                val userEntities = userDao.select(userIds)
                val userMap = mutableMapOf<String, User>()
                for (userEntity in userEntities) {
                    userMap[userEntity.id] = userEntity.toUser()
                }

                val channels = mutableListOf<Channel>()
                for (channelEntity in channelEntities) {
                    val channel = channelEntity.toChannel(userMap)
                    channels.add(channel)
                }

                emit(channels)
            }
            // next run the actual query
            client.queryChannels(request).enqueue {
                // TODO: This storage logic can be merged with the StreamChatChannelRepo
                // TODO store the channel configs



                // check for an error
                if (!it.isSuccess) {
                    addError(it.error())
                }
                // store the results in the database
                val channelsResponse = it.data()
                val users = mutableListOf<User>()
                val configs :MutableMap<String, Config> = mutableMapOf()
                for (channel in channelsResponse) {
                    users.add(channel.createdBy)
                    configs[channel.type] = channel.config
                    // TODO member loop
                }

                // store the channel configs
                insertConfigs(configs)

                // store the users
                insertUsers(users)
                // store the channel info
                insertChannels(channelsResponse)



            }

        }


        return channelsLiveData


    }

    fun insertConfigs(configs: MutableMap<String, Config>) {
        val configEntities = mutableListOf<ChannelConfigEntity>()
        for ((channelType, config) in configs) {
            val entity = ChannelConfigEntity(channelType)
            entity.config = config
        }
        GlobalScope.launch {
            channelConfigDao.insertMany(configEntities)
        }
    }


    fun messagesForChannel(cid: String, limit: Int = 100, offset: Int = 0): LiveData<List<MessageEntity>> {
        var messagesLiveData = liveData(Dispatchers.IO) {
            val messages = messageDao.messagesForChannel(cid, limit, offset)
            emitSource(messages)
        }
        return messagesLiveData
    }




    fun insertUser(user: User) {
        GlobalScope.launch {
            userDao.insert(UserEntity(user))
        }

    }

    fun insertChannel(channel: Channel) {
        var channelEntity = ChannelStateEntity(channel)

        GlobalScope.launch {
            channelStateDao.insert(ChannelStateEntity(channel))
        }
    }
    fun insertChannelStateEntity(channelStateEntity: ChannelStateEntity) {

        GlobalScope.launch {
            channelStateDao.insert(channelStateEntity)
        }
    }
    fun insertChannels(channels: List<Channel>) {
        var entities = mutableListOf<ChannelStateEntity>()
        for (channel in channels) {
            entities.add(ChannelStateEntity(channel))
        }

        GlobalScope.launch {
            channelStateDao.insertMany(entities)
        }
    }



    fun insertReactionEntity(reactionEntity: ReactionEntity) {
        GlobalScope.launch {
            reactionDao.insert(reactionEntity)
        }
    }

    fun insertQuery(queryChannelsEntity: QueryChannelsEntity) {
        GlobalScope.launch {
            channelQueryDao.insert(queryChannelsEntity)
        }
    }

    fun insertUsers(users: List<User>) {
        GlobalScope.launch {
            val userEntities = mutableListOf<UserEntity>()
            for (user in users) {
                userEntities.add(UserEntity(user))
            }
            userDao.insertMany(userEntities)
        }
    }

    fun insertMessages(messages: List<Message>) {
        GlobalScope.launch {
            val messageEntities = mutableListOf<MessageEntity>()
            for (message in messages) {
                messageEntities.add(MessageEntity(message))
            }
            messageDao.insertMany(messageEntities)
        }
    }

    fun insertMessage(message: Message) {
        GlobalScope.launch {
            messageDao.insert(MessageEntity(message))
        }
    }

    fun insertMessageEntity(messageEntity: MessageEntity) {
        GlobalScope.launch {
            messageDao.insert(messageEntity)
        }
    }

    fun connectionRecovered() {
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

    fun retryFailedEntities() {
        GlobalScope.launch {
            // fake the user map for enriching objects
            val user = client.getCurrentUser()!!
            val userMap : Map<String, User> = mutableMapOf(user.id to user)

            val reactionEntities = reactionDao.selectSyncNeeded()
            for (reactionEntity in reactionEntities) {
                client.sendReaction(reactionEntity.toReaction(userMap))
            }

            val messageEntities = messageDao.selectSyncNeeded()
            for (messageEntity in messageEntities) {
                // TODO: remove this as soon as the low level client has a better solution
                val parts = messageEntity.cid.split(":")
                val channel = client.channel(parts[0], parts[1])
                channel.sendMessage(messageEntity.toMessage(userMap))
            }

            // TODO: support channels in version 1.1


        }
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

    suspend fun selectUsers(userIds: MutableList<String>): List<UserEntity> {
        return userDao.select(userIds)
    }


}

var gson = Gson()