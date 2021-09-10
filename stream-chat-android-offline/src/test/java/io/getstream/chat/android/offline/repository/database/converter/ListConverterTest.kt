package io.getstream.chat.android.offline.repository.database.converter

import org.amshove.kluent.shouldBeEqualTo
import org.junit.Test

internal class ListConverterTest {
    val converter = ListConverter()

    @Test
    fun stringListNull() {
        val output = converter.stringListToString(null)
        val converted = converter.stringToStringList(output)
        converted shouldBeEqualTo listOf()
    }

    @Test
    fun stringList() {
        val input = listOf("a", "b")
        val output = converter.stringListToString(input)
        val converted = converter.stringToStringList(output)
        converted!! shouldBeEqualTo input
    }
}
