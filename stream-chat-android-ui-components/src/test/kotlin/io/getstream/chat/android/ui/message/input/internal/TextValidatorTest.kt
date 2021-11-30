package io.getstream.chat.android.ui.message.input.internal

import org.amshove.kluent.`should be equal to`
import org.amshove.kluent.`should be`
import org.junit.jupiter.api.Assertions.*
import org.junit.jupiter.api.Test

internal class TextValidatorTest {

    @Test
    fun `should not be possible to send empty giphys`() {
        val message = "/giphy"
        val result = isMessageTextValid(message)

        result `should be` (false)
    }

    @Test
    fun `should not be possible to send blank giphys`() {
        val message = "/giphy      "
        val result = isMessageTextValid(message)

        result `should be` (false)
    }

    @Test
    fun `should be possible to send giphys with content`() {
        val message = "/giphy hi"
        val result = isMessageTextValid(message)

        result `should be` (true)
    }

    @Test
    fun `should be possible to send text`() {
        val message = "hi"
        val result = isMessageTextValid(message)

        result `should be` (true)
    }
}
