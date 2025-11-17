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

internal class VoteTest {

    private val voteId = "vote-123"
    private val pollId = "poll-456"
    private val optionId = "option-789"
    private val createdAt = Date(1000000)
    private val updatedAt = Date(2000000)

    private val vote = Vote(
        id = voteId,
        pollId = pollId,
        optionId = optionId,
        createdAt = createdAt,
        updatedAt = updatedAt,
        user = null,
    )

    @Test
    fun `getComparableField should return createdAt for snake_case field name 'created_at'`() {
        val result = vote.getComparableField("created_at")
        assertEquals(createdAt, result)
    }

    @Test
    fun `getComparableField should return createdAt for camelCase field name 'createdAt'`() {
        val result = vote.getComparableField("createdAt")
        assertEquals(createdAt, result)
    }

    @Test
    fun `getComparableField should return null for unknown field name`() {
        val result = vote.getComparableField("unknownField")
        assertNull(result)
    }

    @Test
    fun `getComparableField should return null for 'id' field since it's not comparable`() {
        val result = vote.getComparableField("id")
        assertNull(result)
    }

    @Test
    fun `getComparableField should return null for 'pollId' field since it's not comparable`() {
        val result = vote.getComparableField("pollId")
        assertNull(result)
    }

    @Test
    fun `getComparableField should return null for 'optionId' field since it's not comparable`() {
        val result = vote.getComparableField("optionId")
        assertNull(result)
    }

    @Test
    fun `getComparableField should return null for 'user' field since it's not comparable`() {
        val result = vote.getComparableField("user")
        assertNull(result)
    }

    @Test
    fun `getComparableField returns Date type for createdAt`() {
        val result = vote.getComparableField("createdAt")
        assertEquals(Date::class.java, result?.javaClass)
    }
}
