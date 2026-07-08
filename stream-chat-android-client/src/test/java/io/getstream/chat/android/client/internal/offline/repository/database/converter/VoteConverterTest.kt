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

package io.getstream.chat.android.client.internal.offline.repository.database.converter

import io.getstream.chat.android.client.internal.offline.randomVoteEntity
import io.getstream.chat.android.client.internal.offline.repository.database.converter.internal.VoteConverter
import io.getstream.chat.android.client.internal.offline.repository.domain.message.internal.VoteEntity
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertNull
import org.junit.jupiter.api.Test

internal class VoteConverterTest {

    private val sut = VoteConverter()

    @Test
    fun `stringToVote should return null for null input`() {
        assertNull(sut.stringToVote(null))
    }

    @Test
    fun `voteToString should return null for null input`() {
        assertNull(sut.voteToString(null))
    }

    @Test
    fun `vote should survive a round trip`() {
        val vote = randomVoteEntity()

        val result = requireNotNull(sut.stringToVote(sut.voteToString(vote)))

        assertEquals(vote.id, result.id)
        assertEquals(vote.pollId, result.pollId)
        assertEquals(vote.optionId, result.optionId)
        assertEquals(vote.createdAt, result.createdAt)
        assertEquals(vote.updatedAt, result.updatedAt)
        assertEquals(vote.userId, result.userId)
    }

    @Test
    fun `vote without user id should survive a round trip`() {
        val vote = randomVoteEntity(userId = null)

        val result = requireNotNull(sut.stringToVote(sut.voteToString(vote)))

        assertEquals(vote.id, result.id)
        assertNull(result.userId)
    }

    @Test
    fun `stringToVoteList should return empty list for null or blank input`() {
        assertEquals(emptyList<VoteEntity>(), sut.stringToVoteList(null))
        assertEquals(emptyList<VoteEntity>(), sut.stringToVoteList(""))
        assertEquals(emptyList<VoteEntity>(), sut.stringToVoteList("null"))
    }

    @Test
    fun `voteListToString should return null for null input`() {
        assertNull(sut.voteListToString(null))
    }

    @Test
    fun `vote list should survive a round trip`() {
        val vote = randomVoteEntity()

        val result = requireNotNull(sut.stringToVoteList(sut.voteListToString(listOf(vote))))

        assertEquals(1, result.size)
        assertEquals(vote.id, result.first().id)
        assertEquals(vote.pollId, result.first().pollId)
        assertEquals(vote.optionId, result.first().optionId)
        assertEquals(vote.createdAt, result.first().createdAt)
        assertEquals(vote.updatedAt, result.first().updatedAt)
        assertEquals(vote.userId, result.first().userId)
    }
}
