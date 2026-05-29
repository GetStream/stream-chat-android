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

package io.getstream.chat.android.ui.common.feature.messages.composer.query.filter

import io.getstream.chat.android.models.User
import io.getstream.chat.android.randomUser
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Test
import org.junit.jupiter.params.ParameterizedTest
import org.junit.jupiter.params.provider.Arguments
import org.junit.jupiter.params.provider.MethodSource

internal class DefaultUserQueryFilterTest {

    private val filter = DefaultUserQueryFilter()

    @ParameterizedTest(name = "[{index}] {0}")
    @MethodSource("matchCases")
    fun `filter returns the expected matches`(
        @Suppress("UNUSED_PARAMETER") description: String,
        userNames: List<String>,
        query: String,
        expected: List<String>,
    ) {
        val users = userNames.map { randomUser(name = it) }

        assertEquals(expected, filter.filter(users, query).map(User::name))
    }

    @Test
    fun `falls back to id when name is blank`() {
        val users = listOf(randomUser(name = "Bob"), randomUser(name = "", id = "alice123"))

        assertEquals(listOf("alice123", "Bob"), filter.filter(users, "").map { it.name.ifBlank { it.id } })
        assertEquals(listOf("alice123"), filter.filter(users, "alice").map { it.id })
    }

    private companion object {

        @Suppress("LongMethod")
        @JvmStatic
        fun matchCases(): List<Arguments> = listOf(
            Arguments.of(
                "empty query returns all users sorted alphabetically",
                listOf("Charlie", "Alice", "Bob"),
                "",
                listOf("Alice", "Bob", "Charlie"),
            ),
            Arguments.of(
                "no match returns empty list",
                listOf("Alice", "Bob"),
                "xyz",
                emptyList<String>(),
            ),
            Arguments.of(
                "match is case insensitive",
                listOf("Aleksandar Apostolov", "Jc Minarro"),
                "JC",
                listOf("Jc Minarro"),
            ),
            Arguments.of(
                "match ignores diacritics",
                listOf("José", "Bob"),
                "jose",
                listOf("José"),
            ),
            Arguments.of(
                "last query word prefix-matches any name word",
                listOf("Alice Smith", "Bob Jones", "Charlie Smith"),
                "smith",
                listOf("Alice Smith", "Charlie Smith"),
            ),
            Arguments.of(
                "last query word must be a prefix, not a substring",
                listOf("Hart", "Arnold", "Garrick"),
                "ar",
                listOf("Arnold"),
            ),
            Arguments.of(
                "single-word prefix matches the only/last word",
                listOf("First Last"),
                "L",
                listOf("First Last"),
            ),
            Arguments.of(
                "full match plus prefix on the last word matches",
                listOf("First Last"),
                "First L",
                listOf("First Last"),
            ),
            Arguments.of(
                "full-match words may appear in any order",
                listOf("First Last"),
                "Last Fi",
                listOf("First Last"),
            ),
            Arguments.of(
                "non-final words require a full match, not a substring",
                listOf("First Last"),
                "t L",
                emptyList<String>(),
            ),
            Arguments.of(
                "the same name word may satisfy multiple query words",
                listOf("First Last"),
                "first first",
                listOf("First Last"),
            ),
            Arguments.of(
                "the same name word may satisfy a full match and a final prefix",
                listOf("First Last"),
                "first f",
                listOf("First Last"),
            ),
            Arguments.of(
                "results are sorted alphabetically by normalized name",
                listOf("Charlie Smith", "Alice Smith", "Bob Smith"),
                "smith",
                listOf("Alice Smith", "Bob Smith", "Charlie Smith"),
            ),
        )
    }
}
