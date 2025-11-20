/*
 * Copyright (c) 2014-2025 Stream.io Inc. All rights reserved.
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

import io.getstream.chat.android.offline.repository.database.internal.ChatDatabase

/**
 * A [UserDao] implementation which lazily retrieves the original [UserDao] from the currently active
 * [ChatDatabase] instance. The [ChatDatabase] instance can change in runtime if it becomes corrupted
 * and is manually recreated.
 *
 * @param getDatabase Method retrieving the current instance of [ChatDatabase].
 */
internal class RecoverableUserDao(private val getDatabase: () -> ChatDatabase) : UserDao {

    private val delegate: UserDao
        get() = getDatabase().userDao()

    override suspend fun insertMany(users: List<UserEntity>) {
        delegate.insertMany(users)
    }

    override suspend fun insert(userEntity: UserEntity) {
        delegate.insert(userEntity)
    }

    override suspend fun select(ids: List<String>): List<UserEntity> {
        return delegate.select(ids)
    }

    override suspend fun select(id: String): UserEntity? {
        return delegate.select(id)
    }

    override fun selectAllUser(
        limit: Int,
        offset: Int,
    ): List<UserEntity> {
        return delegate.selectAllUser(limit, offset)
    }

    override fun selectUsersLikeName(
        searchString: String,
        limit: Int,
        offset: Int,
    ): List<UserEntity> {
        return delegate.selectUsersLikeName(searchString, limit, offset)
    }

    override suspend fun deleteAll() {
        delegate.deleteAll()
    }
}
