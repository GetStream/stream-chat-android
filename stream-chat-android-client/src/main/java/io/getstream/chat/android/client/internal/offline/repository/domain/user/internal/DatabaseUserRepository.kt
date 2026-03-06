/*
 * Copyright (c) 2014-2026 Stream.io Inc. All rights reserved.
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

package io.getstream.chat.android.client.internal.offline.repository.domain.user.internal

import androidx.collection.LruCache
import io.getstream.chat.android.client.internal.offline.extensions.launchWithMutex
import io.getstream.chat.android.client.internal.offline.repository.domain.push.internal.toEntity
import io.getstream.chat.android.client.internal.offline.repository.domain.push.internal.toModel
import io.getstream.chat.android.client.persistance.repository.UserRepository
import io.getstream.chat.android.models.Mute
import io.getstream.chat.android.models.User
import io.getstream.log.taggedLogger
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
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

    // the user cache is simple, just keeps the last x users in memory, defined by cacheSize
    private val userCache = LruCache<String, User>(cacheSize)
    private val latestUsersFlow: MutableStateFlow<Map<String, User>> = MutableStateFlow(emptyMap())
    private val dbMutex = Mutex()
    private val pendingEntities = mutableListOf<UserEntity>()
    private val pendingMutex = Mutex()
    private var flushJob: Job? = null

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
        if (usersToInsert.isEmpty()) return
        pendingMutex.withLock {
            pendingEntities.addAll(usersToInsert)
            if (flushJob?.isActive != true) {
                flushJob = scope.launch {
                    delay(BATCH_FLUSH_DELAY_MS)
                    flushPendingUsers()
                }
            }
        }
    }

    private suspend fun flushPendingUsers() {
        val snapshot = pendingMutex.withLock {
            val copy = pendingEntities.toList()
            pendingEntities.clear()
            copy
        }
        if (snapshot.isEmpty()) return
        val deduped = snapshot.associateBy { it.id }.values.toList()
        logger.v { "[insertUsers] batch flushing ${deduped.size} entities to DB (from ${snapshot.size} enqueued)" }
        dbMutex.withLock {
            userDao.insertMany(deduped)
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
        var changed = false
        for (user in users) {
            if (userCache[user.id] != user) {
                userCache.put(user.id, user)
                changed = true
            }
        }
        if (changed) {
            scope.launch { latestUsersFlow.value = userCache.snapshot() }
        }
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
            mutes = mutes.map(Mute::toEntity),
            teams = teams,
            teamsRole = teamsRole,
            avgResponseTime = avgResponseTime,
            pushPreference = pushPreference?.toEntity(),
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
            mutes = mutes.map(UserMuteEntity::toModel),
            teams = teams,
            teamsRole = teamsRole,
            avgResponseTime = avgResponseTime,
            pushPreference = pushPreference?.toModel(),
            extraData = extraData.toMutableMap(),
        )
    }

    companion object {
        private const val ME_ID = "me"
        private const val BATCH_FLUSH_DELAY_MS = 3_000L
    }
}
