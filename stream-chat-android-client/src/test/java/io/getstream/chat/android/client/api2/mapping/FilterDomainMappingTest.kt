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
     * [toFilterDomainWithFieldsArguments]
     */
    @ParameterizedTest
    @MethodSource("toFilterDomainWithFieldsArguments")
    fun `Map is parsed to FilterObject with the set of referenced field names`(
        input: Map<String, Any>?,
        expectedFilter: FilterObject?,
        expectedFields: Set<String>?,
    ) {
        val result = input.toFilterDomainWithFields()
        if (expectedFilter == null) {
            assertEquals(null, result)
        } else {
            assertEquals(expectedFilter to expectedFields, result)
        }
    }

    companion object Companion {

        @JvmStatic
        @Suppress("LongMethod")
        fun toFilterDomainWithFieldsArguments() = listOf(
            // --- null / empty ---
            Arguments.of(null, null, null),
            Arguments.of(emptyMap<String, Any>(), NeutralFilterObject, emptySet<String>()),

            // --- Equals (direct value, no $eq) ---
            Arguments.of(
                mapOf("type" to "messaging"),
                Filters.eq("type", "messaging"),
                setOf("type"),
            ),
            Arguments.of(
                mapOf("frozen" to true),
                Filters.eq("frozen", true),
                setOf("frozen"),
            ),
            Arguments.of(
                mapOf("member_count" to 5),
                Filters.eq("member_count", 5),
                setOf("member_count"),
            ),

            // --- Equals (explicit $eq) ---
            Arguments.of(
                mapOf("type" to mapOf("\$eq" to "messaging")),
                Filters.eq("type", "messaging"),
                setOf("type"),
            ),
            Arguments.of(
                mapOf("frozen" to mapOf("\$eq" to false)),
                Filters.eq("frozen", false),
                setOf("frozen"),
            ),

            // --- Number normalization: whole Double → Int ---
            Arguments.of(
                mapOf("member_count" to 42.0),
                Filters.eq("member_count", 42),
                setOf("member_count"),
            ),
            Arguments.of(
                mapOf("member_count" to mapOf("\$eq" to 42.0)),
                Filters.eq("member_count", 42),
                setOf("member_count"),
            ),

            // --- NotEquals ($ne) ---
            Arguments.of(
                mapOf("type" to mapOf("\$ne" to "livestream")),
                @Suppress("DEPRECATION") Filters.ne("type", "livestream"),
                setOf("type"),
            ),

            // --- GreaterThan ($gt) ---
            Arguments.of(
                mapOf("member_count" to mapOf("\$gt" to 5)),
                Filters.greaterThan("member_count", 5),
                setOf("member_count"),
            ),
            Arguments.of(
                mapOf("member_count" to mapOf("\$gt" to 5.0)),
                Filters.greaterThan("member_count", 5),
                setOf("member_count"),
            ),

            // --- GreaterThanOrEquals ($gte) ---
            Arguments.of(
                mapOf("member_count" to mapOf("\$gte" to 10)),
                Filters.greaterThanEquals("member_count", 10),
                setOf("member_count"),
            ),

            // --- LessThan ($lt) ---
            Arguments.of(
                mapOf("member_count" to mapOf("\$lt" to 100)),
                Filters.lessThan("member_count", 100),
                setOf("member_count"),
            ),

            // --- LessThanOrEquals ($lte) ---
            Arguments.of(
                mapOf("member_count" to mapOf("\$lte" to 50)),
                Filters.lessThanEquals("member_count", 50),
                setOf("member_count"),
            ),

            // --- In ($in) ---
            Arguments.of(
                mapOf("type" to mapOf("\$in" to listOf("messaging", "livestream"))),
                Filters.`in`("type", listOf("messaging", "livestream")),
                setOf("type"),
            ),
            Arguments.of(
                mapOf("status" to mapOf("\$in" to listOf(1.0, 2.0, 3.0))),
                Filters.`in`("status", listOf(1, 2, 3)),
                setOf("status"),
            ),

            // --- NotIn ($nin) ---
            Arguments.of(
                mapOf("type" to mapOf("\$nin" to listOf("commerce"))),
                @Suppress("DEPRECATION") Filters.nin("type", listOf("commerce")),
                setOf("type"),
            ),

            // --- Contains ($contains) ---
            Arguments.of(
                mapOf("tags" to mapOf("\$contains" to "vip")),
                Filters.contains("tags", "vip"),
                setOf("tags"),
            ),

            // --- Exists ($exists) ---
            Arguments.of(
                mapOf("avatar" to mapOf("\$exists" to true)),
                Filters.exists("avatar"),
                setOf("avatar"),
            ),
            Arguments.of(
                mapOf("deleted_at" to mapOf("\$exists" to false)),
                Filters.notExists("deleted_at"),
                setOf("deleted_at"),
            ),

            // --- Autocomplete ($autocomplete) ---
            Arguments.of(
                mapOf("name" to mapOf("\$autocomplete" to "joh")),
                Filters.autocomplete("name", "joh"),
                setOf("name"),
            ),

            // --- Distinct branch does NOT contribute field names ---
            Arguments.of(
                mapOf("distinct" to true, "members" to listOf("u1", "u2")),
                Filters.distinct(listOf("u1", "u2")),
                emptySet<String>(),
            ),

            // --- Logical: $and (tracks leaf fields only) ---
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
                setOf("type", "member_count"),
            ),

            // --- Logical: $or (tracks leaf fields only) ---
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
                setOf("type"),
            ),

            // --- Logical: $nor (tracks leaf fields only) ---
            Arguments.of(
                mapOf("\$nor" to listOf(mapOf("type" to "commerce"))),
                Filters.nor(Filters.eq("type", "commerce")),
                setOf("type"),
            ),

            // --- Multi-field implicit AND tracks all fields ---
            Arguments.of(
                mapOf("type" to "messaging", "frozen" to false),
                Filters.and(
                    Filters.eq("type", "messaging"),
                    Filters.eq("frozen", false),
                ),
                setOf("type", "frozen"),
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
                setOf("type", "members"),
            ),

            // --- Date as string (preserved as-is) ---
            Arguments.of(
                mapOf("created_at" to mapOf("\$gt" to "2024-01-15T10:30:00.123456789Z")),
                Filters.greaterThan("created_at", "2024-01-15T10:30:00.123456789Z"),
                setOf("created_at"),
            ),

            // --- The driving case for the sort fallback: last_message_at is tracked ---
            Arguments.of(
                mapOf("last_message_at" to mapOf("\$gt" to "2024-01-15T10:30:00Z")),
                Filters.greaterThan("last_message_at", "2024-01-15T10:30:00Z"),
                setOf("last_message_at"),
            ),
            Arguments.of(
                mapOf(
                    "\$and" to listOf(
                        mapOf("type" to "messaging"),
                        mapOf("last_message_at" to mapOf("\$gt" to "2024-01-15T10:30:00Z")),
                    ),
                ),
                Filters.and(
                    Filters.eq("type", "messaging"),
                    Filters.greaterThan("last_message_at", "2024-01-15T10:30:00Z"),
                ),
                setOf("type", "last_message_at"),
            ),
        )
    }
}
