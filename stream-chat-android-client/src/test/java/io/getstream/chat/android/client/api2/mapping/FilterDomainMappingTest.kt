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

import io.getstream.chat.android.models.FilterObject
import io.getstream.chat.android.models.Filters
import io.getstream.chat.android.models.NeutralFilterObject
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.params.ParameterizedTest
import org.junit.jupiter.params.provider.Arguments
import org.junit.jupiter.params.provider.MethodSource

internal class FilterDomainMappingTest {

    /**
     * [toFilterDomainArguments]
     */
    @ParameterizedTest
    @MethodSource("toFilterDomainArguments")
    fun `Map is correctly parsed to FilterObject`(
        input: Map<String, Any>?,
        expected: FilterObject?,
    ) {
        val result = input.toFilterDomain()
        assertEquals(expected, result)
    }

    companion object Companion {

        @JvmStatic
        @Suppress("LongMethod")
        fun toFilterDomainArguments() = listOf(
            // --- null / empty ---
            Arguments.of(null, null),
            Arguments.of(emptyMap<String, Any>(), NeutralFilterObject),

            // --- Equals (direct value, no $eq) ---
            Arguments.of(
                mapOf("type" to "messaging"),
                Filters.eq("type", "messaging"),
            ),
            Arguments.of(
                mapOf("frozen" to true),
                Filters.eq("frozen", true),
            ),
            Arguments.of(
                mapOf("member_count" to 5),
                Filters.eq("member_count", 5),
            ),

            // --- Equals (explicit $eq) ---
            Arguments.of(
                mapOf("type" to mapOf("\$eq" to "messaging")),
                Filters.eq("type", "messaging"),
            ),
            Arguments.of(
                mapOf("frozen" to mapOf("\$eq" to false)),
                Filters.eq("frozen", false),
            ),

            // --- Number normalization: whole Double → Int ---
            Arguments.of(
                mapOf("member_count" to 42.0),
                Filters.eq("member_count", 42),
            ),
            Arguments.of(
                mapOf("member_count" to mapOf("\$eq" to 42.0)),
                Filters.eq("member_count", 42),
            ),

            // --- NotEquals ($ne) ---
            Arguments.of(
                mapOf("type" to mapOf("\$ne" to "livestream")),
                @Suppress("DEPRECATION") Filters.ne("type", "livestream"),
            ),

            // --- GreaterThan ($gt) ---
            Arguments.of(
                mapOf("member_count" to mapOf("\$gt" to 5)),
                Filters.greaterThan("member_count", 5),
            ),
            Arguments.of(
                mapOf("member_count" to mapOf("\$gt" to 5.0)),
                Filters.greaterThan("member_count", 5),
            ),

            // --- GreaterThanOrEquals ($gte) ---
            Arguments.of(
                mapOf("member_count" to mapOf("\$gte" to 10)),
                Filters.greaterThanEquals("member_count", 10),
            ),

            // --- LessThan ($lt) ---
            Arguments.of(
                mapOf("member_count" to mapOf("\$lt" to 100)),
                Filters.lessThan("member_count", 100),
            ),

            // --- LessThanOrEquals ($lte) ---
            Arguments.of(
                mapOf("member_count" to mapOf("\$lte" to 50)),
                Filters.lessThanEquals("member_count", 50),
            ),

            // --- In ($in) ---
            Arguments.of(
                mapOf("type" to mapOf("\$in" to listOf("messaging", "livestream"))),
                Filters.`in`("type", listOf("messaging", "livestream")),
            ),
            Arguments.of(
                mapOf("status" to mapOf("\$in" to listOf(1.0, 2.0, 3.0))),
                Filters.`in`("status", listOf(1, 2, 3)),
            ),

            // --- NotIn ($nin) ---
            Arguments.of(
                mapOf("type" to mapOf("\$nin" to listOf("commerce"))),
                @Suppress("DEPRECATION") Filters.nin("type", listOf("commerce")),
            ),

            // --- Contains ($contains) ---
            Arguments.of(
                mapOf("tags" to mapOf("\$contains" to "vip")),
                Filters.contains("tags", "vip"),
            ),

            // --- Exists ($exists) ---
            Arguments.of(
                mapOf("avatar" to mapOf("\$exists" to true)),
                Filters.exists("avatar"),
            ),
            Arguments.of(
                mapOf("deleted_at" to mapOf("\$exists" to false)),
                Filters.notExists("deleted_at"),
            ),

            // --- Autocomplete ($autocomplete) ---
            Arguments.of(
                mapOf("name" to mapOf("\$autocomplete" to "joh")),
                Filters.autocomplete("name", "joh"),
            ),

            // --- Distinct ---
            Arguments.of(
                mapOf("distinct" to true, "members" to listOf("u1", "u2")),
                Filters.distinct(listOf("u1", "u2")),
            ),

            // --- Logical: $and ---
            Arguments.of(
                mapOf(
                    "\$and" to listOf(
                        mapOf("type" to "messaging"),
                        mapOf("member_count" to mapOf("\$gt" to 2)),
                    ),
                ),
                Filters.and(
                    Filters.eq("type", "messaging"),
                    Filters.greaterThan("member_count", 2),
                ),
            ),

            // --- Logical: $or ---
            Arguments.of(
                mapOf(
                    "\$or" to listOf(
                        mapOf("type" to "messaging"),
                        mapOf("type" to "livestream"),
                    ),
                ),
                Filters.or(
                    Filters.eq("type", "messaging"),
                    Filters.eq("type", "livestream"),
                ),
            ),

            // --- Logical: $nor ---
            Arguments.of(
                mapOf(
                    "\$nor" to listOf(
                        mapOf("type" to "commerce"),
                    ),
                ),
                Filters.nor(
                    Filters.eq("type", "commerce"),
                ),
            ),

            // --- Multi-field implicit AND ---
            Arguments.of(
                mapOf("type" to "messaging", "frozen" to false),
                Filters.and(
                    Filters.eq("type", "messaging"),
                    Filters.eq("frozen", false),
                ),
            ),

            // --- Nested complex: $and containing $or ---
            Arguments.of(
                mapOf(
                    "\$and" to listOf(
                        mapOf(
                            "\$or" to listOf(
                                mapOf("type" to "messaging"),
                                mapOf("type" to "livestream"),
                            ),
                        ),
                        mapOf("members" to mapOf("\$in" to listOf("u1"))),
                    ),
                ),
                Filters.and(
                    Filters.or(
                        Filters.eq("type", "messaging"),
                        Filters.eq("type", "livestream"),
                    ),
                    Filters.`in`("members", listOf("u1")),
                ),
            ),

            // --- Date as string (preserved as-is) ---
            Arguments.of(
                mapOf("created_at" to mapOf("\$gt" to "2024-01-15T10:30:00.123456789Z")),
                Filters.greaterThan("created_at", "2024-01-15T10:30:00.123456789Z"),
            ),
        )
    }
}
