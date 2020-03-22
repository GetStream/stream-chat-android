package com.getstream.sdk.chat.livedata

import android.content.Context
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.liveData
import androidx.test.core.app.ApplicationProvider
import com.getstream.sdk.chat.livedata.dao.*
import com.getstream.sdk.chat.livedata.entity.*
import io.getstream.chat.android.client.ChatClient
import io.getstream.chat.android.client.api.models.QueryChannelsRequest
import io.getstream.chat.android.client.errors.ChatError
import io.getstream.chat.android.client.events.*
import io.getstream.chat.android.client.models.Channel
import io.getstream.chat.android.client.models.Message
import io.getstream.chat.android.client.models.Reaction
import io.getstream.chat.android.client.models.User
import io.getstream.chat.android.client.utils.observable.Subscription
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.launch


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
 * repo.unreadCount livedata object returns the current unread count for this user
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

    constructor(context: Context, userId: String, client: ChatClient): this(client) {
        val database = ChatDatabase.getDatabase(context, userId)
        channelQueryDao = database.queryChannelsQDao()
        userDao = database.userDao()
        reactionDao = database.reactionDao()
        messageDao = database.messageDao()
        channelStateDao = database.channelStateDao()
        channelConfigDao = database.channelConfigDao()
    }
    var online = false
    /** if we should enable offline storage or not */
    var offlineEnabled = true

    private lateinit var eventSubscription: Subscription
    /** stores the mapping from cid to channelRepository */
    private var activeChannelMap: MutableMap<String, StreamChatChannelRepository> = mutableMapOf()


    private val _errorEvent = MutableLiveData<Event<ChatError>>()


    // TODO: implement retry policy
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

    fun generateMessageId(): String {
        return "user-id"
    }

    suspend fun selectMessageEntity(messageId: String): MessageEntity? {
        return messageDao.select(messageId)
    }

    fun channel(channelType: String, channelId: String): StreamChatChannelRepository {
        val cid = "%s:%s".format(channelType, channelId)
        if (!activeChannelMap.containsKey(cid)) {
            val channelRepo = StreamChatChannelRepository(channelType, channelId, client, this)
            activeChannelMap.put(cid, channelRepo)
        }
        return activeChannelMap.getValue(cid)
    }

    fun setOffline() {
        online = false
    }
    fun setOnline() {
        online = true
    }

    fun isOnline(): Boolean {
        return online
    }
    fun isOffline(): Boolean {
        return !online
    }

    fun recover() {
        // run query channels for queries that we are showing

        // what else?
    }

    fun stopListening() {

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
        // return a livedata object with the channels
        var channelsLiveData = liveData(Dispatchers.IO) {
            // start by getting the query results from offline storage
            val query = channelQueryDao.select(queryChannelsEntity.id)
            // TODO: we should use a transform so it's based on the livedata perhaps?
            if (query != null) {
                val channelEntities = channelStateDao.select(query.channelCIDs)

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
                // check for an error
                if (!it.isSuccess) {
                    addError(it.error())
                }
                // store the results in the database
                val channelsResponse = it.data()
                val users = mutableListOf<User>()
                for (channel in channelsResponse) {
                    users.add(channel.createdBy)
                    // TODO member loop, watcher loop etc...
                }
                // store the users
                insertUsers(users)
                // store the channel info
                insertChannels(channelsResponse)



            }

        }


        return channelsLiveData


    }



    fun messagesForChannel(cid: String, limit: Int = 100, offset: Int = 0): LiveData<List<MessageEntity>> {
        var messagesLiveData = liveData(Dispatchers.IO) {
            val messages = messageDao.messagesForChannel(cid, limit, offset)
            emitSource(messages)
        }
        return messagesLiveData
    }


    fun startListening() {
        eventSubscription = client.events().subscribe {
            // keep the data in Room updated based on the various events..
            when (it) {
                is NewMessageEvent, is MessageDeletedEvent, is MessageUpdatedEvent  -> {
                    insertMessage(it.message)
                }
                is MessageReadEvent -> {

                }
                is ReactionNewEvent -> {

                }
                is ReactionDeletedEvent -> {

                }
                is MemberAddedEvent, is MemberRemovedEvent, is MemberUpdatedEvent -> {

                }
                is ChannelUpdatedEvent, is ChannelHiddenEvent, is ChannelDeletedEvent -> {

                }
                is NotificationAddedToChannelEvent, is NotificationMarkReadEvent -> {

                }
            }
        }
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
        // TODO: Assign a message id here somewhere...
        GlobalScope.launch {
            val messageEntities = mutableListOf<MessageEntity>()
            for (message in messages) {
                messageEntities.add(MessageEntity(message))
            }
            messageDao.insertMany(messageEntities)
        }
    }

    fun insertMessage(message: Message) {
        // TODO: Assign a message id here somewhere...
        GlobalScope.launch {
            messageDao.insert(MessageEntity(message))
        }
    }

    fun insertMessageEntity(messageEntity: MessageEntity) {
        // TODO: Assign a message id here somewhere...
        GlobalScope.launch {
            messageDao.insert(messageEntity)
        }
    }

    fun connectionRecovered() {
        // update the results for queries that are actively being shown right now
        // TODO: how do we know this?

        // update the data for all channels that are being show right now...
    }

    suspend fun selectChannelEntity(cid: String): ChannelStateEntity? {
        return channelStateDao.select(cid)
    }


}