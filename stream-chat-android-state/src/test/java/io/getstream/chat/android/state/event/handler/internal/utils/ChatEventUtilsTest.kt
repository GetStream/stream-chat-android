/*
 * Copyright (c) 2014-2025 Stream.io Inc. All rights reserved.
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

package io.getstream.chat.android.state.event.handler.internal.utils

import io.getstream.chat.android.client.extensions.internal.NEVER
import io.getstream.chat.android.client.test.randomMessageDeliveredEvent
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Test

internal class ChatEventUtilsTest {

    @Test
    fun `MessageDeliveredEvent toChannelUserRead should create correct ChannelUserRead`() {
        val event = randomMessageDeliveredEvent()

        val actual = event.toChannelUserRead()

        assertEquals(event.user, actual.user)
        assertEquals(event.createdAt, actual.lastReceivedEventDate)
        assertEquals(event.lastDeliveredAt, actual.lastDeliveredAt)
        assertEquals(event.lastDeliveredMessageId, actual.lastDeliveredMessageId)
        assertEquals(NEVER, actual.lastRead)
        assertEquals(0, actual.unreadMessages)
        assertEquals(null, actual.lastReadMessageId)
    }
}
