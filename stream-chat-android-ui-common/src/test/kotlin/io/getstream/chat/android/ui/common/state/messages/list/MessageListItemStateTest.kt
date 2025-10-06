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

package io.getstream.chat.android.ui.common.state.messages.list

import io.getstream.chat.android.randomChannel
import io.getstream.chat.android.randomChannelCapabilities
import io.getstream.chat.android.randomDate
import io.getstream.chat.android.randomInt
import io.getstream.chat.android.randomMessage
import io.getstream.chat.android.randomUser
import org.junit.Assert.assertEquals
import org.junit.Test

internal class MessageListItemStateTest {

    @Test
    fun `id for MessageItemState should return message id`() {
        val message = randomMessage()
        val item = MessageItemState(message = message, ownCapabilities = randomChannelCapabilities())
        assertEquals(message.id, item.id)
    }

    @Test
    fun `id for DateSeparatorItemState should return formatted date id`() {
        val date = randomDate()
        val item = DateSeparatorItemState(date)
        assertEquals("date-separator-${date.time}", item.id)
    }

    @Test
    fun `id for ThreadDateSeparatorItemState should return formatted thread date id`() {
        val date = randomDate()
        val item = ThreadDateSeparatorItemState(date, replyCount = randomInt())
        assertEquals("thread-date-separator-${date.time}", item.id)
    }

    @Test
    fun `id for TypingItemState should return typing-indicator`() {
        val item = TypingItemState(typingUsers = listOf(randomUser()))
        assertEquals("typing-indicator", item.id)
    }

    @Test
    fun `id for EmptyThreadPlaceholderItemState should return empty-thread-placeholder`() {
        assertEquals("empty-thread-placeholder", EmptyThreadPlaceholderItemState.id)
    }

    @Test
    fun `id for UnreadSeparatorItemState should return unread-separator`() {
        val item = UnreadSeparatorItemState(unreadCount = randomInt())
        assertEquals("unread-separator", item.id)
    }

    @Test
    fun `id for StartOfTheChannelItemState should return start-of-the-channel`() {
        val channel = randomChannel()
        val item = StartOfTheChannelItemState(channel)
        assertEquals("start-of-the-channel", item.id)
    }
}
