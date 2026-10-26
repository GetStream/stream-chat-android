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

import androidx.test.ext.junit.runners.AndroidJUnit4
import io.getstream.chat.android.client.internal.offline.createRoomDB
import io.getstream.chat.android.client.internal.offline.repository.database.internal.ChatDatabase
import io.getstream.chat.android.models.Message
import io.getstream.chat.android.models.SyncStatus
import io.getstream.chat.android.randomMessage
import io.getstream.chat.android.randomUser
import kotlinx.coroutines.test.TestScope
import kotlinx.coroutines.test.runTest
import org.amshove.kluent.shouldBeEqualTo
import org.junit.After
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import java.util.Date

@RunWith(AndroidJUnit4::class)
internal class DatabaseMessageRepositorySyncStatusTest {

    private val testScope = TestScope()
    private lateinit var database: ChatDatabase
    private lateinit var messageDao: MessageDao
    private lateinit var sut: DatabaseMessageRepository

    @Before
    fun setUp() {
        database = createRoomDB()
        messageDao = database.messageDao()
        sut = DatabaseMessageRepository(
            scope = testScope,
            messageDao = messageDao,
            replyMessageDao = database.replyMessageDao(),
            pollDao = database.pollDao(),
            getUser = { randomUser(id = it) },
            currentUser = randomUser(),
            ignoredChannelTypes = emptySet(),
        )
    }

    @After
    fun tearDown() {
        database.close()
    }

    @Test
    fun `updateMessageSyncStatus sets the status of a deleted message that was read before`() = testScope.runTest {
        // Reading a deleted message blocks every later insert of it
        val deleted = storedMessage(deletedAt = Date(1_000))
        sut.selectMessage(deleted.id)

        sut.updateMessageSyncStatus(deleted.id, SyncStatus.SYNC_NEEDED)

        messageDao.select(deleted.id)?.messageInnerEntity?.syncStatus shouldBeEqualTo SyncStatus.SYNC_NEEDED
        sut.selectMessage(deleted.id)?.syncStatus shouldBeEqualTo SyncStatus.SYNC_NEEDED
    }

    @Test
    fun `markDeletesInProgressAsCompleted updates the stored and the cached copy`() = testScope.runTest {
        val deleted = storedMessage(deletedAt = Date(1_000))
        sut.selectMessage(deleted.id)

        sut.markDeletesInProgressAsCompleted(deletedBefore = Date(2_000))

        messageDao.select(deleted.id)?.messageInnerEntity?.syncStatus shouldBeEqualTo SyncStatus.COMPLETED
        sut.selectMessage(deleted.id)?.syncStatus shouldBeEqualTo SyncStatus.COMPLETED
    }

    private suspend fun storedMessage(deletedAt: Date?): Message = randomMessage(
        syncStatus = SyncStatus.IN_PROGRESS,
        deletedAt = deletedAt,
        deletedForMe = false,
        replyTo = null,
        poll = null,
    ).also { messageDao.insert(it.toEntity()) }
}
