package io.getstream.chat.android.offline.repository.database.converter

import io.getstream.chat.android.offline.utils.TestDataHelper
import org.amshove.kluent.shouldBeEqualTo
import org.junit.Test

internal class ExtraDataConverterTest {

    val data = TestDataHelper()

    @Test
    fun testNullEncoding() {
        val converter = ExtraDataConverter()
        val output = converter.mapToString(null)
        val converted = converter.stringToMap(output)
        converted shouldBeEqualTo mutableMapOf()
    }

    @Test
    fun testSortEncoding() {
        val converter = ExtraDataConverter()
        val output = converter.mapToString(data.extraData1)
        val converted = converter.stringToMap(output)
        converted shouldBeEqualTo data.extraData1
    }
}
