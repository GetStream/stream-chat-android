/*
 * Copyright (c) 2014-2022 Stream.io Inc. All rights reserved.
 *
 * Licensed under the Stream License;
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *    https://github.com/GetStream/stream-chat-android/blob/main/LICENSE
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package io.getstream.chat.android.offline.repository.database.converter

import io.getstream.chat.android.client.test.utils.TestDataHelper
import io.getstream.chat.android.offline.repository.database.converter.internal.MapConverter
import io.getstream.chat.android.offline.repository.domain.channel.member.internal.MemberEntity
import io.getstream.chat.android.offline.repository.domain.channel.userread.internal.ChannelUserReadEntity
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
        val readMap = mutableMapOf(
            data.user1.id to ChannelUserReadEntity(
                userId = data.user1.id,
                lastReceivedEventDate = Date(),
                unreadMessages = 0,
                unreadThreads = null,
                lastRead = Date(),
                lastReadMessageId = null,
            ),
        )
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
