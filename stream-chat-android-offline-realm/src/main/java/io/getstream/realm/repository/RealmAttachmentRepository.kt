package io.getstream.realm.repository

import io.getstream.chat.android.client.models.Attachment
import io.getstream.chat.android.client.persistance.repository.AttachmentRepository
import io.getstream.realm.entity.AttachmentEntityRealm
import io.getstream.realm.entity.toDomain
import io.realm.kotlin.Realm
import io.realm.kotlin.ext.query
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map

internal class RealmAttachmentRepository(private val realm: Realm) : AttachmentRepository {

    override fun observeAttachmentsForMessage(messageId: String): Flow<List<Attachment>> {
        return realm.query<AttachmentEntityRealm>("message_id == '$messageId'")
            .asFlow()
            .map { results ->
                results.list.map { attachmentEntity ->
                    attachmentEntity.toDomain()
                }
            }
    }

    override suspend fun clear() {
        // To implement
    }
}
