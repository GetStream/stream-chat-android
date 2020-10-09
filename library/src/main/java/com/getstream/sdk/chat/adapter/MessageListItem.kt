package com.getstream.sdk.chat.adapter

import com.getstream.sdk.chat.utils.exhaustive
import io.getstream.chat.android.client.models.ChannelUserRead
import io.getstream.chat.android.client.models.Message
import io.getstream.chat.android.client.models.User
import java.util.Date
import java.util.zip.CRC32
import java.util.zip.Checksum

sealed class MessageListItem {
    abstract val isMine: Boolean
    abstract val messageReadBy: MutableList<ChannelUserRead>

    fun getStableId(): Long {
        val checksum: Checksum = CRC32()
        val plaintext = when (this) {
            is TypingItem -> "id_typing"
            is ThreadSeparatorItem -> "id_thread_separator"
            is MessageItem -> message.id
            is DateSeparatorItem -> date.toString()
        }
        checksum.update(plaintext.toByteArray(), 0, plaintext.toByteArray().size)
        return checksum.value
    }

    fun isTheirs(): Boolean = !isMine

    fun deepCopy(): MessageListItem = when (this) {
        is DateSeparatorItem -> copy()
        is MessageItem -> copy()
        is TypingItem -> copy()
        is ThreadSeparatorItem -> copy()
    }.exhaustive

    data class DateSeparatorItem @JvmOverloads constructor(
        val date: Date,
        override val isMine: Boolean = false,
        override val messageReadBy: MutableList<ChannelUserRead> = mutableListOf()
    ) : MessageListItem()

    data class MessageItem @JvmOverloads constructor(
        val message: Message,
        val positions: List<MessageViewHolderFactory.Position> = listOf(),
        override val isMine: Boolean = false,
        override val messageReadBy: MutableList<ChannelUserRead> = mutableListOf()
    ) : MessageListItem()

    data class TypingItem @JvmOverloads constructor(
        val users: List<User>,
        override val isMine: Boolean = false,
        override val messageReadBy: MutableList<ChannelUserRead> = mutableListOf()
    ) : MessageListItem()

    data class ThreadSeparatorItem @JvmOverloads constructor(
        val date: Date = Date(),
        override val isMine: Boolean = false,
        override val messageReadBy: MutableList<ChannelUserRead> = mutableListOf()
    ) : MessageListItem()
}
