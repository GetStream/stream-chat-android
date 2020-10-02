package com.getstream.sdk.chat.adapter

import com.getstream.sdk.chat.createChannelUserRead
import com.getstream.sdk.chat.createMessageItem
import org.amshove.kluent.shouldBe
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.TestInstance
import org.junit.jupiter.api.TestInstance.Lifecycle.PER_METHOD
import java.util.Date

@TestInstance(PER_METHOD)
class MessageListItemDiffCallbackTest {

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
        val diff = MessageListItemDiffCallback(listOf(msg), listOf(msg))

        diff.areItemsTheSame(0, 0) shouldBe true
    }

    @Test
    fun `Should properly check if items are the same when messages have different id`() {
        val diff = MessageListItemDiffCallback(listOf(msg), listOf(msgWithUserRead))

        diff.areItemsTheSame(0, 0) shouldBe false
    }

    @Test
    fun `Should properly check if contents are the same when messages are the same`() {
        val diff = MessageListItemDiffCallback(listOf(msgWithUserRead), listOf(msgWithUserRead))

        diff.areContentsTheSame(0, 0) shouldBe true
    }

    @Test
    fun `Should properly check if contents are the same when messages are different`() {
        val diff = MessageListItemDiffCallback(listOf(msg), listOf(msgWithUserRead))

        diff.areContentsTheSame(0, 0) shouldBe false
    }

    @Test
    fun `Should properly check if contents are the same when messages are the same but with different user read`() {
        val diff =
            MessageListItemDiffCallback(listOf(msgWithUserRead), listOf(msgWithModifiedUserRead))

        diff.areContentsTheSame(0, 0) shouldBe false
    }
}
