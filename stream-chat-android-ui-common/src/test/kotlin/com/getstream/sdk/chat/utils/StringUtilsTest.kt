package com.getstream.sdk.chat.utils

import io.getstream.chat.android.test.randomString
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Test

public class StringUtilsTest {

    @Test
    public fun shouldRemoveAttachmentPrefixFromString() {
        val dateFormat = "HHmmssSSS"
        val fileName = randomString(5)

        val result = StringUtils.removeTimePrefix("STREAM_${dateFormat}_$fileName", dateFormat)

        assertEquals(fileName, result)
    }

    @Test
    public fun shouldNotChangeStringWhenPrefixIsNotPresent() {
        val dateFormat = "HHmmssSSS"
        val randomString = randomString(5)

        val result = StringUtils.removeTimePrefix(randomString, dateFormat)

        assertEquals(randomString, result)
    }

    @Test
    public fun shouldBeAbleNamesWithOurPrefixMark() {
        val dateFormat = "HHmmssSSS"
        val fileName = randomString(5)

        val result = StringUtils.removeTimePrefix("STREAM_${dateFormat}_$fileName", dateFormat)

        assertEquals(fileName, result)
    }
}
