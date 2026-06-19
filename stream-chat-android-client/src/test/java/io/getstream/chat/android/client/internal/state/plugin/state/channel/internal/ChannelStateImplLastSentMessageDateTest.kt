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

import io.getstream.chat.android.randomUser
import kotlinx.coroutines.test.runTest
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertNotNull
import org.junit.jupiter.api.Assertions.assertNull
import org.junit.jupiter.api.Assertions.assertTrue
import org.junit.jupiter.api.Test

internal class ChannelStateImplLastSentMessageDateTest : ChannelStateImplTestBase() {

    @Test
    fun `a thread-only reply by the current user updates lastSentMessageDate`() = runTest {
        val threadReply = createMessage(1, parentId = "parent1", showInChannel = false)

        channelState.upsertMessage(threadReply)

        // the reply is excluded from the visible message list, but still drives the cooldown
        assertTrue(channelState.messages.value.isEmpty())
        assertEquals(threadReply.createdAt, channelState.lastSentMessageDate.value)
    }

    @Test
    fun `a thread-only reply via upsertMessages updates lastSentMessageDate`() = runTest {
        val threadReply = createMessage(1, parentId = "parent1", showInChannel = false)

        channelState.upsertMessages(listOf(threadReply))

        assertEquals(threadReply.createdAt, channelState.lastSentMessageDate.value)
    }

    @Test
    fun `a thread-only reply from another user does not update lastSentMessageDate`() = runTest {
        val otherUserReply = createMessage(1, user = randomUser(), parentId = "parent1", showInChannel = false)

        channelState.upsertMessage(otherUserReply)

        assertNull(channelState.lastSentMessageDate.value)
    }

    @Test
    fun `a channel message by the current user still updates lastSentMessageDate`() = runTest {
        val channelMessage = createMessage(1)

        channelState.upsertMessage(channelMessage)

        assertEquals(channelMessage.createdAt, channelState.lastSentMessageDate.value)
    }

    @Test
    fun `lastSentMessageDate is the later of the channel message and the thread reply`() = runTest {
        val olderThreadReply = createMessage(1, parentId = "parent1", showInChannel = false)
        val newerChannelMessage = createMessage(5)

        channelState.upsertMessage(olderThreadReply)
        channelState.upsertMessage(newerChannelMessage)

        assertEquals(newerChannelMessage.createdAt, channelState.lastSentMessageDate.value)
    }

    @Test
    fun `a newer thread reply wins over an older channel message`() = runTest {
        val olderChannelMessage = createMessage(1)
        val newerThreadReply = createMessage(5, parentId = "parent1", showInChannel = false)

        channelState.upsertMessage(olderChannelMessage)
        channelState.upsertMessage(newerThreadReply)

        assertEquals(newerThreadReply.createdAt, channelState.lastSentMessageDate.value)
    }

    @Test
    fun `the thread reply date survives a later refresh that omits it`() = runTest {
        val threadReply = createMessage(5, parentId = "parent1", showInChannel = false)
        channelState.upsertMessage(threadReply)

        // a server refresh replaces the message list with channel messages only (no thread replies)
        channelState.setMessages(createMessages(count = 2, startIndex = 1))

        assertEquals(threadReply.createdAt, channelState.lastSentMessageDate.value)
    }

    @Test
    fun `destroy clears the thread-reply contribution to lastSentMessageDate`() = runTest {
        channelState.upsertMessage(createMessage(1, parentId = "parent1", showInChannel = false))
        assertNotNull(channelState.lastSentMessageDate.value)

        channelState.destroy()

        assertNull(channelState.lastSentMessageDate.value)
    }
}
