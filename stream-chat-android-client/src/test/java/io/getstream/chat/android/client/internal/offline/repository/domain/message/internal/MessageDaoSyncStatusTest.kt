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
import kotlinx.coroutines.test.runTest
import org.amshove.kluent.shouldBeEqualTo
import org.junit.After
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import java.util.Date

@RunWith(AndroidJUnit4::class)
internal class MessageDaoSyncStatusTest {

    private lateinit var database: ChatDatabase
    private lateinit var messageDao: MessageDao

    private val before = Date(1_000)
    private val cutoff = Date(2_000)
    private val after = Date(3_000)

    @Before
    fun setUp() {
        database = createRoomDB()
        messageDao = database.messageDao()
    }

    @After
    fun tearDown() {
        database.close()
    }

    @Test
    fun `updateSyncStatus sets the status of that message only`() = runTest {
        val target = message(SyncStatus.IN_PROGRESS, deletedAt = before)
        val other = message(SyncStatus.IN_PROGRESS)
        listOf(target, other).forEach { messageDao.insert(it.toEntity()) }

        messageDao.updateSyncStatus(target.id, SyncStatus.COMPLETED)

        messageDao.select(target.id)?.messageInnerEntity?.syncStatus shouldBeEqualTo SyncStatus.COMPLETED
        messageDao.select(target.id)?.messageInnerEntity?.deletedAt shouldBeEqualTo before
        messageDao.select(other.id)?.messageInnerEntity?.syncStatus shouldBeEqualTo SyncStatus.IN_PROGRESS
    }

    @Test
    fun `replaceSyncStatusOfDeletes moves the deletes before the cutoff and every delete for me`() = runTest {
        val deleted = message(SyncStatus.IN_PROGRESS, deletedAt = before)
        val deletedAfter = message(SyncStatus.IN_PROGRESS, deletedAt = after)
        val deletedForMe = message(SyncStatus.IN_PROGRESS, deletedForMe = true)
        val sent = message(SyncStatus.IN_PROGRESS)
        val failedDelete = message(SyncStatus.SYNC_NEEDED, deletedAt = before)
        val messages = listOf(deleted, deletedAfter, deletedForMe, sent, failedDelete)
        messages.forEach { messageDao.insert(it.toEntity()) }

        messageDao.replaceSyncStatusOfDeletes(SyncStatus.IN_PROGRESS, SyncStatus.COMPLETED, cutoff)

        messages.map { messageDao.select(it.id)?.messageInnerEntity?.syncStatus } shouldBeEqualTo listOf(
            SyncStatus.COMPLETED,
            SyncStatus.IN_PROGRESS,
            SyncStatus.COMPLETED,
            SyncStatus.IN_PROGRESS,
            SyncStatus.SYNC_NEEDED,
        )
    }

    private fun message(
        syncStatus: SyncStatus,
        deletedAt: Date? = null,
        deletedForMe: Boolean = false,
    ): Message = randomMessage(
        syncStatus = syncStatus,
        deletedAt = deletedAt,
        deletedForMe = deletedForMe,
        replyTo = null,
        poll = null,
    )
}
