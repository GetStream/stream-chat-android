package io.getstream.chat.android.livedata.controller.helper

import io.getstream.chat.android.livedata.randomMessage
import org.amshove.kluent.shouldBeEqualTo
import org.junit.jupiter.api.Test

internal class MessageHelperTests {

    private val sut = MessageHelper()

    @Test
    fun `Should return list with the same size as new messages`() {
        val newMessages = listOf(randomMessage(), randomMessage())

        val result = sut.updateValidAttachmentsUrl(emptyMap(), newMessages)

        result.size shouldBeEqualTo 2
    }
}