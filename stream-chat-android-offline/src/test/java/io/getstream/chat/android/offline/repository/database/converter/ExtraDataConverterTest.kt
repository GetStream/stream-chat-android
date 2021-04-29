package io.getstream.chat.android.offline.repository.database.converter

import com.google.common.truth.Truth
import io.getstream.chat.android.offline.utils.TestDataHelper
import org.junit.Test

internal class ExtraDataConverterTest {

    val data = TestDataHelper()

    @Test
    fun testNullEncoding() {
        val converter = ExtraDataConverter()
        val output = converter.mapToString(null)
        val converted = converter.stringToMap(output)
        Truth.assertThat(converted).isEqualTo(mutableMapOf<String, Any>())
    }

    @Test
    fun testSortEncoding() {
        val converter = ExtraDataConverter()
        val output = converter.mapToString(data.extraData1)
        val converted = converter.stringToMap(output)
        Truth.assertThat(converted).isEqualTo(data.extraData1)
    }
}
