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
import org.junit.jupiter.api.Assertions.assertFalse
import org.junit.jupiter.api.Assertions.assertTrue
import org.junit.jupiter.api.Test

/**
 * Unit tests for the [Message.isLocalOnly] predicate.
 *
 * Requirements covered: PRES-05
 */
internal class MessageIsLocalOnlyTest {

    @Test
    fun `isLocalOnly returns true for SyncStatus SYNC_NEEDED`() {
        val message = randomMessage(syncStatus = SyncStatus.SYNC_NEEDED, type = MessageType.REGULAR)
        assertTrue(message.isLocalOnly())
    }

    @Test
    fun `isLocalOnly returns true for SyncStatus IN_PROGRESS`() {
        val message = randomMessage(syncStatus = SyncStatus.IN_PROGRESS, type = MessageType.REGULAR)
        assertTrue(message.isLocalOnly())
    }

    @Test
    fun `isLocalOnly returns true for SyncStatus AWAITING_ATTACHMENTS`() {
        val message = randomMessage(syncStatus = SyncStatus.AWAITING_ATTACHMENTS, type = MessageType.REGULAR)
        assertTrue(message.isLocalOnly())
    }

    @Test
    fun `isLocalOnly returns true for SyncStatus FAILED_PERMANENTLY`() {
        val message = randomMessage(syncStatus = SyncStatus.FAILED_PERMANENTLY, type = MessageType.REGULAR)
        assertTrue(message.isLocalOnly())
    }

    @Test
    fun `isLocalOnly returns true for type ephemeral with COMPLETED syncStatus`() {
        val message = randomMessage(syncStatus = SyncStatus.COMPLETED, type = MessageType.EPHEMERAL)
        assertTrue(message.isLocalOnly())
    }

    @Test
    fun `isLocalOnly returns true for type error with COMPLETED syncStatus`() {
        val message = randomMessage(syncStatus = SyncStatus.COMPLETED, type = MessageType.ERROR)
        assertTrue(message.isLocalOnly())
    }

    @Test
    fun `isLocalOnly returns false for SyncStatus COMPLETED with type regular`() {
        val message = randomMessage(syncStatus = SyncStatus.COMPLETED, type = MessageType.REGULAR)
        assertFalse(message.isLocalOnly())
    }

    @Test
    fun `isLocalOnly returns false for system message with COMPLETED`() {
        val message = randomMessage(syncStatus = SyncStatus.COMPLETED, type = MessageType.SYSTEM)
        assertFalse(message.isLocalOnly())
    }
}
