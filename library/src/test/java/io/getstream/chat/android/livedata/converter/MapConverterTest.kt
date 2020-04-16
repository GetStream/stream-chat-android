package io.getstream.chat.android.livedata.converter

import com.google.common.truth.Truth
import io.getstream.chat.android.client.models.ChannelUserRead
import io.getstream.chat.android.livedata.BaseTest
import io.getstream.chat.android.livedata.entity.ChannelUserReadEntity
import org.junit.Test

class MapConverterTest: BaseTest() {
    // read maps
    @Test
    fun testNullEncoding() {
        val converter = MapConverter()
        val output = converter.readMapToString(null)
        val converted = converter.stringToReadMap(output)
        Truth.assertThat(converted).isEqualTo(mutableMapOf<String, ChannelUserReadEntity>())
    }

    @Test
    fun testEncoding() {
        val converter = MapConverter()
        val readMap = mutableMapOf(data.user1.id to ChannelUserReadEntity(data.user1.id))
        val output = converter.readMapToString(readMap)
        val converted = converter.stringToReadMap(output)
        Truth.assertThat(converted).isEqualTo(readMap)
    }
    // string,int map
    @Test
    fun intMapNull() {
        val converter = MapConverter()
        val output = converter.mapToString(null)
        val converted = converter.stringToMap(output)
        Truth.assertThat(converted).isEqualTo(mutableMapOf<String, Int>())
    }

    @Test
    fun intMapRegular() {
        val converter = MapConverter()
        val input = mapOf<String,Int>("score" to 1)
        val output = converter.mapToString(input)
        val converted = converter.stringToMap(output)
        Truth.assertThat(converted).isEqualTo(input)
    }

    // string,string map
    @Test
    fun testStringNullEncoding() {
        val converter = MapConverter()
        val output = converter.stringMapToString(null)
        val converted = converter.stringToStringMap(output)
        Truth.assertThat(converted).isEqualTo(mutableMapOf<String, String>())
    }

    @Test
    fun testStringRegularEncoding() {
        val converter = MapConverter()
        val input = mutableMapOf<String,String>("color" to "green")
        val output = converter.stringMapToString(input)
        val converted = converter.stringToStringMap(output)
        Truth.assertThat(converted).isEqualTo(input)
    }

}