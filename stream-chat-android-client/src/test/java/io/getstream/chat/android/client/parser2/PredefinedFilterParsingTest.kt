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
import io.getstream.chat.android.client.api2.mapping.toFilterDomainWithFields
import io.getstream.chat.android.models.Channel
import io.getstream.chat.android.models.Filters
import io.getstream.chat.android.models.NoOpChannelTransformer
import io.getstream.chat.android.models.NoOpMessageTransformer
import io.getstream.chat.android.models.NoOpUserTransformer
import io.getstream.chat.android.models.querysort.QuerySortByField.Companion.ascByName
import io.getstream.chat.android.models.querysort.QuerySortByField.Companion.descByName
import io.getstream.chat.android.network.models.ParsedPredefinedFilterResponse
import org.amshove.kluent.shouldBeEqualTo
import org.junit.jupiter.api.Test

/**
 * Decodes the predefined filter from JSON, so a regression in reading `sort` fails here instead of the
 * mapper silently falling back to the default sort.
 */
internal class PredefinedFilterParsingTest {
    private val parser = ParserFactory.createMoshiChatParser()
    private val mapping = DomainMapping(
        currentUserIdProvider = { null },
        channelTransformer = NoOpChannelTransformer,
        messageTransformer = NoOpMessageTransformer,
        userTransformer = NoOpUserTransformer,
    )

    @Test
    fun `The predefined filter sort is decoded from the server shape`() {
        val json = """
            {
              "name": "android_sample_filter",
              "filter": { "type": "messaging" },
              "sort": [
                { "field": "created_at", "direction": 1 },
                { "field": "last_updated", "direction": -1 }
              ]
            }
        """.trimIndent()

        val parsed = parser.fromJson(json, ParsedPredefinedFilterResponse::class.java)
        val sort = with(mapping) { parsed.sort.toSortDomain() }

        sort shouldBeEqualTo ascByName<Channel>("created_at").descByName("last_updated")
    }

    @Test
    fun `A predefined filter without a sort decodes to no sort`() {
        val json = """{ "name": "android_sample_filter", "filter": { "type": "messaging" } }"""

        val parsed = parser.fromJson(json, ParsedPredefinedFilterResponse::class.java)

        with(mapping) { parsed.sort.toSortDomain() } shouldBeEqualTo null
    }

    @Test
    fun `A null filter condition decodes to a missing field check`() {
        val json = """
            {
              "name": "android_sample_filter",
              "filter": { "type": "messaging", "team": null, "disabled": { "${'$'}ne": null } }
            }
        """.trimIndent()

        val parsed = parser.fromJson(json, ParsedPredefinedFilterResponse::class.java)

        parsed.filter.toFilterDomainWithFields()?.first shouldBeEqualTo Filters.and(
            Filters.eq("type", "messaging"),
            Filters.notExists("team"),
            Filters.exists("disabled"),
        )
    }
}
