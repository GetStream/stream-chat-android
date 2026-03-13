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

package io.getstream.chat.android.client.internal.state.plugin.state.channel.internal

import io.getstream.chat.android.models.MessageType
import io.getstream.chat.android.models.SyncStatus
import io.getstream.chat.android.randomMessage
import kotlinx.coroutines.ExperimentalCoroutinesApi
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertFalse
import org.junit.jupiter.api.Assertions.assertTrue
import org.junit.jupiter.api.Test
import java.util.Date
import java.util.concurrent.TimeUnit

/**
 * Tests for [ChannelStateImpl.setMessagesPreservingLocalOnly].
 *
 * Requirements covered: PRES-01, PRES-04, PRES-05 (state layer)
 *
 * Setup mirrors [ChannelStateImplTestBase] — extends the base class to reuse
 * the channelState fixture and createMessage/createMessages helpers.
 */
@ExperimentalCoroutinesApi
internal class ChannelStateImplPreservationTest : ChannelStateImplTestBase() {

    // -----------------------------------------------------------------------
    // Case 1: FAILED_PERMANENTLY message survives non-overlapping incoming
    // -----------------------------------------------------------------------
    @Test
    fun `failed message survives setMessagesPreservingLocalOnly with non-overlapping incoming`() {
        val localMsg = randomMessage(
            id = "local-failed",
            syncStatus = SyncStatus.FAILED_PERMANENTLY,
            type = MessageType.REGULAR,
            createdAt = Date(System.currentTimeMillis()),
            createdLocallyAt = null,
            parentId = null,
        )
        channelState.setMessages(listOf(localMsg))

        val serverMsg = randomMessage(
            id = "server-1",
            syncStatus = SyncStatus.COMPLETED,
            type = MessageType.REGULAR,
            createdAt = Date(System.currentTimeMillis() - TimeUnit.HOURS.toMillis(1)),
            createdLocallyAt = null,
            parentId = null,
        )

        channelState.setMessagesPreservingLocalOnly(
            incoming = listOf(serverMsg),
            localOnlyFromDb = emptyList(),
            windowFloor = null,
        )

        val ids = channelState.messages.value.map { it.id }
        assertTrue(ids.contains("local-failed"), "FAILED_PERMANENTLY message must survive")
        assertTrue(ids.contains("server-1"), "Server message must be present")
    }

    // -----------------------------------------------------------------------
    // Case 2: ephemeral (type==ephemeral, COMPLETED) survives
    // -----------------------------------------------------------------------
    @Test
    fun `ephemeral message survives setMessagesPreservingLocalOnly`() {
        val ephemeral = randomMessage(
            id = "local-ephemeral",
            syncStatus = SyncStatus.COMPLETED,
            type = MessageType.EPHEMERAL,
            createdAt = Date(System.currentTimeMillis()),
            createdLocallyAt = null,
            parentId = null,
        )
        channelState.setMessages(listOf(ephemeral))

        val serverMsg = randomMessage(
            id = "server-1",
            syncStatus = SyncStatus.COMPLETED,
            type = MessageType.REGULAR,
            createdAt = Date(System.currentTimeMillis() - TimeUnit.HOURS.toMillis(1)),
            createdLocallyAt = null,
            parentId = null,
        )

        channelState.setMessagesPreservingLocalOnly(
            incoming = listOf(serverMsg),
            localOnlyFromDb = emptyList(),
            windowFloor = null,
        )

        val ids = channelState.messages.value.map { it.id }
        assertTrue(ids.contains("local-ephemeral"), "Ephemeral message must survive")
    }

    // -----------------------------------------------------------------------
    // Case 3: AWAITING_ATTACHMENTS survives
    // -----------------------------------------------------------------------
    @Test
    fun `AWAITING_ATTACHMENTS message survives setMessagesPreservingLocalOnly`() {
        val awaitingMsg = randomMessage(
            id = "local-awaiting",
            syncStatus = SyncStatus.AWAITING_ATTACHMENTS,
            type = MessageType.REGULAR,
            createdAt = Date(System.currentTimeMillis()),
            createdLocallyAt = null,
            parentId = null,
        )
        channelState.setMessages(listOf(awaitingMsg))

        val serverMsg = randomMessage(
            id = "server-1",
            syncStatus = SyncStatus.COMPLETED,
            type = MessageType.REGULAR,
            createdAt = Date(System.currentTimeMillis() - TimeUnit.HOURS.toMillis(1)),
            createdLocallyAt = null,
            parentId = null,
        )

        channelState.setMessagesPreservingLocalOnly(
            incoming = listOf(serverMsg),
            localOnlyFromDb = emptyList(),
            windowFloor = null,
        )

        val ids = channelState.messages.value.map { it.id }
        assertTrue(ids.contains("local-awaiting"), "AWAITING_ATTACHMENTS message must survive")
    }

