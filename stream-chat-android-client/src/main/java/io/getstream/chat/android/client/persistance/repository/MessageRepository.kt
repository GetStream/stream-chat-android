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

package io.getstream.chat.android.client.persistance.repository

import io.getstream.chat.android.client.query.pagination.AnyChannelPaginationRequest
import io.getstream.chat.android.models.DraftMessage
import io.getstream.chat.android.models.Message
import io.getstream.chat.android.models.SyncStatus
import java.util.Date

/**
 * Repository to read and write [Message] data.
 */

@Suppress("TooManyFunctions")
public interface MessageRepository {

    /**
     * Select messages for a channel in a desired page.
     *
     * @param cid String.
     * @param pagination [AnyChannelPaginationRequest]
     */
    public suspend fun selectMessagesForChannel(
        cid: String,
        pagination: AnyChannelPaginationRequest?,
    ): List<Message>

    /**
     * Select messages for a thread in a desired page.
     *
     * @param messageId String.
     * @param limit limit of messages
     */
    public suspend fun selectMessagesForThread(
        messageId: String,
        limit: Int,
    ): List<Message>

    /**
     * Selects all messages from a given user.
     *
     * @param userId The user ID to filter messages.
     */
    public suspend fun selectAllUserMessages(userId: String): List<Message>

    /**
     * Selects all messages from a given user in a given channel.
     *
     * @param cid The channel ID to filter messages.
     * @param userId The user ID to filter messages.
     */
    public suspend fun selectAllChannelUserMessages(cid: String, userId: String): List<Message>

    /**
     * Selects messages by IDs.
     *
     * @param messageIds A list of [Message.id] as query specification.
     *
     * @return A list of messages found in repository.
     */
    public suspend fun selectMessages(messageIds: List<String>): List<Message>

    /**
     * Reads the message with passed ID.
     *
     * @param messageId String.
     */
    public suspend fun selectMessage(messageId: String): Message?

    /**
     * Selects all messages with a poll with the passed ID.
     *
     * @param pollId The ID of the poll.
     *
     * @return A list of messages with the poll.
     */
    public suspend fun selectMessagesWithPoll(pollId: String): List<Message>

    /**
     * Inserts many messages.
     *
     * @param messages list of [Message]
     */
    public suspend fun insertMessages(messages: List<Message>)

    /**
     * Inserts a messages.
     *
     * @param message [Message]
     */
    public suspend fun insertMessage(message: Message)

    /**
     * Deletes all messages before a message with passed ID.
     *
     * @param cid of message - String.
     * @param hideMessagesBefore Boolean.
     */
    public suspend fun deleteChannelMessagesBefore(cid: String, hideMessagesBefore: Date)

    /**
     * Deletes all messages from a channel.
     *
     * @param cid of message - String.
     */
    public suspend fun deleteChannelMessages(cid: String)

    /**
     * Deletes message.
     *
     * @param message [Message]
     */
    public suspend fun deleteChannelMessage(message: Message)

    /**
     * Selects all message ids of a [SyncStatus]
     *
     * @param syncStatus [SyncStatus]
     */
    public suspend fun selectMessageIdsBySyncState(syncStatus: SyncStatus): List<String>

    /**
     * Selects all message of a [SyncStatus]
     *
     * @param syncStatus [SyncStatus]
     */
    public suspend fun selectMessageBySyncState(syncStatus: SyncStatus): List<Message>

    /**
     * Insert a draft message.
     *
     * @param message [DraftMessage] to be inserted.
     */

    public suspend fun insertDraftMessage(message: DraftMessage)

    /**
     * Selects all draft messages.
     */
    public suspend fun selectDraftMessages(): List<DraftMessage>

    /**
     * Selects a draft message by its channel ID.
     */
    public suspend fun selectDraftMessagesByCid(cid: String): DraftMessage?

    /**
     * Selects a draft message by its parent message ID.
     */
    public suspend fun selectDraftMessageByParentId(parentId: String): DraftMessage?

    /**
     * Delete a draft message.
     */
    public suspend fun deleteDraftMessage(message: DraftMessage)

    /**
     * Evict messages from the repository.
     */
    public suspend fun evictMessages()

    /**
     * Evicts a message from the repository.
     *
     * @param messageId String.
     */
    public suspend fun evictMessage(messageId: String)

    /**
     * Deletes a Poll by its ID.
     *
     * @param pollId The ID of the Poll to delete.
     */
    public suspend fun deletePoll(pollId: String)

    /**
     * Clear messages of this repository.
     */
    public suspend fun clear()
}
