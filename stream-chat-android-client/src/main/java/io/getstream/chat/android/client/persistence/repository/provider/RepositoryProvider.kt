package io.getstream.chat.android.client.persistence.repository.provider

import io.getstream.chat.android.client.persistence.repository.factory.InMemoryRepositoryFactory
import io.getstream.chat.android.client.persistence.repository.factory.RepositoryFactory

public class RepositoryProvider private constructor(
    private val repositoryFactory: RepositoryFactory
) : RepositoryFactory by repositoryFactory {

    public companion object {

        private var instance: RepositoryProvider = RepositoryProvider(InMemoryRepositoryFactory())

        public fun changeRepositoryFactory(repositoryFactory: RepositoryFactory) {
            instance = RepositoryProvider(repositoryFactory)
        }

        public fun get(): RepositoryProvider = instance
    }
}
