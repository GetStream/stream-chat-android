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
import org.junit.jupiter.api.Disabled
import org.junit.jupiter.api.Test

/**
 * Stub tests for [ChannelStateImpl.setMessagesPreservingLocalOnly].
 * All tests are @Disabled — they will be enabled in Wave 2 once
 * setMessagesPreservingLocalOnly() is implemented.
 *
 * Requirements covered: PRES-01, PRES-04, PRES-05 (state layer)
 *
 * Setup mirrors [ChannelStateImplTestBase] — extends the base class to reuse
 * the channelState fixture and createMessage/createMessages helpers.
 */
@ExperimentalCoroutinesApi
internal class ChannelStateImplPreservationTest : ChannelStateImplTestBase() {

    @Test
    @Disabled("Wave 2 — implement setMessagesPreservingLocalOnly() first")
    fun `failed message survives setMessagesPreservingLocalOnly with non-overlapping incoming`() {
        TODO("Implement after Wave 2")
    }

    @Test
    @Disabled("Wave 2 — implement setMessagesPreservingLocalOnly() first")
    fun `ephemeral message survives setMessagesPreservingLocalOnly`() {
        TODO("Implement after Wave 2")
    }

    @Test
    @Disabled("Wave 2 — implement setMessagesPreservingLocalOnly() first")
    fun `AWAITING_ATTACHMENTS message survives setMessagesPreservingLocalOnly`() {
        TODO("Implement after Wave 2")
    }

    @Test
    @Disabled("Wave 2 — implement setMessagesPreservingLocalOnly() first")
    fun `pending edit SYNC_NEEDED on existing server ID survives setMessagesPreservingLocalOnly`() {
        TODO("Implement after Wave 2")
    }

    @Test
    @Disabled("Wave 2 — implement setMessagesPreservingLocalOnly() first")
    fun `server COMPLETED version wins when same ID in both incoming and local-only`() {
        TODO("Implement after Wave 2")
    }

    @Test
    @Disabled("Wave 2 — implement setMessagesPreservingLocalOnly() first")
    fun `below-floor local-only excluded above-floor included`() {
        TODO("Implement after Wave 2")
    }

    @Test
    @Disabled("Wave 2 — implement setMessagesPreservingLocalOnly() first")
    fun `floor boundary message at exactly floor date is included`() {
        TODO("Implement after Wave 2")
    }

    @Test
    @Disabled("Wave 2 — implement setMessagesPreservingLocalOnly() first")
    fun `empty incoming page with null floor includes all local-only`() {
        TODO("Implement after Wave 2")
    }

    @Test
    @Disabled("Wave 2 — implement setMessagesPreservingLocalOnly() first")
    fun `localOnlyFromDb empty no-DB path local-only from state messages value preserved`() {
        TODO("Implement after Wave 2")
    }

    @Test
    @Disabled("Wave 2 — implement setMessagesPreservingLocalOnly() first")
    fun `localOnlyFromDb non-empty union of state and DB deduped`() {
        TODO("Implement after Wave 2")
    }

    @Test
    @Disabled("Wave 2 — implement setMessagesPreservingLocalOnly() first")
    fun `COMPLETED messages not re-inserted from state isLocalOnly returns false`() {
        TODO("Implement after Wave 2")
    }

    @Test
    @Disabled("Wave 2 — implement setMessagesPreservingLocalOnly() first")
    fun `setMessages DB seed does NOT preserve local-only full replace semantics intact`() {
        TODO("Implement after Wave 2")
    }
}
