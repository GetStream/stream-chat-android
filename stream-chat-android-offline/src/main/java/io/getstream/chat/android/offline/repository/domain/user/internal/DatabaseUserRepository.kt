/*
 * Copyright (c) 2014-2022 Stream.io Inc. All rights reserved.
 *
 * Licensed under the Stream License;
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *    https://github.com/GetStream/stream-chat-android/blob/main/LICENSE
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package io.getstream.chat.android.offline.repository.domain.user.internal

import androidx.collection.LruCache
import io.getstream.chat.android.client.persistance.repository.UserRepository
import io.getstream.chat.android.models.User
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow

internal class DatabaseUserRepository(
    private val userDao: UserDao,
    cacheSize: Int = 1000,
) : UserRepository {
    // the user cache is simple, just keeps the last 100 users in memory
    private val userCache = LruCache<String, User>(cacheSize)

    private val latestUsersFlow: MutableStateFlow<Map<String, User>> = MutableStateFlow(emptyMap())

    override fun observeLatestUsers(): StateFlow<Map<String, User>> = latestUsersFlow

    override suspend fun clear() {
        userDao.deleteAll()
    }

    /**
     * Insert many users.
     *
     * @param users collection of [User]
     */
    override suspend fun insertUsers(users: Collection<User>) {
        if (users.isEmpty()) return
        cacheUsers(users)
        userDao.insertMany(users.map(::toEntity))
    }

    /**
     * Inserts a users.
     *
     * @param user [User]
     */
    override suspend fun insertUser(user: User) {
        cacheUsers(listOf(user))
        userDao.insert(toEntity(user))
    }

    /**
     * Inserts the current user of the SDK.
     *
     * @param user [User]
     */
    override suspend fun insertCurrentUser(user: User) {
        insertUser(user)
        val userEntity = toEntity(user).copy(id = ME_ID)
        userDao.insert(userEntity)
    }

    /**
     * Selects a user by id.
     *
     * @param userId String.
     */
    override suspend fun selectUser(userId: String): User? {
        return userCache[userId] ?: userDao.select(userId)?.let(::toModel)?.also { cacheUsers(listOf(it)) }
    }

    /**
     * @return The list of users stored in the cache.
     */
    override suspend fun selectUsers(ids: List<String>): List<User> {
        val cachedUsers = ids.mapNotNullTo(mutableListOf(), userCache::get)
        val missingUserIds = ids.minus(cachedUsers.map(User::id))

        return cachedUsers + userDao.select(missingUserIds).map(::toModel).also { cacheUsers(it) }
    }

    /**
     * Select all users respecting a limit and a offset.
     *
     * @param limit Int.
     * @param offset Int.
     */
    override suspend fun selectAllUsers(limit: Int, offset: Int): List<User> {
        return userDao.selectAllUser(limit, offset).map(::toModel)
    }

    /**
     * Selects users with a name that looks like the of wanted.
     *
     * @param searchString - The name of the user.
     * @param limit Int
     * @param offset Int
     */
    override suspend fun selectUsersLikeName(searchString: String, limit: Int, offset: Int): List<User> {
        return userDao.selectUsersLikeName("$searchString%", limit, offset).map(::toModel)
    }

    private fun cacheUsers(users: Collection<User>) {
        for (userEntity in users) {
            userCache.put(userEntity.id, userEntity)
        }
        latestUsersFlow.value = userCache.snapshot()
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
            mutes = mutes.map { mute -> mute.target.id },
        )
    }

    private fun toModel(userEntity: UserEntity): User = with(userEntity) {
        User(
            id = this.originalId,
            name = name,
            image = image,
            role = role,
            createdAt = createdAt,
            updatedAt = updatedAt,
            lastActive = lastActive,
            invisible = invisible,
            extraData = extraData.toMutableMap(),
            banned = banned,
        )
    }

    companion object {
        private const val ME_ID = "me"
    }
}
