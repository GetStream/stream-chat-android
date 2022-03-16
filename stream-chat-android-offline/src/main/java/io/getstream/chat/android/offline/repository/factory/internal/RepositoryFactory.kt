package io.getstream.chat.android.offline.repository.factory.internal

import io.getstream.chat.android.client.models.Message
import io.getstream.chat.android.client.models.User
import io.getstream.chat.android.offline.repository.database.internal.ChatDatabase
import io.getstream.chat.android.offline.repository.domain.channel.internal.ChannelRepository
import io.getstream.chat.android.offline.repository.domain.channel.internal.ChannelRepositoryImpl
import io.getstream.chat.android.offline.repository.domain.channelconfig.internal.ChannelConfigRepository
import io.getstream.chat.android.offline.repository.domain.channelconfig.internal.ChannelConfigRepositoryImpl
import io.getstream.chat.android.offline.repository.domain.message.attachment.internal.AttachmentRepository
import io.getstream.chat.android.offline.repository.domain.message.attachment.internal.AttachmentRepositoryImpl
import io.getstream.chat.android.offline.repository.domain.message.internal.MessageRepository
import io.getstream.chat.android.offline.repository.domain.message.internal.MessageRepositoryImpl
import io.getstream.chat.android.offline.repository.domain.queryChannels.internal.QueryChannelsRepository
import io.getstream.chat.android.offline.repository.domain.queryChannels.internal.QueryChannelsRepositoryImpl
import io.getstream.chat.android.offline.repository.domain.reaction.internal.ReactionRepository
import io.getstream.chat.android.offline.repository.domain.reaction.internal.ReactionRepositoryImpl
import io.getstream.chat.android.offline.repository.domain.syncState.internal.SyncStateRepository
import io.getstream.chat.android.offline.repository.domain.syncState.internal.SyncStateRepositoryImpl
import io.getstream.chat.android.offline.repository.domain.user.internal.UserRepository
import io.getstream.chat.android.offline.repository.domain.user.internal.UserRepositoryImpl

internal class RepositoryFactory(
    private val database: ChatDatabase,
    private val currentUser: User?,
) {
    fun createUserRepository(): UserRepository = UserRepositoryImpl(database.userDao(), 100)
    fun createChannelConfigRepository(): ChannelConfigRepository =
        ChannelConfigRepositoryImpl(database.channelConfigDao())

    fun createChannelRepository(
        getUser: suspend (userId: String) -> User,
        getMessage: suspend (messageId: String) -> Message?,
    ): ChannelRepository = ChannelRepositoryImpl(database.channelStateDao(), getUser, getMessage, 100)

    fun createQueryChannelsRepository(): QueryChannelsRepository =
        QueryChannelsRepositoryImpl(database.queryChannelsDao())

    fun createMessageRepository(
        getUser: suspend (userId: String) -> User,
    ): MessageRepository = MessageRepositoryImpl(database.messageDao(), getUser, currentUser, 100)

    fun createReactionRepository(getUser: suspend (userId: String) -> User): ReactionRepository =
        ReactionRepositoryImpl(database.reactionDao(), getUser)

    fun createSyncStateRepository(): SyncStateRepository = SyncStateRepositoryImpl(database.syncStateDao())

    fun createAttachmentRepository(): AttachmentRepository = AttachmentRepositoryImpl(database.attachmentDao())
}
