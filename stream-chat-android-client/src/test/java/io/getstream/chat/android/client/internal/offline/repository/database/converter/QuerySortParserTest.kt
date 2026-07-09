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

package io.getstream.chat.android.client.internal.offline.repository.database.converter

import io.getstream.chat.android.client.internal.offline.repository.database.converter.internal.QuerySortParser
import io.getstream.chat.android.models.Channel
import io.getstream.chat.android.models.querysort.QuerySortByField
import io.getstream.chat.android.models.querysort.QuerySortByField.Companion.ascByName
import io.getstream.chat.android.models.querysort.QuerySorter
import io.getstream.chat.android.models.querysort.SortDirection
import io.getstream.chat.android.models.querysort.internal.SortAttribute
import io.getstream.chat.android.models.querysort.internal.SortSpecification
import org.amshove.kluent.shouldBeEqualTo
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.assertThrows

internal class QuerySortParserTest {

    private val parser = QuerySortParser<Channel>()

    @Test
    fun `fromRawInfo should create an ascending sort`() {
        val specs = listOf(
            mapOf(
                QuerySorter.KEY_FIELD_NAME to "member_count",
                QuerySorter.KEY_DIRECTION to SortDirection.ASC.value,
            ),
        )

        val result = parser.fromRawInfo(specs)

        result shouldBeEqualTo QuerySortByField.ascByName<Channel>("member_count")
    }

    @Test
    fun `fromRawInfo should create a descending sort`() {
        val specs = listOf(
            mapOf(
                QuerySorter.KEY_FIELD_NAME to "member_count",
                QuerySorter.KEY_DIRECTION to SortDirection.DESC.value,
            ),
        )

        val result = parser.fromRawInfo(specs)

        result shouldBeEqualTo QuerySortByField.descByName<Channel>("member_count")
    }

    @Test
    fun `fromRawInfo should combine multiple sort specs`() {
        val specs = listOf(
            mapOf(
                QuerySorter.KEY_FIELD_NAME to "last_message_at",
                QuerySorter.KEY_DIRECTION to SortDirection.DESC.value,
            ),
            mapOf(
                QuerySorter.KEY_FIELD_NAME to "created_at",
                QuerySorter.KEY_DIRECTION to SortDirection.ASC.value,
            ),
        )

        val result = parser.fromRawInfo(specs)

        result shouldBeEqualTo QuerySortByField.descByName<Channel>("last_message_at").ascByName("created_at")
    }

    @Test
    fun `fromRawInfo should create an empty sort for empty specs`() {
        val result = parser.fromRawInfo(emptyList())

        result shouldBeEqualTo QuerySortByField<Channel>()
    }

    @Test
    fun `fromRawInfo should throw when field name is missing`() {
        val specs = listOf(
            mapOf<String, Any>(QuerySorter.KEY_DIRECTION to SortDirection.ASC.value),
        )

        assertThrows<IllegalStateException> { parser.fromRawInfo(specs) }
    }

    @Test
    fun `fromRawInfo should throw when direction is missing`() {
        val specs = listOf(
            mapOf<String, Any>(QuerySorter.KEY_FIELD_NAME to "member_count"),
        )

        assertThrows<IllegalStateException> { parser.fromRawInfo(specs) }
    }

    @Test
    fun `fromRawInfo should throw for an unsupported direction`() {
        val specs = listOf(
            mapOf(
                QuerySorter.KEY_FIELD_NAME to "member_count",
                QuerySorter.KEY_DIRECTION to 0,
            ),
        )

        assertThrows<IllegalStateException> { parser.fromRawInfo(specs) }
    }

    @Test
    fun `fromSpecifications should create sorts for each specification`() {
        val specs = listOf(
            SortSpecification<Channel>(
                sortAttribute = SortAttribute.FieldNameSortAttribute("last_message_at"),
                sortDirection = SortDirection.DESC,
            ),
            SortSpecification<Channel>(
                sortAttribute = SortAttribute.FieldNameSortAttribute("created_at"),
                sortDirection = SortDirection.ASC,
            ),
        )

        val result = parser.fromSpecifications(specs)

        result shouldBeEqualTo QuerySortByField.descByName<Channel>("last_message_at").ascByName("created_at")
    }

    @Test
    fun `fromSpecifications should create an empty sort for empty specs`() {
        val result = parser.fromSpecifications(emptyList())

        result shouldBeEqualTo QuerySortByField<Channel>()
    }
}
