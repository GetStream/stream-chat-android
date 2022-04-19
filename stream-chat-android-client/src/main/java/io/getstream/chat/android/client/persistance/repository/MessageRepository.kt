package io.getstream.chat.android.client.persistance.repository

import io.getstream.chat.android.client.models.Message
import io.getstream.chat.android.client.query.pagination.AnyChannelPaginationRequest
import io.getstream.chat.android.client.utils.SyncStatus
import java.util.Date

public interface MessageRepository {
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
    public suspend fun selectMessage(messageId: String): Message?
    public suspend fun insertMessages(messages: List<Message>, cache: Boolean = false)
    public suspend fun insertMessage(message: Message, cache: Boolean = false)
    public suspend fun deleteChannelMessagesBefore(cid: String, hideMessagesBefore: Date)
    public suspend fun deleteChannelMessage(message: Message)
    public suspend fun selectMessageBySyncState(syncStatus: SyncStatus): List<Message>
}
