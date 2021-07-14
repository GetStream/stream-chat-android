package io.getstream.chat.android.client.offline.repository.database.converter

import com.google.common.truth.Truth
import org.junit.Test

internal class ExtraDataConverterTest {

    private val extraData1: MutableMap<String, Any> = mutableMapOf("color" to "green", "score" to 1.1)

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
        val output = converter.mapToString(extraData1)
        val converted = converter.stringToMap(output)
        Truth.assertThat(converted).isEqualTo(extraData1)
    }
}
