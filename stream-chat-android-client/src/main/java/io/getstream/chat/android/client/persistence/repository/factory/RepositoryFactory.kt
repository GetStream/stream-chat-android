package io.getstream.chat.android.client.persistence.repository.factory

import io.getstream.chat.android.client.models.User
import io.getstream.chat.android.client.persistence.repository.MessageRepository
import io.getstream.chat.android.client.persistence.repository.UserRepository

public interface RepositoryFactory {

    public fun createUserRepository(): UserRepository

    public fun createMessageRepository(
        getUser: suspend (userId: String) -> User,
    ): MessageRepository

    //Todo: Add the other Repositories
}
