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

import io.getstream.chat.android.client.models.Message
import io.getstream.chat.android.client.query.pagination.AnyChannelPaginationRequest
import io.getstream.chat.android.client.utils.SyncStatus
import java.util.Date

/**
 * Repository to read and write [Message] data.
 */
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
     * Selects messages by IDs.
     *
     * @param messageIds A list of [Message.id] as query specification.
     * @param forceCache A boolean flag that forces cache in repository and fetches data directly in database if passed
     * value is true.
     *
     * @return A list of messages found in repository.
     */
    public suspend fun selectMessages(messageIds: List<String>, forceCache: Boolean = false): List<Message>

    /**
     * Reads the message with passed ID.
     *
     * @param messageId String.
     */
    public suspend fun selectMessage(messageId: String): Message?

    /**
     * Inserts many messages.
     *
     * @param messages list of [Message]
     * @param cache Boolean.
     */
    public suspend fun insertMessages(messages: List<Message>, cache: Boolean = false)

    /**
     * Inserts a messages.
     *
     * @param message [Message]
     * @param cache Boolean.
     */
    public suspend fun insertMessage(message: Message, cache: Boolean = false)

    /**
     * Deletes all messages before a message with passed ID.
     *
     * @param cid of message - String.
     * @param hideMessagesBefore Boolean.
     */
    public suspend fun deleteChannelMessagesBefore(cid: String, hideMessagesBefore: Date)

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
}
