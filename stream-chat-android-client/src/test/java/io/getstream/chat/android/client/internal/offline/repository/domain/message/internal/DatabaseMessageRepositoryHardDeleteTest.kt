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

package io.getstream.chat.android.client.internal.offline.repository.domain.message.internal

import io.getstream.chat.android.models.Message
import io.getstream.chat.android.models.SyncStatus
import io.getstream.chat.android.randomMessage
import io.getstream.chat.android.randomUser
import kotlinx.coroutines.test.TestScope
import kotlinx.coroutines.test.advanceUntilIdle
import kotlinx.coroutines.test.runTest
import org.amshove.kluent.shouldBeEqualTo
import org.junit.jupiter.api.Test
import org.junit.jupiter.params.ParameterizedTest
import org.junit.jupiter.params.provider.ValueSource
import org.mockito.kotlin.any
import org.mockito.kotlin.argThat
import org.mockito.kotlin.doReturn
import org.mockito.kotlin.mock
import org.mockito.kotlin.times
import org.mockito.kotlin.verify
import java.util.Date

internal class DatabaseMessageRepositoryHardDeleteTest {

    private val messageDao: MessageDao = mock()

    @ParameterizedTest
    @ValueSource(booleans = [true, false])
    fun `insertDeletedMessage stores whether the delete is hard before it returns`(hard: Boolean) = runTest {
        val message = deletedMessage()

        repository().insertDeletedMessage(message, hard)

        verify(messageDao).insert(
            argThat<List<MessageEntity>> {
                single().messageInnerEntity.let { it.id == message.id && it.hardDelete == hard }
            },
        )
    }

    @Test
    fun `a later insert of the deleted message does not overwrite its hard delete`() = runTest {
        val message = deletedMessage()
        val sut = repository()
        sut.insertDeletedMessage(message, hard = true)

        sut.insertMessage(message.copy(text = "edited elsewhere"))
        advanceUntilIdle()

        verify(messageDao, times(1)).insert(any<List<MessageEntity>>())
    }

    @Test
    fun `a hard delete of a message with a pending soft delete stores the hard delete`() = runTest {
        val message = deletedMessage()
        val sut = repository()
        sut.insertDeletedMessage(message, hard = false)

        sut.insertDeletedMessage(message.copy(deletedAt = Date(2_000)), hard = true)

        verify(messageDao).insert(
            argThat<List<MessageEntity>> { single().messageInnerEntity.hardDelete },
        )
    }

    @Test
    fun `a hard delete identical to a pending soft delete stores the hard delete`() = runTest {
        val message = deletedMessage()
        val sut = repository()
        sut.insertDeletedMessage(message, hard = false)

        sut.insertDeletedMessage(message, hard = true)

        verify(messageDao).insert(
            argThat<List<MessageEntity>> { single().messageInnerEntity.hardDelete },
        )
    }

    @Test
    fun `selectIsHardDelete reads the stored flag`() = runTest {
        val dao: MessageDao = mock {
            onBlocking { selectHardDelete("hard") } doReturn true
            onBlocking { selectHardDelete("soft") } doReturn false
            onBlocking { selectHardDelete("missing") } doReturn null
        }
        val sut = repository(dao)

        sut.selectIsHardDelete("hard") shouldBeEqualTo true
        sut.selectIsHardDelete("soft") shouldBeEqualTo false
        sut.selectIsHardDelete("missing") shouldBeEqualTo false
    }

    private fun TestScope.repository(dao: MessageDao = messageDao) = DatabaseMessageRepository(
        scope = this,
        messageDao = dao,
        replyMessageDao = mock(),
        pollDao = mock<PollDao>(),
        getUser = { randomUser(id = it) },
        currentUser = randomUser(),
        ignoredChannelTypes = emptySet(),
    )

    private fun deletedMessage(): Message = randomMessage(
        syncStatus = SyncStatus.SYNC_NEEDED,
        deletedAt = Date(1_000),
        deletedForMe = false,
        replyTo = null,
        poll = null,
    )
}
