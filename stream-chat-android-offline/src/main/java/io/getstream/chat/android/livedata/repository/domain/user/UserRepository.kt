package io.getstream.chat.android.livedata.repository.domain.user

import androidx.collection.LruCache
import io.getstream.chat.android.client.models.User

internal interface UserRepository {
    suspend fun insertUsers(users: Collection<User>)
    suspend fun insertUser(user: User)
    suspend fun insertCurrentUser(user: User)
    suspend fun selectCurrentUser(): User?
    suspend fun selectUser(userId: String): User?
    suspend fun selectUsers(userIds: List<String>): List<User>
    suspend fun selectUserMap(userIds: List<String>): Map<String, User>
}

internal class UserRepositoryImpl(
    private val userDao: UserDao,
    private val currentUser: User,
    cacheSize: Int = 100
) : UserRepository {
    // the user cache is simple, just keeps the last 100 users in memory
    private val userCache = LruCache<String, User>(cacheSize)

    override suspend fun insertUsers(users: Collection<User>) {
        if (users.isEmpty()) return
        cacheUsers(users)
        userDao.insertMany(users.map(::toEntity))
    }

    private fun cacheUsers(users: Collection<User>) {
        for (userEntity in users) {
            userCache.put(userEntity.id, userEntity)
        }
    }

    override suspend fun insertUser(user: User) {
        userDao.insert(toEntity(user))
    }

    override suspend fun insertCurrentUser(user: User) {
        val userEntity = toEntity(user).copy(id = ME_ID)
        userDao.insert(userEntity)
    }

    override suspend fun selectCurrentUser(): User? {
        return userDao.select(ME_ID)?.let(::toModel)
    }

    override suspend fun selectUser(userId: String): User? {
        return userCache[userId] ?: userDao.select(userId)?.let(::toModel)?.also { cacheUsers(listOf(it)) }
    }

    override suspend fun selectUsers(userIds: List<String>): List<User> {
        val cacheUsers: List<User> = userIds.mapNotNull(userCache::get)
        val missingUserIds = userIds.filter { userCache.get(it) == null }
        val dbUsers = if (missingUserIds.isNotEmpty()) {
            userDao.select(missingUserIds).map(::toModel).also(::cacheUsers)
        } else {
            emptyList()
        }
        return dbUsers + cacheUsers
    }

    override suspend fun selectUserMap(userIds: List<String>): Map<String, User> =
        selectUsers(userIds).associateBy(User::id) + (currentUser.id to currentUser)

    private fun toEntity(user: User): UserEntity = with(user) {
        UserEntity(
            id = id,
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
