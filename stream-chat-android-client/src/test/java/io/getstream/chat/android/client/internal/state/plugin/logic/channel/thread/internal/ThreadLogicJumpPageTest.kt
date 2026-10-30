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

package io.getstream.chat.android.client.internal.state.plugin.logic.channel.thread.internal

import io.getstream.chat.android.client.internal.state.plugin.state.channel.thread.internal.ThreadMutableState
import io.getstream.chat.android.client.test.randomMessageDeletedEvent
import io.getstream.chat.android.client.test.randomMessageUpdateEvent
import io.getstream.chat.android.client.test.randomNewMessageEvent
import io.getstream.chat.android.randomMessage
import io.getstream.chat.android.randomString
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.test.TestScope
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.mockito.kotlin.any
import org.mockito.kotlin.doReturn
import org.mockito.kotlin.mock
import org.mockito.kotlin.never
import org.mockito.kotlin.times
import org.mockito.kotlin.verify
import org.mockito.kotlin.whenever

internal class ThreadLogicJumpPageTest {

    private lateinit var threadMutableState: ThreadMutableState
    private lateinit var threadStateLogic: ThreadStateLogic
    private lateinit var threadLogic: ThreadLogic

    @BeforeEach
    fun setUp() {
        threadMutableState = mock()
        threadStateLogic = mock()
        whenever(threadStateLogic.writeThreadState()).doReturn(threadMutableState)
        whenever(threadMutableState.heldReplies).doReturn(emptyMap())
        threadLogic = ThreadLogic(threadStateLogic, TestScope())
    }

    @Test
    fun `Given a jump page When a reply that is not loaded is updated Should not add it but update its quotes`() {
        val reply = randomMessage(replyMessageId = null, poll = null, ownReactions = emptyList())
        givenJumpPage(endOfNewerMessages = true)
        whenever(threadMutableState.rawMessage).doReturn(MutableStateFlow(emptyMap()))
        whenever(threadMutableState.messages).doReturn(MutableStateFlow(emptyList()))

        threadLogic.handleMessageEvents(listOf(randomMessageUpdateEvent(message = reply)))

        verify(threadStateLogic).upsertMessages(emptyList())
        verify(threadStateLogic).updateQuotedMessageReferences(reply.copy(replyTo = null))
        verify(threadMutableState, never()).holdReply(any())
    }

    @Test
    fun `Given a jump page When the parent that is not loaded is updated Should add it`() {
        val parentId = randomString()
        val parent = randomMessage(id = parentId, replyMessageId = null, poll = null, ownReactions = emptyList())
        givenJumpPage(endOfNewerMessages = false)
        whenever(threadMutableState.parentId).doReturn(parentId)
        whenever(threadMutableState.rawMessage).doReturn(MutableStateFlow(emptyMap()))
        whenever(threadMutableState.messages).doReturn(MutableStateFlow(emptyList()))

        threadLogic.handleMessageEvents(listOf(randomMessageUpdateEvent(message = parent)))

        verify(threadStateLogic).upsertMessages(listOf(parent.copy(replyTo = null)))
    }

    @Test
    fun `Given a jump page without the newest replies When a new reply arrives Should hold it`() {
        val reply = randomMessage(ownReactions = emptyList())
        givenJumpPage(endOfNewerMessages = false)
        whenever(threadMutableState.rawMessage).doReturn(MutableStateFlow(emptyMap()))

        threadLogic.handleMessageEvents(listOf(randomNewMessageEvent(message = reply)))

        verify(threadStateLogic).upsertMessages(emptyList())
        verify(threadMutableState).holdReply(reply)
    }

    @Test
    fun `Given a jump page that reaches the newest replies When a new reply arrives Should add it`() {
        val reply = randomMessage(ownReactions = emptyList())
        givenJumpPage(endOfNewerMessages = true)
        whenever(threadMutableState.rawMessage).doReturn(MutableStateFlow(emptyMap()))

        threadLogic.handleMessageEvents(listOf(randomNewMessageEvent(message = reply)))

        verify(threadStateLogic).upsertMessages(listOf(reply))
    }

