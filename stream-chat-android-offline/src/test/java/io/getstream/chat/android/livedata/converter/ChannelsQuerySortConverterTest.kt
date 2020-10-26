package io.getstream.chat.android.livedata.converter

import androidx.test.ext.junit.runners.AndroidJUnit4
import com.google.common.truth.Truth
import io.getstream.chat.android.client.api.models.QuerySort
import io.getstream.chat.android.client.models.Channel
import io.getstream.chat.android.livedata.BaseTest
import org.junit.Test
import org.junit.runner.RunWith

@RunWith(AndroidJUnit4::class)
internal class ChannelsQuerySortConverterTest : BaseTest() {
    @Test
    fun testNullEncoding() {
        val converter = ChannelsQuerySortConverter()
        val output = converter.objectToString(null)
        val converted = converter.stringToObject(output)
        Truth.assertThat(converted).isNull()
    }

    @Test
    fun testSortEncoding() {
        val converter = ChannelsQuerySortConverter()
        val sort = QuerySort<Channel>().desc("id", Channel::class.java)
        val output = converter.objectToString(sort)
        val converted = converter.stringToObject(output)
        Truth.assertThat(converted!!).isEqualTo(sort)
    }
}
