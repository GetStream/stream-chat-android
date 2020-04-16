package io.getstream.chat.android.livedata.converter

import androidx.test.ext.junit.runners.AndroidJUnit4
import com.google.common.truth.Truth
import io.getstream.chat.android.client.utils.FilterObject
import io.getstream.chat.android.livedata.BaseTest
import org.junit.Test
import org.junit.runner.RunWith
import java.util.*

@RunWith(AndroidJUnit4::class)
class FilterObjectConverterTest: BaseTest() {
    @Test
    fun testNullEncoding() {
        val converter = FilterObjectConverter()
        val output = converter.objectToString(null)
        val converted = converter.stringToObject(output)
        Truth.assertThat(converted).isEqualTo(FilterObject())
    }

    @Test
    fun testEncoding() {
        val converter = FilterObjectConverter()
        val output = converter.objectToString(data.filter1)
        val converted = converter.stringToObject(output)
        Truth.assertThat(converted!!).isEqualTo(data.filter1)
    }

}