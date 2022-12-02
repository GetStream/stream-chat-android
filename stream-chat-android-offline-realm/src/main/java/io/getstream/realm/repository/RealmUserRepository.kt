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

package io.getstream.realm.repository

import io.getstream.chat.android.client.persistance.repository.UserRepository
import io.getstream.chat.android.models.User
import io.getstream.realm.entity.UserEntityRealm
import io.getstream.realm.entity.toDomain
import io.getstream.realm.entity.toRealm
import io.realm.kotlin.Realm
import io.realm.kotlin.UpdatePolicy
import io.realm.kotlin.ext.query
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow

public class RealmUserRepository(private val realm: Realm) : UserRepository {

    override suspend fun clear() {
        val allUsers = realm.query<UserEntityRealm>().find()
        realm.writeBlocking {
            delete(allUsers)
        }
    }

    override suspend fun insertCurrentUser(user: User) {
        realm.writeBlocking { copyToRealm(user.toRealm(), updatePolicy = UpdatePolicy.ALL) }
    }

    override suspend fun insertUser(user: User) {
        realm.writeBlocking { copyToRealm(user.toRealm(), updatePolicy = UpdatePolicy.ALL) }
    }

    override suspend fun insertUsers(users: Collection<User>) {
        val usersRealm = users.map { user -> user.toRealm() }

        realm.writeBlocking {
            usersRealm.forEach { entity ->
                copyToRealm(entity, updatePolicy = UpdatePolicy.ALL)
            }
        }
    }

    override fun observeLatestUsers(): StateFlow<Map<String, User>> = MutableStateFlow(emptyMap())

    override suspend fun selectAllUsers(limit: Int, offset: Int): List<User> =
        realm.query<UserEntityRealm>().find().map { entity -> entity.toDomain() }

    override suspend fun selectUser(userId: String): User? {
        val id = userId.takeIf { it.isNotEmpty() } ?: "null"

        return realm.query<UserEntityRealm>("id = '$id'").first().find()?.toDomain()
    }

    override suspend fun selectUsers(ids: List<String>): List<User> =
        realm.query<UserEntityRealm>().find().map { userEntity ->
            userEntity.toDomain()
        }

    override suspend fun selectUsersLikeName(
        searchString: String,
        limit: Int,
        offset: Int,
    ): List<User> = emptyList()
}
