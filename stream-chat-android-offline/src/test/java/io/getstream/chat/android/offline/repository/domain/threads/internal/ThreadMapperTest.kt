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

package io.getstream.chat.android.offline.repository.domain.threads.internal

import io.getstream.chat.android.models.Thread
import io.getstream.chat.android.models.ThreadParticipant
import io.getstream.chat.android.offline.randomThreadEntity
import io.getstream.chat.android.offline.repository.domain.channel.userread.internal.toEntity
import io.getstream.chat.android.offline.repository.domain.channel.userread.internal.toModel
import io.getstream.chat.android.randomChannel
import io.getstream.chat.android.randomMessage
import io.getstream.chat.android.randomThread
import io.getstream.chat.android.randomUser
import kotlinx.coroutines.test.runTest
import org.junit.jupiter.api.Assertions
import org.junit.jupiter.api.Test

internal class ThreadMapperTest {

    @Test
    fun `Should map ThreadEntity to Thread correctly`() = runTest {
        // Given
        val user = randomUser()
        val message = randomMessage()
        val channel = randomChannel()
        val entity = randomThreadEntity()
        val expected = Thread(
            parentMessageId = entity.parentMessageId,
            parentMessage = message,
            cid = entity.cid,
            channel = channel,
            createdByUserId = entity.createdByUserId,
            createdBy = user,
            activeParticipantCount = entity.activeParticipantCount,
            participantCount = entity.participantCount,
            threadParticipants = entity.threadParticipants.map {
                ThreadParticipant(
                    user = user,
                    threadId = it.threadId,
                    createdAt = it.createdAt,
                    lastReadAt = it.lastReadAt,
                )
            },
            lastMessageAt = entity.lastMessageAt,
            createdAt = entity.createdAt,
            updatedAt = entity.updatedAt,
            deletedAt = entity.deletedAt,
            title = entity.title,
            read = entity.read.map { it.toModel { user } },
            latestReplies = emptyList(),
            extraData = entity.extraData,
            draft = null,
        )
        // When
        val model = entity.toModel(
            getUser = { user },
            getMessage = { message },
            getChannel = { channel },
            getDraftMessage = { null },
        )
        // Then
        Assertions.assertEquals(expected, model)
    }

    @Test
    fun `Should map Thread to ThreadEntity correctly`() {
        // Given
        val thread = randomThread()
        val expected = ThreadEntity(
            parentMessageId = thread.parentMessageId,
            cid = thread.cid,
            createdByUserId = thread.createdByUserId,
            activeParticipantCount = thread.activeParticipantCount,
            participantCount = thread.participantCount,
            threadParticipants = thread.threadParticipants.map {
                ThreadParticipantEntity(
                    userId = it.user.id,
                    threadId = it.threadId,
                    createdAt = it.createdAt,
                    lastReadAt = it.lastReadAt,
                )
            },
            lastMessageAt = thread.lastMessageAt,
            createdAt = thread.createdAt,
            updatedAt = thread.updatedAt,
            deletedAt = thread.deletedAt,
            title = thread.title,
            read = thread.read.map { it.toEntity() },
            latestReplyIds = thread.latestReplies.map { it.id },
            extraData = thread.extraData,
        )
        // When
        val entity = thread.toEntity()
        // Then
        Assertions.assertEquals(expected, entity)
    }
}
