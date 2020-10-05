package io.getstream.chat.android.livedata.repository

import androidx.collection.LruCache
import io.getstream.chat.android.client.models.User
import io.getstream.chat.android.livedata.dao.UserDao
import io.getstream.chat.android.livedata.entity.UserEntity

class UserRepository(
    private val userDao: UserDao,
    private val currentUser: User?,
    private val userMapper: UserMapper = UserMapper(),
    cacheSize: Int = 100
) {
    // the user cache is simple, just keeps the last 100 users in memory
    var userCache = LruCache<String, User>(cacheSize)

    suspend fun insert(users: List<User>) {
        if (users.isEmpty()) return
        cacheUsers(users)
        userDao.insertMany(users.map(userMapper::modelToEntity))
    }

    private fun cacheUsers(users: List<User>) {
        for (userEntity in users) {
            userCache.put(userEntity.id, userEntity)
        }
    }

    suspend fun insertUser(user: User) {
        userDao.insert(userMapper.modelToEntity(user))
    }

    suspend fun insertMe(user: User) {
        val userEntity = userMapper.modelToEntity(user)
        userEntity.originalId = user.id
        userEntity.id = "me"
        userDao.insert(userEntity)
    }

    suspend fun selectMe(): User? {
        return userDao.select("me")
            ?.apply { id = originalId }
            ?.let(userMapper::entityToModel)
    }

    suspend fun select(userId: String): User? {
        return userDao.select(userId)?.let(userMapper::entityToModel)
    }

    suspend fun select(userIds: List<String>): List<User> {
        val cacheUsers: List<User> = userIds.mapNotNull(userCache::get)
        val missingUserIds = userIds.filter { userCache.get(it) == null }
        val dbUsers = userDao.select(missingUserIds).map(userMapper::entityToModel).also(::cacheUsers)
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
}

class UserMapper {
    fun modelToEntity(user: User): UserEntity = UserEntity(user.id).apply {
        role = user.role
        createdAt = user.createdAt
        updatedAt = user.updatedAt
        lastActive = user.lastActive
        invisible = user.invisible
        banned = user.banned
        extraData = user.extraData
        val muteList = user.mutes
        mutes = muteList.map { it.target.id }
    }

    fun entityToModel(entity: UserEntity): User = with(entity) {
        val u = User(id = this.id)
        u.role = role
        u.createdAt = createdAt
        u.updatedAt = updatedAt
        u.lastActive = lastActive
        u.invisible = invisible
        u.extraData = extraData
        u.banned = banned
        return u
    }
}
