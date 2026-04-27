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

import io.getstream.chat.android.randomUser
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Test

internal class DefaultUserQueryFilterTest {

    private val filter = DefaultUserQueryFilter()

    @Test
    fun `empty query returns all users`() {
        val users = listOf(user("Alice"), user("Bob"))

        assertEquals(listOf("Alice", "Bob"), filter.filter(users, "").names())
    }

    @Test
    fun `no match returns empty list`() {
        val users = listOf(user("Alice"), user("Bob"))

        assertEquals(emptyList<String>(), filter.filter(users, "xyz").names())
    }

    @Test
    fun `match is case insensitive`() {
        val users = listOf(user("Aleksandar Apostolov"), user("Jc Minarro"))

        assertEquals(listOf("Jc Minarro"), filter.filter(users, "JC").names())
    }

    @Test
    fun `match ignores diacritics`() {
        val users = listOf(user("José"), user("Bob"))

        assertEquals(listOf("José"), filter.filter(users, "jose").names())
    }

    @Test
    fun `short query only matches users containing that substring`() {
        val users = listOf(user("Aleksandar Apostolov"), user("Jc Minarro"))

        assertEquals(listOf("Jc Minarro"), filter.filter(users, "jc").names())
    }

    @Test
    fun `query does not fuzzy match unrelated names`() {
        val users = listOf(user("Aleksandar Apostolov"), user("Ara"), user("Abel"))

        assertEquals(listOf("Aleksandar Apostolov"), filter.filter(users, "ale").names())
    }

    @Test
    fun `query matches substring in any word`() {
        val users = listOf(user("Alice Smith"), user("Bob Jones"), user("Charlie Smith"))

        assertEquals(listOf("Alice Smith", "Charlie Smith"), filter.filter(users, "smith").names())
    }

    @Test
    fun `results are sorted by match position`() {
        val users = listOf(user("Johann"), user("Anne"), user("Marianne"))

        assertEquals(listOf("Anne", "Johann", "Marianne"), filter.filter(users, "ann").names())
    }

    @Test
    fun `falls back to id when name is blank`() {
        val users = listOf(randomUser(name = "", id = "alice123"), user("Bob"))

        assertEquals(listOf("alice123", "Bob"), filter.filter(users, "").map { it.name.ifBlank { it.id } })
        assertEquals(listOf("alice123"), filter.filter(users, "alice").map { it.id })
    }

    private fun user(name: String) = randomUser(name = name)

    private fun List<io.getstream.chat.android.models.User>.names() = map { it.name }
}
