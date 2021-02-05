package io.getstream.chat.android.livedata.repository

import io.getstream.chat.android.client.models.Message
import io.getstream.chat.android.client.models.User
import io.getstream.chat.android.livedata.ChatDatabase

internal class RepositoryFactory(
    private val database: ChatDatabase,
    private val currentUser: User,
) {
    fun createUserRepository(): UserRepository = UserRepositoryImpl(database.userDao(), currentUser, 100)
    fun createChannelConfigRepository(): ChannelConfigRepository = ChannelConfigRepository(database.channelConfigDao())

    fun createChannelRepository(
        getUser: suspend (userId: String) -> User,
        getMessage: suspend (messageId: String) -> Message?,
    ): ChannelRepository = ChannelRepositoryImpl(database.channelStateDao(), getUser, getMessage, 100)

    fun createQueryChannelsRepository(): QueryChannelsRepository = QueryChannelsRepository(database.queryChannelsQDao())
    fun createMessageRepository(getUser: suspend (userId: String) -> User): MessageRepository =
        MessageRepository(database.messageDao(), getUser, 100)

    fun createReactionRepository(): ReactionRepository = ReactionRepository(database.reactionDao())
    fun createSyncStateRepository(): SyncStateRepository = SyncStateRepository(database.syncStateDao())
}
