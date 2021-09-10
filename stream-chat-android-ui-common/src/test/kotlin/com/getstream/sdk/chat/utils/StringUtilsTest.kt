package com.getstream.sdk.chat.utils

import com.getstream.sdk.chat.utils.StorageHelper.Companion.FILE_NAME_PREFIX
import io.getstream.chat.android.test.randomString
import org.amshove.kluent.shouldBeEqualTo
import org.junit.jupiter.api.Test

public class StringUtilsTest {

    @Test
    public fun shouldRemoveAttachmentPrefixFromString() {
        val dateFormat = "HHmmssSSS"
        val fileName = randomString(5)

        val result = StringUtils.removeTimePrefix("${FILE_NAME_PREFIX}${dateFormat}_$fileName", dateFormat)

        result shouldBeEqualTo fileName
    }

    @Test
    public fun shouldNotChangeStringWhenPrefixIsNotPresent() {
        val dateFormat = "HHmmssSSS"
        val randomString = randomString(5)

        val result = StringUtils.removeTimePrefix(randomString, dateFormat)

        result shouldBeEqualTo randomString
    }

    @Test
    public fun shouldBeAbleNamesWithOurPrefixMark() {
        val dateFormat = "HHmmssSSS"
        val fileName = randomString(5)

        val result = StringUtils.removeTimePrefix("${FILE_NAME_PREFIX}${dateFormat}_$fileName", dateFormat)

        result shouldBeEqualTo fileName
    }
}
