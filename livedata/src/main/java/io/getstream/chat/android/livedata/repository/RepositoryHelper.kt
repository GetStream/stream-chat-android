package io.getstream.chat.android.livedata.repository

import androidx.annotation.VisibleForTesting
import io.getstream.chat.android.client.ChatClient
import io.getstream.chat.android.client.models.Channel
import io.getstream.chat.android.client.models.Config
import io.getstream.chat.android.client.models.User
import io.getstream.chat.android.livedata.ChatDatabase
import io.getstream.chat.android.livedata.entity.ChannelEntity
import io.getstream.chat.android.livedata.entity.MessageEntity
import io.getstream.chat.android.livedata.entity.ReactionEntity
import io.getstream.chat.android.livedata.request.AnyChannelPaginationRequest
import io.getstream.chat.android.livedata.request.isRequestingMoreThanLastMessage
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.async
import kotlinx.coroutines.awaitAll

internal class RepositoryHelper(
    client: ChatClient,
    currentUser: User,
    database: ChatDatabase,
    private val scope: CoroutineScope
) {
    val users = UserRepository(database.userDao(), currentUser, 100)
    val configs = ChannelConfigRepository(database.channelConfigDao())
    val channels = ChannelRepository(database.channelStateDao(), 100, currentUser, client)
    val queryChannels = QueryChannelsRepository(database.queryChannelsQDao())
    val messages = MessageRepository(database.messageDao(), 100)
    val reactions = ReactionRepository(database.reactionDao(), currentUser, client)
    val syncState = SyncStateRepository(database.syncStateDao())

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
        return channelEntities.fold(emptySet()) { acc, channel ->
            acc + channel.createdByUserId.orEmpty() +
                channel.members.keys +
                channel.reads.keys +
                channelMessagesMap[channel.cid]?.flatMap { message ->
                    message.latestReactions.map(ReactionEntity::userId) + message.userId
                }.orEmpty()
        }
    }

    internal suspend fun selectChannels(
        channelIds: List<String>,
        pagination: AnyChannelPaginationRequest,
        defaultConfig: Config
    ): List<Channel> {
        // fetch the channel entities from room
        val channelEntities = channels.select(channelIds)
        val messageEntitiesMap = if (pagination.isRequestingMoreThanLastMessage()) {
            // with postgres this could be optimized into a single query instead of N, not sure about sqlite on android
            // sqlite has window functions: https://sqlite.org/windowfunctions.html
            // but android runs a very dated version: https://developer.android.com/reference/android/database/sqlite/package-summary
            channelIds.map { cid ->
                scope.async { cid to messages.selectMessagesEntitiesForChannel(cid, pagination) }
            }.awaitAll().toMap()
        } else {
            emptyMap()
        }

        // gather the user ids from channels, members and the last message
        val userMap = getUsersForChannels(channelEntities, messageEntitiesMap)

        val messagesMap = messageEntitiesMap.mapValues { entry ->
            entry.value.map { messageEntity -> MessageRepository.toModel(messageEntity, userMap) }
        }

        // convert the channels
        return channelEntities.map { entity ->
            entity.toChannel(userMap).apply {
                config = configs.select(type) ?: defaultConfig
                messages = messagesMap[cid] ?: emptyList()
            }
        }
    }
}
