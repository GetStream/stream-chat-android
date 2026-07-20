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
import io.getstream.chat.android.models.Message
import io.getstream.chat.android.randomMessage
import io.getstream.chat.android.randomString
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.test.runTest
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertNull
import org.junit.jupiter.api.Test
import java.util.Date

internal class ThreadStateLogicTest {

    private val parentId = randomString()

    @Test
    fun `updateQuotedMessageReferences should update messages quoting via replyTo`() = runTest {
        // given
        val (mutableState, threadStateLogic) = threadStateLogic(backgroundScope)
        val quotedMessage = quotedMessage()
        val quotingMessage = quotingMessage(replyTo = quotedMessage, replyMessageId = null)
        threadStateLogic.upsertMessages(listOf(quotedMessage, quotingMessage))
        // when
        val deletedQuotedMessage = quotedMessage.copy(deletedAt = Date())
        threadStateLogic.updateQuotedMessageReferences(deletedQuotedMessage)
        // then
        assertEquals(deletedQuotedMessage, mutableState.rawMessage.value[quotingMessage.id]?.replyTo)
    }

    @Test
    fun `updateQuotedMessageReferences should update messages quoting via replyMessageId`() = runTest {
        // given
        val (mutableState, threadStateLogic) = threadStateLogic(backgroundScope)
        val quotedMessage = quotedMessage()
        val quotingMessage = quotingMessage(replyTo = null, replyMessageId = quotedMessage.id)
        threadStateLogic.upsertMessages(listOf(quotedMessage, quotingMessage))
        // when
        val updatedQuotedMessage = quotedMessage.copy(text = "Updated text")
        threadStateLogic.updateQuotedMessageReferences(updatedQuotedMessage)
        // then
        assertEquals(updatedQuotedMessage, mutableState.rawMessage.value[quotingMessage.id]?.replyTo)
    }

    @Test
    fun `updateQuotedMessageReferences should not touch messages that do not quote the message`() = runTest {
        // given
        val (mutableState, threadStateLogic) = threadStateLogic(backgroundScope)
        val quotedMessage = quotedMessage()
        val otherMessage = quotingMessage(replyTo = null, replyMessageId = null)
        threadStateLogic.upsertMessages(listOf(quotedMessage, otherMessage))
        // when
        threadStateLogic.updateQuotedMessageReferences(quotedMessage.copy(text = "Updated text"))
        // then
        assertEquals(otherMessage, mutableState.rawMessage.value[otherMessage.id])
    }

    @Test
    fun `deleteQuotedMessageReferences should clear replyTo of quoting messages`() = runTest {
        // given
        val (mutableState, threadStateLogic) = threadStateLogic(backgroundScope)
        val quotedMessage = quotedMessage()
        val quotingMessage = quotingMessage(replyTo = quotedMessage, replyMessageId = quotedMessage.id)
        threadStateLogic.upsertMessages(listOf(quotedMessage, quotingMessage))
        // when
        threadStateLogic.deleteQuotedMessageReferences(quotedMessage.id)
        // then
        assertNull(mutableState.rawMessage.value[quotingMessage.id]?.replyTo)
    }

    private fun threadStateLogic(scope: CoroutineScope): Pair<ThreadMutableState, ThreadStateLogic> {
        val mutableState = ThreadMutableState(parentId, scope)
        return mutableState to ThreadStateLogic(mutableState)
    }

    private fun quotedMessage(): Message = randomMessage(
        id = parentId,
        parentId = null,
        replyTo = null,
        replyMessageId = null,
        deletedAt = null,
        deletedForMe = false,
    )

    private fun quotingMessage(replyTo: Message?, replyMessageId: String?): Message = randomMessage(
        parentId = parentId,
        replyTo = replyTo,
        replyMessageId = replyMessageId,
        deletedAt = null,
        deletedForMe = false,
    )
}
