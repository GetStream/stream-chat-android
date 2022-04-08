package io.getstream.chat.android.client.persistence.repository.factory

import io.getstream.chat.android.client.models.Message
import io.getstream.chat.android.client.models.User
import io.getstream.chat.android.client.persistence.repository.AttachmentRepository
import io.getstream.chat.android.client.persistence.repository.ChannelConfigRepository
import io.getstream.chat.android.client.persistence.repository.ChannelRepository
import io.getstream.chat.android.client.persistence.repository.MessageRepository
import io.getstream.chat.android.client.persistence.repository.QueryChannelsRepository
import io.getstream.chat.android.client.persistence.repository.ReactionRepository
import io.getstream.chat.android.client.persistence.repository.SyncStateRepository
import io.getstream.chat.android.client.persistence.repository.UserRepository
import io.getstream.chat.android.client.persistence.repository.inmemory.AttachmentInMemoryRepository
import io.getstream.chat.android.client.persistence.repository.inmemory.ChannelConfigInMemoryRepository
import io.getstream.chat.android.client.persistence.repository.inmemory.ChannelInMemoryRepository
import io.getstream.chat.android.client.persistence.repository.inmemory.MessageInMemoryRepository
import io.getstream.chat.android.client.persistence.repository.inmemory.QueryChannelsInMemoryRepository
import io.getstream.chat.android.client.persistence.repository.inmemory.ReactionInMemoryRepository
import io.getstream.chat.android.client.persistence.repository.inmemory.SyncStateInMemoryRepository
import io.getstream.chat.android.client.persistence.repository.inmemory.UserInMemoryRepository

internal class InMemoryRepositoryFactory: RepositoryFactory {

    override fun userRepository(): UserRepository = UserInMemoryRepository()

    override fun channelConfigRepository(): ChannelConfigRepository = ChannelConfigInMemoryRepository()
    override fun channelRepository(
        getUser: suspend (userId: String) -> User,
        getMessage: suspend (messageId: String) -> Message?,
    ): ChannelRepository = ChannelInMemoryRepository()

    override fun queryChannelsRepository(): QueryChannelsRepository  = QueryChannelsInMemoryRepository()

    override fun messageRepository(
        getUser: suspend (userId: String) -> User
    ): MessageRepository = MessageInMemoryRepository()

    override fun reactionRepository(
        getUser: suspend (userId: String) -> User
    ): ReactionRepository = ReactionInMemoryRepository()

    override fun syncStateRepository(): SyncStateRepository = SyncStateInMemoryRepository()

    override fun attachmentRepository(): AttachmentRepository = AttachmentInMemoryRepository()
}
