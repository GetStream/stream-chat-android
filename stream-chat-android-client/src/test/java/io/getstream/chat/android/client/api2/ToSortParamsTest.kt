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

package io.getstream.chat.android.client.api2

import io.getstream.chat.android.models.querysort.ComparableFieldProvider
import io.getstream.chat.android.models.querysort.QuerySortByField
import io.getstream.chat.android.models.querysort.QuerySorter
import io.getstream.chat.android.models.querysort.internal.SortSpecification
import io.getstream.chat.android.network.models.SortParamRequest
import org.junit.jupiter.api.Assertions
import org.junit.jupiter.params.ParameterizedTest
import org.junit.jupiter.params.provider.Arguments
import org.junit.jupiter.params.provider.MethodSource

internal class ToSortParamsTest {

    @ParameterizedTest
    @MethodSource("arguments")
    fun `maps sorter to sort params`(sorter: QuerySorter<*>, expected: List<SortParamRequest>) {
        Assertions.assertEquals(expected, sorter.toSortParams())
    }

    companion object {

        @JvmStatic
        fun arguments(): List<Arguments> = listOf(
            // Custom sorter emitting a type: field, direction and type are all forwarded.
            Arguments.of(
                FakeSorter(mapOf("field" to "created_at", "direction" to -1, "type" to "number")),
                listOf(SortParamRequest(field = "created_at", direction = -1, type = "number")),
            ),
            // Custom sorter without a type: type stays null.
            Arguments.of(
                FakeSorter(mapOf("field" to "created_at", "direction" to 1)),
                listOf(SortParamRequest(field = "created_at", direction = 1, type = null)),
            ),
            // Built-in sorter: only field and direction, no type.
            Arguments.of(
                QuerySortByField.descByName<Item>("created_at"),
                listOf(SortParamRequest(field = "created_at", direction = -1, type = null)),
            ),
        )
    }

    private class FakeSorter(private val dto: Map<String, Any>) : QuerySorter<Any> {
        override var sortSpecifications: List<SortSpecification<Any>> = emptyList()
        override val comparator: Comparator<in Any> = Comparator { _, _ -> 0 }
        override fun toDto(): List<Map<String, Any>> = listOf(dto)
    }

    private data class Item(val createdAt: Int) : ComparableFieldProvider {
        override fun getComparableField(fieldName: String): Comparable<*>? = when (fieldName) {
            "created_at" -> createdAt
            else -> null
        }
    }
}
