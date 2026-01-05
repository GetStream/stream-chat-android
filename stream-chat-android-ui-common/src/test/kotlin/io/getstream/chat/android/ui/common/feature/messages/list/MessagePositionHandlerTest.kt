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

package io.getstream.chat.android.ui.common.feature.messages.list

import io.getstream.chat.android.models.Message
import io.getstream.chat.android.models.MessageType
import io.getstream.chat.android.models.User
import io.getstream.chat.android.randomMessage
import io.getstream.chat.android.ui.common.state.messages.list.MessagePosition
import org.amshove.kluent.`should be equal to`
import org.junit.jupiter.api.Test

internal class MessagePositionHandlerTest {

    private val defaultHandler = MessagePositionHandler.defaultHandler()

    private val user1 = User(id = "user1")
    private val user2 = User(id = "user2")

    private fun createMessage(user: User, isSystem: Boolean = false, isError: Boolean = false): Message {
        return randomMessage(
            user = user,
            type = when {
                isSystem -> MessageType.SYSTEM
                isError -> MessageType.ERROR
                else -> MessageType.REGULAR
            },
        )
    }

    @Test
    fun `Message should be TOP when it starts a new user sequence`() {
        val previousMessage = createMessage(user2)
        val currentMessage = createMessage(user1)
        val nextMessage = createMessage(user1)

        val result = defaultHandler.handleMessagePosition(
            previousMessage,
            currentMessage,
            nextMessage,
            isAfterDateSeparator = true,
            isBeforeDateSeparator = false,
            isInThread = false,
        )

        result `should be equal to` listOf(MessagePosition.TOP)
    }

    @Test
    fun `Message should be MIDDLE when it's between two messages from the same user`() {
        val previousMessage = createMessage(user1)
        val currentMessage = createMessage(user1)
        val nextMessage = createMessage(user1)

        val result = defaultHandler.handleMessagePosition(
            previousMessage,
            currentMessage,
            nextMessage,
            isAfterDateSeparator = false,
            isBeforeDateSeparator = false,
            isInThread = false,
        )

        result `should be equal to` listOf(MessagePosition.MIDDLE)
    }

    @Test
    fun `Message should be BOTTOM when it's the last message in a user sequence`() {
        val previousMessage = createMessage(user1)
        val currentMessage = createMessage(user1)
        val nextMessage = createMessage(user2)

        val result = defaultHandler.handleMessagePosition(
            previousMessage,
            currentMessage,
            nextMessage,
            isAfterDateSeparator = false,
            isBeforeDateSeparator = true,
            isInThread = false,
        )

        result `should be equal to` listOf(MessagePosition.BOTTOM)
    }

    @Test
    fun `Message should be NONE when it's a single isolated message`() {
        val previousMessage = createMessage(user2)
        val currentMessage = createMessage(user1)
        val nextMessage = createMessage(user2)

        val result = defaultHandler.handleMessagePosition(
            previousMessage,
            currentMessage,
            nextMessage,
            isAfterDateSeparator = true,
            isBeforeDateSeparator = true,
            isInThread = false,
        )

        result `should be equal to` listOf(MessagePosition.NONE)
    }

    @Test
    fun `Message should be NONE when it doesn't match any position`() {
        val previousMessage = createMessage(user1, isSystem = true)
        val currentMessage = createMessage(user1, isSystem = true)
        val nextMessage = createMessage(user1, isSystem = true)

        val result = defaultHandler.handleMessagePosition(
            previousMessage,
            currentMessage,
            nextMessage,
            isAfterDateSeparator = false,
            isBeforeDateSeparator = false,
            isInThread = false,
        )

        result `should be equal to` listOf(MessagePosition.NONE)
    }

    @Test
    fun `Message should be TOP when there's a date separator before it`() {
        val previousMessage = createMessage(user1)
        val currentMessage = createMessage(user1)
        val nextMessage = createMessage(user1)

        val result = defaultHandler.handleMessagePosition(
            previousMessage,
            currentMessage,
            nextMessage,
            isAfterDateSeparator = true,
            isBeforeDateSeparator = false,
            isInThread = false,
        )

        result `should be equal to` listOf(MessagePosition.TOP)
    }

    @Test
    fun `Message should be BOTTOM when there's a date separator after it`() {
        val previousMessage = createMessage(user1)
        val currentMessage = createMessage(user1)
        val nextMessage = createMessage(user1)

        val result = defaultHandler.handleMessagePosition(
            previousMessage,
            currentMessage,
            nextMessage,
            isAfterDateSeparator = false,
            isBeforeDateSeparator = true,
            isInThread = false,
        )

        result `should be equal to` listOf(MessagePosition.BOTTOM)
    }
}
