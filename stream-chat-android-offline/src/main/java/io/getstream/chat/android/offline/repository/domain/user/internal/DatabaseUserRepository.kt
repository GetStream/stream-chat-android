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
import io.getstream.chat.android.offline.extensions.launchWithMutex
import io.getstream.log.taggedLogger
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch
import kotlinx.coroutines.sync.Mutex
import kotlinx.coroutines.sync.withLock

internal class DatabaseUserRepository(
    private val scope: CoroutineScope,
    private val userDao: UserDao,
    cacheSize: Int = 1000,
) : UserRepository {
    private val logger by taggedLogger("Chat:UserRepository")

    // the user cache is simple, just keeps the last 100 users in memory
    private val userCache = LruCache<String, User>(cacheSize)
    private val latestUsersFlow: MutableStateFlow<Map<String, User>> = MutableStateFlow(emptyMap())
    private val dbMutex = Mutex()

    override fun observeLatestUsers(): StateFlow<Map<String, User>> = latestUsersFlow

    override suspend fun clear() {
        dbMutex.withLock {
            userDao.deleteAll()
        }
    }

    /**
     * Insert many users.
     *
     * @param users collection of [User]
     */
    override suspend fun insertUsers(users: Collection<User>) {
        if (users.isEmpty()) return
        val usersToInsert = users
            .filter { it != userCache[it.id] }
            .map { it.toEntity() }
        cacheUsers(users)
        scope.launchWithMutex(dbMutex) {
            logger.v { "[insertUsers] inserting ${usersToInsert.size} entities on DB, updated ${users.size} on cache" }
            usersToInsert
                .takeUnless { it.isEmpty() }
                ?.let { userDao.insertMany(it) }
        }
    }

    /**
     * Inserts a users.
     *
     * @param user [User]
     */
    override suspend fun insertUser(user: User) {
        insertUsers(listOf(user))
    }

    /**
     * Inserts the current user of the SDK.
     *
     * @param user [User]
     */
    override suspend fun insertCurrentUser(user: User) {
        insertUser(user)
        scope.launchWithMutex(dbMutex) {
            val userEntity = user.toEntity().copy(id = ME_ID)
            userDao.insert(userEntity)
        }
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
        val missingUserIds = ids.minus(cachedUsers.map(User::id).toSet())

        return cachedUsers + userDao.select(missingUserIds).map(::toModel).also { cacheUsers(it) }
    }

    private fun cacheUsers(users: Collection<User>) {
        for (userEntity in users) {
            userCache.put(userEntity.id, userEntity)
        }
        scope.launch { latestUsersFlow.value = userCache.snapshot() }
    }

    private fun User.toEntity(): UserEntity =
        UserEntity(
            id = id,
            name = name,
            image = image,
            originalId = id,
            role = role,
            createdAt = createdAt,
            updatedAt = updatedAt,
            lastActive = lastActive,
            invisible = isInvisible,
            privacySettings = privacySettings?.toEntity(),
            banned = isBanned,
            mutes = mutes.map { mute -> mute.target?.id.orEmpty() },
            teams = teams,
            teamsRole = teamsRole,
            avgResponseTime = avgResponseTime,
            extraData = extraData,
        )

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
            privacySettings = privacySettings?.toModel(),
            banned = banned,
            teams = teams,
            teamsRole = teamsRole,
            avgResponseTime = avgResponseTime,
            extraData = extraData.toMutableMap(),
        )
    }

    companion object {
        private const val ME_ID = "me"
    }
}
