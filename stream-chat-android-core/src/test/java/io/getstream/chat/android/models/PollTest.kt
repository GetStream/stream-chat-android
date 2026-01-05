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

package io.getstream.chat.android.models

import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertNull
import org.junit.jupiter.api.Test
import java.util.Date

internal class PollTest {

    private val pollId = "poll-123"
    private val pollName = "Test Poll"
    private val createdAt = Date(1000000)
    private val updatedAt = Date(2000000)
    private val isClosed = false

    private val poll = Poll(
        id = pollId,
        name = pollName,
        description = "Test poll description",
        options = listOf(
            Option(id = "opt1", text = "Option 1"),
            Option(id = "opt2", text = "Option 2"),
        ),
        votingVisibility = VotingVisibility.PUBLIC,
        enforceUniqueVote = true,
        maxVotesAllowed = 1,
        allowUserSuggestedOptions = false,
        allowAnswers = false,
        voteCount = 5,
        voteCountsByOption = mapOf("opt1" to 3, "opt2" to 2),
        votes = emptyList(),
        ownVotes = emptyList(),
        createdAt = createdAt,
        updatedAt = updatedAt,
        closed = isClosed,
        answersCount = 0,
        answers = emptyList(),
        createdBy = null,
        extraData = emptyMap(),
    )

    @Test
    fun `getComparableField should return id for 'id' field name`() {
        val result = poll.getComparableField("id")
        assertEquals(pollId, result)
    }

    @Test
    fun `getComparableField should return name for 'name' field name`() {
        val result = poll.getComparableField("name")
        assertEquals(pollName, result)
    }

    @Test
    fun `getComparableField should return createdAt for snake_case field name 'created_at'`() {
        val result = poll.getComparableField("created_at")
        assertEquals(createdAt, result)
    }

    @Test
    fun `getComparableField should return createdAt for camelCase field name 'createdAt'`() {
        val result = poll.getComparableField("createdAt")
        assertEquals(createdAt, result)
    }

    @Test
    fun `getComparableField should return updatedAt for snake_case field name 'updated_at'`() {
        val result = poll.getComparableField("updated_at")
        assertEquals(updatedAt, result)
    }

    @Test
    fun `getComparableField should return updatedAt for camelCase field name 'updatedAt'`() {
        val result = poll.getComparableField("updatedAt")
        assertEquals(updatedAt, result)
    }

    @Test
    fun `getComparableField should return closed boolean for snake_case field name 'is_closed'`() {
        val result = poll.getComparableField("is_closed")
        assertEquals(isClosed, result)
    }

    @Test
    fun `getComparableField should return closed boolean for camelCase field name 'isClosed'`() {
        val result = poll.getComparableField("isClosed")
        assertEquals(isClosed, result)
    }

    @Test
    fun `getComparableField should return closed=true when poll is closed`() {
        val closedPoll = poll.copy(closed = true)
        val result = closedPoll.getComparableField("isClosed")
        assertEquals(true, result)
    }

    @Test
    fun `getComparableField should return null for unknown field name`() {
        val result = poll.getComparableField("unknownField")
        assertNull(result)
    }

    @Test
    fun `getComparableField should be case-sensitive for field names`() {
        val result1 = poll.getComparableField("ID")
        val result2 = poll.getComparableField("Name")
        val result3 = poll.getComparableField("CREATED_AT")
        assertNull(result1)
        assertNull(result2)
        assertNull(result3)
    }
}
