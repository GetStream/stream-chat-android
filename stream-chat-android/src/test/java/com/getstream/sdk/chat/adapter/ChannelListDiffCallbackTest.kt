package com.getstream.sdk.chat.adapter

import com.getstream.sdk.chat.createChannel
import com.getstream.sdk.chat.createChannelUserRead
import com.getstream.sdk.chat.createMessage
import com.getstream.sdk.chat.createUser
import com.getstream.sdk.chat.model.ModelType
import io.getstream.chat.android.client.models.Channel
import org.amshove.kluent.`should be equal to`
import org.junit.jupiter.api.Test
import java.util.Date

internal class ChannelListDiffCallbackTest {

    private val currentUser = createUser()

    @Test
    fun `should properly check if items are the same when channels have the same cid`() {
        val channel1 = createChannel()
        val channel2 = createChannel().apply { cid = channel1.cid }

        areItemsTheSame(channel1, channel2) `should be equal to` true
    }

    @Test
    fun `should properly check if items are the same when channels have different cid`() {
        val channel1 = createChannel()
        val channel2 = channel1.copy(cid = "cid")

        areItemsTheSame(channel1, channel2) `should be equal to` false
    }

    @Test
    fun `should properly check if contents are the same when channels are the same`() {
        val channel = createChannel()

        areContentsTheSame(channel, channel) `should be equal to` true
    }

    @Test
    fun `should properly check if contents are the same when channel updated time was set`() {
        val channel1 = createChannel().apply { updatedAt = null }
        val channel2 = channel1.copy(updatedAt = Date())

        areContentsTheSame(channel1, channel2) `should be equal to` false
    }

    @Test
    fun `should properly check if contents are the same when channel updated time changed`() {
        val date1 = Date()
        val channel1 = createChannel().apply { updatedAt = date1 }

        val date2 = Date(date1.time + 2000)
        val channel2 = channel1.copy(updatedAt = date2)

        areContentsTheSame(channel1, channel2) `should be equal to` false
    }

    @Test
    fun `should properly check if contents are the same when extra data changed for a channel`() {
        val channel1 = createChannel().apply {
            extraData = mutableMapOf("key" to "value1")
        }
        val channel2 = channel1.copy(extraData = mutableMapOf("key" to "value2"))

        areContentsTheSame(channel1, channel2) `should be equal to` false
    }

    @Test
    fun `should properly check if contents are the same when a message was added to a channel`() {
        val regularMessage1 = createMessage().apply {
            type = ModelType.message_regular
            deletedAt = null
        }
        val regularMessage2 = createMessage().apply {
            type = ModelType.message_regular
            deletedAt = null
        }

        val channel1 = createChannel().apply { messages = listOf(regularMessage1) }
        val channel2 = channel1.copy(messages = listOf(regularMessage1, regularMessage2))

        areContentsTheSame(channel1, channel2) `should be equal to` false
    }

    @Test
    fun `should properly check if contents are the same when an ephemeral message was added to a channel`() {
        val regularMessage = createMessage().apply {
            type = ModelType.message_regular
            deletedAt = null
        }
        val ephemeralMessage = createMessage().apply {
            type = ModelType.message_ephemeral
            deletedAt = null
        }

        val channel1 = createChannel().apply { messages = listOf(regularMessage) }
        val channel2 = channel1.copy(messages = listOf(regularMessage, ephemeralMessage))

        areContentsTheSame(channel1, channel2) `should be equal to` true
    }

    @Test
    fun `should properly check if contents are the same when chanel user reads are different`() {
        val channelUserRead1 = createChannelUserRead(user = currentUser)
        val channelUserRead2 = channelUserRead1.copy(
            lastRead = Date.from(channelUserRead1.lastRead!!.toInstant().plusSeconds(10))
        )

        val channel1 = createChannel().apply { read = listOf(channelUserRead1) }
        val channel2 = channel1.copy(read = listOf(channelUserRead2))

        areContentsTheSame(channel1, channel2) `should be equal to` false
    }

    @Test
    fun `should provide a valid change payload object when channels are the same`() {
        val channel1 = createChannel()
        val channel2 = channel1.copy()

        val expectedDiff = ChannelItemPayloadDiff(
            unreadCount = false,
            lastMessage = false,
            name = false,
            avatarView = false,
            readState = false,
            lastMessageDate = false
        )
        getChangePayload(channel1, channel2) `should be equal to` expectedDiff
    }

    private fun areItemsTheSame(channel1: Channel, channel2: Channel): Boolean {
        return ChannelListDiffCallback(listOf(channel1), listOf(channel2), currentUser)
            .areItemsTheSame(0, 0)
    }

    private fun areContentsTheSame(channel1: Channel, channel2: Channel): Boolean {
        return ChannelListDiffCallback(listOf(channel1), listOf(channel2), currentUser)
            .areContentsTheSame(0, 0)
    }

    private fun getChangePayload(channel1: Channel, channel2: Channel): Any? {
        return ChannelListDiffCallback(listOf(channel1), listOf(channel2), currentUser)
            .getChangePayload(0, 0)
    }
}
