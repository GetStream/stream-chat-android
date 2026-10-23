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
import io.getstream.chat.android.client.internal.offline.randomChannelEntity
import io.getstream.chat.android.client.internal.offline.repository.database.internal.ChatDatabase
import io.getstream.chat.android.models.Message
import io.getstream.chat.android.models.MessageType
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
internal class MessageDaoTruncationTest {

    private lateinit var database: ChatDatabase
    private lateinit var messageDao: MessageDao

    private val before = message(id = "before", createdAt = Date(1_000))
    private val atTruncation = message(id = "at", createdAt = TRUNCATED_AT)
    private val after = message(id = "after", createdAt = Date(3_000))
    private val notCreatedYet = message(id = "pending", createdAt = null, createdLocallyAt = Date(1_500))

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
    fun `channel queries keep every message of a channel never truncated`(): Unit = runTest {
        givenChannel(truncatedAt = null)
        givenMessages(before, atTruncation, after, notCreatedYet)

        channelQueries().forEach { (name, ids) -> (name to ids()) shouldBeEqualTo (name to setOf("before", "at", "after", "pending")) }
    }

    @Test
    fun `channel queries keep every message when the channel is not stored`(): Unit = runTest {
        givenMessages(before, atTruncation, after, notCreatedYet)

        channelQueries().forEach { (name, ids) -> (name to ids()) shouldBeEqualTo (name to setOf("before", "at", "after", "pending")) }
    }

    @Test
    fun `channel queries skip messages created before the truncation`(): Unit = runTest {
        givenChannel(truncatedAt = TRUNCATED_AT)
        givenMessages(before, atTruncation, after, notCreatedYet)

        channelQueries().forEach { (name, ids) -> (name to ids()) shouldBeEqualTo (name to setOf("at", "after", "pending")) }
    }

    @Test
    fun `thread query skips replies created before the truncation`(): Unit = runTest {
        givenChannel(truncatedAt = TRUNCATED_AT)
        val parent = message(id = "parent", createdAt = Date(3_000))
        val oldReply = message(id = "old-reply", createdAt = Date(1_000), parentId = parent.id)
        val newReply = message(id = "new-reply", createdAt = Date(4_000), parentId = parent.id)
        givenMessages(parent, oldReply, newReply)

        messageDao.messagesForThread(parent.id).ids() shouldBeEqualTo setOf("parent", "new-reply")
    }

    @Test
    fun `local-only query skips a pending edit of a message created before the truncation`(): Unit = runTest {
        givenChannel(truncatedAt = TRUNCATED_AT)
        val oldEdit = message(id = "old-edit", createdAt = Date(1_000), syncStatus = SyncStatus.SYNC_NEEDED)
        val newEdit = message(id = "new-edit", createdAt = Date(3_000), syncStatus = SyncStatus.SYNC_NEEDED)
        givenMessages(oldEdit, newEdit, notCreatedYet)

        messageDao.selectBySyncStatusOrTypeForChannel(
            cid = CID,
            syncStatuses = listOf(SyncStatus.SYNC_NEEDED.status),
            types = listOf(MessageType.ERROR),
        ).ids() shouldBeEqualTo setOf("new-edit", "pending")
    }

    private fun channelQueries(): List<Pair<String, suspend () -> Set<String>>> = listOf(
        "messagesForChannel" to { messageDao.messagesForChannel(CID).ids() },
        "newerThan" to { messageDao.messagesForChannelNewerThan(CID, dateFilter = Date(0)).ids() },
        "equalOrNewerThan" to { messageDao.messagesForChannelEqualOrNewerThan(CID, dateFilter = Date(0)).ids() },
        "olderThan" to { messageDao.messagesForChannelOlderThan(CID, dateFilter = Date(10_000)).ids() },
        "equalOrOlderThan" to { messageDao.messagesForChannelEqualOrOlderThan(CID, dateFilter = Date(10_000)).ids() },
    )

    private suspend fun givenChannel(truncatedAt: Date?) {
        database.channelStateDao().insert(randomChannelEntity(type = TYPE, channelId = ID, truncatedAt = truncatedAt))
    }

    private suspend fun givenMessages(vararg messages: Message) {
        messages.forEach { messageDao.insert(it.toEntity()) }
    }

    private fun List<MessageEntity>.ids(): Set<String> = mapTo(mutableSetOf()) { it.messageInnerEntity.id }

    private fun message(
        id: String,
        createdAt: Date?,
        createdLocallyAt: Date? = createdAt,
        parentId: String? = null,
        syncStatus: SyncStatus = SyncStatus.COMPLETED,
    ): Message = randomMessage(
        id = id,
        cid = CID,
        createdAt = createdAt,
        createdLocallyAt = createdLocallyAt,
        parentId = parentId,
        syncStatus = syncStatus,
        type = MessageType.REGULAR,
        replyTo = null,
        poll = null,
    )

    private companion object {
        private const val TYPE = "messaging"
        private const val ID = "truncated"
        private const val CID = "$TYPE:$ID"
        private val TRUNCATED_AT = Date(2_000)
    }
}
