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

import io.getstream.chat.android.client.parser.toMap
import io.getstream.chat.android.models.Filters
import io.getstream.chat.android.network.models.GroupedChannelsGroupRequest
import io.getstream.chat.android.network.models.GroupedQueryChannelsRequest
import io.getstream.chat.android.network.models.SortParamRequest
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Test
import java.util.Date

internal class QueryGroupedChannelsRequestSerializationTest {

    private val parser = ParserFactory.createMoshiChatParser()

    @Test
    fun `Serialize group filters with a date cutoff, a relative cutoff, an empty filter and no filter`() {
        val request = GroupedQueryChannelsRequest(
            limit = 10,
            groups = mapOf(
                "expired" to group(Filters.greaterThanEquals("query_timestamp", Date(1_800_000_000_000)).toMap()),
                "current" to group(Filters.greaterThanEquals("query_timestamp", "now-14d").toMap()),
                "new" to group(Filters.neutral().toMap()),
                "campaign" to group(filterConditions = null),
            ),
            watch = true,
            presence = false,
            sort = listOf(SortParamRequest(field = "query_timestamp", direction = -1)),
        )

        val json = parser.toJson(request)

        assertEquals(
            "{\"limit\":10,\"presence\":false,\"watch\":true," +
                "\"sort\":[{\"direction\":-1,\"field\":\"query_timestamp\"}]," +
                "\"groups\":{" +
                "\"expired\":{\"filter_conditions\":{\"query_timestamp\":{\"\$gte\":\"2027-01-15T08:00:00.000Z\"}}}," +
                "\"current\":{\"filter_conditions\":{\"query_timestamp\":{\"\$gte\":\"now-14d\"}}}," +
                "\"new\":{\"filter_conditions\":{}}," +
                "\"campaign\":{}}}",
            json,
        )
    }

    private fun group(filterConditions: Map<String, Any>?) =
        GroupedChannelsGroupRequest(limit = null, next = null, prev = null, filterConditions = filterConditions)
}