    // -----------------------------------------------------------------------
    // Case 4: SYNC_NEEDED with server-assigned ID (pending edit) survives
    // -----------------------------------------------------------------------
    @Test
    fun `pending edit SYNC_NEEDED on existing server ID survives setMessagesPreservingLocalOnly`() {
        // A message that was sent (has a server ID) but has a pending edit
        val pendingEdit = randomMessage(
            id = "server-existing-id",
            syncStatus = SyncStatus.SYNC_NEEDED,
            type = MessageType.REGULAR,
            createdAt = Date(System.currentTimeMillis()),
            createdLocallyAt = null,
            parentId = null,
        )
        channelState.setMessages(listOf(pendingEdit))

        // incoming does NOT include the same ID
        val serverMsg = randomMessage(
            id = "server-other",
            syncStatus = SyncStatus.COMPLETED,
            type = MessageType.REGULAR,
            createdAt = Date(System.currentTimeMillis() - TimeUnit.HOURS.toMillis(1)),
            createdLocallyAt = null,
            parentId = null,
        )

        channelState.setMessagesPreservingLocalOnly(
            incoming = listOf(serverMsg),
            localOnlyFromDb = emptyList(),
            windowFloor = null,
        )

        val ids = channelState.messages.value.map { it.id }
        assertTrue(ids.contains("server-existing-id"), "SYNC_NEEDED pending edit must survive")
    }

    // -----------------------------------------------------------------------
    // Case 5: server COMPLETED version wins on ID collision
    // -----------------------------------------------------------------------
    @Test
    fun `server COMPLETED version wins when same ID in both incoming and local-only`() {
        val localVersion = randomMessage(
            id = "msg-collision",
            syncStatus = SyncStatus.SYNC_NEEDED,
            type = MessageType.REGULAR,
            text = "local text",
            createdAt = Date(System.currentTimeMillis()),
            createdLocallyAt = null,
            parentId = null,
        )
        channelState.setMessages(listOf(localVersion))

        val serverVersion = randomMessage(
            id = "msg-collision",
            syncStatus = SyncStatus.COMPLETED,
            type = MessageType.REGULAR,
            text = "server text",
            createdAt = Date(System.currentTimeMillis()),
            createdLocallyAt = null,
            parentId = null,
        )

        channelState.setMessagesPreservingLocalOnly(
            incoming = listOf(serverVersion),
            localOnlyFromDb = emptyList(),
            windowFloor = null,
        )

        val messages = channelState.messages.value
        val result = messages.find { it.id == "msg-collision" }
        assertFalse(result == null, "Message with collision ID must be in result")
        assertEquals(SyncStatus.COMPLETED, result!!.syncStatus, "Server COMPLETED version must win")
        assertEquals("server text", result.text, "Server text must win")
        // Only one message with that ID
        assertEquals(1, messages.count { it.id == "msg-collision" }, "Must be exactly one entry for collision ID")
    }

