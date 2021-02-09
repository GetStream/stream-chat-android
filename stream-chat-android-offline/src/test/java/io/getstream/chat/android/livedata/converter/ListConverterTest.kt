package io.getstream.chat.android.livedata.converter

import androidx.test.ext.junit.runners.AndroidJUnit4
import com.google.common.truth.Truth
import io.getstream.chat.android.livedata.BaseTest
import io.getstream.chat.android.livedata.repository.database.converter.ListConverter
import org.junit.Test
import org.junit.runner.RunWith

@RunWith(AndroidJUnit4::class)
internal class ListConverterTest : BaseTest() {
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
