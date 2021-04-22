package io.getstream.chat.android.offline.repository.database.converter

import androidx.test.ext.junit.runners.AndroidJUnit4
import com.google.common.truth.Truth
import io.getstream.chat.android.livedata.BaseTest
import org.junit.Test
import org.junit.runner.RunWith

@RunWith(AndroidJUnit4::class)
internal class ExtraDataConverterTest : BaseTest() {
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
