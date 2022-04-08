package io.getstream.chat.android.client.persistence.repository.inmemory

import io.getstream.chat.android.client.models.User
import io.getstream.chat.android.client.persistence.repository.UserRepository
import kotlinx.coroutines.flow.StateFlow

internal class UserInMemoryRepository: UserRepository {

    override suspend fun insertUsers(users: Collection<User>) {
        TODO("Not yet implemented")
    }

    override suspend fun insertUser(user: User) {
        TODO("Not yet implemented")
    }

    override suspend fun insertCurrentUser(user: User) {
        TODO("Not yet implemented")
    }

    override suspend fun selectUser(userId: String): User? {
        TODO("Not yet implemented")
    }

    override suspend fun selectUsers(ids: List<String>): List<User> {
        TODO("Not yet implemented")
    }

    override suspend fun selectAllUsers(limit: Int, offset: Int): List<User> {
        TODO("Not yet implemented")
    }

    override suspend fun selectUsersLikeName(searchString: String, limit: Int, offset: Int): List<User> {
        TODO("Not yet implemented")
    }

    override fun observeLatestUsers(): StateFlow<Map<String, User>> {
        TODO("Not yet implemented")
    }
}
