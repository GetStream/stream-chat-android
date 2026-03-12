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
import org.junit.jupiter.api.Disabled
import org.junit.jupiter.api.Test

/**
 * Stub tests for the [Message.isLocalOnly] predicate.
 * All tests are @Disabled — they will be enabled in Wave 1 once isLocalOnly() is implemented.
 *
 * Requirements covered: PRES-05
 */
internal class MessageIsLocalOnlyTest {

    @Test
    @Disabled("Wave 1 — implement isLocalOnly() first")
    fun `isLocalOnly returns true for SyncStatus SYNC_NEEDED`() {
        TODO("Implement after Wave 1")
    }

    @Test
    @Disabled("Wave 1 — implement isLocalOnly() first")
    fun `isLocalOnly returns true for SyncStatus IN_PROGRESS`() {
        TODO("Implement after Wave 1")
    }

    @Test
    @Disabled("Wave 1 — implement isLocalOnly() first")
    fun `isLocalOnly returns true for SyncStatus AWAITING_ATTACHMENTS`() {
        TODO("Implement after Wave 1")
    }

    @Test
    @Disabled("Wave 1 — implement isLocalOnly() first")
    fun `isLocalOnly returns true for SyncStatus FAILED_PERMANENTLY`() {
        TODO("Implement after Wave 1")
    }

    @Test
    @Disabled("Wave 1 — implement isLocalOnly() first")
    fun `isLocalOnly returns true for type ephemeral with COMPLETED syncStatus`() {
        TODO("Implement after Wave 1")
    }

    @Test
    @Disabled("Wave 1 — implement isLocalOnly() first")
    fun `isLocalOnly returns true for type error with COMPLETED syncStatus`() {
        TODO("Implement after Wave 1")
    }

    @Test
    @Disabled("Wave 1 — implement isLocalOnly() first")
    fun `isLocalOnly returns false for SyncStatus COMPLETED with type regular`() {
        TODO("Implement after Wave 1")
    }

    @Test
    @Disabled("Wave 1 — implement isLocalOnly() first")
    fun `isLocalOnly returns false for system message with COMPLETED`() {
        TODO("Implement after Wave 1")
    }
}
