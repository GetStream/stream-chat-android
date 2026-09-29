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

import io.getstream.chat.android.client.api2.model.dto.DownstreamLocationDto
import io.getstream.chat.android.client.api2.model.response.ChannelResponse
import io.getstream.chat.android.client.api2.model.response.QueryChannelsResponse
import io.getstream.chat.android.client.api2.model.response.QueryGroupedChannelsResponse
import io.getstream.chat.android.client.parser2.testdata.ChannelTestData
import io.getstream.chat.android.client.parser2.testdata.LocationTestData
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Test
import java.util.Date

/**
 * `active_live_locations` is sent next to `channel` in the channel state, not inside it.
 */
internal class ChannelStateActiveLiveLocationsParsingTest {

    private val parser = ParserFactory.createMoshiChatParser()

    private val channel =
        """{"cid":"messaging:123","id":"123","type":"messaging","frozen":false,""" +
            """"config":{${ChannelTestData.MINIMAL_CONFIG}}}"""

    private val channelState =
        """{"channel":$channel,"active_live_locations":[${LocationTestData.jsonAllFields}]}"""

    private val expected = listOf(
        DownstreamLocationDto(
            channel_cid = "messaging:123",
            message_id = "msg-1",
            user_id = "user-1",
            latitude = 37.7749,
            longitude = -122.4194,
            created_by_device_id = "device-1",
            end_at = Date(1744113600000L),
        ),
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
        val json = """{"channel":$channel}"""

        val response = parser.fromJson(json, ChannelResponse::class.java)

        assertEquals(emptyList<DownstreamLocationDto>(), response.active_live_locations)
    }
}
