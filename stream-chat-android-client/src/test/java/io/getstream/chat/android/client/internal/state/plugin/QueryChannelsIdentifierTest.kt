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

package io.getstream.chat.android.client.internal.state.plugin

import io.getstream.chat.android.client.api.models.QueryChannelsRequest
import io.getstream.chat.android.client.query.QueryChannelsSpec
import io.getstream.chat.android.models.Channel
import io.getstream.chat.android.models.Filters
import io.getstream.chat.android.models.querysort.QuerySortByField
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertNotEquals
import org.junit.jupiter.api.Test

internal class QueryChannelsIdentifierTest {

    private val standardFilter = Filters.eq("type", "messaging")
    private val standardSort = QuerySortByField.descByName<Channel>("last_message_at")

    @Test
    fun `request identifier returns Standard when predefinedFilter is null`() {
        val request = QueryChannelsRequest(filter = standardFilter, querySort = standardSort, limit = 30)

        val identifier = request.identifier

        assertEquals(QueryChannelsIdentifier.Standard(standardFilter, standardSort), identifier)
    }

    @Test
    fun `request identifier returns Predefined when predefinedFilter is set`() {
        val filterValues = mapOf("a" to 1)
        val sortValues = mapOf("b" to 2)
        val request = QueryChannelsRequest(
            limit = 30,
            predefinedFilter = "my-filter",
            filterValues = filterValues,
            sortValues = sortValues,
        )

        val identifier = request.identifier

        assertEquals(
            QueryChannelsIdentifier.Predefined("my-filter", filterValues, sortValues),
            identifier,
        )
    }

    @Test
    fun `request identifier ignores filter and querySort when predefinedFilter is set`() {
        val request = QueryChannelsRequest(
            // Even if a caller passes filter/querySort, they don't define identity for predefined
            filter = standardFilter,
            querySort = standardSort,
            limit = 30,
            predefinedFilter = "my-filter",
            filterValues = mapOf("a" to 1),
            sortValues = mapOf("b" to 2),
        )

        val identifier = request.identifier

        assertEquals(
            QueryChannelsIdentifier.Predefined("my-filter", mapOf("a" to 1), mapOf("b" to 2)),
            identifier,
        )
    }

    @Test
    fun `Predefined identifiers with same name but different filterValues are not equal`() {
        val a = QueryChannelsIdentifier.Predefined("p", mapOf("a" to 1), null)
        val b = QueryChannelsIdentifier.Predefined("p", mapOf("a" to 2), null)

        assertNotEquals(a, b)
    }

    @Test
    fun `Predefined identifiers with same name but different sortValues are not equal`() {
        val a = QueryChannelsIdentifier.Predefined("p", null, mapOf("b" to 1))
        val b = QueryChannelsIdentifier.Predefined("p", null, mapOf("b" to 2))

        assertNotEquals(a, b)
    }

    @Test
    fun `QueryChannelsSpec identifier returns Standard when predefinedFilterName is null`() {
        val spec = QueryChannelsSpec(filter = standardFilter, querySort = standardSort)

        assertEquals(QueryChannelsIdentifier.Standard(standardFilter, standardSort), spec.identifier)
    }

    @Test
    fun `QueryChannelsSpec identifier returns Predefined when predefinedFilterName is set`() {
        val spec = QueryChannelsSpec.create(
            filter = standardFilter,
            querySort = standardSort,
            predefinedFilterName = "p",
            predefinedFilterValues = mapOf("a" to 1),
            predefinedSortValues = mapOf("b" to 2),
        )

        assertEquals(
            QueryChannelsIdentifier.Predefined("p", mapOf("a" to 1), mapOf("b" to 2)),
            spec.identifier,
        )
    }
}
