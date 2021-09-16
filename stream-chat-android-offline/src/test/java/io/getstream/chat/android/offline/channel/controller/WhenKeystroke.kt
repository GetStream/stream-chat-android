package io.getstream.chat.android.offline.channel.controller

import com.nhaarman.mockitokotlin2.doReturn
import com.nhaarman.mockitokotlin2.mock
import com.nhaarman.mockitokotlin2.never
import com.nhaarman.mockitokotlin2.reset
import com.nhaarman.mockitokotlin2.verify
import com.nhaarman.mockitokotlin2.whenever
import io.getstream.chat.android.client.channel.ChannelClient
import io.getstream.chat.android.client.errors.ChatError
import io.getstream.chat.android.client.events.ChatEvent
import io.getstream.chat.android.client.utils.Result
import io.getstream.chat.android.offline.randomConfig
import io.getstream.chat.android.test.TestCall
import kotlinx.coroutines.delay
import kotlinx.coroutines.test.runBlockingTest
import org.amshove.kluent.shouldBeFalse
import org.amshove.kluent.shouldBeTrue
import org.amshove.kluent.shouldNotBeNull
import org.junit.jupiter.api.Test

internal class WhenKeystroke : BaseChannelControllerTests() {

    @Test
    fun `Given config without typing events And no keystroke before Should return false result`() = runBlockingTest {
        Fixture().givenTypingEvents(false)

        val result = sut.keystroke(null)

        result.isSuccess.shouldBeTrue()
        result.data().shouldBeFalse()
    }

    @Test
    fun `Given config with typing events And not null parentId And no keystroke before Should invoke keystroke with parentId to ChannelClient`() = runBlockingTest {
        Fixture()
            .givenTypingEvents(true)
            .givenParentId("parentId")
            .givenSuccessfulResponse()

        sut.keystroke("parentId")

        verify(channelClient).keystroke("parentId")
    }

    @Test
    fun `Given config with typing events And null parentId And no keystroke before Should invoke keystroke without parentId to ChannelClient`() = runBlockingTest {
        Fixture()
            .givenTypingEvents(true)
            .givenSuccessfulResponse()

        sut.keystroke(null)

        verify(channelClient).keystroke()
    }

    @Test
    fun `Given config with typing events And successful response And no keystroke before Should return result with True`() = runBlockingTest {
        val channelClient = mock<ChannelClient>()
        Fixture()
            .givenTypingEvents(true)
            .givenSuccessfulResponse()

        val result = sut.keystroke(null)

        result.isSuccess.shouldBeTrue()
        result.data().shouldBeTrue()
    }

    @Test
    fun `Given config with typing events And failed response And no keystroke before Should return result with error`() = runBlockingTest {
        val channelClient = mock<ChannelClient>()
        Fixture()
            .givenTypingEvents(true)
            .givenFailedResponse()

        val result = sut.keystroke(null)

        result.isSuccess.shouldBeFalse()
        result.error().shouldNotBeNull()
    }

    @Test
    fun `Given first keystroke less than 3 sec ago Should return result with false`() = runBlockingTest {
        Fixture().givenKeystrokeBefore(keystrokeBefore = 2000L)

        val result = sut.keystroke(null)

        result.isSuccess.shouldBeTrue()
        result.data().shouldBeFalse()
        verify(chatClient, never()).channel(channelType, channelId)
    }

    private inner class Fixture {

        private var parentId: String? = null

        fun givenTypingEvents(areSupported: Boolean) = apply {
            whenever(chatDomainImpl.getChannelConfig(channelType)) doReturn randomConfig(typingEventsEnabled = areSupported)
        }

        fun givenParentId(parentId: String?) = apply {
            this.parentId = parentId
        }

        fun givenSuccessfulResponse() = apply {
            if (parentId != null) {
                whenever(channelClient.keystroke(parentId!!)) doReturn TestCall(Result(mock<ChatEvent>()))
            } else {
                whenever(channelClient.keystroke()) doReturn TestCall(Result(mock<ChatEvent>()))
            }
        }

        fun givenFailedResponse() = apply {
            if (parentId != null) {
                whenever(channelClient.keystroke(parentId!!)) doReturn TestCall(Result(mock<ChatError>()))
            } else {
                whenever(channelClient.keystroke()) doReturn TestCall(Result(mock<ChatError>()))
            }
        }

        suspend fun givenKeystrokeBefore(keystrokeBefore: Long) = givenTypingEvents(true).apply {
            val channelClient = mock<ChannelClient> {
                on { keystroke() } doReturn TestCall(Result(mock<ChatEvent>()))
            }
            whenever(chatClient.channel(channelType, channelId)) doReturn channelClient
            sut.keystroke(null)
            delay(keystrokeBefore)
            reset(chatClient)
        }
    }
}
