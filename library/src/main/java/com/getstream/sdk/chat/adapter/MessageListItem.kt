package com.getstream.sdk.chat.adapter

import com.getstream.sdk.chat.exhaustive
import io.getstream.chat.android.client.models.ChannelUserRead
import io.getstream.chat.android.client.models.Message
import io.getstream.chat.android.client.models.User
import java.util.Date
import java.util.zip.CRC32
import java.util.zip.Checksum

sealed class MessageListItem(
    val isMine: Boolean = false,
    val messageReadBy: MutableList<ChannelUserRead> = mutableListOf()
) {
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

    data class DateSeparatorItem(val date: Date) : MessageListItem()

    data class MessageItem(
        val message: Message,
        val positions: List<MessageViewHolderFactory.Position> = listOf(),
        val isMessageMine: Boolean
    ) : MessageListItem(isMine = isMessageMine)

    data class TypingItem(val users: List<User>) : MessageListItem()

    data class ThreadSeparatorItem(val date: Date = Date()) : MessageListItem()
}
