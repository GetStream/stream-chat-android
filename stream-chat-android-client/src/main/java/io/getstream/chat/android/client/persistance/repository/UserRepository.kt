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

package io.getstream.chat.android.client.persistance.repository

import io.getstream.chat.android.models.User
import kotlinx.coroutines.flow.StateFlow

/**
 * Repository to read and write data about users.
 */
public interface UserRepository {

    /**
     * Insert many users.
     *
     * @param users collection of [User]
     */
    public suspend fun insertUsers(users: Collection<User>)

    /**
     * Inserts a users.
     *
     * @param user [User]
     */
    public suspend fun insertUser(user: User)

    /**
     * Inserts the current user of the SDK.
     *
     * @param user [User]
     */
    public suspend fun insertCurrentUser(user: User)

    /**
     * Selects a user by id.
     *
     * @param userId String.
     */
    public suspend fun selectUser(userId: String): User?

    /**
     * @return The list of users stored in the cache.
     */
    public suspend fun selectUsers(ids: List<String>): List<User>

    /**
     * Returns flow of latest updated users.
     */
    public fun observeLatestUsers(): StateFlow<Map<String, User>>

    /**
     * Clear users of this repository.
     */
    public suspend fun clear()
}
