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

import io.getstream.chat.android.client.api2.mapping.DomainMapping
import io.getstream.chat.android.client.parser2.testdata.LocationTestData
import io.getstream.chat.android.models.NoOpChannelTransformer
import io.getstream.chat.android.models.NoOpMessageTransformer
import io.getstream.chat.android.models.NoOpUserTransformer
import io.getstream.chat.android.network.models.ChannelStateResponse
import io.getstream.chat.android.network.models.GroupedQueryChannelsResponse
import io.getstream.chat.android.network.models.QueryChannelsResponse
import io.getstream.chat.android.network.models.SharedLocationResponseData
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Test

/**
 * `active_live_locations` is sent next to `channel` in the channel state, not inside it.
 */
internal class ChannelStateActiveLiveLocationsParsingTest {

    private val parser = ParserFactory.createMoshiChatParser()

    private val channelState =
        """{"channel":$CHANNEL,"active_live_locations":[${LocationTestData.jsonAllFields}]}"""

    private val domainMapping = DomainMapping(
        currentUserIdProvider = { "" },
        channelTransformer = NoOpChannelTransformer,
        messageTransformer = NoOpMessageTransformer,
        userTransformer = NoOpUserTransformer,
    )

    private val expected = listOf(LocationTestData.expectedAllFields)

    private fun String.withDuration() = replaceFirst("{", """{"duration":"1ms",""")

    private fun List<SharedLocationResponseData>.toDomain() = with(domainMapping) { map { it.toDomain() } }

    @Test
    fun `query channel response`() {
        val response = parser.fromJson(channelState.withDuration(), ChannelStateResponse::class.java)

        assertEquals(expected, response.activeLiveLocations.orEmpty().toDomain())
    }

    @Test
    fun `query channels response`() {
        val json = """{"channels":[$channelState],"duration":"1ms"}"""

        val response = parser.fromJson(json, QueryChannelsResponse::class.java)

        assertEquals(expected, response.channels.single().activeLiveLocations.orEmpty().toDomain())
    }

    @Test
    fun `query grouped channels response`() {
        val json = """{"groups":{"all":{"channels":[$channelState]}},"duration":"1ms"}"""

        val response = parser.fromJson(json, GroupedQueryChannelsResponse::class.java)

        val state = response.groups.getValue("all").channels.single()
        assertEquals(expected, state.activeLiveLocations.orEmpty().toDomain())
    }

    @Test
    fun `missing field parses as null`() {
        val response = parser.fromJson("""{"channel":$CHANNEL}""".withDuration(), ChannelStateResponse::class.java)

        assertEquals(null, response.activeLiveLocations)
    }

    private companion object {
        private const val CHANNEL = """{"cid":"messaging:123","id":"123","type":"messaging","frozen":false,""" +
            """"disabled":false,"created_at":"2025-04-01T10:00:00.000Z","updated_at":"2025-04-01T10:00:00.000Z"}"""
    }
}
