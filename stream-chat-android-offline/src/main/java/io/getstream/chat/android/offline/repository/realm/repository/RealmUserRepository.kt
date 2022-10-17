package io.getstream.chat.android.offline.repository.realm.repository

import io.getstream.chat.android.client.models.User
import io.getstream.chat.android.client.persistance.repository.UserRepository
import io.getstream.chat.android.offline.repository.realm.entity.UserEntityRealm
import io.getstream.chat.android.offline.repository.realm.entity.toModel
import io.getstream.chat.android.offline.repository.realm.entity.toRealm
import io.realm.kotlin.Realm
import io.realm.kotlin.UpdatePolicy
import io.realm.kotlin.ext.query
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow

public class RealmUserRepository(private val realm: Realm) : UserRepository {

  override suspend fun clear() {
//    realm.writeBlocking {
//      val allUsers = realm.query<UserEntityRealm>().find()
//      delete(allUsers)
//    }
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
    realm.query<UserEntityRealm>().find().map { entity -> entity.toModel() }

  override suspend fun selectUser(userId: String): User? {
    val id = userId.takeIf { it.isNotEmpty() } ?: "null"

    return realm.query<UserEntityRealm>("id = '$id'").first().find()?.toModel()
  }


  override suspend fun selectUsers(ids: List<String>): List<User> =
    realm.query<UserEntityRealm>().find().map { userEntity ->
      userEntity.toModel()
    }

  override suspend fun selectUsersLikeName(
    searchString: String,
    limit: Int,
    offset: Int
  ): List<User> = emptyList()
}



