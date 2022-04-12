package io.getstream.chat.android.offline.repository.facade

import io.getstream.chat.android.offline.extensions.internal.lastMessage
import io.getstream.chat.android.offline.randomChannel
import io.getstream.chat.android.offline.randomMessage
import io.getstream.chat.android.test.TestCoroutineRule
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.test.runTest
import org.amshove.kluent.`should be equal to`
import org.junit.Rule
import org.junit.jupiter.api.Test
import org.mockito.kotlin.any
import org.mockito.kotlin.argThat
import org.mockito.kotlin.doReturn
import org.mockito.kotlin.eq
import org.mockito.kotlin.never
import org.mockito.kotlin.verify
import org.mockito.kotlin.whenever
import java.util.Date

@ExperimentalCoroutinesApi
internal class WhenUpdateLastMessage : BaseRepositoryFacadeTest() {
    @get:Rule
    val testCoroutines: TestCoroutineRule = TestCoroutineRule()

    @Test
    fun `Given no channel in DB Should not do insert`() = runTest {
        whenever(channels.selectChannelWithoutMessages(eq("cid"))) doReturn null

        sut.updateLastMessageForChannel("cid", randomMessage())

        verify(channels, never()).insertChannel(any())
    }

    @Test
    fun `Given channel without messages in DB Should insert channel with updated last message`() = runTest {
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
        runTest {
            val channel = randomChannel(messages = listOf(randomMessage()), lastMessageAt = null)
            val lastMessage = randomMessage(createdAt = Date())
            whenever(channels.selectChannelWithoutMessages(eq("cid"))) doReturn channel

            sut.updateLastMessageForChannel("cid", lastMessage)

            channel.lastMessageAt `should be equal to` lastMessage.createdAt
            channel.lastMessage `should be equal to` lastMessage
            verify(channels).insertChannel(argThat { lastMessageAt == lastMessage.createdAt })
        }

    @Test
    fun `Given channel with outdated lastMessage in DB Should insert channel with updated last message`() = runTest {
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
    fun `Given channel with actual lastMessage in DB Should not insert any channel`() = runTest {
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
