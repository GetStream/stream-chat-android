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
    private val channelMap: MutableMap<String, Channel>,
    private val messageMap: MutableMap<String, Message>,
    private val userMap: MutableMap<String, User>
) {

    fun addMessageData(cid: String, message: Message) {
        addMessage(message)
        getCurrentChannel(cid)?.also { channel -> channel.updateLastMessage(message) }
    }

    fun addChannel(channel: Channel) {
        // ensure we store all users for this channel
        addUsers(channel.users())
        // TODO: this overwrites members which in the case when you have > 100 members isn't the right behaviour
        channelMap += (channel.cid to channel)
    }

    fun getCurrentChannel(cId: String): Channel? = channelMap[cId]
    fun getAllChannels(): List<Channel> = channelMap.values.toList()
    fun getCurrentMessage(messageId: String): Message? = messageMap[messageId]

    fun addMessage(message: Message) {
        // ensure we store all users for this channel
        addUsers(message.users())
        messageMap += (message.id to message)
    }

    fun addUsers(newUsers: List<User>) {
        userMap += newUsers.associateBy(User::id)
    }

    fun addUser(newUser: User) {
        userMap += (newUser.id to newUser)
    }

    suspend fun execute() {
        // actually insert the data
        userMap -= domainImpl.currentUser.id
        domainImpl.repos.users.insert(userMap.values.toList())
        // we only cache messages for which we're receiving events
        domainImpl.repos.messages.insert(messageMap.values.toList(), true)
        domainImpl.repos.channels.insertChannels(channelMap.values)
    }

    internal class Builder {
        private val channelsToFetch = mutableSetOf<String?>()
        private val messagesToFetch = mutableSetOf<String>()
        private val users = mutableSetOf<User>()

        fun addToFetchChannels(cIds: List<String?>) {
            channelsToFetch += cIds
        }

        fun addToFetchChannels(cId: String?) {
            channelsToFetch += cId
        }

        fun addToFetchMessages(ids: List<String>) {
            messagesToFetch += ids
        }

        fun addToFetchMessages(id: String) {
            messagesToFetch += id
        }

        fun addUsers(usersToAdd: List<User>) {
            users += usersToAdd
        }

        suspend fun build(domainImpl: ChatDomainImpl): EventBatchUpdate {
            val messageMap: Map<String, Message> =
                domainImpl.repos.selectMessages(messagesToFetch.toList()).associateBy(Message::id)
            val channelMap: Map<String, Channel> =
                domainImpl.repos.selectChannels(channelsToFetch.toList(), domainImpl.defaultConfig)
                    .associateBy(Channel::cid)
            return EventBatchUpdate(
                domainImpl,
                channelMap.toMutableMap(),
                messageMap.toMutableMap(),
                users.associateBy(User::id).toMutableMap()
            )
        }
    }
}
