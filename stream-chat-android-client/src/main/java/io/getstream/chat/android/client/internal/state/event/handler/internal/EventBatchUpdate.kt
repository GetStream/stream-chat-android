/*
 * Copyright (c) 2014-2026 Stream.io Inc. All rights reserved.
 *
 * Licensed under the Stream License;
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *    https://github.com/GetStream/stream-chat-android/blob/main/LICENSE
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package io.getstream.chat.android.client.internal.state.event.handler.internal

import io.getstream.chat.android.client.api.state.GlobalState
import io.getstream.chat.android.client.extensions.internal.updateLastMessage
import io.getstream.chat.android.client.extensions.internal.updateUsers
import io.getstream.chat.android.client.extensions.internal.users
import io.getstream.chat.android.client.persistance.repository.RepositoryFacade
import io.getstream.chat.android.client.utils.message.latestOrNull
import io.getstream.chat.android.models.Channel
import io.getstream.chat.android.models.Message
import io.getstream.chat.android.models.Poll
import io.getstream.chat.android.models.Thread
import io.getstream.chat.android.models.User
import io.getstream.log.StreamLog
import io.getstream.log.taggedLogger
import java.util.Date

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
 * batch.addUser, addChannel, addMessage and addThread methods
 *
 * fourth, execute the batch using
 * batch.execute()
 */
@Suppress("LongParameterList")
internal class EventBatchUpdate private constructor(
    private val id: Int,
    private val currentUserId: String,
    private val globalState: GlobalState,
    private val repos: RepositoryFacade,
    private val channelMap: MutableMap<String, Channel>,
    private val messageMap: MutableMap<String, Message>,
    private val threadMap: MutableMap<String, Thread>,
    private val userMap: MutableMap<String, User>,
) {

    private val logger by taggedLogger(TAG)

    /**
     * Adds the message and updates the last message for the given channel.
     * Increments the unread count if the right conditions apply.
     */
    fun addMessageData(receivedEventDate: Date, cid: String, message: Message) {
        addMessage(message)
        getCurrentChannel(cid)
            ?.updateLastMessage(receivedEventDate, message, currentUserId)
            ?.let(::addChannel)
    }

    @Suppress("ForbiddenComment")
    fun addChannel(channel: Channel) {
        logger.v {
            "[addChannel] id: $id" +
                ", channel.lastMessageAt: ${channel.lastMessageAt}" +
                ", channel.latestMessageId: ${channel.messages.latestOrNull()?.id}"
        }
        // ensure we store all users for this channel
        addUsers(channel.users())
        // TODO: this overwrites members which in the case when you have > 100 members isn't the right behaviour
        channelMap += (channel.cid to channel)
    }

    fun addThread(thread: Thread) {
        threadMap += (thread.parentMessageId to thread)
    }

    fun getCurrentChannel(cId: String): Channel? = channelMap[cId]
    fun getCurrentMessage(messageId: String): Message? = messageMap[messageId]
    fun getCurrentThread(threadId: String): Thread? = threadMap[threadId]

    fun addMessage(message: Message) {
        // ensure we store all users for this channel
        addUsers(message.users())
        messageMap += (message.id to message)
    }

    fun addUsers(newUsers: List<User>) {
        newUsers.forEach { user ->
            if (userMap.containsKey(user.id).not()) {
                userMap[user.id] = user
            }
        }
    }

    fun addUser(newUser: User) {
        userMap += (newUser.id to newUser)
    }

    fun getPoll(pollId: String): Poll? =
        messageMap.values
            .asSequence()
            .mapNotNull { it.poll }
            .firstOrNull { it.id == pollId }

    fun addPoll(poll: Poll) {
        messageMap.values
            .filter { it.poll?.id == poll.id }
            .forEach { addMessage(it.copy(poll = poll)) }
    }

    /**
     * Removes the [Poll] from the [Message]s in the cache.
     */
    fun deletePoll(poll: Poll) {
        messageMap.values
            .filter { it.poll?.id == poll.id }
            .forEach { addMessage(it.copy(poll = null)) }
    }

    suspend fun execute() {
        // actually insert the data
        currentUserId?.let { userMap -= it }
        logger.v { "[execute] id: $id, channelMap.size: ${channelMap.size}" }

        repos.insertUsers(userMap.values.toList())
        repos.insertChannels(channelMap.values.updateUsers(userMap))
        repos.insertMessages(messageMap.values.toList().updateUsers(userMap))
        repos.insertThreads(threadMap.values.toList())
    }

    internal class Builder(
        private val id: Int,
    ) {
        private val channelsToFetch = mutableSetOf<String>()
        private val channelsToRemove = mutableSetOf<String>()
        private val messagesToFetch = mutableSetOf<String>()
        private val users = mutableSetOf<User>()
        private val pollsToFetch = mutableSetOf<String>()
        private val threadsToFetch = mutableSetOf<String>()

        fun addToFetchChannels(cIds: List<String>) {
            channelsToFetch += cIds
        }

        fun addToRemoveChannels(cIds: List<String>) {
            channelsToRemove += cIds
        }

        fun addToFetchChannels(cId: String) {
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

        fun addPollToFetch(pollId: String) {
            pollsToFetch += pollId
        }

        fun addThreadToFetch(threadId: String) {
            threadsToFetch += threadId
        }

        suspend fun build(
            globalState: GlobalState,
            repos: RepositoryFacade,
            currentUserId: String,
        ): EventBatchUpdate {
            channelsToRemove.forEach { repos.deleteChannel(it) }
            // Update users in DB in order to fetch channels and messages with sync data.
            repos.insertUsers(users)
            val messageMap: Map<String, Message> =
                (
                    repos.selectMessages(messagesToFetch.toList()) +
                        pollsToFetch.flatMap { repos.selectMessagesWithPoll(it) }
                    ).associateBy(Message::id)

            val channelMap: Map<String, Channel> =
                repos.selectChannels(channelsToFetch.toList()).associateBy(Channel::cid)
            val threadMap: Map<String, Thread> =
                repos.selectThreads(threadsToFetch.toList()).associateBy(Thread::parentMessageId)
            StreamLog.v(TAG) {
                "[builder.build] id: $id, messageMap.size: ${messageMap.size}" +
                    ", channelMap.size: ${channelMap.size}"
            }

            return EventBatchUpdate(
                id,
                currentUserId,
                globalState,
                repos,
                channelMap.toMutableMap(),
                messageMap.toMutableMap(),
                threadMap.toMutableMap(),
                users.associateBy(User::id).toMutableMap(),
            )
        }
    }

    private companion object {
        private const val TAG = "Chat:EventBatchUpdate"
    }
}