    // -----------------------------------------------------------------------
    // Case 6: window floor filtering — below excluded, above included
    // -----------------------------------------------------------------------
    @Test
    fun `below-floor local-only excluded above-floor included`() {
        val now = System.currentTimeMillis()
        val oneDayMs = TimeUnit.DAYS.toMillis(1)
        val oneHourMs = TimeUnit.HOURS.toMillis(1)

        // Below floor: 2 days ago; floor is 1 day ago => excluded
        val belowFloor = randomMessage(
            id = "below-floor",
            syncStatus = SyncStatus.FAILED_PERMANENTLY,
            type = MessageType.REGULAR,
            createdAt = Date(now - 2 * oneDayMs),
            createdLocallyAt = null,
            parentId = null,
        )
        // Above floor: 1 hour ago; floor is 1 day ago => included
        val aboveFloor = randomMessage(
            id = "above-floor",
            syncStatus = SyncStatus.FAILED_PERMANENTLY,
            type = MessageType.REGULAR,
            createdAt = Date(now - oneHourMs),
            createdLocallyAt = null,
            parentId = null,
        )
        channelState.setMessages(listOf(belowFloor, aboveFloor))

        val windowFloor = Date(now - oneDayMs)

        channelState.setMessagesPreservingLocalOnly(
            incoming = emptyList(),
            localOnlyFromDb = emptyList(),
            windowFloor = windowFloor,
        )

        val ids = channelState.messages.value.map { it.id }
        assertFalse(ids.contains("below-floor"), "Below-floor local-only must be excluded")
        assertTrue(ids.contains("above-floor"), "Above-floor local-only must be included")
    }

    // -----------------------------------------------------------------------
    // Case 7: message at exactly windowFloor is included (>= not >)
    // -----------------------------------------------------------------------
    @Test
    fun `floor boundary message at exactly floor date is included`() {
        val now = System.currentTimeMillis()
        val oneDayMs = TimeUnit.DAYS.toMillis(1)
        val floorTime = now - oneDayMs

        val atFloor = randomMessage(
            id = "at-floor",
            syncStatus = SyncStatus.FAILED_PERMANENTLY,
            type = MessageType.REGULAR,
            createdAt = Date(floorTime),
            createdLocallyAt = null,
            parentId = null,
        )
        channelState.setMessages(listOf(atFloor))

        val windowFloor = Date(floorTime)

        channelState.setMessagesPreservingLocalOnly(
            incoming = emptyList(),
            localOnlyFromDb = emptyList(),
            windowFloor = windowFloor,
        )

        val ids = channelState.messages.value.map { it.id }
        assertTrue(ids.contains("at-floor"), "Message at exactly windowFloor must be included (>= not >)")
    }

    // -----------------------------------------------------------------------
    // Case 8: windowFloor = null — all local-only included
    // -----------------------------------------------------------------------
    @Test
    fun `empty incoming page with null floor includes all local-only`() {
        val old = randomMessage(
            id = "local-old",
            syncStatus = SyncStatus.FAILED_PERMANENTLY,
            type = MessageType.REGULAR,
            createdAt = Date(System.currentTimeMillis() - TimeUnit.DAYS.toMillis(30)),
            createdLocallyAt = null,
            parentId = null,
        )
        val recent = randomMessage(
            id = "local-recent",
            syncStatus = SyncStatus.SYNC_NEEDED,
            type = MessageType.REGULAR,
            createdAt = Date(System.currentTimeMillis()),
            createdLocallyAt = null,
            parentId = null,
        )
        channelState.setMessages(listOf(old, recent))

        channelState.setMessagesPreservingLocalOnly(
            incoming = emptyList(),
            localOnlyFromDb = emptyList(),
            windowFloor = null,
        )

        val ids = channelState.messages.value.map { it.id }
        assertTrue(ids.contains("local-old"), "Old local-only must be included when floor is null")
        assertTrue(ids.contains("local-recent"), "Recent local-only must be included when floor is null")
    }

    // -----------------------------------------------------------------------
    // Case 9: localOnlyFromDb = emptyList(), in-memory local-only still preserved
    // -----------------------------------------------------------------------
    @Test
    fun `localOnlyFromDb empty no-DB path local-only from state messages value preserved`() {
        val localMsg = randomMessage(
            id = "in-memory-local",
            syncStatus = SyncStatus.FAILED_PERMANENTLY,
            type = MessageType.REGULAR,
            createdAt = Date(System.currentTimeMillis()),
            createdLocallyAt = null,
            parentId = null,
        )
        channelState.setMessages(listOf(localMsg))

        val serverMsg = randomMessage(
            id = "server-1",
            syncStatus = SyncStatus.COMPLETED,
            type = MessageType.REGULAR,
            createdAt = Date(System.currentTimeMillis() - TimeUnit.HOURS.toMillis(1)),
            createdLocallyAt = null,
            parentId = null,
        )

        channelState.setMessagesPreservingLocalOnly(
            incoming = listOf(serverMsg),
            localOnlyFromDb = emptyList(), // no DB — in-memory fallback
            windowFloor = null,
        )

        val ids = channelState.messages.value.map { it.id }
        assertTrue(ids.contains("in-memory-local"), "In-memory local-only must be preserved even when localOnlyFromDb is empty")
    }

