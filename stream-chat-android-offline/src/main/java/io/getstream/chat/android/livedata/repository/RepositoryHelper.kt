package io.getstream.chat.android.livedata.repository

import androidx.annotation.VisibleForTesting
import io.getstream.chat.android.client.models.Channel
import io.getstream.chat.android.client.models.Config
import io.getstream.chat.android.client.models.Message
import io.getstream.chat.android.client.models.Reaction
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
import java.util.Date

internal class RepositoryHelper private constructor(
    private val userRepository: UserRepository,
    private val configsRepository: ChannelConfigRepository,
    private val channelsRepository: ChannelRepository,
    private val queryChannelsRepository: QueryChannelsRepository,
    private val messageRepository: MessageRepository,
    private val reactionsRepository: ReactionRepository,
    private val syncStateRepository: SyncStateRepository,
    private val scope: CoroutineScope,
    private val defaultConfig: Config,
) : UserRepository by userRepository, ChannelRepository by channelsRepository {

    private val selectUser: suspend (userId: String) -> User = { userId ->
        requireNotNull(selectUser(userId)) { "User with the userId: `$userId` has not been found" }
    }

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
                scope.async {
                    cid to messageRepository.selectMessagesForChannel(cid, pagination)
                }
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
        userRepository.insertUsers(channel.let(Channel::users))
    }

    override suspend fun insertChannels(channels: Collection<Channel>) {
        channelsRepository.insertChannels(channels)
        userRepository.insertUsers(channels.flatMap(Channel::users))
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

    internal suspend fun selectMessageSyncNeeded(): List<Message> {
        return messageRepository.selectMessagesSyncNeeded()
    }

    internal suspend fun selectMessages(messageIds: List<String>): List<Message> =
        messageRepository.selectMessages(messageIds)

    internal suspend fun selectUserReactionsToMessage(
        messageId: String,
        userId: String,
    ): List<Reaction> = reactionsRepository.selectUserReactionsToMessage(messageId, userId, selectUser)

    internal suspend fun updateReactionsForMessageByDeletedDate(userId: String, messageId: String, deletedAt: Date) =
        reactionsRepository.updateReactionsForMessageByDeletedDate(userId, messageId, deletedAt)

    @VisibleForTesting
    internal suspend fun selectUserReactionsToMessageByType(
        messageId: String,
        userId: String,
        type: String,
    ) = reactionsRepository.selectUserReactionsToMessageByType(messageId, userId, type, selectUser)

    internal suspend fun selectReactionSyncNeeded(): List<Reaction> = reactionsRepository.selectSyncNeeded(selectUser)

    suspend fun selectMessage(
        messageId: String,
    ): Message? {
        return messageRepository.selectMessage(messageId)
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

    suspend fun selectMessagesForChannel(
        cid: String,
        pagination: AnyChannelPaginationRequest?,
    ): List<Message> {
        return messageRepository.selectMessagesForChannel(cid, pagination)
    }

    suspend fun insertMessage(message: Message, cache: Boolean = false) {
        messageRepository.insertMessage(message, cache)
    }

    suspend fun insertMessages(messages: List<Message>, cache: Boolean = false) {
        messageRepository.insertMessages(messages, cache)
    }

    suspend fun deleteChannelMessagesBefore(cid: String, hideMessagesBefore: Date) {
        messageRepository.deleteChannelMessagesBefore(cid, hideMessagesBefore)
    }

    suspend fun deleteChannelMessage(message: Message) {
        messageRepository.deleteChannelMessage(message)
    }

    internal suspend fun insertReaction(reaction: Reaction) {
        reactionsRepository.insert(reaction)
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
                reactionsRepository = factory.createReactionRepository(),
                syncStateRepository = factory.createSyncStateRepository(),
                scope = scope,
                defaultConfig = defaultConfig,
            )
        }
    }
}
