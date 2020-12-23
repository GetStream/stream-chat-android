package io.getstream.chat.android.livedata.repository

import androidx.annotation.VisibleForTesting
import io.getstream.chat.android.client.models.Channel
import io.getstream.chat.android.client.models.Config
import io.getstream.chat.android.client.models.Message
import io.getstream.chat.android.client.models.User
import io.getstream.chat.android.livedata.entity.ChannelEntity
import io.getstream.chat.android.livedata.entity.MessageEntity
import io.getstream.chat.android.livedata.entity.ReactionEntity
import io.getstream.chat.android.livedata.extensions.users
import io.getstream.chat.android.livedata.request.AnyChannelPaginationRequest
import io.getstream.chat.android.livedata.request.isRequestingMoreThanLastMessage
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.async
import kotlinx.coroutines.awaitAll

internal class RepositoryHelper(
    factory: RepositoryFactory,
    private val scope: CoroutineScope
) {
    val users = factory.createUserRepository()
    val configs = factory.createChannelConfigRepository()
    val channels = factory.createChannelRepository()
    val queryChannels = factory.createQueryChannelsRepository()
    val messages = factory.createMessageRepository()
    val reactions = factory.createReactionRepository()
    val syncState = factory.createSyncStateRepository()

    private suspend fun getUsersForChannels(
        channelEntities: Collection<ChannelEntity>,
        channelMessagesMap: Map<String, Collection<MessageEntity>>
    ): Map<String, User> {
        return users.selectUserMap(calculateUserIds(channelEntities, channelMessagesMap).toList())
    }

    @VisibleForTesting
    internal fun calculateUserIds(
        channelEntities: Collection<ChannelEntity>,
        channelMessagesMap: Map<String, Collection<MessageEntity>>
    ): Collection<String> {
        return channelEntities.fold(emptySet<String>()) { acc, channel ->
            acc + channel.createdByUserId.orEmpty() +
                channel.members.keys +
                channel.reads.keys +
                channel.lastMessage?.let(::userIdsFor).orEmpty()
        } + channelMessagesMap.values.flatMap { it.flatMap(::userIdsFor) }
    }

    private fun userIdsFor(message: MessageEntity): List<String> =
        message.latestReactions.map(ReactionEntity::userId) + message.userId

    internal suspend fun selectChannels(
        channelIds: List<String>,
        defaultConfig: Config,
        pagination: AnyChannelPaginationRequest? = null
    ): List<Channel> {
        // fetch the channel entities from room
        val channelEntities = channels.select(channelIds)
        val messagesMap = if (pagination?.isRequestingMoreThanLastMessage() != false) {
            // with postgres this could be optimized into a single query instead of N, not sure about sqlite on android
            // sqlite has window functions: https://sqlite.org/windowfunctions.html
            // but android runs a very dated version: https://developer.android.com/reference/android/database/sqlite/package-summary
            channelIds.map { cid ->
                scope.async {
                    cid to messages.selectMessagesForChannel(cid, pagination, ::selectUser)
                }
            }.awaitAll().toMap()
        } else {
            emptyMap()
        }

        // gather the user ids from channels, members and the last message
        val userMap = getUsersForChannels(channelEntities, emptyMap())

        // convert the channels
        return channelEntities.map { entity ->
            entity.toChannel(userMap).apply {
                config = configs.select(type) ?: defaultConfig
                messages = messagesMap[cid] ?: messages
            }
        }
    }

    internal suspend fun selectMessageSyncNeeded(): List<Message> {
        return messages.selectSyncNeeded(::selectUser)
    }

    internal suspend fun selectMessages(messageIds: List<String>): List<Message> =
        messages.select(messageIds, ::selectUser)

    suspend fun insertChannel(channel: Channel) {
        insertChannels(listOf(channel))
    }

    suspend fun insertChannels(channels: Collection<Channel>) {
        this.channels.insertChannels(channels)
        users.insert(channels.flatMap(Channel::users))
    }

    private suspend fun selectUser(userId: String): User =
        users.select(userId) ?: error("User with the userId: `$userId` has not been found")
}
