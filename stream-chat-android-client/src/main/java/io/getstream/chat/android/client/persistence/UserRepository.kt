package io.getstream.chat.android.client.persistence

import io.getstream.chat.android.client.models.User
import kotlinx.coroutines.flow.StateFlow

public interface UserRepository {
    public suspend fun insertUsers(users: Collection<User>)
    public suspend fun insertUser(user: User)
    public suspend fun insertCurrentUser(user: User)
    public suspend fun selectUser(userId: String): User?

    /**
     * @return The list of users stored in the cache.
     */
    public suspend fun selectUsers(ids: List<String>): List<User>
    public suspend fun selectAllUsers(limit: Int, offset: Int): List<User>
    public suspend fun selectUsersLikeName(searchString: String, limit: Int, offset: Int): List<User>

    /** Returns flow of latest updated users. */
    public fun observeLatestUsers(): StateFlow<Map<String, User>>
}
