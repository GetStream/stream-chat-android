package io.getstream.chat.android.livedata.repository.builder

import io.getstream.chat.android.client.models.Message
import io.getstream.chat.android.client.models.User
import io.getstream.chat.android.livedata.repository.ChannelConfigRepository
import io.getstream.chat.android.livedata.repository.ChannelConfigRepositoryImpl
import io.getstream.chat.android.livedata.repository.ChannelRepository
import io.getstream.chat.android.livedata.repository.ChannelRepositoryImpl
import io.getstream.chat.android.livedata.repository.MessageRepository
import io.getstream.chat.android.livedata.repository.MessageRepositoryImpl
import io.getstream.chat.android.livedata.repository.QueryChannelsRepository
import io.getstream.chat.android.livedata.repository.QueryChannelsRepositoryImpl
import io.getstream.chat.android.livedata.repository.ReactionRepository
import io.getstream.chat.android.livedata.repository.ReactionRepositoryImpl
import io.getstream.chat.android.livedata.repository.SyncStateRepository
import io.getstream.chat.android.livedata.repository.SyncStateRepositoryImpl
import io.getstream.chat.android.livedata.repository.UserRepository
import io.getstream.chat.android.livedata.repository.UserRepositoryImpl
import io.getstream.chat.android.livedata.repository.database.ChatDatabase

internal class RepositoryFactory(
    private val database: ChatDatabase,
    private val currentUser: User,
) {
    fun createUserRepository(): UserRepository = UserRepositoryImpl(database.userDao(), currentUser, 100)
    fun createChannelConfigRepository(): ChannelConfigRepository =
        ChannelConfigRepositoryImpl(database.channelConfigDao())

    fun createChannelRepository(
        getUser: suspend (userId: String) -> User,
        getMessage: suspend (messageId: String) -> Message?,
    ): ChannelRepository = ChannelRepositoryImpl(database.channelStateDao(), getUser, getMessage, 100)

    fun createQueryChannelsRepository(): QueryChannelsRepository =
        QueryChannelsRepositoryImpl(database.queryChannelsQDao())

    fun createMessageRepository(getUser: suspend (userId: String) -> User): MessageRepository =
        MessageRepositoryImpl(database.messageDao(), getUser, 100)

    fun createReactionRepository(getUser: suspend (userId: String) -> User): ReactionRepository =
        ReactionRepositoryImpl(database.reactionDao(), getUser)

    fun createSyncStateRepository(): SyncStateRepository = SyncStateRepositoryImpl(database.syncStateDao())
}
