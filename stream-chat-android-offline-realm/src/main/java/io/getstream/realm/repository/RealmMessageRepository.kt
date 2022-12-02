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

import io.getstream.chat.android.client.persistance.repository.MessageRepository
import io.getstream.chat.android.client.query.pagination.AnyChannelPaginationRequest
import io.getstream.chat.android.models.Message
import io.getstream.chat.android.models.SyncStatus
import io.getstream.realm.entity.MessageEntityRealm
import io.getstream.realm.entity.toDomain
import io.getstream.realm.entity.toRealm
import io.realm.kotlin.Realm
import io.realm.kotlin.UpdatePolicy
import io.realm.kotlin.ext.query
import java.util.Date

public class RealmMessageRepository(private val realm: Realm) : MessageRepository {

    override suspend fun clear() {
        realm.writeBlocking {
            query<MessageEntityRealm>().find().let(this::delete)
        }
    }

    override suspend fun deleteChannelMessage(message: Message) {
        realm.query<MessageEntityRealm>("id == '${message.id}'")
            .first()
            .find()
            ?.let { messageEntity ->
                realm.writeBlocking {
                    findLatest(messageEntity)?.let(::delete)
                }
            }
    }

    override suspend fun deleteChannelMessagesBefore(cid: String, hideMessagesBefore: Date) {
        realm.query<MessageEntityRealm>("created_at < $0", hideMessagesBefore)
            .find()
            .let { messagesBefore ->
                realm.writeBlocking {
                    messagesBefore.mapNotNull(::findLatest).forEach(::delete)
                }
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
            ?.toDomain()
    }

    override suspend fun selectMessageBySyncState(syncStatus: SyncStatus): List<Message> {
        return realm.query<MessageEntityRealm>("sync_status == $0", syncStatus.status)
            .find()
            .map { entity -> entity.toDomain() }
    }

    override suspend fun selectMessageIdsBySyncState(syncStatus: SyncStatus): List<String> {
        return selectMessageBySyncState(syncStatus).map { message -> message.id }
    }

    override suspend fun selectMessages(
        messageIds: List<String>,
        forceCache: Boolean,
    ): List<Message> {
        val idsString = messageIds.joinToString(separator = ", ", prefix = "{", postfix = "}") { id -> "'$id'" }

        return realm.query<MessageEntityRealm>("id IN $idsString")
            .find()
            .map { entity -> entity.toDomain() }
    }

    override suspend fun selectMessagesForChannel(
        cid: String,
        pagination: AnyChannelPaginationRequest?,
    ): List<Message> {
        return realm.query<MessageEntityRealm>("cid == '$cid'")
            .find()
            .map { entity -> entity.toDomain() }
    }

    override suspend fun selectMessagesForThread(messageId: String, limit: Int): List<Message> {
        return realm.query<MessageEntityRealm>("parent_id == '$messageId'")
            .find()
            .map { entity -> entity.toDomain() }
    }
}
