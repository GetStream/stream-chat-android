package io.getstream.chat.android.client.persistence.repository.provider

import io.getstream.chat.android.client.persistence.repository.factory.InMemoryRepositoryFactory
import io.getstream.chat.android.client.persistence.repository.factory.RepositoryFactory

internal class RepositoryProvider private constructor(
    private val repositoryFactory: RepositoryFactory
) : RepositoryFactory by repositoryFactory {

    internal companion object {

        private var instance: RepositoryProvider = RepositoryProvider(InMemoryRepositoryFactory())

        fun setRepositoryFactory(repositoryFactory: RepositoryFactory) {
            instance = RepositoryProvider(repositoryFactory)
        }

        fun get(): RepositoryProvider = instance
    }
}
