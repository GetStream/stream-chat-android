package io.getstream.chat.android.livedata.repository

import androidx.collection.LruCache
import io.getstream.chat.android.client.ChatClient
import io.getstream.chat.android.client.models.User
import io.getstream.chat.android.livedata.dao.UserDao
import io.getstream.chat.android.livedata.entity.UserEntity

class UserRepository(
    var userDao: UserDao,
    var cacheSize: Int = 100,
    var client: ChatClient
) {
    // the user cache is simple, just keeps the last 100 users in memory
    var userCache = LruCache<String, UserEntity>(cacheSize)

    suspend fun insertUserEntities(userEntities: List<UserEntity>) {
        if (userEntities.isEmpty()) return

        userDao.insertMany(userEntities)
        for (userEntity in userEntities) {
            userCache.put(userEntity.id, userEntity)
        }
    }
    suspend fun insertMany(users: List<User>) {
        val userEntities = users.map { UserEntity(it) }
        insertUserEntities(userEntities)
    }

    suspend fun insert(user: User) {
        val userEntity = UserEntity(user)
        insertUserEntities(listOf(userEntity))
    }

    suspend fun select(userId: String): UserEntity? {
        return select(listOf(userId)).getOrElse(0) {null}
    }

    suspend fun select(userIds: List<String>): List<UserEntity> {
        val cacheUsers: MutableList<UserEntity> = mutableListOf()
        for (userId in userIds) {
            val user = userCache.get(userId)
            user?.let { cacheUsers.add(it) }
        }
        val missingUserIds = userIds.filter { userCache.get(it) == null }
        val dbUsers = userDao.select(missingUserIds).toMutableList()
        for (userEntity in dbUsers) {
            userCache.put(userEntity.id, userEntity)
        }
        dbUsers.addAll(cacheUsers)
        return dbUsers
    }

    suspend fun selectUserMap(userIds: List<String>): MutableMap<String, User> {
        val userEntities = select(userIds.toSet().toList())
        val userMap = mutableMapOf<String, User>()
        for (userEntity in userEntities) {
            userMap[userEntity.id] = userEntity.toUser()
        }
        client.getCurrentUser()?.let {
            userMap[it.id] = it
        }

        return userMap
    }
}