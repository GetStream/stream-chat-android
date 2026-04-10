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

import io.getstream.chat.android.ui.common.feature.messages.composer.transliteration.DefaultStreamTransliterator
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Test

internal class DefaultQueryFilterTest {

    private val filter = DefaultQueryFilter<String>(
        transliterator = DefaultStreamTransliterator(),
        target = { it },
    )

    @Test
    fun `empty query returns all items`() {
        val names = listOf("Alice", "Bob")

        assertEquals(listOf("Alice", "Bob"), filter.filter(names, ""))
    }

    @Test
    fun `no match returns empty list`() {
        val names = listOf("Alice", "Bob")

        assertEquals(emptyList<String>(), filter.filter(names, "xyz"))
    }

    @Test
    fun `match is case insensitive`() {
        val names = listOf("Aleksandar Apostolov", "Jc Minarro")

        assertEquals(listOf("Jc Minarro"), filter.filter(names, "JC"))
    }

    @Test
    fun `match ignores diacritics`() {
        val names = listOf("José", "Bob")

        assertEquals(listOf("José"), filter.filter(names, "jose"))
    }

    @Test
    fun `short query only matches names containing that substring`() {
        val names = listOf("Aleksandar Apostolov", "Jc Minarro")

        assertEquals(listOf("Jc Minarro"), filter.filter(names, "jc"))
    }

    @Test
    fun `query does not fuzzy match unrelated names`() {
        val names = listOf("Aleksandar Apostolov", "Ara", "AA BB CC", "Abel")

        assertEquals(listOf("Aleksandar Apostolov"), filter.filter(names, "ale"))
    }

    @Test
    fun `query matches substring in any word`() {
        val names = listOf("Alice Smith", "Bob Jones", "Charlie Smith")

        assertEquals(listOf("Alice Smith", "Charlie Smith"), filter.filter(names, "smith"))
    }

    @Test
    fun `results are sorted by levenshtein distance`() {
        val names = listOf("Charlie Alice", "Alice", "Bob Alice Smith")

        assertEquals(listOf("Alice", "Charlie Alice", "Bob Alice Smith"), filter.filter(names, "alice"))
    }
}
