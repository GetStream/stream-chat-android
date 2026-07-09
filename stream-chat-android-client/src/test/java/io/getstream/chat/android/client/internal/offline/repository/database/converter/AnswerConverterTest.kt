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

import io.getstream.chat.android.client.internal.offline.randomAnswerEntity
import io.getstream.chat.android.client.internal.offline.repository.database.converter.internal.AnswerConverter
import io.getstream.chat.android.client.internal.offline.repository.domain.message.internal.AnswerEntity
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertNull
import org.junit.jupiter.api.Test

internal class AnswerConverterTest {

    private val sut = AnswerConverter()

    @Test
    fun `stringToAnswer should return null for null input`() {
        assertNull(sut.stringToAnswer(null))
    }

    @Test
    fun `answerToString should return null for null input`() {
        assertNull(sut.answerToString(null))
    }

    @Test
    fun `answer should survive a round trip`() {
        val answer = randomAnswerEntity()

        val result = requireNotNull(sut.stringToAnswer(sut.answerToString(answer)))

        assertEquals(answer.id, result.id)
        assertEquals(answer.pollId, result.pollId)
        assertEquals(answer.text, result.text)
        assertEquals(answer.createdAt, result.createdAt)
        assertEquals(answer.updatedAt, result.updatedAt)
        assertEquals(answer.userId, result.userId)
    }

    @Test
    fun `answer without user id should survive a round trip`() {
        val answer = randomAnswerEntity(userId = null)

        val result = requireNotNull(sut.stringToAnswer(sut.answerToString(answer)))

        assertEquals(answer.id, result.id)
        assertNull(result.userId)
    }

    @Test
    fun `stringToAnswerList should return empty list for null or blank input`() {
        assertEquals(emptyList<AnswerEntity>(), sut.stringToAnswerList(null))
        assertEquals(emptyList<AnswerEntity>(), sut.stringToAnswerList(""))
        assertEquals(emptyList<AnswerEntity>(), sut.stringToAnswerList("null"))
    }

    @Test
    fun `answerListToString should return null for null input`() {
        assertNull(sut.answerListToString(null))
    }

    @Test
    fun `answer list should survive a round trip`() {
        val answer = randomAnswerEntity()

        val result = requireNotNull(sut.stringToAnswerList(sut.answerListToString(listOf(answer))))

        assertEquals(1, result.size)
        assertEquals(answer.id, result.first().id)
        assertEquals(answer.pollId, result.first().pollId)
        assertEquals(answer.text, result.first().text)
        assertEquals(answer.createdAt, result.first().createdAt)
        assertEquals(answer.updatedAt, result.first().updatedAt)
        assertEquals(answer.userId, result.first().userId)
    }
}
