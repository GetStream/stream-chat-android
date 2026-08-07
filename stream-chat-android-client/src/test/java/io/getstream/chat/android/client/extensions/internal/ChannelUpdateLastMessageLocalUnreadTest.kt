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

package io.getstream.chat.android.client.extensions.internal

import io.getstream.chat.android.models.Channel
import io.getstream.chat.android.models.ChannelUserRead
import io.getstream.chat.android.models.Config
import io.getstream.chat.android.randomChannel
import io.getstream.chat.android.randomMessage
import io.getstream.chat.android.randomUser
import org.amshove.kluent.shouldBeEqualTo
import org.junit.jupiter.api.Test
import java.util.Date

internal class ChannelUpdateLastMessageLocalUnreadTest {

    private val newMessage = randomMessage(
        id = "new",
        createdAt = Date(2000),
        createdLocallyAt = null,
        deletedAt = null,
        deletedForMe = false,
        silent = false,
    )

    private fun readEventsDisabledChannel(): Channel = randomChannel(
        messages = listOf(
            randomMessage(
                id = "existing",
                createdAt = Date(1000),
                createdLocallyAt = null,
                deletedAt = null,
                deletedForMe = false,
            ),
        ),
        read = listOf(
            ChannelUserRead(
                user = randomUser(id = "current-user"),
                lastRead = Date(1000),
                lastReceivedEventDate = Date(1000),
                unreadMessages = 0,
                lastReadMessageId = null,
            ),
        ),
        config = Config(readEventsEnabled = false),
    )

    @Test
    fun `flag off leaves the non-opted-in increment behavior unchanged`() {
        val read = readEventsDisabledChannel()
            .updateLastMessage(Date(2500), newMessage, "current-user", isLocalUnreadCountEnabled = false)
            .read.first { it.user.id == "current-user" }

        read.unreadMessages shouldBeEqualTo 1
        read.lastReceivedEventDate shouldBeEqualTo Date(2500)
    }

    @Test
    fun `flag on leaves the current-user read to the on-device tracking`() {
        val read = readEventsDisabledChannel()
            .updateLastMessage(Date(2500), newMessage, "current-user", isLocalUnreadCountEnabled = true)
            .read.first { it.user.id == "current-user" }

        read.unreadMessages shouldBeEqualTo 0
        read.lastReceivedEventDate shouldBeEqualTo Date(1000)
    }
}
