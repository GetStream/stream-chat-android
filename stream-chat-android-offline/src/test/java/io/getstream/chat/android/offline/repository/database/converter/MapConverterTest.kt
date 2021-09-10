package io.getstream.chat.android.offline.repository.database.converter

import io.getstream.chat.android.offline.repository.domain.channel.member.MemberEntity
import io.getstream.chat.android.offline.repository.domain.channel.userread.ChannelUserReadEntity
import io.getstream.chat.android.offline.utils.TestDataHelper
import org.amshove.kluent.shouldBeEqualTo
import org.junit.Test
import java.util.Date

internal class MapConverterTest {
    private val data = TestDataHelper()
    // read maps
    @Test
    fun testNullEncoding() {
        val converter = MapConverter()
        val output = converter.readMapToString(null)
        val converted = converter.stringToReadMap(output)
        converted shouldBeEqualTo mutableMapOf()
    }

    @Test
    fun testEncoding() {
        val converter = MapConverter()
        val readMap = mutableMapOf(data.user1.id to ChannelUserReadEntity(data.user1.id, Date(), 0))
        val output = converter.readMapToString(readMap)
        val converted = converter.stringToReadMap(output)
        converted shouldBeEqualTo readMap
    }

    // member maps
    @Test
    fun testNullMemberEncoding() {
        val converter = MapConverter()
        val output = converter.memberMapToString(null)
        val converted = converter.stringToMemberMap(output)
        converted shouldBeEqualTo mutableMapOf()
    }

    @Test
    fun testMemberEncoding() {
        val converter = MapConverter()
        val memberMap = mutableMapOf(data.user1.id to MemberEntity(data.user1.id))
        val output = converter.memberMapToString(memberMap)
        val converted = converter.stringToMemberMap(output)
        converted shouldBeEqualTo memberMap
    }

    // string,int map
    @Test
    fun intMapNull() {
        val converter = MapConverter()
        val output = converter.mapToString(null)
        val converted = converter.stringToMap(output)
        converted shouldBeEqualTo mutableMapOf()
    }

    @Test
    fun intMapRegular() {
        val converter = MapConverter()
        val input = mapOf("score" to 1)
        val output = converter.mapToString(input)
        val converted = converter.stringToMap(output)
        converted shouldBeEqualTo input
    }

    // string,string map
    @Test
    fun testStringNullEncoding() {
        val converter = MapConverter()
        val output = converter.stringMapToString(null)
        val converted = converter.stringToStringMap(output)
        converted shouldBeEqualTo mutableMapOf()
    }

    @Test
    fun testStringRegularEncoding() {
        val converter = MapConverter()
        val input = mutableMapOf("color" to "green")
        val output = converter.stringMapToString(input)
        val converted = converter.stringToStringMap(output)
        converted shouldBeEqualTo input
    }
}
