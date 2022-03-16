package io.getstream.chat.android.offline.repository.domain.user.internal

import androidx.collection.LruCache
import io.getstream.chat.android.client.models.User
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow

internal interface UserRepository {
    suspend fun insertUsers(users: Collection<User>)
    suspend fun insertUser(user: User)
    suspend fun insertCurrentUser(user: User)
    suspend fun selectUser(userId: String): User?

    /**
     * @return The list of users stored in the cache.
     */
    suspend fun selectUsers(ids: List<String>): List<User>
    suspend fun selectAllUsers(limit: Int, offset: Int): List<User>
    suspend fun selectUsersLikeName(searchString: String, limit: Int, offset: Int): List<User>

    /** Returns flow of latest updated users. */
    fun observeLatestUsers(): StateFlow<Map<String, User>>
}

internal class UserRepositoryImpl(
    private val userDao: UserDao,
    cacheSize: Int = 100,
) : UserRepository {
    // the user cache is simple, just keeps the last 100 users in memory
    private val userCache = LruCache<String, User>(cacheSize)

    private val latestUsersFlow: MutableStateFlow<Map<String, User>> = MutableStateFlow(emptyMap())

    override fun observeLatestUsers(): StateFlow<Map<String, User>> = latestUsersFlow

    override suspend fun insertUsers(users: Collection<User>) {
        if (users.isEmpty()) return
        cacheUsers(users)
        userDao.insertMany(users.map(::toEntity))
    }

    private fun cacheUsers(users: Collection<User>) {
        for (userEntity in users) {
            userCache.put(userEntity.id, userEntity)
        }
        latestUsersFlow.value = userCache.snapshot()
    }

    override suspend fun insertUser(user: User) {
        cacheUsers(listOf(user))
        userDao.insert(toEntity(user))
    }

    override suspend fun insertCurrentUser(user: User) {
        insertUser(user)
        val userEntity = toEntity(user).copy(id = ME_ID)
        userDao.insert(userEntity)
    }

    override suspend fun selectAllUsers(limit: Int, offset: Int): List<User> {
        return userDao.selectAllUser(limit, offset).map(::toModel)
    }

    override suspend fun selectUsersLikeName(searchString: String, limit: Int, offset: Int): List<User> {
        return userDao.selectUsersLikeName("$searchString%", limit, offset).map(::toModel)
    }

    override suspend fun selectUser(userId: String): User? {
        return userCache[userId] ?: userDao.select(userId)?.let(::toModel)?.also { cacheUsers(listOf(it)) }
    }

    override suspend fun selectUsers(ids: List<String>): List<User> {
        val cachedUsers = ids.mapNotNullTo(mutableListOf(), userCache::get)
        val missingUserIds = ids.minus(cachedUsers.map(User::id))

        return cachedUsers + userDao.select(missingUserIds).map(::toModel).also { cacheUsers(it) }
    }

    private fun toEntity(user: User): UserEntity = with(user) {
        UserEntity(
            id = id,
            name = name,
            image = image,
            originalId = id,
            role = role,
            createdAt = createdAt,
            updatedAt = updatedAt,
            lastActive = lastActive,
            invisible = invisible,
            banned = banned,
            extraData = extraData,
            mutes = mutes.map { mute -> mute.target.id }
        )
    }

    private fun toModel(userEntity: UserEntity): User = with(userEntity) {
        User(id = this.originalId).also { user ->
            user.name = name
            user.image = image
            user.role = role
            user.createdAt = createdAt
            user.updatedAt = updatedAt
            user.lastActive = lastActive
            user.invisible = invisible
            user.extraData = extraData.toMutableMap()
            user.banned = banned
        }
    }

    companion object {
        private const val ME_ID = "me"
    }
}
