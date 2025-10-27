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

package io.getstream.chat.android.client.persistence.repository

import io.getstream.chat.android.client.persistence.db.dao.MessageReceiptDao
import io.getstream.chat.android.client.persistence.db.entity.MessageReceiptEntity
import io.getstream.chat.android.client.receipts.MessageReceipt
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map

internal class MessageReceiptRepositoryImpl(
    private val dao: MessageReceiptDao,
) : MessageReceiptRepository {

    override suspend fun upsertMessageReceipts(receipts: List<MessageReceipt>) {
        dao.upsert(receipts.map(MessageReceipt::toEntity))
    }

    override fun getAllMessageReceiptsByType(type: String, limit: Int): Flow<List<MessageReceipt>> =
        dao.selectAllByType(type, limit).map { receipts ->
            receipts.map(MessageReceiptEntity::toModel)
        }

    override suspend fun deleteMessageReceiptsByMessageIds(messageIds: List<String>) {
        dao.deleteByMessageIds(messageIds)
    }

    override suspend fun clearMessageReceipts() {
        dao.deleteAll()
    }
}
