package io.getstream.chat.android.offline.channel.controller

import com.google.common.truth.Truth
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
import io.getstream.chat.android.livedata.randomConfig
import io.getstream.chat.android.test.TestCall
import kotlinx.coroutines.delay
import kotlinx.coroutines.test.runBlockingTest
import org.junit.jupiter.api.Test

internal class WhenKeystroke : BaseChannelControllerTests() {

    @Test
    fun `Given config without typing events And no keystroke before Should return false result`() {
        Fixture().givenTypingEvents(false)

        val result = sut.keystroke(null)

        Truth.assertThat(result.isSuccess).isTrue()
        Truth.assertThat(result.data()).isFalse()
    }

    @Test
    fun `Given config with typing events And not null parentId And no keystroke before Should invoke keystroke with parentId to ChannelClient`() {
        Fixture()
            .givenTypingEvents(true)
            .givenParentId("parentId")
            .givenSuccessfulResponse()

        sut.keystroke("parentId")

        verify(channelClient).keystroke("parentId")
    }

    @Test
    fun `Given config with typing events And null parentId And no keystroke before Should invoke keystroke without parentId to ChannelClient`() {
        Fixture()
            .givenTypingEvents(true)
            .givenSuccessfulResponse()

        sut.keystroke(null)

        verify(channelClient).keystroke()
    }

    @Test
    fun `Given config with typing events And successful response And no keystroke before Should return result with True`() {
        val channelClient = mock<ChannelClient>()
        Fixture()
            .givenTypingEvents(true)
            .givenSuccessfulResponse()

        val result = sut.keystroke(null)

        Truth.assertThat(result.isSuccess).isTrue()
        Truth.assertThat(result.data()).isTrue()
    }

    @Test
    fun `Given config with typing events And failed response And no keystroke before Should return result with error`() {
        val channelClient = mock<ChannelClient>()
        Fixture()
            .givenTypingEvents(true)
            .givenFailedResponse()

        val result = sut.keystroke(null)

        Truth.assertThat(result.isSuccess).isFalse()
        Truth.assertThat(result.error()).isNotNull()
    }

    @Test
    fun `Given first keystroke less than 3 sec ago Should return result with false`() = runBlockingTest {
        Fixture().givenKeystrokeBefore(keystrokeBefore = 2000L)

        val result = sut.keystroke(null)

        Truth.assertThat(result.isSuccess).isTrue()
        Truth.assertThat(result.data()).isFalse()
        verify(chatClient, never()).channel(channelType, channelId)
    }

    private inner class Fixture {

        private var parentId: String? = null

        fun givenTypingEvents(areSupported: Boolean) = apply {
            whenever(chatDomainImpl.getChannelConfig(channelType)) doReturn randomConfig(isTypingEvents = areSupported)
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
