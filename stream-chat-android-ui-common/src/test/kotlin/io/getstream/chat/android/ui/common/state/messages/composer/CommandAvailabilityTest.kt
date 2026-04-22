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

package io.getstream.chat.android.ui.common.state.messages.composer

import io.getstream.chat.android.models.Command
import io.getstream.chat.android.randomMessage
import io.getstream.chat.android.randomString
import io.getstream.chat.android.ui.common.state.messages.Edit
import io.getstream.chat.android.ui.common.state.messages.Reply
import org.junit.jupiter.api.Assertions.assertFalse
import org.junit.jupiter.api.Assertions.assertTrue
import org.junit.jupiter.api.Test

internal class CommandAvailabilityTest {

    private val giphy = Command(
        name = randomString(),
        description = randomString(),
        args = randomString(),
        set = "fun_set",
    )
    private val mute = Command(
        name = randomString(),
        description = randomString(),
        args = randomString(),
        set = "moderation_set",
    )
    private val custom = Command(
        name = randomString(),
        description = randomString(),
        args = randomString(),
        set = randomString(),
    )

    @Test
    fun `No active action makes every command available`() {
        assertTrue(giphy.isAvailableFor(null))
        assertTrue(mute.isAvailableFor(null))
        assertTrue(custom.isAvailableFor(null))
    }

    @Test
    fun `Edit action makes every command unavailable`() {
        val editAction = Edit(randomMessage())

        assertFalse(giphy.isAvailableFor(editAction))
        assertFalse(mute.isAvailableFor(editAction))
        assertFalse(custom.isAvailableFor(editAction))
    }

    @Test
    fun `Reply action makes moderation commands unavailable`() {
        val replyAction = Reply(randomMessage())

        assertFalse(mute.isAvailableFor(replyAction))
    }

    @Test
    fun `Reply action leaves fun_set commands available`() {
        val replyAction = Reply(randomMessage())

        assertTrue(giphy.isAvailableFor(replyAction))
    }

    @Test
    fun `Reply action leaves custom set commands available`() {
        val replyAction = Reply(randomMessage())

        assertTrue(custom.isAvailableFor(replyAction))
    }
}
