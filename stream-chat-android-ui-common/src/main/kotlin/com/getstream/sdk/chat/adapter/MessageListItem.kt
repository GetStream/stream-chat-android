package com.getstream.sdk.chat.adapter

import com.getstream.sdk.chat.adapter.MessageListItem.DateSeparatorItem
import com.getstream.sdk.chat.adapter.MessageListItem.LoadingMoreIndicatorItem
import com.getstream.sdk.chat.adapter.MessageListItem.MessageItem
import com.getstream.sdk.chat.adapter.MessageListItem.ReadStateItem
import com.getstream.sdk.chat.adapter.MessageListItem.ThreadSeparatorItem
import com.getstream.sdk.chat.adapter.MessageListItem.TypingItem
import io.getstream.chat.android.client.models.ChannelUserRead
import io.getstream.chat.android.client.models.Message
import io.getstream.chat.android.client.models.User
import java.io.Serializable
import java.util.Date
import java.util.zip.CRC32
import java.util.zip.Checksum

/**
 * [MessageListItem] represents elements that are displayed in a [com.getstream.sdk.chat.view.MessageListView].
 * There are the following subclasses of the [MessageListItem] available:
 * - [DateSeparatorItem]
 * - [MessageItem]
 * - [TypingItem]
 * - [ThreadSeparatorItem]
 * - [ReadStateItem]
 * - [LoadingMoreIndicatorItem]
 */
public sealed class MessageListItem {

    public fun getStableId(): Long {
        val checksum: Checksum = CRC32()
        val plaintext = when (this) {
            is TypingItem -> "id_typing"
            is ThreadSeparatorItem -> "id_thread_separator"
            is MessageItem -> message.id
            is DateSeparatorItem -> date.toString()
            is ReadStateItem -> "read_" + reads.map { it.user.id }.joinToString { "," }
            is LoadingMoreIndicatorItem -> "id_loading_more_indicator"
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
    ) : MessageListItem(), Serializable {
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
        val date: Date,
        val messageCount: Int,
    ) : MessageListItem()

    public object LoadingMoreIndicatorItem : MessageListItem()

    public enum class Position {
        TOP,
        MIDDLE,
        BOTTOM,
    }
}
