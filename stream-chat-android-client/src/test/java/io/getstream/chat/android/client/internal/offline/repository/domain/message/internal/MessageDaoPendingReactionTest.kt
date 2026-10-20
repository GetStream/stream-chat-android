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
import io.getstream.chat.android.client.internal.offline.repository.domain.reaction.internal.ReactionDao
import io.getstream.chat.android.client.internal.offline.repository.domain.reaction.internal.toEntity
import io.getstream.chat.android.models.Message
import io.getstream.chat.android.models.Reaction
import io.getstream.chat.android.models.SyncStatus
import io.getstream.chat.android.randomMessage
import io.getstream.chat.android.randomReaction
import io.getstream.chat.android.randomString
import kotlinx.coroutines.test.runTest
import org.amshove.kluent.shouldBeEqualTo
import org.junit.After
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import java.util.Date

@RunWith(AndroidJUnit4::class)
internal class MessageDaoPendingReactionTest {

    private lateinit var database: ChatDatabase
    private lateinit var messageDao: MessageDao
    private lateinit var reactionDao: ReactionDao

    private val userId = randomString()
    private val type = randomString()

    @Before
    fun setUp() {
        database = createRoomDB()
        messageDao = database.messageDao()
        reactionDao = database.reactionDao()
    }

    @After
    fun tearDown() {
        database.close()
    }

    @Test
    fun `a pending deletion survives a server copy of the message`() = runTest {
        assertStoredReactionAfterServerCopy(storedSyncStatus = SyncStatus.SYNC_NEEDED, keepsStored = true)
    }

    @Test
    fun `an in-progress reaction is replaced by a server copy of the message`() = runTest {
        // IN_PROGRESS rows are never retried, so keeping them would freeze one left behind by a killed process
        assertStoredReactionAfterServerCopy(storedSyncStatus = SyncStatus.IN_PROGRESS, keepsStored = false)
    }

    @Test
    fun `a synced reaction is replaced by a server copy of the message`() = runTest {
        assertStoredReactionAfterServerCopy(storedSyncStatus = SyncStatus.COMPLETED, keepsStored = false)
    }

    @Test
    fun `a permanently failed reaction is replaced by a server copy of the message`() = runTest {
        assertStoredReactionAfterServerCopy(storedSyncStatus = SyncStatus.FAILED_PERMANENTLY, keepsStored = false)
    }

    @Test
    fun `only the pending reaction is skipped when the server copy carries several`() = runTest {
        val message = randomMessage()
        messageDao.insert(message.toEntity())
        val pending = reaction(message, deletedAt = Date(2_000), syncStatus = SyncStatus.SYNC_NEEDED).toEntity()
        reactionDao.insert(pending)
        val serverReaction = reaction(message, deletedAt = null, syncStatus = SyncStatus.COMPLETED)
        val otherServerReaction = serverReaction.copy(type = randomString())

        messageDao.insert(
            message.copy(
                ownReactions = listOf(serverReaction, otherServerReaction),
                latestReactions = listOf(serverReaction, otherServerReaction),
            ).toEntity(),
        )

        reactionDao.selectReactionById(pending.id) shouldBeEqualTo pending
        val other = otherServerReaction.toEntity()
        reactionDao.selectReactionById(other.id) shouldBeEqualTo other
    }

    private suspend fun assertStoredReactionAfterServerCopy(storedSyncStatus: SyncStatus, keepsStored: Boolean) {
        val message = randomMessage()
        messageDao.insert(message.toEntity())
        val stored = reaction(message, deletedAt = Date(2_000), syncStatus = storedSyncStatus).toEntity()
        reactionDao.insert(stored)
        val serverReaction = reaction(message, deletedAt = null, syncStatus = SyncStatus.COMPLETED)

        messageDao.insert(
            message.copy(ownReactions = listOf(serverReaction), latestReactions = listOf(serverReaction)).toEntity(),
        )

        val expected = if (keepsStored) stored else serverReaction.toEntity()
        reactionDao.selectReactionById(stored.id) shouldBeEqualTo expected
    }

    private fun reaction(message: Message, deletedAt: Date?, syncStatus: SyncStatus): Reaction = randomReaction(
        messageId = message.id,
        type = type,
        user = null,
        userId = userId,
        createdAt = Date(1_000),
        updatedAt = Date(1_000),
        deletedAt = deletedAt,
        syncStatus = syncStatus,
    )
}
