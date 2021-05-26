package io.getstream.chat.android.offline.repository

import androidx.annotation.VisibleForTesting
import io.getstream.chat.android.client.models.Channel
import io.getstream.chat.android.client.models.Config
import io.getstream.chat.android.client.models.Member
import io.getstream.chat.android.client.models.Message
import io.getstream.chat.android.client.models.Reaction
import io.getstream.chat.android.client.models.User
import io.getstream.chat.android.offline.extensions.lastMessage
import io.getstream.chat.android.offline.extensions.users
import io.getstream.chat.android.offline.model.ChannelConfig
import io.getstream.chat.android.offline.repository.domain.channel.ChannelRepository
import io.getstream.chat.android.offline.repository.domain.channelconfig.ChannelConfigRepository
import io.getstream.chat.android.offline.repository.domain.message.MessageRepository
import io.getstream.chat.android.offline.repository.domain.queryChannels.QueryChannelsRepository
import io.getstream.chat.android.offline.repository.domain.reaction.ReactionRepository
import io.getstream.chat.android.offline.repository.domain.syncState.SyncStateRepository
import io.getstream.chat.android.offline.repository.domain.user.UserRepository
import io.getstream.chat.android.offline.request.AnyChannelPaginationRequest
import io.getstream.chat.android.offline.request.isRequestingMoreThanLastMessage
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.async
import kotlinx.coroutines.awaitAll

internal class RepositoryFacade constructor(
    userRepository: UserRepository,
    configsRepository: ChannelConfigRepository,
    private val channelsRepository: ChannelRepository,
    queryChannelsRepository: QueryChannelsRepository,
    private val messageRepository: MessageRepository,
    private val reactionsRepository: ReactionRepository,
    syncStateRepository: SyncStateRepository,
    private val scope: CoroutineScope,
    private val defaultConfig: Config,
) : UserRepository by userRepository,
    ChannelRepository by channelsRepository,
    ReactionRepository by reactionsRepository,
    MessageRepository by messageRepository,
    ChannelConfigRepository by configsRepository,
    QueryChannelsRepository by queryChannelsRepository,
    SyncStateRepository by syncStateRepository {

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
        config = selectChannelConfig(type)?.config ?: defaultConfig
        messages = if (messageMap.containsKey(cid)) {
            val fullList = (messageMap[cid] ?: error("Messages must be in the map")) + messages
            fullList.distinct()
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

    internal suspend fun storeStateForChannels(
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
}
