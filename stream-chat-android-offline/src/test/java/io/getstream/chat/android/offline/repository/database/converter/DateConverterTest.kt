package io.getstream.chat.android.offline.repository.database.converter

import org.amshove.kluent.shouldBeEqualTo
import org.amshove.kluent.shouldBeNull
import org.junit.Test
import java.util.Date

internal class DateConverterTest {
    @Test
    fun testNullEncoding() {
        val converter = DateConverter()
        val output = converter.dateToTimestamp(null)
        val converted = converter.fromTimestamp(output)
        converted.shouldBeNull()
    }

    @Test
    fun testSortEncoding() {
        val converter = DateConverter()
        val date = Date()
        val output = converter.dateToTimestamp(date)
        val converted = converter.fromTimestamp(output)
        converted!! shouldBeEqualTo date
    }
}
