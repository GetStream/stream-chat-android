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

import io.getstream.chat.android.client.persistance.repository.MessageRepository
import io.getstream.chat.android.client.query.pagination.AnyChannelPaginationRequest
import io.getstream.chat.android.models.DraftMessage
import io.getstream.chat.android.models.Message
import io.getstream.chat.android.models.SyncStatus
import java.util.Date

/**
 * No-Op MessageRepository.
 */

@Suppress("TooManyFunctions")
internal object NoOpMessageRepository : MessageRepository {
    override suspend fun selectMessages(messageIds: List<String>): List<Message> = emptyList()
    override suspend fun selectMessage(messageId: String): Message? = null
    override suspend fun insertMessages(messages: List<Message>) { /* No-Op */ }
    override suspend fun insertMessage(message: Message) { /* No-Op */ }
    override suspend fun deleteChannelMessagesBefore(cid: String, hideMessagesBefore: Date) { /* No-Op */ }
    override suspend fun deleteChannelMessages(cid: String) { /* No-Op */ }
    override suspend fun deleteChannelMessage(message: Message) { /* No-Op */ }
    override suspend fun deleteMessages(messages: List<Message>) { /* No-Op */ }
    override suspend fun selectMessageIdsBySyncState(syncStatus: SyncStatus): List<String> = emptyList()
    override suspend fun selectMessageBySyncState(syncStatus: SyncStatus): List<Message> = emptyList()
    override suspend fun selectMessagesWithPoll(pollId: String): List<Message> = emptyList()
    override suspend fun deleteDraftMessage(message: DraftMessage) { /* No-Op */ }
    override suspend fun selectDraftMessages(): List<DraftMessage> = emptyList()
    override suspend fun selectDraftMessagesByCid(cid: String): DraftMessage? = null
    override suspend fun selectDraftMessageByParentId(parentId: String): DraftMessage? = null
    override suspend fun insertDraftMessage(message: DraftMessage) { /* No-Op */ }
    override suspend fun evictMessages() { /* No-Op */ }
    override suspend fun evictMessage(messageId: String) { /* No-Op */ }
    override suspend fun deletePoll(pollId: String) { /* No-Op */ }

    override suspend fun clear() { /* No-Op */ }
    override suspend fun selectMessagesForChannel(
        cid: String,
        pagination: AnyChannelPaginationRequest?,
    ): List<Message> = emptyList()
    override suspend fun selectMessagesForThread(messageId: String, limit: Int): List<Message> = emptyList()
    override suspend fun selectAllUserMessages(userId: String): List<Message> = emptyList()
    override suspend fun selectAllChannelUserMessages(cid: String, userId: String): List<Message> = emptyList()
}
