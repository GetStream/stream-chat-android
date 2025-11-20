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

package io.getstream.chat.android.offline.repository.domain.message.internal

import io.getstream.chat.android.models.SyncStatus
import io.getstream.chat.android.offline.repository.database.internal.ChatDatabase
import io.getstream.chat.android.offline.repository.domain.message.attachment.internal.AttachmentEntity
import io.getstream.chat.android.offline.repository.domain.reaction.internal.ReactionEntity
import java.util.Date

/**
 * A [MessageDao] implementation which lazily retrieves the original [MessageDao] from the currently active
 * [ChatDatabase] instance. The [ChatDatabase] instance can change in runtime if it becomes corrupted
 * and is manually recreated.
 *
 * @param getDatabase Method retrieving the current instance of [ChatDatabase].
 */
@Suppress("TooManyFunctions")
internal class RecoverableMessageDao(private val getDatabase: () -> ChatDatabase) : MessageDao {

    private val delegate: MessageDao
        get() = getDatabase().messageDao()

    override suspend fun insert(messageEntities: List<MessageEntity>) {
        delegate.insert(messageEntities)
    }

    override fun deleteAttachments(messageIds: List<String>) {
        delegate.deleteAttachments(messageIds)
    }

    override fun deleteAttachmentsChunked(messageIds: List<String>) {
        delegate.deleteAttachmentsChunked(messageIds)
    }

    @Deprecated("This method is no longer used and will be removed in the future.")
    override suspend fun insert(messageEntity: MessageEntity) {
        delegate.insert(messageEntity)
    }

    override suspend fun insertMessageInnerEntity(messageInnerEntity: MessageInnerEntity): Long {
        return delegate.insertMessageInnerEntity(messageInnerEntity)
    }

    override suspend fun insertMessageInnerEntities(messageInnerEntities: List<MessageInnerEntity>): List<Long> {
        return delegate.insertMessageInnerEntities(messageInnerEntities)
    }

    override fun updateMessageInnerEntity(messageInnerEntity: MessageInnerEntity) {
        delegate.updateMessageInnerEntity(messageInnerEntity)
    }

    @Deprecated("This method is no longer used and will be removed in the future.")
    override suspend fun upsertMessageInnerEntity(messageInnerEntity: MessageInnerEntity) {
        delegate.upsertMessageInnerEntity(messageInnerEntity)
    }

    override suspend fun upsertMessageInnerEntities(messageInnerEntities: List<MessageInnerEntity>) {
        delegate.upsertMessageInnerEntities(messageInnerEntities)
    }

    override suspend fun insertAttachments(attachmentEntities: List<AttachmentEntity>) {
        delegate.insertAttachments(attachmentEntities)
    }

    override suspend fun insertReactions(reactions: List<ReactionEntity>) {
        delegate.insertReactions(reactions)
    }

    override suspend fun insertDraftMessages(draftMessage: DraftMessageEntity) {
        delegate.insertDraftMessages(draftMessage)
    }

    override suspend fun selectDraftMessages(): List<DraftMessageEntity> {
        return delegate.selectDraftMessages()
    }

    override suspend fun selectDraftMessageByCid(cid: String): DraftMessageEntity? {
        return delegate.selectDraftMessageByCid(cid)
    }

    override suspend fun selectDraftMessageByParentId(parentId: String): DraftMessageEntity? {
        return delegate.selectDraftMessageByParentId(parentId)
    }

    override suspend fun deleteDraftMessage(messageId: String) {
        delegate.deleteDraftMessage(messageId)
    }

    override suspend fun messagesForChannelNewerThan(
        cid: String,
        limit: Int,
        dateFilter: Date,
    ): List<MessageEntity> {
        return delegate.messagesForChannelNewerThan(cid, limit, dateFilter)
    }

    override suspend fun messagesForChannelEqualOrNewerThan(
        cid: String,
        limit: Int,
        dateFilter: Date,
    ): List<MessageEntity> {
        return delegate.messagesForChannelEqualOrNewerThan(cid, limit, dateFilter)
    }

    override suspend fun messagesForChannelOlderThan(
        cid: String,
        limit: Int,
        dateFilter: Date,
    ): List<MessageEntity> {
        return delegate.messagesForChannelOlderThan(cid, limit, dateFilter)
    }

    override suspend fun messagesForChannelEqualOrOlderThan(
        cid: String,
        limit: Int,
        dateFilter: Date,
    ): List<MessageEntity> {
        return delegate.messagesForChannelEqualOrOlderThan(cid, limit, dateFilter)
    }

    override suspend fun messagesForChannel(cid: String, limit: Int): List<MessageEntity> {
        return delegate.messagesForChannel(cid, limit)
    }

    override suspend fun messagesForThread(messageId: String, limit: Int): List<MessageEntity> {
        return delegate.messagesForThread(messageId, limit)
    }

    override suspend fun deleteChannelMessagesBefore(cid: String, deleteMessagesBefore: Date) {
        delegate.deleteChannelMessagesBefore(cid, deleteMessagesBefore)
    }

    override suspend fun deleteMessage(cid: String, messageId: String) {
        delegate.deleteMessage(cid, messageId)
    }

    override suspend fun deleteMessages(cid: String) {
        delegate.deleteMessages(cid)
    }

    override suspend fun deleteMessages(ids: List<String>) {
        delegate.deleteMessages(ids)
    }

    override suspend fun select(ids: List<String>): List<MessageEntity> {
        return delegate.select(ids)
    }

    override suspend fun selectChunked(ids: List<String>): List<MessageEntity> {
        return delegate.selectChunked(ids)
    }

    override suspend fun select(id: String): MessageEntity? {
        return delegate.select(id)
    }

    override suspend fun selectWaitForAttachments(): List<MessageEntity> {
        return delegate.selectWaitForAttachments()
    }

    override suspend fun selectBySyncStatus(syncStatus: SyncStatus, limit: Int): List<MessageEntity> {
        return delegate.selectBySyncStatus(syncStatus, limit)
    }

    override suspend fun selectByUserId(userId: String): List<MessageEntity> {
        return delegate.selectByUserId(userId)
    }

    override suspend fun selectByCidAndUserId(cid: String, userId: String): List<MessageEntity> {
        return delegate.selectByCidAndUserId(cid, userId)
    }

    override suspend fun selectIdsBySyncStatus(syncStatus: SyncStatus, limit: Int): List<String> {
        return delegate.selectIdsBySyncStatus(syncStatus, limit)
    }

    override suspend fun selectMessagesWithPoll(pollId: String): List<MessageEntity> {
        return delegate.selectMessagesWithPoll(pollId)
    }

    override suspend fun deleteAll() {
        delegate.deleteAll()
    }
}
