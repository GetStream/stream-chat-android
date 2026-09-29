/*
 * Copyright (c) 2014-2026 Stream.io Inc. All rights reserved.
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

package io.getstream.chat.android.client.parser2

import io.getstream.chat.android.client.api2.model.response.ChannelResponse
import io.getstream.chat.android.client.api2.model.response.QueryChannelsResponse
import io.getstream.chat.android.client.api2.model.response.QueryGroupedChannelsResponse
import io.getstream.chat.android.client.parser2.testdata.LocationTestData
import io.getstream.chat.android.network.models.SharedLocationResponseData
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Test

/**
 * `active_live_locations` is sent next to `channel` in the channel state, not inside it.
 */
internal class ChannelStateActiveLiveLocationsParsingTest {

    private val parser = ParserFactory.createMoshiChatParser()

    private val channelState =
        """{"channel":{"cid":"messaging:123","id":"123","type":"messaging","frozen":false},""" +
            """"active_live_locations":[${LocationTestData.jsonAllFields}]}"""

    private val expected = listOf(
        parser.fromJson(LocationTestData.jsonAllFields, SharedLocationResponseData::class.java),
    )

    @Test
    fun `query channel response`() {
        val response = parser.fromJson(channelState, ChannelResponse::class.java)

        assertEquals(expected, response.active_live_locations)
    }

    @Test
    fun `query channels response`() {
        val response = parser.fromJson("""{"channels":[$channelState]}""", QueryChannelsResponse::class.java)

        assertEquals(expected, response.channels.single().active_live_locations)
    }

    @Test
    fun `query grouped channels response`() {
        val json = """{"groups":{"all":{"channels":[$channelState]}},"duration":"1ms"}"""

        val response = parser.fromJson(json, QueryGroupedChannelsResponse::class.java)

        assertEquals(expected, response.groups.getValue("all").channels.single().active_live_locations)
    }

    @Test
    fun `missing field parses as empty`() {
        val json = """{"channel":{"cid":"messaging:123","id":"123","type":"messaging","frozen":false}}"""

        val response = parser.fromJson(json, ChannelResponse::class.java)

        assertEquals(emptyList<SharedLocationResponseData>(), response.active_live_locations)
    }
}
