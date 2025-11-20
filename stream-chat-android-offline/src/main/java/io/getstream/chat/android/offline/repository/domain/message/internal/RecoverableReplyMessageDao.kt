/*
 * Copyright (c) 2014-2023 Stream.io Inc. All rights reserved.
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

package io.getstream.chat.android.offline.repository.domain.message.internal

import io.getstream.chat.android.offline.repository.database.internal.ChatDatabase
import io.getstream.chat.android.offline.repository.domain.message.attachment.internal.ReplyAttachmentEntity

/**
 * A [ReplyMessageDao] implementation which lazily retrieves the original [ReplyMessageDao] from the currently active
 * [ChatDatabase] instance. The [ChatDatabase] instance can change in runtime if it becomes corrupted
 * and is manually recreated.
 *
 * @param getDatabase Method retrieving the current instance of [ChatDatabase].
 */
internal class RecoverableReplyMessageDao(private val getDatabase: () -> ChatDatabase) : ReplyMessageDao {

    private val delegate: ReplyMessageDao
        get() = getDatabase().replyMessageDao()

    override suspend fun selectById(id: String): ReplyMessageEntity? {
        return delegate.selectById(id)
    }

    override suspend fun insert(replyMessageEntities: List<ReplyMessageEntity>) {
        delegate.insert(replyMessageEntities)
    }

    override suspend fun insertInnerEntity(replyMessageEntities: List<ReplyMessageInnerEntity>) {
        delegate.insertInnerEntity(replyMessageEntities)
    }

    override suspend fun insertAttachments(attachmentEntities: List<ReplyAttachmentEntity>) {
        delegate.insertAttachments(attachmentEntities)
    }

    @Deprecated("This method is no longer used and will be removed in the future.")
    override suspend fun delete(replyMessageInnerEntity: ReplyMessageInnerEntity) {
        delegate.delete(replyMessageInnerEntity)
    }

    override suspend fun deleteAll() {
        delegate.deleteAll()
    }
}
