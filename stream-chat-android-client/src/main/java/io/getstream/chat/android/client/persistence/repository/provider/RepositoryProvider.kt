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
import io.getstream.chat.android.client.persistence.repository.factory.RepositoryFactory

public object RepositoryProvider : RepositoryFactory {

    private var repositoryFactory: RepositoryFactory? = null

    public fun setRepositoryFactory(repositoryFactory: RepositoryFactory) {
        this.repositoryFactory = repositoryFactory
    }

    override fun createUserRepository(): UserRepository {
        checkNotNull(repositoryFactory) { "You need to define repositoryFactory" }
        return repositoryFactory!!.createUserRepository()
    }

    override fun createMessageRepository(getUser: suspend (userId: String) -> User): MessageRepository {
        checkNotNull(repositoryFactory) { "You need to define repositoryFactory" }
        return repositoryFactory!!.createMessageRepository(getUser)
    }

    override fun createChannelConfigRepository(): ChannelConfigRepository {
        checkNotNull(repositoryFactory) { "You need to define repositoryFactory" }
        return repositoryFactory!!.createChannelConfigRepository()
    }

    override fun createChannelRepository(
        getUser: suspend (userId: String) -> User,
        getMessage: suspend (messageId: String) -> Message?,
    ): ChannelRepository {
        checkNotNull(repositoryFactory) { "You need to define repositoryFactory" }
        return repositoryFactory!!.createChannelRepository(getUser, getMessage)
    }

    override fun createQueryChannelsRepository(): QueryChannelsRepository {
        checkNotNull(repositoryFactory) { "You need to define repositoryFactory" }
        return repositoryFactory!!.createQueryChannelsRepository()
    }

    override fun createReactionRepository(getUser: suspend (userId: String) -> User): ReactionRepository {
        checkNotNull(repositoryFactory) { "You need to define repositoryFactory" }
        return repositoryFactory!!.createReactionRepository(getUser)
    }

    override fun createSyncStateRepository(): SyncStateRepository {
        checkNotNull(repositoryFactory) { "You need to define repositoryFactory" }
        return repositoryFactory!!.createSyncStateRepository()
    }

    override fun createAttachmentRepository(): AttachmentRepository {
        checkNotNull(repositoryFactory) { "You need to define repositoryFactory" }
        return repositoryFactory!!.createAttachmentRepository()
    }
}
