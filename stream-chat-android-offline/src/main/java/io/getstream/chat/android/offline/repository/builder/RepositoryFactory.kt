package io.getstream.chat.android.offline.repository.builder

import io.getstream.chat.android.client.models.Message
import io.getstream.chat.android.client.models.User
import io.getstream.chat.android.offline.repository.database.ChatDatabase
import io.getstream.chat.android.offline.repository.domain.channel.ChannelRepository
import io.getstream.chat.android.offline.repository.domain.channel.ChannelRepositoryImpl
import io.getstream.chat.android.offline.repository.domain.channelconfig.ChannelConfigRepository
import io.getstream.chat.android.offline.repository.domain.channelconfig.ChannelConfigRepositoryImpl
import io.getstream.chat.android.offline.repository.domain.message.MessageRepository
import io.getstream.chat.android.offline.repository.domain.message.MessageRepositoryImpl
import io.getstream.chat.android.offline.repository.domain.queryChannels.QueryChannelsRepository
import io.getstream.chat.android.offline.repository.domain.queryChannels.QueryChannelsRepositoryImpl
import io.getstream.chat.android.offline.repository.domain.reaction.ReactionRepository
import io.getstream.chat.android.offline.repository.domain.reaction.ReactionRepositoryImpl
import io.getstream.chat.android.offline.repository.domain.syncState.SyncStateRepository
import io.getstream.chat.android.offline.repository.domain.syncState.SyncStateRepositoryImpl
import io.getstream.chat.android.offline.repository.domain.user.UserRepository
import io.getstream.chat.android.offline.repository.domain.user.UserRepositoryImpl

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
        QueryChannelsRepositoryImpl(database.queryChannelsDao())

    fun createMessageRepository(getUser: suspend (userId: String) -> User): MessageRepository =
        MessageRepositoryImpl(database.messageDao(), getUser, 100)

    fun createReactionRepository(getUser: suspend (userId: String) -> User): ReactionRepository =
        ReactionRepositoryImpl(database.reactionDao(), getUser)

    fun createSyncStateRepository(): SyncStateRepository = SyncStateRepositoryImpl(database.syncStateDao())
}
