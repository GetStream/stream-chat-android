package io.getstream.chat.android.client.offline.repository.database.converter

import com.google.common.truth.Truth
import org.junit.Test

internal class ListConverterTest {
    val converter = ListConverter()

    @Test
    fun stringListNull() {
        val output = converter.stringListToString(null)
        val converted = converter.stringToStringList(output)
        Truth.assertThat(converted).isEqualTo(listOf<String>())
    }

    @Test
    fun stringList() {
        val input = listOf("a", "b")
        val output = converter.stringListToString(input)
        val converted = converter.stringToStringList(output)
        Truth.assertThat(converted!!).isEqualTo(input)
    }
}
