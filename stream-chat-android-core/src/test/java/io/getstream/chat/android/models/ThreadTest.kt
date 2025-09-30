/*
 * Copyright (c) 2014-2025 Stream.io Inc. All rights reserved.
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

internal class ThreadTest {

    private val now = Date()
    private val createdAt = Date(now.time - 10000)
    private val updatedAt = Date(now.time - 5000)

    private val parentMessage = Message(
        id = "parent1",
        text = "Parent message",
        createdAt = createdAt,
        replyCount = 5,
    )

    private val thread = Thread(
        activeParticipantCount = 3,
        cid = "messaging:123",
        channel = null,
        parentMessageId = "parent1",
        parentMessage = parentMessage,
        createdByUserId = "user1",
        createdBy = null,
        participantCount = 10,
        threadParticipants = emptyList(),
        lastMessageAt = now,
        createdAt = createdAt,
        updatedAt = updatedAt,
        deletedAt = null,
        title = "Test Thread",
        latestReplies = emptyList(),
        read = emptyList(),
        draft = null,
        extraData = mapOf(
            "customField" to "customValue",
            "customNumber" to 42,
            "customDate" to now,
        ),
    )

    @Test
    fun `getComparableField should return activeParticipantCount for snake_case field name`() {
        val result = thread.getComparableField("active_participant_count")
        assertEquals(3, result)
    }

    @Test
    fun `getComparableField should return activeParticipantCount for camelCase field name`() {
        val result = thread.getComparableField("activeParticipantCount")
        assertEquals(3, result)
    }

    @Test
    fun `getComparableField should return createdAt for snake_case field name`() {
        val result = thread.getComparableField("created_at")
        assertEquals(createdAt, result)
    }

    @Test
    fun `getComparableField should return createdAt for camelCase field name`() {
        val result = thread.getComparableField("createdAt")
        assertEquals(createdAt, result)
    }

    @Test
    fun `getComparableField should return lastMessageAt for snake_case field name`() {
        val result = thread.getComparableField("last_message_at")
        assertEquals(now, result)
    }

    @Test
    fun `getComparableField should return lastMessageAt for camelCase field name`() {
        val result = thread.getComparableField("lastMessageAt")
        assertEquals(now, result)
    }

    @Test
    fun `getComparableField should return parentMessageId for snake_case field name`() {
        val result = thread.getComparableField("parent_message_id")
        assertEquals("parent1", result)
    }

    @Test
    fun `getComparableField should return parentMessageId for camelCase field name`() {
        val result = thread.getComparableField("parentMessageId")
        assertEquals("parent1", result)
    }

    @Test
    fun `getComparableField should return participantCount for snake_case field name`() {
        val result = thread.getComparableField("participant_count")
        assertEquals(10, result)
    }

    @Test
    fun `getComparableField should return participantCount for camelCase field name`() {
        val result = thread.getComparableField("participantCount")
        assertEquals(10, result)
    }

    @Test
    fun `getComparableField should return replyCount for snake_case field name`() {
        val result = thread.getComparableField("reply_count")
        assertEquals(5, result)
    }

    @Test
    fun `getComparableField should return replyCount for camelCase field name`() {
        val result = thread.getComparableField("replyCount")
        assertEquals(5, result)
    }

    @Test
    fun `getComparableField should return updatedAt for snake_case field name`() {
        val result = thread.getComparableField("updated_at")
        assertEquals(updatedAt, result)
    }

    @Test
    fun `getComparableField should return updatedAt for camelCase field name`() {
        val result = thread.getComparableField("updatedAt")
        assertEquals(updatedAt, result)
    }

    @Test
    fun `getComparableField should return extraData value for custom field name`() {
        val result = thread.getComparableField("customField")
        assertEquals("customValue", result)
    }

    @Test
    fun `getComparableField should return extraData value for custom comparable field`() {
        val result = thread.getComparableField("customNumber")
        assertEquals(42, result)
    }

    @Test
    fun `getComparableField should return extraData value for custom date field`() {
        val result = thread.getComparableField("customDate")
        assertEquals(now, result)
    }

    @Test
    fun `getComparableField should return null for unknown field name`() {
        val result = thread.getComparableField("unknownField")
        assertNull(result)
    }

    @Test
    fun `getComparableField should return null for non-comparable extraData value`() {
        val threadWithNonComparable = thread.copy(
            extraData = mapOf("nonComparable" to listOf("not", "comparable")),
        )
        val result = threadWithNonComparable.getComparableField("nonComparable")
        assertNull(result)
    }

    @Test
    fun `getComparableField should return null for empty field name`() {
        val result = thread.getComparableField("")
        assertNull(result)
    }

    @Test
    fun `getComparableField should return null for blank field name`() {
        val result = thread.getComparableField("   ")
        assertNull(result)
    }
}
