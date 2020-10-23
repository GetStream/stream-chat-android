package com.getstream.sdk.chat.adapter

import io.getstream.chat.android.client.models.ChannelUserRead
import io.getstream.chat.android.client.models.Message
import io.getstream.chat.android.client.models.User
import java.util.Date
import java.util.zip.CRC32
import java.util.zip.Checksum

/**
 * MessageListItem is a sealed class with everything that is typically displayed in a message list
 * - DateSeparatorItem
 * - MessageItem
 * - TypingItem
 * - ThreadSeparatorItem
 * - ReadStateItem
 */
public sealed class MessageListItem {

    internal fun getStableId(): Long {
        val checksum: Checksum = CRC32()
        val plaintext = when (this) {
            is TypingItem -> "id_typing"
            is ThreadSeparatorItem -> "id_thread_separator"
            is MessageItem -> message.id
            is DateSeparatorItem -> date.toString()
            is ReadStateItem -> "read_" + reads.map { it.user.id }.joinToString { "," }
        }
        checksum.update(plaintext.toByteArray(), 0, plaintext.toByteArray().size)
        return checksum.value
    }

    public data class DateSeparatorItem(
        val date: Date,
    ) : MessageListItem()

    public data class MessageItem(
        val message: Message,
        val positions: List<Position> = listOf(),
        val isMine: Boolean = false,
        val messageReadBy: List<ChannelUserRead> = listOf()
    ) : MessageListItem() {
        public val isTheirs: Boolean
            get() = !isMine
    }

    public data class TypingItem(
        val users: List<User>,
    ) : MessageListItem()

    public data class ReadStateItem(
        val reads: List<ChannelUserRead>,
    ) : MessageListItem()

    public data class ThreadSeparatorItem(
        val date: Date = Date(),
    ) : MessageListItem()

    public enum class Position {
        TOP,
        MIDDLE,
        BOTTOM,
    }
}
