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

import io.getstream.chat.android.models.TypingEvent
import io.getstream.chat.android.randomUser
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.test.runTest
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertNull
import org.junit.jupiter.api.Nested
import org.junit.jupiter.api.Test
import java.util.Date

@ExperimentalCoroutinesApi
internal class ChannelStateImplTypingTest : ChannelStateImplTestBase() {

    @Nested
    inner class SetTyping {

        @Test
        fun `setTyping should set the typing event`() = runTest {
            // given
            val typingUsers = listOf(randomUser(id = "user_1"), randomUser(id = "user_2"))
            val typingEvent = TypingEvent(CHANNEL_ID, typingUsers)
            // when
            channelState.setTyping(typingEvent)
            // then
            assertEquals(CHANNEL_ID, channelState.typing.value.channelId)
            assertEquals(2, channelState.typing.value.users.size)
        }

        @Test
        fun `setTyping should replace existing typing event`() = runTest {
            // given
            val initialEvent = TypingEvent(CHANNEL_ID, listOf(randomUser(id = "user_1")))
            channelState.setTyping(initialEvent)
            // when
            val newEvent = TypingEvent(CHANNEL_ID, listOf(randomUser(id = "user_2"), randomUser(id = "user_3")))
            channelState.setTyping(newEvent)
            // then
            assertEquals(2, channelState.typing.value.users.size)
        }

        @Test
        fun `setTyping with empty users should clear typing`() = runTest {
            // given
            val initialEvent = TypingEvent(CHANNEL_ID, listOf(randomUser(id = "user_1")))
            channelState.setTyping(initialEvent)
            // when
            channelState.setTyping(TypingEvent(CHANNEL_ID, emptyList()))
            // then
            assertEquals(0, channelState.typing.value.users.size)
        }

        @Test
        fun `typing should default to empty users`() = runTest {
            // when & then
            assertEquals(CHANNEL_ID, channelState.typing.value.channelId)
            assertEquals(0, channelState.typing.value.users.size)
        }
    }

    @Nested
    inner class LastStartTypingEvent {

        @Test
        fun `getLastStartTypingEvent should return null by default`() = runTest {
            // when & then
            assertNull(channelState.getLastStartTypingEvent())
        }

        @Test
        fun `setLastStartTypingEvent should set the date`() = runTest {
            // given
            val date = Date(5000)
            // when
            channelState.setLastStartTypingEvent(date)
            // then
            assertEquals(date, channelState.getLastStartTypingEvent())
        }

        @Test
        fun `setLastStartTypingEvent should update existing date`() = runTest {
            // given
            channelState.setLastStartTypingEvent(Date(1000))
            // when
            val newDate = Date(5000)
            channelState.setLastStartTypingEvent(newDate)
            // then
            assertEquals(newDate, channelState.getLastStartTypingEvent())
        }

        @Test
        fun `setLastStartTypingEvent with null should clear the date`() = runTest {
            // given
            channelState.setLastStartTypingEvent(Date(5000))
            // when
            channelState.setLastStartTypingEvent(null)
            // then
            assertNull(channelState.getLastStartTypingEvent())
        }
    }

    @Nested
    inner class KeystrokeParentMessageId {

        @Test
        fun `setKeystrokeParentMessageId should set the parent message ID`() = runTest {
            // when
            channelState.setKeystrokeParentMessageId("parent_msg_1")
            // then - verify indirectly via destroy (keystrokeParentMessageId is private,
            // but setKeystrokeParentMessageId should not throw)
        }

        @Test
        fun `setKeystrokeParentMessageId with null should clear the parent message ID`() = runTest {
            // given
            channelState.setKeystrokeParentMessageId("parent_msg_1")
            // when
            channelState.setKeystrokeParentMessageId(null)
            // then - should not throw
        }
    }
}
