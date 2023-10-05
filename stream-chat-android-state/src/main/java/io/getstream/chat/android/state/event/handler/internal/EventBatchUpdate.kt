/*
 * Copyright (c) 2014-2022 Stream.io Inc. All rights reserved.
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

package io.getstream.chat.android.state.event.handler.internal

import io.getstream.chat.android.client.extensions.internal.incrementUnreadCount
import io.getstream.chat.android.client.extensions.internal.shouldIncrementUnreadCount
import io.getstream.chat.android.client.extensions.internal.updateLastMessage
import io.getstream.chat.android.client.extensions.internal.updateUsers
import io.getstream.chat.android.client.extensions.internal.users
import io.getstream.chat.android.client.persistance.repository.RepositoryFacade
import io.getstream.chat.android.client.utils.message.latestOrNull
import io.getstream.chat.android.models.Channel
import io.getstream.chat.android.models.Message
import io.getstream.chat.android.models.User
import io.getstream.chat.android.state.plugin.state.global.GlobalState
import io.getstream.chat.android.state.utils.internal.isChannelMutedForCurrentUser
import io.getstream.log.StreamLog
import io.getstream.log.taggedLogger

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
@Suppress("LongParameterList")
internal class EventBatchUpdate private constructor(
    private val id: Int,
    private val currentUserId: String?,
    private val globalState: GlobalState,
    private val repos: RepositoryFacade,
    private val channelMap: MutableMap<String, Channel>,
    private val messageMap: MutableMap<String, Message>,
    private val userMap: MutableMap<String, User>,
) {

    private val logger by taggedLogger(TAG)

    /**
     * Adds the message and updates the last message for the given channel.
     * Increments the unread count if the right conditions apply.
     */
    fun addMessageData(cid: String, message: Message, isNewMessage: Boolean = false) {
        addMessage(message)
        getCurrentChannel(cid)
            ?.updateLastMessage(message)
            ?.also { channel ->
                addChannel(channel)
                val currentUserId = currentUserId ?: return

                if (isNewMessage) {
                    val lastReadDate = channel.read.firstOrNull { it.user.id == currentUserId }?.lastMessageSeenDate

                    if (message.shouldIncrementUnreadCount(
                            currentUserId = currentUserId,
                            lastMessageAtDate = lastReadDate,
                            isChannelMuted = globalState.isChannelMutedForCurrentUser(channel.cid),
                        )
                    ) {
                        addChannel(channel.incrementUnreadCount(currentUserId, message.createdAt))
                    }
                }
            }
    }

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

    fun getCurrentChannel(cId: String): Channel? = channelMap[cId]
    fun getCurrentMessage(messageId: String): Message? = messageMap[messageId]

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

    suspend fun execute() {
        // actually insert the data
        currentUserId?.let { userMap -= it }
        enrichChannelsWithCapabilities()
        logger.v { "[execute] id: $id, channelMap.size: ${channelMap.size}" }

        repos.insertUsers(userMap.values.toList())
        repos.insertChannels(channelMap.values.updateUsers(userMap))
        repos.insertMessages(messageMap.values.toList().updateUsers(userMap))
    }

    /**
     * Enriches channels with capabilities if needed.
     * Channels from events don't contain ownCapabilities field therefore,
     * they need to be enriched based on capabilities stored in the cache.
     */
    private suspend fun enrichChannelsWithCapabilities() {
        val channelsWithoutCapabilities = channelMap.values
            .filter { channel -> channel.ownCapabilities.isEmpty() }
            .map { channel -> channel.cid }
        val cachedChannels = repos.selectChannels(channelsWithoutCapabilities)
        logger.v { "[enrichChannelsWithCapabilities] id: $id, cachedChannels.size: ${cachedChannels.size}" }
        channelMap.putAll(cachedChannels.associateBy(Channel::cid))
    }

    internal class Builder(
        private val id: Int,
    ) {
        private val channelsToFetch = mutableSetOf<String>()
        private val messagesToFetch = mutableSetOf<String>()
        private val users = mutableSetOf<User>()

        fun addToFetchChannels(cIds: List<String>) {
            channelsToFetch += cIds
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

        suspend fun build(
            globalState: GlobalState,
            repos: RepositoryFacade,
            currentUserId: String?,
        ): EventBatchUpdate {
            // Update users in DB in order to fetch channels and messages with sync data.
            repos.insertUsers(users)
            val messageMap: Map<String, Message> =
                repos.selectMessages(messagesToFetch.toList()).associateBy(Message::id)
            val channelMap: Map<String, Channel> =
                repos.selectChannels(channelsToFetch.toList()).associateBy(Channel::cid)
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
                users.associateBy(User::id).toMutableMap(),
            )
        }
    }

    private companion object {
        private const val TAG = "Chat:EventBatchUpdate"
    }
}