    @Test
    fun `Given a jump page without the newest replies When a loaded reply is updated Should update it`() {
        val reply = randomMessage(replyMessageId = null, poll = null, ownReactions = emptyList())
        givenJumpPage(endOfNewerMessages = false)
        whenever(threadMutableState.rawMessage).doReturn(MutableStateFlow(mapOf(reply.id to reply)))
        whenever(threadMutableState.messages).doReturn(MutableStateFlow(listOf(reply)))

        threadLogic.handleMessageEvents(listOf(randomMessageUpdateEvent(message = reply)))

        verify(threadStateLogic).upsertMessages(listOf(reply.copy(replyTo = null)))
    }

    @Test
    fun `Given a jump page When a batch adds a reply and then updates it Should apply both`() {
        val reply = randomMessage(replyMessageId = null, poll = null, ownReactions = emptyList())
        givenJumpPage(endOfNewerMessages = true)
        whenever(threadMutableState.rawMessage).doReturn(MutableStateFlow(emptyMap()))
        whenever(threadMutableState.messages).doReturn(MutableStateFlow(emptyList()))

        threadLogic.handleMessageEvents(
            listOf(randomNewMessageEvent(message = reply), randomMessageUpdateEvent(message = reply)),
        )

        verify(threadStateLogic).upsertMessages(listOf(reply, reply.copy(replyTo = null)))
    }

    @Test
    fun `Given a held reply When it is updated Should update the held copy`() {
        val reply = randomMessage(replyMessageId = null, poll = null, ownReactions = emptyList())
        givenJumpPage(endOfNewerMessages = false)
        whenever(threadMutableState.heldReplies).doReturn(mapOf(reply.id to reply))
        whenever(threadMutableState.rawMessage).doReturn(MutableStateFlow(emptyMap()))
        whenever(threadMutableState.messages).doReturn(MutableStateFlow(emptyList()))

        threadLogic.handleMessageEvents(listOf(randomMessageUpdateEvent(message = reply)))

        verify(threadMutableState).holdReply(reply.copy(replyTo = null))
    }

    @Test
    fun `Given a held reply When it is hard deleted Should drop it`() {
        val reply = randomMessage(ownReactions = emptyList())
        givenJumpPage(endOfNewerMessages = false)
        whenever(threadMutableState.heldReplies).doReturn(mapOf(reply.id to reply))
        whenever(threadMutableState.rawMessage).doReturn(MutableStateFlow(emptyMap()))

        threadLogic.handleMessageEvents(listOf(randomMessageDeletedEvent(message = reply, hardDelete = true)))

        verify(threadMutableState).removeHeldReply(reply.id)
        verify(threadMutableState, never()).holdReply(any())
    }

    @Test
    fun `Given a jump page When the newest replies are reached Should release the held replies`() {
        givenJumpPage(endOfNewerMessages = false)

        threadLogic.setEndOfNewerMessages(true)

        verify(threadStateLogic).releaseHeldReplies()
    }

    @Test
    fun `Given a jump page When the newest replies are no longer reached Should keep holding replies`() {
        givenJumpPage(endOfNewerMessages = true)

        threadLogic.setEndOfNewerMessages(false)

        verify(threadStateLogic, never()).releaseHeldReplies()
    }

    @Test
    fun `Given the newest replies are reached while a new reply is held Should release it`() {
        val reply = randomMessage(ownReactions = emptyList())
        whenever(threadMutableState.isJumpPage).doReturn(true)
        whenever(threadMutableState.endOfNewerMessages).doReturn(MutableStateFlow(false), MutableStateFlow(true))
        whenever(threadMutableState.rawMessage).doReturn(MutableStateFlow(emptyMap()))

        threadLogic.handleMessageEvents(listOf(randomNewMessageEvent(message = reply)))

        verify(threadMutableState).holdReply(reply)
        verify(threadStateLogic).releaseHeldReplies()
    }

    private fun givenJumpPage(endOfNewerMessages: Boolean) {
        whenever(threadMutableState.isJumpPage).doReturn(true)
        whenever(threadMutableState.endOfNewerMessages).doReturn(MutableStateFlow(endOfNewerMessages))
    }
}
