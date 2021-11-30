package io.getstream.chat.android.ui.message.input.internal

import org.junit.jupiter.api.Assertions.*
import org.junit.jupiter.api.Test

internal class TextValidatorTest {

    @Test
    fun `should not be possible to send empty giphys`() {
        val message = "/giphy"
        val result = isMessageTextValid(message)

        assertFalse(result)
    }

    @Test
    fun `should not be possible to send blank giphys`() {
        val message = "/giphy      "
        val result = isMessageTextValid(message)

        assertFalse(result)
    }

    @Test
    fun `should be possible to send giphys with content`() {
        val message = "/giphy hi"
        val result = isMessageTextValid(message)

        assertTrue(result)
    }

    @Test
    fun `should be possible to send text`() {
        val message = "hi"
        val result = isMessageTextValid(message)

        assertTrue(result)
    }
}
