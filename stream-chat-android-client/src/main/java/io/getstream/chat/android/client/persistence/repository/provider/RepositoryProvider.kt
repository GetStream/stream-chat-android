package io.getstream.chat.android.client.persistence.repository.provider

import io.getstream.chat.android.client.models.User
import io.getstream.chat.android.client.persistence.repository.MessageRepository
import io.getstream.chat.android.client.persistence.repository.UserRepository
import io.getstream.chat.android.client.persistence.repository.factory.RepositoryFactory

internal object RepositoryProvider : RepositoryFactory {

    private var repositoryFactory: RepositoryFactory? = null

    fun setRepositoryFactory(repositoryFactory: RepositoryFactory) {
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
}
