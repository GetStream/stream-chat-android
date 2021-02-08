package io.getstream.chat.android.livedata.repository

import androidx.annotation.VisibleForTesting
import io.getstream.chat.android.client.models.Channel
import io.getstream.chat.android.client.models.Config
import io.getstream.chat.android.client.models.Message
import io.getstream.chat.android.client.models.User
import io.getstream.chat.android.livedata.controller.QueryChannelsSpec
import io.getstream.chat.android.livedata.extensions.lastMessage
import io.getstream.chat.android.livedata.extensions.users
import io.getstream.chat.android.livedata.model.ChannelConfig
import io.getstream.chat.android.livedata.model.SyncState
import io.getstream.chat.android.livedata.request.AnyChannelPaginationRequest
import io.getstream.chat.android.livedata.request.isRequestingMoreThanLastMessage
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.async
import kotlinx.coroutines.awaitAll

internal class RepositoryHelper private constructor(
    userRepository: UserRepository,
    private val configsRepository: ChannelConfigRepository,
    private val channelsRepository: ChannelRepository,
    private val queryChannelsRepository: QueryChannelsRepository,
    messageRepository: MessageRepository,
    reactionsRepository: ReactionRepository,
    private val syncStateRepository: SyncStateRepository,
    private val scope: CoroutineScope,
    private val defaultConfig: Config,
) : UserRepository by userRepository,
    ChannelRepository by channelsRepository,
    ReactionRepository by reactionsRepository,
    MessageRepository by messageRepository {

    override suspend fun selectChannels(channelCIDs: List<String>): List<Channel> = selectChannels(channelCIDs, null)

    internal suspend fun selectChannels(
        channelIds: List<String>,
        pagination: AnyChannelPaginationRequest?,
    ): List<Channel> {
        // fetch the channel entities from room
        val channels = channelsRepository.selectChannels(channelIds)
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

        return channels.onEach { it.enrichChannel(messagesMap, defaultConfig) }
    }

    @VisibleForTesting
    internal fun Channel.enrichChannel(messageMap: Map<String, List<Message>>, defaultConfig: Config) {
        config = configsRepository.select(type)?.config ?: defaultConfig
        messages = if (messageMap.containsKey(cid)) {
            val fullList = (messageMap[cid] ?: error("Messages must be in the map")) + messages
            fullList.distinct()
        } else {
            messages
        }
    }

    override suspend fun insertChannel(channel: Channel) {
        channelsRepository.insertChannel(channel)
        insertUsers(channel.let(Channel::users))
    }

    override suspend fun insertChannels(channels: Collection<Channel>) {
        channelsRepository.insertChannels(channels)
        insertUsers(channels.flatMap(Channel::users))
    }

    internal suspend fun insertConfigChannel(configs: Collection<ChannelConfig>) {
        configsRepository.insert(configs)
    }

    internal suspend fun storeStateForChannels(
        configs: Collection<ChannelConfig>? = null,
        users: List<User>,
        channels: Collection<Channel>,
        messages: List<Message>,
        cacheForMessages: Boolean = false,
    ) {
        configs?.let { insertConfigChannel(it) }
        insertUsers(users)
        insertChannels(channels)
        insertMessages(messages, cacheForMessages)
    }

    internal suspend fun insertConfigChannel(config: ChannelConfig) {
        configsRepository.insert(config)
    }

    internal fun selectConfig(channelType: String): ChannelConfig? {
        return configsRepository.select(channelType)
    }

    internal suspend fun loadChannelConfig() {
        configsRepository.load()
    }

    @VisibleForTesting
    internal fun clearCache() {
        configsRepository.clearCache()
    }

    internal suspend fun updateLastMessageForChannel(cid: String, lastMessage: Message) {
        selectChannelWithoutMessages(cid)?.also { channel ->
            val messageCreatedAt = checkNotNull(
                lastMessage.createdAt
                    ?: lastMessage.createdLocallyAt
            ) { "created at cant be null, be sure to set message.createdAt" }

            val oldLastMessage = channel.lastMessage
            val updateNeeded = if (oldLastMessage != null) {
                lastMessage.id == oldLastMessage.id || channel.lastMessageAt == null || messageCreatedAt.after(channel.lastMessageAt)
            } else {
                true
            }

            if (updateNeeded) {
                channel.apply {
                    lastMessageAt = messageCreatedAt
                    messages = listOf(lastMessage)
                }.also { channelsRepository.insertChannel(it) }
            }
        }
    }

    suspend fun querySelectById(ids: List<String>): List<QueryChannelsSpec> {
        return queryChannelsRepository.selectById(ids)
    }

    suspend fun querySelectByFilterAndQuerySort(queryChannelsSpec: QueryChannelsSpec): QueryChannelsSpec? {
        return queryChannelsRepository.selectByFilterAndQuerySort(queryChannelsSpec)
    }

    suspend fun queryInsert(queryChannelsSpec: QueryChannelsSpec) {
        return queryChannelsRepository.insert(queryChannelsSpec)
    }

    internal suspend fun selectSyncState(userId: String): SyncState? {
        return syncStateRepository.select(userId)
    }

    internal suspend fun insertSyncState(newSyncState: SyncState) {
        syncStateRepository.insert(newSyncState)
    }

    internal companion object {
        fun create(factory: RepositoryFactory, scope: CoroutineScope, defaultConfig: Config): RepositoryHelper {
            val userRepository = factory.createUserRepository()
            val getUser: suspend (userId: String) -> User = { userId ->
                requireNotNull(userRepository.selectUser(userId)) { "User with the userId: `$userId` has not been found" }
            }

            val messageRepository = factory.createMessageRepository(getUser)
            val getMessage: suspend (messageId: String) -> Message? = messageRepository::selectMessage

            return RepositoryHelper(
                userRepository = factory.createUserRepository(),
                configsRepository = factory.createChannelConfigRepository(),
                channelsRepository = factory.createChannelRepository(getUser, getMessage),
                queryChannelsRepository = factory.createQueryChannelsRepository(),
                messageRepository = messageRepository,
                reactionsRepository = factory.createReactionRepository(getUser),
                syncStateRepository = factory.createSyncStateRepository(),
                scope = scope,
                defaultConfig = defaultConfig,
            )
        }
    }
}
