package io.getstream.chat.android.client.persistence.repository.provider

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
import io.getstream.chat.android.client.persistence.repository.factory.InMemoryRepositoryFactory
import io.getstream.chat.android.client.persistence.repository.factory.RepositoryFactory

public object RepositoryProvider : RepositoryFactory {

    private var repositoryFactory: RepositoryFactory = InMemoryRepositoryFactory()

    public fun setRepositoryFactory(repositoryFactory: RepositoryFactory) {
        this.repositoryFactory = repositoryFactory
    }

    override fun userRepository(): UserRepository = repositoryFactory.userRepository()

    override fun messageRepository(getUser: suspend (userId: String) -> User): MessageRepository =
        repositoryFactory.messageRepository(getUser)

    override fun channelConfigRepository(): ChannelConfigRepository = repositoryFactory.channelConfigRepository()

    override fun channelRepository(
        getUser: suspend (userId: String) -> User,
        getMessage: suspend (messageId: String) -> Message?,
    ): ChannelRepository = repositoryFactory.channelRepository(getUser, getMessage)

    override fun queryChannelsRepository(): QueryChannelsRepository = repositoryFactory.queryChannelsRepository()

    override fun reactionRepository(getUser: suspend (userId: String) -> User): ReactionRepository =
        repositoryFactory.reactionRepository(getUser)

    override fun syncStateRepository(): SyncStateRepository = repositoryFactory.syncStateRepository()

    override fun attachmentRepository(): AttachmentRepository = repositoryFactory.attachmentRepository()
}
