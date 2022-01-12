package io.getstream.chat.android.ui.utils

import io.getstream.chat.android.test.randomString
import org.amshove.kluent.internal.assertEquals
import org.junit.jupiter.api.Test

internal class MessageEllipsizeTest {

    @Test
    fun longMessagesShouldBeEllipsized() {
        val randomString = randomString(size = 6000)
        val textLimit = 100

        val expected = "${randomString.substring(0..textLimit)}..."
        val result = ellipsizeText(randomString, textLimit)

        assertEquals(expected, result)
    }

    @OptIn(ExperimentalStdlibApi::class)
    @Test
    fun tallMessagesShouldBeEllipsized() {
        val maxLineBreaks = 2
        val tallTextList = buildList {
            repeat(4) {
                add("${randomString(4)}\n")
            }
        }

        val text = buildString {
                tallTextList.forEach(::append)
                appendLine("...")
            }
        val expected = buildString {
                tallTextList.take(maxLineBreaks).forEach(::append)
                appendLine("...")
            }

        val textLimit = 100
        val result = ellipsizeText(text, textLimit, maxLineBreaks)

        assertEquals(expected, result)
    }
}
