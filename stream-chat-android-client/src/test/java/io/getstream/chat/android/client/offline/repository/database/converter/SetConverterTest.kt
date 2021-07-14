package io.getstream.chat.android.client.offline.repository.database.converter

import com.google.common.truth.Truth
import org.junit.Test

internal class SetConverterTest {
    @Test
    fun testNullEncoding() {
        val converter = SetConverter()
        val output = converter.sortedSetToString(null)
        val converted = converter.stringToSortedSet(output)
        Truth.assertThat(converted).isEqualTo(sortedSetOf<String>())
    }

    @Test
    fun testSortEncoding() {
        val converter = SetConverter()
        val colors = mutableSetOf("green", "blue")
        val output = converter.sortedSetToString(colors)
        val converted = converter.stringToSortedSet(output)
        Truth.assertThat(converted).isEqualTo(colors)
    }
}
