package io.getstream.chat.android.livedata

import io.getstream.chat.android.client.models.Channel
import io.getstream.chat.android.client.models.Message
import io.getstream.chat.android.client.models.User
import io.getstream.chat.android.livedata.extensions.updateLastMessage
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
internal class EventBatchUpdate private constructor(
    private val domainImpl: ChatDomainImpl,
    channelMap: Map<String, Channel>,
    messageMap: Map<String, Message>
) {
    private var users: Map<String, User> = emptyMap()
    private var channels: Map<String, Channel> = channelMap
    private var messages: Map<String, Message> = messageMap

    internal class Builder {
        private var channelsToFetch = setOf<String>()
        private var messagesToFetch = setOf<String>()
        private var users = setOf<User>()

        fun addToFetchChannels(cIds: List<String>) {
            channelsToFetch = channelsToFetch + cIds
        }

        fun addToFetchChannels(cId: String) {
            channelsToFetch = channelsToFetch + listOf(cId)
        }

        fun addToFetchMessages(ids: List<String>) {
            messagesToFetch = messagesToFetch + ids
        }

        fun addToFetchMessages(id: String) {
            messagesToFetch = messagesToFetch + id
        }

        fun addUsers(usersToAdd: List<User>) {
            users = users + usersToAdd
        }

        suspend fun build(domainImpl: ChatDomainImpl): EventBatchUpdate {
            val messageMap: Map<String, Message> = domainImpl.repos.selectMessages(messagesToFetch.toList()).associateBy(Message::id)
            val channelMap: Map<String, Channel> = domainImpl.repos.selectChannels(channelsToFetch.toList(), domainImpl.defaultConfig).associateBy(Channel::cid)
            return EventBatchUpdate(domainImpl, channelMap, messageMap)
        }
    }

    fun addMessageData(cid: String, message: Message) {
        addMessage(message)

        getCurrentChannel(cid)?.also { channel -> channel.updateLastMessage(message) }
    }

    fun addChannel(channel: Channel) {
        // ensure we store all users for this channel
        addUsers(channel.users())
        // TODO: this overwrites members which in the case when you have > 100 members isn't the right behaviour
        channels = channels + (channel.cid to channel)
    }

    fun getCurrentChannel(cId: String): Channel? = channels[cId]
    fun getCurrentMessage(messageId: String): Message? = messages[messageId]

    fun addMessage(message: Message) {
        // ensure we store all users for this channel
        addUsers(message.users())
        messages = messages + (message.id to message)
    }

    fun addUsers(newUsers: List<User>) {
        users = users + newUsers.associateBy(User::id)
    }

    fun addUser(newUser: User) {
        users = users + (newUser.id to newUser)
    }

    suspend fun execute() {
        // actually insert the data
        val currentUser = domainImpl.currentUser
        domainImpl.updateCurrentUser(currentUser)
        users = users - currentUser.id
        domainImpl.repos.users.insert(users.values.toList())
        domainImpl.repos.channels.insertChannels(channels.values)
        // we only cache messages for which we're receiving events
        domainImpl.repos.messages.insert(messages.values.toList(), true)
    }
}
