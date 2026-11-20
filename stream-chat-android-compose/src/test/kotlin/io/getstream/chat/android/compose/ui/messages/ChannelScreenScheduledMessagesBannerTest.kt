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

package io.getstream.chat.android.compose.ui.messages

import io.getstream.chat.android.randomString
import io.getstream.chat.android.ui.common.state.messages.composer.ScheduledMessagesBannerState
import org.junit.Assert.assertEquals
import org.junit.Test
import java.util.Date

internal class ChannelScreenScheduledMessagesBannerTest {

    @Test
    fun `the banner of a thread opens the scheduled replies of the thread`() {
        val threadParentId = randomString()

        assertEquals(threadParentId, ScheduledMessagesBannerState.Failed(1).listParentMessageId(threadParentId))
        assertEquals(
            threadParentId,
            ScheduledMessagesBannerState.Scheduled(count = 1, nextScheduledAt = Date(), isInThread = true)
                .listParentMessageId(threadParentId),
        )
    }

    @Test
    fun `the channel limit opens the scheduled messages of the whole channel, also from a thread`() {
        val limitReached = ScheduledMessagesBannerState.LimitReached(count = 10, limit = 10)

        assertEquals(null, limitReached.listParentMessageId(threadParentId = randomString()))
    }

    @Test
    fun `the banner of the channel opens the scheduled messages of the channel`() {
        assertEquals(null, ScheduledMessagesBannerState.Failed(1).listParentMessageId(threadParentId = null))
    }
}
