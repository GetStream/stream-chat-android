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

package io.getstream.chat.android.client.api2.mapping

import com.squareup.moshi.Moshi
import com.squareup.moshi.adapter
import io.getstream.chat.android.client.parser.toMap
import io.getstream.chat.android.models.FilterObject
import io.getstream.chat.android.models.Filters
import io.getstream.chat.android.models.NeutralFilterObject
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.params.ParameterizedTest
import org.junit.jupiter.params.provider.Arguments
import org.junit.jupiter.params.provider.MethodSource

internal class FilterObjectRoundTripTest {

    /**
     * In-memory round-trip: FilterObject -> toMap() -> toFilterDomainWithFields()
     *
     * [roundTripArguments]
     */
    @ParameterizedTest
    @MethodSource("roundTripArguments")
    fun `FilterObject survives in-memory round-trip through toMap and toFilterDomainWithFields`(
        original: FilterObject,
    ) {
        val map = original.toMap()
        val restored = map.toFilterDomainWithFields()?.first
        assertEquals(original, restored)
    }

    /**
     * JSON round-trip: FilterObject -> toMap() -> JSON string -> Map -> toFilterDomainWithFields()
     * This verifies that Moshi's Double-for-Int quirk is properly handled by normalizeValue.
     *
     * [roundTripArguments]
     */
    @OptIn(ExperimentalStdlibApi::class)
    @ParameterizedTest
    @MethodSource("roundTripArguments")
    fun `FilterObject survives JSON round-trip through Moshi serialization`(
        original: FilterObject,
    ) {
        val moshi = Moshi.Builder().build()
        val adapter = moshi.adapter<Map<String, Any>>()

        val map = original.toMap()
        val json = adapter.toJson(map)
        val deserializedMap = adapter.fromJson(json)
        val restored = deserializedMap.toFilterDomainWithFields()?.first
        assertEquals(original, restored)
    }

    companion object {

        @JvmStatic
        @Suppress("LongMethod")
        fun roundTripArguments() = listOf(
            Arguments.of(NeutralFilterObject),
            Arguments.of(Filters.eq("type", "messaging")),
            Arguments.of(Filters.eq("count", 42)),
            Arguments.of(Filters.eq("frozen", false)),
            Arguments.of(@Suppress("DEPRECATION") Filters.ne("type", "commerce")),
            Arguments.of(Filters.greaterThan("age", 18)),
            Arguments.of(Filters.greaterThanEquals("age", 18)),
            Arguments.of(Filters.lessThan("age", 65)),
            Arguments.of(Filters.lessThanEquals("age", 65)),
            Arguments.of(Filters.`in`("status", listOf("active", "pending"))),
            Arguments.of(Filters.`in`("ids", listOf(1, 2, 3))),
            Arguments.of(@Suppress("DEPRECATION") Filters.nin("type", listOf("commerce"))),
            Arguments.of(Filters.contains("tags", "vip")),
            Arguments.of(Filters.exists("avatar")),
            Arguments.of(Filters.notExists("deleted_at")),
            Arguments.of(Filters.autocomplete("name", "joh")),
            Arguments.of(Filters.distinct(listOf("u1", "u2"))),
            Arguments.of(
                Filters.and(
                    Filters.eq("type", "messaging"),
                    Filters.`in`("members", listOf("u1")),
                ),
            ),
            Arguments.of(
                Filters.or(
                    Filters.eq("type", "messaging"),
                    Filters.eq("type", "livestream"),
                ),
            ),
            Arguments.of(
                Filters.nor(
                    Filters.eq("type", "commerce"),
                ),
            ),
            // deeply nested
            Arguments.of(
                Filters.and(
                    Filters.or(
                        Filters.eq("type", "messaging"),
                        Filters.eq("type", "livestream"),
                    ),
                    Filters.`in`("members", listOf("u1")),
                ),
            ),
        )
    }
}
