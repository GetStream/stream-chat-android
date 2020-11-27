package com.getstream.sdk.chat.utils

import com.getstream.sdk.chat.R
import org.amshove.kluent.`should be equal to`
import org.amshove.kluent.`should contain same`
import org.amshove.kluent.`should not be`
import org.junit.jupiter.api.Test
import org.junit.jupiter.params.ParameterizedTest
import org.junit.jupiter.params.provider.Arguments
import org.junit.jupiter.params.provider.MethodSource

internal class UiUtilsTest {

    @ParameterizedTest
    @MethodSource("fileSizeArguments")
    fun `verify that file size is properly formatted`(fileSize: Int, expectedResult: String) {
        UiUtils.getFileSizeHumanized(fileSize) `should be equal to` expectedResult
    }

    @ParameterizedTest
    @MethodSource("iconResArguments")
    fun `verify that correct icon is returned for mime type`(mimeType: String?, expectedResult: Int) {
        UiUtils.getIcon(mimeType) `should be equal to` expectedResult
    }

    @Test
    fun `verify that returned reaction type map is valid`() {
        val reactionTypes = UiUtils.getReactionTypes()
        reactionTypes.size `should not be` 0
        reactionTypes.keys `should contain same` listOf("like", "love", "haha", "wow", "sad", "angry")
    }

    @Suppress("unused")
    private fun fileSizeArguments() = listOf(
        Arguments.of(-1, "0"),
        Arguments.of(1024, "1 KB"),
        Arguments.of(1024 + 512, "1.5 KB"),
        Arguments.of(800 * 1024 + 512, "800.5 KB"),
        Arguments.of(1024 * 1024 + 64, "1 MB"),
        Arguments.of(100 * 1024 * 1024, "100 MB"),
        Arguments.of(210 * 1024 * 1024 + 1024 * 512, "210.5 MB"),
        Arguments.of(1024 * 1024 * 1024, "1 GB")
    )

    @Suppress("unused")
    private fun iconResArguments() = listOf(
        Arguments.of(null, R.drawable.stream_ic_file),
        Arguments.of("application/zip", R.drawable.stream_ic_file_zip),
        Arguments.of("video/mp4", R.drawable.stream_ic_file_mov),
        Arguments.of("audio/mp3", R.drawable.stream_ic_file_mp3),
        Arguments.of("unsupported", R.drawable.stream_ic_file)
    )
}
