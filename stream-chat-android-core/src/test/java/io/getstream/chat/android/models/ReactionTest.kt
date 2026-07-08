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

import io.getstream.chat.android.randomBoolean
import io.getstream.chat.android.randomDate
import io.getstream.chat.android.randomInt
import io.getstream.chat.android.randomReaction
import io.getstream.chat.android.randomString
import io.getstream.chat.android.randomSyncStatus
import io.getstream.chat.android.randomUser
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertFalse
import org.junit.jupiter.api.Assertions.assertNull
import org.junit.jupiter.api.Test

internal class ReactionTest {

    @Test
    fun `builder should set every field`() {
        val expected = Reaction(
            messageId = randomString(),
            type = randomString(),
            score = randomInt(),
            user = randomUser(),
            userId = randomString(),
            createdAt = randomDate(),
            createdLocallyAt = randomDate(),
            updatedAt = randomDate(),
            deletedAt = randomDate(),
            syncStatus = randomSyncStatus(),
            extraData = mapOf(randomString() to randomString()),
            enforceUnique = randomBoolean(),
            emojiCode = randomString(),
        )

        val built = Reaction.Builder()
            .withMessageId(expected.messageId)
            .withType(expected.type)
            .withScore(expected.score)
            .withUser(expected.user)
            .withUserId(expected.userId)
            .withCreatedAt(expected.createdAt)
            .withCreatedLocallyAt(expected.createdLocallyAt)
            .withUpdatedAt(expected.updatedAt)
            .withDeletedAt(expected.deletedAt)
            .withSyncStatus(expected.syncStatus)
            .withExtraData(expected.extraData)
            .withEnforceUnique(expected.enforceUnique)
            .withEmojiCode(expected.emojiCode)
            .build()

        assertEquals(expected, built)
    }

    @Test
    fun `messageId setter should behave like withMessageId`() {
        val messageId = randomString()
        val built = Reaction.Builder().messageId(messageId).build()
        assertEquals(messageId, built.messageId)
    }

    @Test
    fun `builder copy constructor should copy every field`() {
        val reaction = randomReaction().copy(createdLocallyAt = randomDate())

        val built = Reaction.Builder(reaction).build()

        assertEquals(reaction, built)
    }

    @Test
    fun `builder copy constructor should not carry skipPush`() {
        val reaction = randomReaction().copy(skipPush = true)

        val built = Reaction.Builder(reaction).build()

        assertFalse(built.skipPush)
        assertEquals(reaction.copy(skipPush = false), built)
    }

    @Test
    fun `id should concatenate messageId, type, score and user id`() {
        val reaction = Reaction(messageId = "m1", type = "like", score = 2, user = randomUser(id = "u1"))
        assertEquals("m1like2u1", reaction.id)
    }

    @Test
    fun `fetchUserId should return the user id when user is set`() {
        val reaction = randomReaction(user = randomUser(id = "user1"), userId = "other")
        assertEquals("user1", reaction.fetchUserId())
    }

    @Test
    fun `fetchUserId should return userId when user is null`() {
        val reaction = randomReaction(user = null, userId = "user2")
        assertEquals("user2", reaction.fetchUserId())
    }

    @Test
    fun `getComparableField should return createdAt for snake_case and camelCase field names`() {
        val reaction = randomReaction(createdAt = randomDate())
        assertEquals(reaction.createdAt, reaction.getComparableField("created_at"))
        assertEquals(reaction.createdAt, reaction.getComparableField("createdAt"))
    }

    @Test
    fun `getComparableField should return extraData value for custom field`() {
        val reaction = randomReaction(extraData = mutableMapOf("customField" to "customValue"))
        assertEquals("customValue", reaction.getComparableField("customField"))
    }

    @Test
    fun `getComparableField should return null for unknown field`() {
        val reaction = randomReaction(extraData = mutableMapOf())
        assertNull(reaction.getComparableField("unknownField"))
    }
}
