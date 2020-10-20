package io.getstream.chat.android.livedata

import io.getstream.chat.android.client.models.Channel
import io.getstream.chat.android.client.models.Message
import io.getstream.chat.android.client.models.User
import io.getstream.chat.android.livedata.entity.ChannelEntity
import io.getstream.chat.android.livedata.entity.MessageEntity
import io.getstream.chat.android.livedata.extensions.users

/**
 * EventBatchUpdate helps you efficiently implement a 4 step batch update process
 * It updates multiple messages, users and channels at once.
 *
 * val batch = EventBatchUpdate(domainImpl)
 *
 * as a first step specify which channels and messages to fetch
 * batch.addToFetchChannels()
 * batch.addToFetchMessages()
 *
 * as a second step, load the required data for batch updating using
 * batch.fetch()
 *
 * third, add the required updates via
 * addUser, addChannel and addMessage methods
 *
 * fourth, execute the batch using
 * batch.execute()
 */
internal class EventBatchUpdate(private val domainImpl: ChatDomainImpl) {
    val users: MutableMap<String, User> = mutableMapOf()
    val channels: MutableMap<String, ChannelEntity> = mutableMapOf()

    val messages: MutableMap<String, MessageEntity> = mutableMapOf()
    private val channelsToFetch = mutableSetOf<String>()
    private val messagesToFetch = mutableSetOf<String>()

    private lateinit var channelMap: Map<String, ChannelEntity>
    private lateinit var messageMap: Map<String, MessageEntity>
    private var fetchCompleted: Boolean = false

    fun addMessageData(cid: String, message: Message) {
        require(fetchCompleted) { "be sure to run batch.fetch before calling this method" }
        addMessage(MessageEntity(message), message.users())

        getCurrentChannel(cid)?.let {
            it.updateLastMessage(MessageEntity(message))
            addChannelEntity(it, emptyList())
        }
    }

    fun addChannel(channel: Channel) {
        require(fetchCompleted) { "be sure to run batch.fetch before calling this method" }
        // ensure we store all users for this channel
        addUsers(channel.users())
        // TODO: this overwrites members which in the case when you have > 100 members isn't the right behaviour
        channels[channel.cid] = ChannelEntity(channel)
    }

    fun addChannelEntity(channelEntity: ChannelEntity, channelUsers: List<User>) {
        require(fetchCompleted) { "be sure to run batch.fetch before calling this method" }
        // ensure we store all users for this channel
        addUsers(channelUsers)
        channels[channelEntity.cid] = channelEntity
    }

    fun getCurrentChannel(cId: String): ChannelEntity? {
        require(fetchCompleted) { "be sure to run batch.fetch before calling this method" }
        return channels[cId] ?: channelMap[cId]
    }

    fun getCurrentMessage(messageId: String): MessageEntity? {
        require(fetchCompleted) { "be sure to run batch.fetch before calling this method" }
        return messages[messageId] ?: messageMap[messageId]
    }

    fun addMessage(messageEntity: MessageEntity, messageUsers: List<User>) {
        require(fetchCompleted) { "be sure to run batch.fetch before calling this method" }
        // ensure we store all users for this channel
        addUsers(messageUsers)
        messages[messageEntity.id] = messageEntity
    }

    fun addUsers(newUsers: List<User>) {
        require(fetchCompleted) { "be sure to run batch.fetch before calling this method" }
        users.putAll(newUsers.associateBy(User::id))
    }

    fun addUser(newUser: User) {
        require(fetchCompleted) { "be sure to run batch.fetch before calling this method" }
        addUsers(listOf(newUser))
    }

    fun addToFetchChannels(cIds: List<String>) {
        channelsToFetch += cIds
    }

    fun addToFetchChannels(cId: String) {
        channelsToFetch += listOf(cId)
    }

    fun addToFetchMessages(ids: List<String>) {
        messagesToFetch += ids
    }

    fun addToFetchMessages(id: String) {
        messagesToFetch += id
    }

    suspend fun fetch() {
        messageMap = domainImpl.repos.messages.select(messagesToFetch.toList()).associateBy { it.id }
        channelMap = domainImpl.repos.channels.select(channelsToFetch.toList()).associateBy { it.cid }
        fetchCompleted = true
    }

    suspend fun execute() {
        // actually insert the data
        users.remove(domainImpl.currentUser.id)?.let { domainImpl.updateCurrentUser(it) }
        domainImpl.repos.users.insert(users.values.toList())
        domainImpl.repos.channels.insert(channels.values.toList())
        // we only cache messages for which we're receiving events
        domainImpl.repos.messages.insert(messages.values.toList(), true)
    }
}
