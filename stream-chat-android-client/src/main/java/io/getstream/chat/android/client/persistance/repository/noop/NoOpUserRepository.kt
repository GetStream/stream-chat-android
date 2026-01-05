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

package io.getstream.chat.android.client.persistance.repository.noop

import io.getstream.chat.android.client.persistance.repository.UserRepository
import io.getstream.chat.android.models.User
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow

/**
 * No-Op UserRepository.
 */
internal object NoOpUserRepository : UserRepository {
    override suspend fun insertUsers(users: Collection<User>) { /* No-Op */ }
    override suspend fun insertUser(user: User) { /* No-Op */ }
    override suspend fun insertCurrentUser(user: User) { /* No-Op */ }
    override suspend fun selectUser(userId: String): User? = null
    override suspend fun selectUsers(ids: List<String>): List<User> = emptyList()
    override fun observeLatestUsers(): StateFlow<Map<String, User>> = MutableStateFlow(emptyMap())
    override suspend fun clear() { /* No-Op */ }
}
