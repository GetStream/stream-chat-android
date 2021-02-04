package io.getstream.chat.android.livedata.repository

import androidx.collection.LruCache
import io.getstream.chat.android.client.models.User
import io.getstream.chat.android.livedata.dao.UserDao
import io.getstream.chat.android.livedata.entity.UserEntity

internal class UserRepository(
    private val userDao: UserDao,
    private val currentUser: User,
    cacheSize: Int = 100
) {
    // the user cache is simple, just keeps the last 100 users in memory
    private val userCache = LruCache<String, User>(cacheSize)

    suspend fun insert(users: Collection<User>) {
        if (users.isEmpty()) return
        cacheUsers(users)
        userDao.insertMany(users.map(::toEntity))
    }

    private fun cacheUsers(users: Collection<User>) {
        for (userEntity in users) {
            userCache.put(userEntity.id, userEntity)
        }
    }

    suspend fun insertUser(user: User) {
        userDao.insert(toEntity(user))
    }

    suspend fun insertMe(user: User) {
        val userEntity = toEntity(user).copy(id = ME_ID)
        userDao.insert(userEntity)
    }

    suspend fun selectMe(): User? {
        return userDao.select(ME_ID)?.let(::toModel)
    }

    suspend fun select(userId: String): User? {
        return userCache[userId] ?: userDao.select(userId)?.let(::toModel)?.also { cacheUsers(listOf(it)) }
    }

    suspend fun select(userIds: List<String>): List<User> {
        val cacheUsers: List<User> = userIds.mapNotNull(userCache::get)
        val missingUserIds = userIds.filter { userCache.get(it) == null }
        val dbUsers = if (missingUserIds.isNotEmpty()) {
            userDao.select(missingUserIds).map(::toModel).also(::cacheUsers)
        } else {
            emptyList()
        }
        return dbUsers + cacheUsers
    }

    suspend fun selectUserMap(userIds: List<String>): Map<String, User> =
        select(userIds).associateBy(User::id) + (currentUser.id to currentUser)

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
