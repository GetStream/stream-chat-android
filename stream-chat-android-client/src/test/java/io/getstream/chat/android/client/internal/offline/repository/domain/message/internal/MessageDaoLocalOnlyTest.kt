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
import io.getstream.chat.android.models.MessageType
import io.getstream.chat.android.models.SyncStatus
import io.getstream.chat.android.randomCID
import io.getstream.chat.android.randomMessage
import kotlinx.coroutines.test.runTest
import org.amshove.kluent.shouldBeEqualTo
import org.junit.After
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import java.util.Date

@RunWith(AndroidJUnit4::class)
internal class MessageDaoLocalOnlyTest {

    private lateinit var database: ChatDatabase
    private lateinit var messageDao: MessageDao

    private val cid = randomCID()

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
    fun `a synced message without a creation date is selected and ordered by its local date`(): Unit = runTest {
        // Inserted ahead of the message it has to sort after, so a plain "ORDER BY createdAt" cannot pass
        val pendingWithoutDate = localMessage(
            syncStatus = SyncStatus.SYNC_NEEDED,
            createdAt = null,
            createdLocallyAt = Date(2_000),
        )
        val syncedWithoutDate = localMessage(
            syncStatus = SyncStatus.COMPLETED,
            createdAt = null,
            createdLocallyAt = Date(1_000),
        )
        val pending = localMessage(syncStatus = SyncStatus.SYNC_NEEDED, createdAt = Date(3_000))
        val synced = localMessage(syncStatus = SyncStatus.COMPLETED, createdAt = Date(4_000))
        listOf(pendingWithoutDate, syncedWithoutDate, pending, synced)
            .forEach { messageDao.insert(it.toEntity()) }

        val selected = messageDao.selectBySyncStatusOrTypeForChannel(
            cid = cid,
            syncStatuses = listOf(SyncStatus.SYNC_NEEDED.status),
            types = listOf(MessageType.ERROR),
        )

        selected.map { it.messageInnerEntity.id } shouldBeEqualTo
            listOf(syncedWithoutDate.id, pendingWithoutDate.id, pending.id)
    }

    private fun localMessage(
        syncStatus: SyncStatus,
        createdAt: Date?,
        createdLocallyAt: Date? = Date(0),
    ): Message = randomMessage(
        cid = cid,
        syncStatus = syncStatus,
        type = MessageType.REGULAR,
        createdAt = createdAt,
        createdLocallyAt = createdLocallyAt,
        replyTo = null,
        poll = null,
    )
}
