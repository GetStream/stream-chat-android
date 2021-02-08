package io.getstream.chat.android.livedata.converter

import androidx.test.ext.junit.runners.AndroidJUnit4
import com.google.common.truth.Truth
import io.getstream.chat.android.client.utils.FilterObject
import io.getstream.chat.android.livedata.BaseTest
import io.getstream.chat.android.livedata.repository.database.converter.FilterObjectConverter
import org.junit.Test
import org.junit.runner.RunWith

@RunWith(AndroidJUnit4::class)
internal class FilterObjectConverterTest : BaseTest() {
    @Test
    fun testNullEncoding() {
        val converter = FilterObjectConverter()
        val output = converter.objectToString(null)
        val converted = converter.stringToObject(output)
        Truth.assertThat(converted).isEqualTo(FilterObject())
    }

    @Test
    // @Ignore("Filter object decoding/encoding is not entirely ok")
    fun testEncoding() {
        val converter = FilterObjectConverter()
        val output = converter.objectToString(data.filter1)
        val converted = converter.stringToObject(output)
        Truth.assertThat(converted.toMap()).isEqualTo(data.filter1.toMap())
        Truth.assertThat(converted).isEqualTo(data.filter1)
    }
}
