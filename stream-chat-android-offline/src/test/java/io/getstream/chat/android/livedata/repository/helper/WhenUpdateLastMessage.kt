package io.getstream.chat.android.livedata.repository.helper

import com.nhaarman.mockitokotlin2.argThat
import com.nhaarman.mockitokotlin2.doReturn
import com.nhaarman.mockitokotlin2.eq
import io.getstream.chat.android.livedata.extensions.lastMessage
import io.getstream.chat.android.livedata.randomChannel
import io.getstream.chat.android.livedata.randomMessage
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.test.runBlockingTest
import org.amshove.kluent.Verify
import org.amshove.kluent.VerifyNotCalled
import org.amshove.kluent.When
import org.amshove.kluent.`should be equal to`
import org.amshove.kluent.any
import org.amshove.kluent.called
import org.amshove.kluent.calling
import org.amshove.kluent.on
import org.amshove.kluent.that
import org.amshove.kluent.was
import org.junit.jupiter.api.Test
import java.util.Date

@ExperimentalCoroutinesApi
internal class WhenUpdateLastMessage : BaseRepositoryFacadeTest() {

    @Test
    fun `Given no channel in DB Should not do insert`() = runBlockingTest {
        When calling channels.selectChannelWithoutMessages(eq("cid")) doReturn null

        sut.updateLastMessageForChannel("cid", randomMessage())

        VerifyNotCalled on channels that channels.insertChannel(any())
    }

    @Test
    fun `Given channel without messages in DB Should insert channel with updated last message`() = runBlockingTest {
        val channel = randomChannel(messages = emptyList())
        val lastMessage = randomMessage(createdAt = Date())
        When calling channels.selectChannelWithoutMessages(eq("cid")) doReturn channel

        sut.updateLastMessageForChannel("cid", lastMessage)

        channel.lastMessageAt `should be equal to` lastMessage.createdAt
        channel.lastMessage `should be equal to` lastMessage
        Verify on channels that channels.insertChannel(eq(channel)) was called
    }

    @Test
    fun `Given channel without lastMessageAt in DB Should insert channel with updated last message at`() = runBlockingTest {
        val channel = randomChannel(messages = listOf(randomMessage()), lastMessageAt = null)
        val lastMessage = randomMessage(createdAt = Date())
        When calling channels.selectChannelWithoutMessages(eq("cid")) doReturn channel

        sut.updateLastMessageForChannel("cid", lastMessage)

        channel.lastMessageAt `should be equal to` lastMessage.createdAt
        channel.lastMessage `should be equal to` lastMessage
        Verify on channels that channels.insertChannel(argThat { lastMessageAt == lastMessage.createdAt }) was called
    }

    @Test
    fun `Given channel with outdated lastMessage in DB Should insert channel with updated last message`() = runBlockingTest {
        val before = Date(1000)
        val after = Date(2000)
        val outdatedMessage = randomMessage(id = "messageId1", createdAt = before)
        val newLastMessage = randomMessage(id = "messageId2", createdAt = after)
        val channel = randomChannel(messages = listOf(outdatedMessage), lastMessageAt = before)
        When calling channels.selectChannelWithoutMessages(eq("cid")) doReturn channel

        sut.updateLastMessageForChannel("cid", newLastMessage)

        channel.lastMessageAt `should be equal to` newLastMessage.createdAt
        channel.lastMessage `should be equal to` newLastMessage
        Verify on channels that channels.insertChannel(argThat { lastMessageAt == after }) was called
    }

    @Test
    fun `Given channel with actual lastMessage in DB Should not insert any channel`() = runBlockingTest {
        val before = Date(1000)
        val after = Date(2000)
        val outdatedMessage = randomMessage(id = "messageId1", createdAt = before)
        val newLastMessage = randomMessage(id = "messageId2", createdAt = after)
        val channel = randomChannel(messages = listOf(newLastMessage), lastMessageAt = after)
        When calling channels.selectChannelWithoutMessages(eq("cid")) doReturn channel

        sut.updateLastMessageForChannel("cid", outdatedMessage)

        VerifyNotCalled on channels that channels.insertChannel(any())
    }
}
