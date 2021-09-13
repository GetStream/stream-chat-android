package io.getstream.chat.android.offline.repository.database.converter

import org.amshove.kluent.shouldBeEqualTo
import org.junit.Test

internal class SetConverterTest {
    @Test
    fun testNullEncoding() {
        val converter = SetConverter()
        val output = converter.sortedSetToString(null)
        val converted = converter.stringToSortedSet(output)
        converted shouldBeEqualTo sortedSetOf()
    }

    @Test
    fun testSortEncoding() {
        val converter = SetConverter()
        val colors = mutableSetOf("green", "blue")
        val output = converter.sortedSetToString(colors)
        val converted = converter.stringToSortedSet(output)
        converted shouldBeEqualTo colors
    }
}
