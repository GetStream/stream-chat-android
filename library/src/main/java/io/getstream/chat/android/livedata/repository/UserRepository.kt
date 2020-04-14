package io.getstream.chat.android.livedata.repository

import io.getstream.chat.android.client.ChatClient
import io.getstream.chat.android.client.models.User
import io.getstream.chat.android.livedata.dao.UserDao
import io.getstream.chat.android.livedata.entity.UserEntity

class UserRepository(
    var userDao: UserDao,
    var cacheSize: Int = 1000,
    var client: ChatClient
) {
    suspend fun insertUserEntities(userEntities: List<UserEntity>) {

        userDao.insertMany(userEntities)
    }
    suspend fun insert(users: List<User>) {
        val userEntities = mutableListOf<UserEntity>()
        for (user in users) {
            userEntities.add(UserEntity(user))
        }
        userDao.insertMany(userEntities)
    }

    suspend fun insert(user: User) {
        userDao.insert(UserEntity(user))
    }

    suspend fun select(userId: String): UserEntity? {
        return userDao.select(userId)
    }

    suspend fun select(userIds: List<String>): List<UserEntity> {
        return userDao.select(userIds)
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