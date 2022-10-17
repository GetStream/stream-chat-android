package io.getstream.chat.android.offline.repository.realm.repository

import io.getstream.chat.android.client.models.Message
import io.getstream.chat.android.client.models.User
import io.getstream.chat.android.client.persistance.repository.MessageRepository
import io.getstream.chat.android.client.query.pagination.AnyChannelPaginationRequest
import io.getstream.chat.android.client.utils.SyncStatus
import io.getstream.chat.android.offline.repository.realm.entity.MessageEntityRealm
import io.getstream.chat.android.offline.repository.realm.entity.toDomain
import io.getstream.chat.android.offline.repository.realm.entity.toRealm
import io.realm.kotlin.Realm
import io.realm.kotlin.UpdatePolicy
import io.realm.kotlin.ext.query
import java.util.*

public class RealmMessageRepository(
  private val realm: Realm,
  private val user: suspend (userId: String) -> User
) : MessageRepository {

  override suspend fun clear() {
    realm.writeBlocking {
      query<MessageEntityRealm>().find().let(this::delete)
    }
  }

  override suspend fun deleteChannelMessage(message: Message) {
    realm.writeBlocking {
      delete(message.toRealm())
    }
  }

  override suspend fun deleteChannelMessagesBefore(cid: String, hideMessagesBefore: Date) {
    realm.query<MessageEntityRealm>("created_at < $0", hideMessagesBefore)
      .let { messagesBefore ->
        realm.writeBlocking { delete(messagesBefore) }
      }
  }

  override suspend fun insertMessage(message: Message, cache: Boolean) {
    realm.writeBlocking {
      copyToRealm(message.toRealm(), updatePolicy = UpdatePolicy.ALL)
    }
  }

  override suspend fun insertMessages(messages: List<Message>, cache: Boolean) {
    messages.forEach { message ->
      realm.writeBlocking {
        copyToRealm(message.toRealm(), updatePolicy = UpdatePolicy.ALL)
      }
    }
  }

  override suspend fun selectMessage(messageId: String): Message? {
    return realm.query<MessageEntityRealm>("id == '$messageId'")
      .first()
      .find()
      ?.toDomain(user)
  }

  override suspend fun selectMessageBySyncState(syncStatus: SyncStatus): List<Message> {
    return realm.query<MessageEntityRealm>("sync_status == $0", syncStatus.status)
      .find()
      .map { entity ->
        entity.toDomain(user)
      }
  }

  override suspend fun selectMessageIdsBySyncState(syncStatus: SyncStatus): List<String> {
    return selectMessageBySyncState(syncStatus).map { message -> message.id }
  }

  override suspend fun selectMessages(
    messageIds: List<String>,
    forceCache: Boolean
  ): List<Message> {
    val idsString = messageIds.joinToString(separator = ", ", prefix = "{", postfix = "}")

    return realm.query<MessageEntityRealm>("id IN $idsString")
      .find()
      .map { entity ->
        entity.toDomain(user)
      }
  }

  override suspend fun selectMessagesForChannel(
    cid: String,
    pagination: AnyChannelPaginationRequest?
  ): List<Message> {
    return realm.query<MessageEntityRealm>("cid == \"$cid\"")
      .find()
      .map { entity ->
        entity.toDomain(user)
      }
  }

  override suspend fun selectMessagesForThread(messageId: String, limit: Int): List<Message> {
    return realm.query<MessageEntityRealm>("parent_id == '$messageId'")
      .find()
      .map { entity ->
        entity.toDomain(user)
      }
  }
}
