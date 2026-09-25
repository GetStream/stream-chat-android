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

import io.getstream.chat.android.client.api.models.Pagination
import io.getstream.chat.android.client.api.models.QueryChannelRequest
import io.getstream.chat.android.client.api2.mapping.DtoMapping
import io.getstream.chat.android.models.NoOpMessageTransformer
import io.getstream.chat.android.models.NoOpUserTransformer
import io.kotest.assertions.json.shouldEqualJson
import org.junit.jupiter.api.Test
import org.junit.jupiter.params.ParameterizedTest
import org.junit.jupiter.params.provider.Arguments
import org.junit.jupiter.params.provider.MethodSource
import java.util.Date

internal class QueryChannelRequestSerializationTest {

    private val parser = ParserFactory.createMoshiChatParser()

    private val dtoMapping = DtoMapping(NoOpMessageTransformer, NoOpUserTransformer)

    private fun QueryChannelRequest.toJson() = parser.toJson(with(dtoMapping) { toChannelGetOrCreateRequest() })

    @Test
    fun `Serialize an empty request with every nested object present`() {
        QueryChannelRequest().toJson().shouldEqualJson(
            """
            {
              "state": false,
              "watch": false,
              "presence": false,
              "messages": {},
              "watchers": {},
              "members": {},
              "data": {}
            }
            """,
        )
    }

    @Test
    fun `Serialize the pagination of messages, members and watchers`() {
        val query = QueryChannelRequest()
            .withMessages(Pagination.AROUND_ID, "message-id", 30)
            .withMembers(limit = 10, offset = 5)
            .withWatchers(limit = 20, offset = 2)
            .apply {
                watch = true
                presence = true
                messages["created_at_before"] = Date(1_700_000_000_123)
            }

        query.toJson().shouldEqualJson(
            """
            {
              "state": true,
              "watch": true,
              "presence": true,
              "messages": { "limit": 30, "id_around": "message-id", "created_at_before": "2023-11-14T22:13:20.123Z" },
              "watchers": { "limit": 20, "offset": 2 },
              "members": { "limit": 10, "offset": 5 },
              "data": {}
            }
            """,
        )
    }

    @Test
    fun `Serialize the channel data flat, with no custom object`() {
        val query = QueryChannelRequest().withData(
            mapOf(
                "name" to "Team",
                "frozen" to true,
                "color" to mapOf("primary" to "red"),
            ),
        )

        query.toJson().shouldEqualJson(
            """
            {
              "state": false,
              "watch": false,
              "presence": false,
              "messages": {},
              "watchers": {},
              "members": {},
              "data": { "name": "Team", "frozen": true, "color": { "primary": "red" } }
            }
            """,
        )
    }

    @ParameterizedTest
    @MethodSource("paginationKeys")
    fun `Serialize each pagination key`(field: String, key: String, value: Any, json: String) {
        val query = QueryChannelRequest().apply {
            when (field) {
                "messages" -> messages[key] = value
                "members" -> members[key] = value
                else -> watchers[key] = value
            }
        }
        val empty = mapOf("messages" to "{}", "members" to "{}", "watchers" to "{}") + (field to "{\"$key\":$json}")

        query.toJson().shouldEqualJson(
            """
            {
              "state": false,
              "watch": false,
              "presence": false,
              "messages": ${empty["messages"]},
              "watchers": ${empty["watchers"]},
              "members": ${empty["members"]},
              "data": {}
            }
            """,
        )
    }

    @Test
    fun `Do not send pagination keys the backend does not read`() {
        val query = QueryChannelRequest().apply {
            messages["limit"] = 30
            messages["unknown"] = "value"
            members["id_around"] = "member-id"
        }

        query.toJson().shouldEqualJson(
            """
            {
              "state": false,
              "watch": false,
              "presence": false,
              "messages": { "limit": 30 },
              "watchers": {},
              "members": {},
              "data": {}
            }
            """,
        )
    }

    companion object {
        private val date = Date(1_700_000_000_123)
        private const val DATE_JSON = "\"2023-11-14T22:13:20.123Z\""

        @JvmStatic
        fun paginationKeys(): List<Arguments> = listOf(
            Arguments.of("messages", "limit", 30, "30"),
            Arguments.of("messages", "id_gt", "m", "\"m\""),
            Arguments.of("messages", "id_gte", "m", "\"m\""),
            Arguments.of("messages", "id_lt", "m", "\"m\""),
            Arguments.of("messages", "id_lte", "m", "\"m\""),
            Arguments.of("messages", "id_around", "m", "\"m\""),
            Arguments.of("messages", "created_at_after", date, DATE_JSON),
            Arguments.of("messages", "created_at_after_or_equal", date, DATE_JSON),
            Arguments.of("messages", "created_at_before", date, DATE_JSON),
            Arguments.of("messages", "created_at_before_or_equal", date, DATE_JSON),
            Arguments.of("messages", "created_at_around", date, DATE_JSON),
        ) + listOf("members", "watchers").flatMap { field ->
            listOf("limit", "offset", "id_gt", "id_gte", "id_lt", "id_lte").map { key ->
                Arguments.of(field, key, 7, "7")
            }
        }
    }
}
