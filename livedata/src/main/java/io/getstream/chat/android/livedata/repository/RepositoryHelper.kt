package io.getstream.chat.android.livedata.repository

import androidx.annotation.VisibleForTesting
import io.getstream.chat.android.client.ChatClient
import io.getstream.chat.android.client.models.User
import io.getstream.chat.android.livedata.ChatDatabase
import io.getstream.chat.android.livedata.entity.ChannelEntity
import io.getstream.chat.android.livedata.entity.MessageEntity
import io.getstream.chat.android.livedata.entity.ReactionEntity

internal class RepositoryHelper(
    client: ChatClient,
    currentUser: User,
    database: ChatDatabase
) {
    val users = UserRepository(database.userDao(), currentUser, 100)
    val configs = ChannelConfigRepository(database.channelConfigDao())
    val channels = ChannelRepository(database.channelStateDao(), 100, currentUser, client)
    val queryChannels = QueryChannelsRepository(database.queryChannelsQDao())
    val messages = MessageRepository(database.messageDao(), currentUser, 100)
    val reactions = ReactionRepository(database.reactionDao(), currentUser, client)
    val syncState = SyncStateRepository(database.syncStateDao())

    internal suspend fun getUsersForChannels(
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
}
