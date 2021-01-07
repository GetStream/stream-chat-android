package com.getstream.sdk.chat.utils

import io.getstream.chat.android.test.randomString
import org.junit.jupiter.api.Assertions.*
import org.junit.jupiter.api.Test

public class StringUtilsTest {

    @Test
    public fun shouldRemoveAttachmentPrefixFromString() {
        val dateFormat = "HHmmssSSS"

        val result = StringUtils.removeTimePrefix("prefix_${dateFormat}_stm_file_name", dateFormat)

        assertEquals("file_name", result)
    }

    @Test
    public fun shouldNotChangeStringWhenPrefixIsNotPresent() {
        val dateFormat = "HHmmssSSS"
        val randomString = randomString(5)

        val result = StringUtils.removeTimePrefix(randomString, dateFormat)

        assertEquals(randomString, result)
    }

}
