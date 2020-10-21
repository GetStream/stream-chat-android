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
 * val batchBuilder = EventBatchUpdate.Builder()
 *
 * as a first step specify which channels and messages to fetch
 * batchBuilder.addToFetchChannels()
 * batchBuilder.addToFetchMessages()
 *
 * as a second step, load the required data for batch updating using
 * val batch = batchBuilder.build(domainImpl)
 *
 * third, add the required updates via
 * batch.addUser, addChannel and addMessage methods
 *
 * fourth, execute the batch using
 * batch.execute()
 */
internal class EventBatchUpdate private constructor (private val domainImpl: ChatDomainImpl, private val channelMap: Map<String, ChannelEntity>, private val messageMap: Map<String, MessageEntity>) {
    val users: MutableMap<String, User> = mutableMapOf()
    val channels: MutableMap<String, ChannelEntity> = mutableMapOf()
    val messages: MutableMap<String, MessageEntity> = mutableMapOf()

    internal class Builder {
        private val channelsToFetch = mutableSetOf<String>()
        private val messagesToFetch = mutableSetOf<String>()

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

        fun build(domainImpl: ChatDomainImpl): EventBatchUpdate {
            // TODO fix it
            val messageMap: Map<String, MessageEntity> = emptyMap()//domainImpl.repos.messages.select(messagesToFetch.toList()).associateBy { it.id }
            val channelMap: Map<String, ChannelEntity> = emptyMap()//domainImpl.repos.channels.select(channelsToFetch.toList()).associateBy { it.cid }
            return EventBatchUpdate(domainImpl, channelMap, messageMap)
        }
    }

    fun addMessageData(cid: String, message: Message) {
        // TODO rewrite it
        /*addMessage(MessageEntity(message), message.users())

        getCurrentChannel(cid)?.let {
            it.updateLastMessage(MessageEntity(message))
            addChannelEntity(it, emptyList())
        }*/
    }

    fun addChannel(channel: Channel) {
        // ensure we store all users for this channel
        addUsers(channel.users())
        // TODO: this overwrites members which in the case when you have > 100 members isn't the right behaviour
        channels[channel.cid] = ChannelEntity(channel)
    }

    fun addChannelEntity(channelEntity: ChannelEntity, channelUsers: List<User>) {
        // ensure we store all users for this channel
        addUsers(channelUsers)
        channels[channelEntity.cid] = channelEntity
    }

    fun getCurrentChannel(cId: String): ChannelEntity? {
        return channels[cId] ?: channelMap[cId]
    }

    fun getCurrentMessage(messageId: String): MessageEntity? {
        return messages[messageId] ?: messageMap[messageId]
    }

    fun addMessage(messageEntity: MessageEntity, messageUsers: List<User>) {
        // ensure we store all users for this channel
        addUsers(messageUsers)
        messages[messageEntity.id] = messageEntity
    }

    fun addUsers(newUsers: List<User>) {
        users.putAll(newUsers.associateBy(User::id))
    }

    fun addUser(newUser: User) {
        addUsers(listOf(newUser))
    }

    suspend fun execute() {
        // actually insert the data
        users.remove(domainImpl.currentUser.id)?.let { domainImpl.updateCurrentUser(it) }
        domainImpl.repos.users.insert(users.values.toList())
        domainImpl.repos.channels.insert(channels.values.toList())
        // we only cache messages for which we're receiving events
        // TODO fixit
        //domainImpl.repos.messages.insert(messages.values.toList(), true)
    }
}