    // -----------------------------------------------------------------------
    // Case 10: localOnlyFromDb non-empty — union of state and DB, deduped by ID
    // -----------------------------------------------------------------------
    @Test
    fun `localOnlyFromDb non-empty union of state and DB deduped`() {
        // In-memory state has one local-only
        val inStateLocal = randomMessage(
            id = "state-local",
            syncStatus = SyncStatus.FAILED_PERMANENTLY,
            type = MessageType.REGULAR,
            createdAt = Date(System.currentTimeMillis()),
            createdLocallyAt = null,
            parentId = null,
        )
        channelState.setMessages(listOf(inStateLocal))

        // DB has a different local-only (not in state)
        val dbOnlyLocal = randomMessage(
            id = "db-local",
            syncStatus = SyncStatus.FAILED_PERMANENTLY,
            type = MessageType.REGULAR,
            createdAt = Date(System.currentTimeMillis() - TimeUnit.MINUTES.toMillis(5)),
            createdLocallyAt = null,
            parentId = null,
        )

        channelState.setMessagesPreservingLocalOnly(
            incoming = emptyList(),
            localOnlyFromDb = listOf(dbOnlyLocal),
            windowFloor = null,
        )

        val ids = channelState.messages.value.map { it.id }
        assertTrue(ids.contains("state-local"), "In-state local-only must be in union result")
        assertTrue(ids.contains("db-local"), "DB local-only must be in union result")
        // Dedup: no duplicates
        assertEquals(ids.size, ids.toSet().size, "No duplicate IDs in result")
    }

    // -----------------------------------------------------------------------
    // Case 11: COMPLETED messages NOT included in survivingLocalOnly
    // -----------------------------------------------------------------------
    @Test
    fun `COMPLETED messages not re-inserted from state isLocalOnly returns false`() {
        val completedMsg = randomMessage(
            id = "completed-msg",
            syncStatus = SyncStatus.COMPLETED,
            type = MessageType.REGULAR,
            createdAt = Date(System.currentTimeMillis()),
            createdLocallyAt = null,
            parentId = null,
        )
        channelState.setMessages(listOf(completedMsg))

        // Incoming is empty — no server messages
        channelState.setMessagesPreservingLocalOnly(
            incoming = emptyList(),
            localOnlyFromDb = emptyList(),
            windowFloor = null,
        )

        val ids = channelState.messages.value.map { it.id }
        assertFalse(ids.contains("completed-msg"), "COMPLETED message must NOT be preserved (isLocalOnly() = false)")
    }

    // -----------------------------------------------------------------------
    // Case 12: setMessages retains full-replace semantics — NOT preservation
    // -----------------------------------------------------------------------
    @Test
    fun `setMessages DB seed does NOT preserve local-only full replace semantics intact`() {
        // Seed state with a local-only message
        val localMsg = randomMessage(
            id = "local-pending",
            syncStatus = SyncStatus.FAILED_PERMANENTLY,
            type = MessageType.REGULAR,
            createdAt = Date(System.currentTimeMillis()),
            createdLocallyAt = null,
            parentId = null,
        )
        channelState.setMessages(listOf(localMsg))

        // setMessages called with server messages (DB seed path — full replace)
        val serverMsg = randomMessage(
            id = "server-db-seed",
            syncStatus = SyncStatus.COMPLETED,
            type = MessageType.REGULAR,
            createdAt = Date(System.currentTimeMillis() - TimeUnit.HOURS.toMillis(1)),
            createdLocallyAt = null,
            parentId = null,
        )
        channelState.setMessages(listOf(serverMsg))

        val ids = channelState.messages.value.map { it.id }
        // Full replace: local-only is gone
        assertFalse(ids.contains("local-pending"), "setMessages must NOT preserve local-only (full-replace semantics)")
        assertTrue(ids.contains("server-db-seed"), "setMessages must contain the new messages")
    }
}
