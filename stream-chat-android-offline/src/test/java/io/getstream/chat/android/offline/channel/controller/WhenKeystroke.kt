package io.getstream.chat.android.offline.channel.controller

import com.google.common.truth.Truth
import com.nhaarman.mockitokotlin2.doReturn
import com.nhaarman.mockitokotlin2.whenever
import io.getstream.chat.android.livedata.randomConfig
import org.junit.jupiter.api.Test

internal class WhenKeystroke : BaseChannelControllerTests() {

    @Test
    fun `Given config without typing events Should return false result`() {
        whenever(chatDomainImpl.getChannelConfig(channelType)) doReturn randomConfig(isTypingEvents = false)

        val result = sut.keystroke(null)

        Truth.assertThat(result.isSuccess).isTrue()
        Truth.assertThat(result.data()).isFalse()
    }
}
