package io.getstream.chat.android.offline.repository.database.converter

import com.google.common.truth.Truth
import org.junit.Test
import java.util.Date

internal class DateConverterTest {
    @Test
    fun testNullEncoding() {
        val converter = DateConverter()
        val output = converter.dateToTimestamp(null)
        val converted = converter.fromTimestamp(output)
        Truth.assertThat(converted).isNull()
    }

    @Test
    fun testSortEncoding() {
        val converter = DateConverter()
        val date = Date()
        val output = converter.dateToTimestamp(date)
        val converted = converter.fromTimestamp(output)
        Truth.assertThat(converted!!).isEqualTo(date)
    }
}
