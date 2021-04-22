package io.getstream.chat.android.offline.repository.facade

import com.nhaarman.mockitokotlin2.any
import com.nhaarman.mockitokotlin2.argThat
import com.nhaarman.mockitokotlin2.doReturn
import com.nhaarman.mockitokotlin2.eq
import com.nhaarman.mockitokotlin2.never
import com.nhaarman.mockitokotlin2.verify
import com.nhaarman.mockitokotlin2.whenever
import io.getstream.chat.android.livedata.randomChannel
import io.getstream.chat.android.livedata.randomMessage
import io.getstream.chat.android.offline.extensions.lastMessage
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.test.runBlockingTest
import org.amshove.kluent.`should be equal to`
import org.junit.jupiter.api.Test
import java.util.Date

@ExperimentalCoroutinesApi
internal class WhenUpdateLastMessage : BaseRepositoryFacadeTest() {

    @Test
    fun `Given no channel in DB Should not do insert`() = runBlockingTest {
        whenever(channels.selectChannelWithoutMessages(eq("cid"))) doReturn null

        sut.updateLastMessageForChannel("cid", randomMessage())

        verify(channels, never()).insertChannel(any())
    }

    @Test
    fun `Given channel without messages in DB Should insert channel with updated last message`() = runBlockingTest {
        val channel = randomChannel(messages = emptyList())
        val lastMessage = randomMessage(createdAt = Date())
        whenever(channels.selectChannelWithoutMessages(eq("cid"))) doReturn channel

        sut.updateLastMessageForChannel("cid", lastMessage)

        channel.lastMessageAt `should be equal to` lastMessage.createdAt
        channel.lastMessage `should be equal to` lastMessage
        verify(channels).insertChannel(eq(channel))
    }

    @Test
    fun `Given channel without lastMessageAt in DB Should insert channel with updated last message at`() =
        runBlockingTest {
            val channel = randomChannel(messages = listOf(randomMessage()), lastMessageAt = null)
            val lastMessage = randomMessage(createdAt = Date())
            whenever(channels.selectChannelWithoutMessages(eq("cid"))) doReturn channel

            sut.updateLastMessageForChannel("cid", lastMessage)

            channel.lastMessageAt `should be equal to` lastMessage.createdAt
            channel.lastMessage `should be equal to` lastMessage
            verify(channels).insertChannel(argThat { lastMessageAt == lastMessage.createdAt })
        }

    @Test
    fun `Given channel with outdated lastMessage in DB Should insert channel with updated last message`() = runBlockingTest {
        val before = Date(1000)
        val after = Date(2000)
        val outdatedMessage = randomMessage(id = "messageId1", createdAt = before)
        val newLastMessage = randomMessage(id = "messageId2", createdAt = after)
        val channel = randomChannel(messages = listOf(outdatedMessage), lastMessageAt = before)
        whenever(channels.selectChannelWithoutMessages(eq("cid"))) doReturn channel

        sut.updateLastMessageForChannel("cid", newLastMessage)

        channel.lastMessageAt `should be equal to` newLastMessage.createdAt
        channel.lastMessage `should be equal to` newLastMessage
        verify(channels).insertChannel(argThat { lastMessageAt == after })
    }

    @Test
    fun `Given channel with actual lastMessage in DB Should not insert any channel`() = runBlockingTest {
        val before = Date(1000)
        val after = Date(2000)
        val outdatedMessage = randomMessage(id = "messageId1", createdAt = before)
        val newLastMessage = randomMessage(id = "messageId2", createdAt = after)
        val channel = randomChannel(messages = listOf(newLastMessage), lastMessageAt = after)
        whenever(channels.selectChannelWithoutMessages(eq("cid"))) doReturn channel

        sut.updateLastMessageForChannel("cid", outdatedMessage)

        verify(channels, never()).insertChannel(any())
    }
}
