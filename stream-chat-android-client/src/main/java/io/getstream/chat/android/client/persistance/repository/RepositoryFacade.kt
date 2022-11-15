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

package io.getstream.chat.android.client.persistance.repository

import androidx.annotation.VisibleForTesting
import io.getstream.chat.android.client.extensions.internal.users
import io.getstream.chat.android.client.models.Channel
import io.getstream.chat.android.client.models.Member
import io.getstream.chat.android.client.models.Message
import io.getstream.chat.android.client.models.Reaction
import io.getstream.chat.android.client.models.User
import io.getstream.chat.android.client.persistance.repository.factory.RepositoryFactory
import io.getstream.chat.android.client.query.pagination.AnyChannelPaginationRequest
import io.getstream.chat.android.client.query.pagination.isRequestingMoreThanLastMessage
import io.getstream.chat.android.core.internal.InternalStreamChatApi
import io.getstream.chat.android.models.ChannelConfig
import io.getstream.chat.android.models.Config
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.async
import kotlinx.coroutines.awaitAll
import java.util.Date

@InternalStreamChatApi
@SuppressWarnings("LongParameterList")
public class RepositoryFacade private constructor(
    private val userRepository: UserRepository,
    private val configsRepository: ChannelConfigRepository,
    private val channelsRepository: ChannelRepository,
    private val queryChannelsRepository: QueryChannelsRepository,
    private val messageRepository: MessageRepository,
    private val reactionsRepository: ReactionRepository,
    private val syncStateRepository: SyncStateRepository,
    private val attachmentRepository: AttachmentRepository,
    private val scope: CoroutineScope,
    private val defaultConfig: Config,
) : UserRepository by userRepository,
    ChannelRepository by channelsRepository,
    ReactionRepository by reactionsRepository,
    MessageRepository by messageRepository,
    ChannelConfigRepository by configsRepository,
    QueryChannelsRepository by queryChannelsRepository,
    SyncStateRepository by syncStateRepository,
    AttachmentRepository by attachmentRepository {

    override suspend fun selectChannels(channelCIDs: List<String>, forceCache: Boolean): List<Channel> =
        selectChannels(channelCIDs, null, forceCache)

    public suspend fun selectChannels(
        channelIds: List<String>,
        pagination: AnyChannelPaginationRequest?,
        forceCache: Boolean = false,
    ): List<Channel> {
        // fetch the channel entities from room
        val channels = channelsRepository.selectChannels(channelIds, forceCache)
        // TODO why it is not compared this way?
        //  pagination?.isRequestingMoreThanLastMessage() == true
        val messagesMap = if (pagination?.isRequestingMoreThanLastMessage() != false) {
            // with postgres this could be optimized into a single query instead of N, not sure about sqlite on android
            // sqlite has window functions: https://sqlite.org/windowfunctions.html
            // but android runs a very dated version: https://developer.android.com/reference/android/database/sqlite/package-summary
            channelIds.map { cid ->
                scope.async { cid to selectMessagesForChannel(cid, pagination) }
            }.awaitAll().toMap()
        } else {
            emptyMap()
        }

        return channels.onEach { channel ->
            channel.enrichChannel(messagesMap, defaultConfig)
        }
    }

    @VisibleForTesting
    public fun Channel.enrichChannel(messageMap: Map<String, List<Message>>, defaultConfig: Config) {
        config = selectChannelConfig(type)?.config ?: defaultConfig
        messages = if (messageMap.containsKey(cid)) {
            val fullList = (messageMap[cid] ?: error("Messages must be in the map")) + messages
            fullList.distinctBy(Message::id)
        } else {
            messages
        }
    }

    override suspend fun insertChannel(channel: Channel) {
        insertUsers(channel.let(Channel::users))
        channelsRepository.insertChannel(channel)
    }

    override suspend fun insertChannels(channels: Collection<Channel>) {
        insertUsers(channels.flatMap(Channel::users))
        channelsRepository.insertChannels(channels)
    }

    override suspend fun insertMessage(message: Message, cache: Boolean) {
        insertUsers(message.users())
        messageRepository.insertMessage(message, cache)
    }

    override suspend fun insertMessages(messages: List<Message>, cache: Boolean) {
        insertUsers(messages.flatMap(Message::users))
        messageRepository.insertMessages(messages, cache)
    }

    /**
     * Deletes channel messages before [hideMessagesBefore] and removes channel from the cache.
     */
    override suspend fun deleteChannelMessagesBefore(cid: String, hideMessagesBefore: Date) {
        evictChannel(cid)
        messageRepository.deleteChannelMessagesBefore(cid, hideMessagesBefore)
    }

    override suspend fun insertReaction(reaction: Reaction) {
        reaction.user?.let {
            insertUser(it)
            reactionsRepository.insertReaction(reaction)
        }
    }

    override suspend fun updateMembersForChannel(cid: String, members: List<Member>) {
        insertUsers(members.map(Member::user))
        channelsRepository.updateMembersForChannel(cid, members)
    }

    public suspend fun storeStateForChannels(
        configs: Collection<ChannelConfig>? = null,
        users: List<User>,
        channels: Collection<Channel>,
        messages: List<Message>,
        cacheForMessages: Boolean = false,
    ) {
        configs?.let { insertChannelConfigs(it) }
        insertUsers(users)
        insertChannels(channels)
        insertMessages(messages, cacheForMessages)
    }

    override suspend fun clear() {
        userRepository.clear()
        channelsRepository.clear()
        reactionsRepository.clear()
        messageRepository.clear()
        configsRepository.clear()
        queryChannelsRepository.clear()
        syncStateRepository.clear()
        attachmentRepository.clear()
    }

    @InternalStreamChatApi
    public companion object {

        /**
         * Creates a new instance of [RepositoryFacade] and populate the Singleton instance. This method should be
         * used mainly for tests or internally by other constructor methods.
         *
         * @param factory [RepositoryFacade]
         * @param scope [CoroutineScope]
         * @param defaultConfig [Config]
         */
        @InternalStreamChatApi
        public fun create(
            factory: RepositoryFactory,
            scope: CoroutineScope,
            defaultConfig: Config = Config(),
        ): RepositoryFacade {
            val userRepository = factory.createUserRepository()
            val getUser: suspend (userId: String) -> User = { userId ->
                requireNotNull(userRepository.selectUser(userId)) {
                    "User with the userId: `$userId` has not been found"
                }
            }

            val messageRepository = factory.createMessageRepository(getUser)
            val getMessage: suspend (messageId: String) -> Message? = messageRepository::selectMessage

            return RepositoryFacade(
                userRepository = userRepository,
                configsRepository = factory.createChannelConfigRepository(),
                channelsRepository = factory.createChannelRepository(getUser, getMessage),
                queryChannelsRepository = factory.createQueryChannelsRepository(),
                messageRepository = messageRepository,
                reactionsRepository = factory.createReactionRepository(getUser),
                syncStateRepository = factory.createSyncStateRepository(),
                attachmentRepository = factory.createAttachmentRepository(),
                scope = scope,
                defaultConfig = defaultConfig,
            )
        }
    }
}
