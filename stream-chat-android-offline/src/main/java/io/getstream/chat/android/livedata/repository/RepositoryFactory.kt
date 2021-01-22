package io.getstream.chat.android.livedata.repository

import io.getstream.chat.android.client.ChatClient
import io.getstream.chat.android.client.models.User
import io.getstream.chat.android.livedata.ChatDatabase

internal class RepositoryFactory(
    private val database: ChatDatabase,
    private val client: ChatClient,
    private val currentUser: User
) {
    fun createUserRepository(): UserRepository = UserRepository(database.userDao(), currentUser, 100)
    fun createChannelConfigRepository(): ChannelConfigRepository = ChannelConfigRepository(database.channelConfigDao())
    fun createChannelRepository(): ChannelRepository =
        ChannelRepository(database.channelStateDao(), 100, currentUser, client)

    fun createQueryChannelsRepository(): QueryChannelsRepository = QueryChannelsRepository(database.queryChannelsQDao())
    fun createMessageRepository(): MessageRepository = MessageRepository(database.messageDao(), 100)
    fun createReactionRepository(): ReactionRepository = ReactionRepository(database.reactionDao())
    fun createSyncStateRepository(): SyncStateRepository = SyncStateRepository(database.syncStateDao())
}
