package io.getstream.chat.android.livedata.repository

import androidx.collection.LruCache
import io.getstream.chat.android.client.models.User
import io.getstream.chat.android.livedata.dao.UserDao
import io.getstream.chat.android.livedata.entity.UserEntity

class UserRepository(
    var userDao: UserDao,
    var cacheSize: Int = 100,
    var currentUser: User?
) {
    // the user cache is simple, just keeps the last 100 users in memory
    var userCache = LruCache<String, UserEntity>(cacheSize)

    suspend fun insert(userEntities: List<UserEntity>) {
        if (userEntities.isEmpty()) return
        cacheUserEntities(userEntities)
        userDao.insertMany(userEntities)
    }

    private fun cacheUserEntities(userEntities: List<UserEntity>) {
        for (userEntity in userEntities) {
            userCache.put(userEntity.id, userEntity)
        }
    }

    suspend fun insertManyUsers(users: List<User>) {
        val userEntities = users.map { UserEntity(it) }
        insert(userEntities)
    }

    suspend fun insertUser(user: User) {
        val userEntity = UserEntity(user)
        insert(listOf(userEntity))
    }

    suspend fun insertMe(user: User) {
        val userEntity = UserEntity(user)
        userEntity.originalId = user.id
        userEntity.id = "me"
        insert(listOf(userEntity))
    }

    suspend fun selectMe(): User? {
        val userEntity = select("me")
        if (userEntity != null) {
            userEntity.id = userEntity.originalId
            return userEntity.toUser()
        }
        return null
    }

    suspend fun select(userId: String): UserEntity? {
        return select(listOf(userId)).getOrElse(0) { null }
    }

    suspend fun select(userIds: List<String>): List<UserEntity> {
        val cacheUsers: MutableList<UserEntity> = mutableListOf()
        for (userId in userIds) {
            val user = userCache.get(userId)
            user?.let { cacheUsers.add(it) }
        }
        val missingUserIds = userIds.filter { userCache.get(it) == null }
        val dbUsers = userDao.select(missingUserIds).toMutableList()
        cacheUserEntities(dbUsers)
        dbUsers.addAll(cacheUsers)
        return dbUsers
    }

    suspend fun selectUserMap(userIds: List<String>): MutableMap<String, User> {
        val userEntities = select(userIds.toSet().toList())

        val userMap = mutableMapOf<String, User>()
        for (userEntity in userEntities) {
            userMap[userEntity.id] = userEntity.toUser()
        }
        // add the current user
        currentUser?.let {
            userMap[it.id] = it
        }

        return userMap
    }
}
