package io.getstream.chat.android.client.persistance.repository.factory

public class RepositoryProvider private constructor(
    private val repositoryFactory: RepositoryFactory,
) : RepositoryFactory by repositoryFactory {

    public companion object {

        private var instance: RepositoryProvider? = null

        public fun changeRepositoryFactory(repositoryFactory: RepositoryFactory) {
            instance = RepositoryProvider(repositoryFactory)
        }

        public fun get(): RepositoryProvider? = instance
    }
}
