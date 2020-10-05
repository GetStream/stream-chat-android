package io.getstream.chat.android.livedata.repository

import androidx.collection.LruCache
import io.getstream.chat.android.client.models.User
import io.getstream.chat.android.livedata.dao.UserDao
import io.getstream.chat.android.livedata.entity.UserEntity

class UserRepository(
    private val userDao: UserDao,
    private val currentUser: User?,
    cacheSize: Int = 100
) {
    // the user cache is simple, just keeps the last 100 users in memory
    var userCache = LruCache<String, User>(cacheSize)

    suspend fun insert(users: List<User>) {
        if (users.isEmpty()) return
        cacheUsers(users)
        userDao.insertMany(users.map(::toEntity))
    }

    private fun cacheUsers(users: List<User>) {
        for (userEntity in users) {
            userCache.put(userEntity.id, userEntity)
        }
    }

    suspend fun insertUser(user: User) {
        userDao.insert(toEntity(user))
    }

    suspend fun insertMe(user: User) {
        val userEntity = toEntity(user)
        userEntity.originalId = user.id
        userEntity.id = "me"
        userDao.insert(userEntity)
    }

    suspend fun selectMe(): User? {
        return userDao.select("me")
            ?.apply { id = originalId }
            ?.let(::toModel)
    }

    suspend fun select(userId: String): User? {
        return userDao.select(userId)?.let(::toModel)
    }

    suspend fun select(userIds: List<String>): List<User> {
        val cacheUsers: List<User> = userIds.mapNotNull(userCache::get)
        val missingUserIds = userIds.filter { userCache.get(it) == null }
        val dbUsers = userDao.select(missingUserIds).map(::toModel).also(::cacheUsers)
        return dbUsers + cacheUsers
    }

    suspend fun selectUserMap(userIds: List<String>): Map<String, User> = select(userIds)
        .associateBy(User::id)
        .let { userMap ->
            if (currentUser != null) {
                userMap + (currentUser.id to currentUser)
            } else {
                userMap
            }
        }

    private fun toEntity(user: User): UserEntity = with(user) {
        UserEntity(id).also {
            it.role = role
            it.createdAt = createdAt
            it.updatedAt = updatedAt
            it.lastActive = lastActive
            it.invisible = invisible
            it.banned = banned
            it.extraData = extraData
            it.mutes = mutes.map { mute -> mute.target.id }
        }
    }

    private fun toModel(userEntity: UserEntity): User = with(userEntity) {
        User(id = this.id).also { user ->
            user.role = role
            user.createdAt = createdAt
            user.updatedAt = updatedAt
            user.lastActive = lastActive
            user.invisible = invisible
            user.extraData = extraData
            user.banned = banned
        }
    }
}
