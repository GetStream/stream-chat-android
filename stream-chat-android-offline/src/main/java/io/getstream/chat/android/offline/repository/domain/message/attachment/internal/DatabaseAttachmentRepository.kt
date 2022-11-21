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

package io.getstream.chat.android.offline.repository.domain.message.attachment.internal

import io.getstream.chat.android.client.persistance.repository.AttachmentRepository
import io.getstream.chat.android.models.Attachment
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.distinctUntilChanged
import kotlinx.coroutines.flow.map

/**
 * Repository to access data of attachments. This implementation uses database.
 */
internal class DatabaseAttachmentRepository(private val attachmentDao: AttachmentDao) : AttachmentRepository {

    /**
     * Observes any change in attachments for an specific message.
     */
    override fun observeAttachmentsForMessage(messageId: String): Flow<List<Attachment>> {
        return attachmentDao.observeAttachmentsForMessage(messageId)
            .distinctUntilChanged()
            .map { attachments -> attachments.map(AttachmentEntity::toModel) }
    }

    override suspend fun clear() {
        attachmentDao.deleteAll()
    }
}
