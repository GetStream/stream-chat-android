package com.getstream.sdk.chat.adapter

import com.getstream.sdk.chat.createChannelUserRead
import com.getstream.sdk.chat.createMessageItem
import org.amshove.kluent.shouldBe
import org.junit.jupiter.api.Test
import java.util.Date

internal class MessageListItemDiffCallbackTest {

    private val channelUserRead = createChannelUserRead()
    private val msg = createMessageItem()
    private val msgWithUserRead = createMessageItem(messageReadBy = mutableListOf(channelUserRead))
    private val msgWithModifiedUserRead = createMessageItem(
        message = msgWithUserRead.message,
        positions = msgWithUserRead.positions,
        isMine = msgWithUserRead.isMine,
        messageReadBy = mutableListOf(
            channelUserRead.copy(
                lastRead = Date.from(
                    channelUserRead.lastRead!!.toInstant().plusSeconds(10)
                )
            )
        )
    )

    @Test
    fun `Should properly check if items are the same when messages have the same id`() {
        val result = MessageListItemDiffCallback.areItemsTheSame(msg, msg)

        result shouldBe true
    }

    @Test
    fun `Should properly check if items are the same when messages have different id`() {
        val result = MessageListItemDiffCallback.areItemsTheSame(msg, msgWithUserRead)

        result shouldBe false
    }

    @Test
    fun `Should properly check if contents are the same when messages are the same`() {
        val result = MessageListItemDiffCallback.areContentsTheSame(msgWithUserRead, msgWithUserRead)

        result shouldBe true
    }

    @Test
    fun `Should properly check if contents are the same when messages are different`() {
        val result = MessageListItemDiffCallback.areContentsTheSame(msg, msgWithUserRead)

        result shouldBe false
    }

    @Test
    fun `Should properly check if contents are the same when messages are the same but with different user read`() {
        val result = MessageListItemDiffCallback.areContentsTheSame(msgWithUserRead, msgWithModifiedUserRead)

        result shouldBe false
    }
}
