package com.getstream.sdk.chat.utils.extension

import com.getstream.sdk.chat.createChannel
import com.getstream.sdk.chat.createChannelUserRead
import com.getstream.sdk.chat.createMember
import com.getstream.sdk.chat.createMessage
import com.getstream.sdk.chat.createUser
import com.getstream.sdk.chat.model.ModelType
import com.getstream.sdk.chat.utils.extensions.computeLastMessage
import com.getstream.sdk.chat.utils.extensions.getChannelNameOrMembers
import com.getstream.sdk.chat.utils.extensions.getLastMessageReads
import com.getstream.sdk.chat.utils.extensions.getReadDateOfChannelLastMessage
import com.getstream.sdk.chat.utils.extensions.readLastMessage
import io.getstream.chat.android.client.models.name
import org.amshove.kluent.`should be equal to`
import org.junit.jupiter.api.Test
import java.util.Date

internal class ChannelTest {

    @Test
    fun `should ignore ephemeral messages when computing last message`() {
        val regularMessage = createMessage().apply {
            type = ModelType.message_regular
            deletedAt = null
        }
        val ephemeralMessage = createMessage().apply {
            type = ModelType.message_ephemeral
            deletedAt = null
        }
        val channel = createChannel().apply { messages = listOf(regularMessage, ephemeralMessage) }

        channel.computeLastMessage() `should be equal to` regularMessage
    }

    @Test
    fun `should return the latest regular message when computing last message`() {
        val regularMessage1 = createMessage().apply {
            type = ModelType.message_regular
            deletedAt = null
        }
        val regularMessage2 = createMessage().apply {
            type = ModelType.message_regular
            deletedAt = null
        }
        val channel = createChannel().apply { messages = listOf(regularMessage1, regularMessage2) }

        channel.computeLastMessage() `should be equal to` regularMessage2
    }

    @Test
    fun `should return null when computing last message for a channel without messages`() {
        val channel = createChannel()

        channel.computeLastMessage() `should be equal to` null
    }

    @Test
    fun `should ignore deleted messages when computing last message`() {
        val regularMessage1 = createMessage().apply {
            type = ModelType.message_regular
            deletedAt = null
        }
        val regularMessage2 = createMessage().apply {
            type = ModelType.message_regular
            deletedAt = Date()
        }
        val channel = createChannel().apply { messages = listOf(regularMessage1, regularMessage2) }

        channel.computeLastMessage() `should be equal to` regularMessage1
    }

    @Test
    fun `should return a proper read date of the last channel message for the current user`() {
        val currentUser = createUser()
        val otherUser = createUser()

        val date1 = Date()
        val read1 = createChannelUserRead(user = currentUser, lastReadDate = date1)
        val date2 = Date(date1.time + 1000)
        val read2 = createChannelUserRead(user = otherUser, lastReadDate = date2)

        val channel = createChannel().apply {
            read = listOf(read1, read2)
        }

        channel.getReadDateOfChannelLastMessage(currentUser.id) `should be equal to` date1
    }

    @Test
    fun `should return a proper channel name when channel name is present`() {
        val currentUser = createUser().apply { name = "current user" }
        val channel = createChannel().apply {
            name = "Channel name"
        }

        channel.getChannelNameOrMembers(currentUser) `should be equal to` "Channel name"
    }

    @Test
    fun `should return a proper channel name when channel name is absent`() {
        val currentUser = createUser().apply { name = "current user" }
        val otherUser1 = createUser().apply { name = "other user 1" }
        val otherUser2 = createUser().apply { name = "other user 2" }
        val otherUser3 = createUser().apply { name = "other user 3" }
        val otherUser4 = createUser().apply { name = "other user 4" }

        val channel = createChannel().apply {
            members = listOf(
                createMember(user = currentUser),
                createMember(user = otherUser1),
                createMember(user = otherUser2),
                createMember(user = otherUser3),
                createMember(user = otherUser4)
            )
        }

        channel.getChannelNameOrMembers(currentUser) `should be equal to` "other user 1, other user 2, other user 3..."
    }

    @Test
    fun `should return true when last message was read by the current user`() {
        val currentUser = createUser().apply { name = "current user" }
        val date1 = Date()
        val date2 = Date(date1.time + 1000)

        val channel = createChannel().apply {
            messages = listOf(createMessage(createdAt = date1))
            read = listOf(createChannelUserRead(user = currentUser, lastReadDate = date2))
        }

        channel.readLastMessage(currentUser) `should be equal to` true
    }

    @Test
    fun `should return proper last message reads`() {
        val currentUser = createUser().apply { name = "current user" }
        val otherUser1 = createUser().apply { name = "other user 1" }
        val otherUser2 = createUser().apply { name = "other user 2" }
        val otherUser3 = createUser().apply { name = "other user 3" }
        val otherUser4 = createUser().apply { name = "other user 4" }

        val date1 = Date()
        val date2 = Date(date1.time + 1000)
        val date3 = Date(date1.time + 2000)
        val date4 = Date(date1.time + 3000)
        val date5 = Date(date1.time + 4000)

        val channelUserRead1 = createChannelUserRead(user = currentUser, lastReadDate = date5)
        val channelUserRead2 = createChannelUserRead(user = otherUser1, lastReadDate = date4)
        val channelUserRead3 = createChannelUserRead(user = otherUser2, lastReadDate = date3)
        val channelUserRead4 = createChannelUserRead(user = otherUser3, lastReadDate = date2)
        val channelUserRead5 = createChannelUserRead(user = otherUser4, lastReadDate = date1)

        val channel = createChannel().apply {
            messages = listOf(createMessage(createdAt = date3, deletedAt = null, type = ModelType.message_regular))
            read = listOf(channelUserRead1, channelUserRead2, channelUserRead3, channelUserRead4, channelUserRead5)
        }

        channel.getLastMessageReads(currentUser) `should be equal to` listOf(channelUserRead3, channelUserRead2)
    }
}
