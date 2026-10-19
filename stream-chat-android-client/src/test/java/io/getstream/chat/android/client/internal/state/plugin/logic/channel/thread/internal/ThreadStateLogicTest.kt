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
import io.getstream.chat.android.models.MemberInfo
import io.getstream.chat.android.models.Message
import io.getstream.chat.android.models.SyncStatus
import io.getstream.chat.android.models.User
import io.getstream.chat.android.randomCID
import io.getstream.chat.android.randomMessage
import io.getstream.chat.android.randomString
import io.getstream.chat.android.randomUser
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.test.runTest
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertFalse
import org.junit.jupiter.api.Assertions.assertNull
import org.junit.jupiter.api.Assertions.assertTrue
import org.junit.jupiter.api.Test
import java.util.Date

internal class ThreadStateLogicTest {

    private val parentId = randomString()
    private val cid = randomCID()
    private val author = randomUser()

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

    @Test
    fun `updateMessagesMemberInfo should refresh the member snapshot on the author's thread replies`() = runTest {
        // given - thread replies live in this state, so the channel refresh does not reach them
        val (mutableState, threadStateLogic) = threadStateLogic(backgroundScope)
        val reply = threadReply(user = author)
        threadStateLogic.upsertMessages(listOf(reply))
        val memberInfo = MemberInfo(channelRole = "channel_moderator", extraData = mapOf("flair" to "gold"))
        // when
        threadStateLogic.updateMessagesMemberInfo(cid, author.id, memberInfo)
        // then
        assertEquals(memberInfo, mutableState.rawMessage.value[reply.id]?.member)
    }

    @Test
    fun `updateMessagesMemberInfo should leave replies of other authors and other channels alone`() = runTest {
        // given
        val (mutableState, threadStateLogic) = threadStateLogic(backgroundScope)
        val otherAuthor = threadReply(user = randomUser())
        val otherChannel = threadReply(user = author, cid = randomCID())
        threadStateLogic.upsertMessages(listOf(otherAuthor, otherChannel))
        // when
        threadStateLogic.updateMessagesMemberInfo(cid, author.id, MemberInfo(channelRole = "channel_moderator"))
        // then
        assertNull(mutableState.rawMessage.value[otherAuthor.id]?.member)
        assertNull(mutableState.rawMessage.value[otherChannel.id]?.member)
    }

    /** A reply that survives the thread state's deleted-message filtering. */
    private fun threadReply(user: User, cid: String = this.cid): Message = randomMessage(
        cid = cid,
        user = user,
        parentId = parentId,
        member = null,
        replyTo = null,
        poll = null,
        deletedAt = null,
        deletedForMe = false,
    )

    @Test
    fun `Given loaded replies When a jump page is set Should keep only the parent, the local-only replies and the page`() =
        runTest {
            val (mutableState, threadStateLogic) = threadStateLogic(backgroundScope)
            val parent = reply(id = parentId, createdAt = 1)
            val loaded = reply(createdAt = 50)
            val pending = reply(createdAt = 60, syncStatus = SyncStatus.SYNC_NEEDED)
            val page = listOf(reply(createdAt = 10), reply(createdAt = 11))
            threadStateLogic.upsertMessages(listOf(parent, loaded, pending))

            threadStateLogic.setReplies(page, isJumpPage = true)

            assertEquals((listOf(parent, pending) + page).toSet(), mutableState.rawMessage.value.values.toSet())
            assertTrue(mutableState.isJumpPage)
        }

    @Test
    fun `Given a reply deleted offline When a page with its server copy is set Should keep it deleted`() = runTest {
        val (mutableState, threadStateLogic) = threadStateLogic(backgroundScope)
        val serverCopy = reply(createdAt = 10)
        val deletedOffline = serverCopy.copy(deletedAt = Date(20), syncStatus = SyncStatus.SYNC_NEEDED)
        threadStateLogic.upsertMessages(listOf(deletedOffline))

        threadStateLogic.setReplies(listOf(serverCopy), isJumpPage = true)

        assertEquals(deletedOffline, mutableState.rawMessage.value[serverCopy.id])
    }

    @Test
    fun `Given a loaded reply newer than the page copy When the page is set Should keep the loaded copy`() = runTest {
        val (mutableState, threadStateLogic) = threadStateLogic(backgroundScope)
        val pageCopy = reply(createdAt = 10)
        val edited = pageCopy.copy(text = "Edited", updatedAt = Date(30))
        threadStateLogic.upsertMessages(listOf(edited))

        threadStateLogic.setReplies(listOf(pageCopy), isJumpPage = true)

        assertEquals(edited, mutableState.rawMessage.value[pageCopy.id])
    }

    @Test
    fun `Given replies newer than the newest page When the newest page is set Should keep them`() = runTest {
        val (mutableState, threadStateLogic) = threadStateLogic(backgroundScope)
        val olderLoaded = reply(createdAt = 5)
        val sentDuringLoad = reply(createdAt = 40)
        val page = listOf(reply(createdAt = 20), reply(createdAt = 30))
        threadStateLogic.upsertMessages(listOf(olderLoaded, sentDuringLoad))
        mutableState.isJumpPage = true

        threadStateLogic.setReplies(page, isJumpPage = false)

        assertEquals(page + sentDuringLoad, mutableState.rawMessage.value.values.sortedBy { it.createdAt })
        assertFalse(mutableState.isJumpPage)
    }

    @Test
    fun `Given replies newer than a jump page When the jump page is set Should not keep them`() = runTest {
        val (mutableState, threadStateLogic) = threadStateLogic(backgroundScope)
        val newer = reply(createdAt = 40)
        val held = reply(createdAt = 50)
        val page = listOf(reply(createdAt = 20))
        threadStateLogic.upsertMessages(listOf(newer))
        mutableState.holdReply(held)

        threadStateLogic.setReplies(page, isJumpPage = true)

        assertEquals(page, mutableState.rawMessage.value.values.toList())
        assertEquals(mapOf(held.id to held), mutableState.heldReplies)
    }

    @Test
    fun `Given a jump page that reaches the newest replies When it is set Should keep the newer replies`() =
        runTest {
            val (mutableState, threadStateLogic) = threadStateLogic(backgroundScope)
            val newer = reply(createdAt = 40)
            val page = listOf(reply(createdAt = 20))
            threadStateLogic.upsertMessages(listOf(newer))
            mutableState.setEndOfNewerMessages(true)

            threadStateLogic.setReplies(page, isJumpPage = true)

            assertEquals(page + newer, mutableState.rawMessage.value.values.sortedBy { it.createdAt })
        }

    @Test
    fun `Given held replies When they are released Should add them and clear the held ones`() = runTest {
        val (mutableState, threadStateLogic) = threadStateLogic(backgroundScope)
        val loaded = reply(createdAt = 10)
        val held = reply(createdAt = 50)
        threadStateLogic.upsertMessages(listOf(loaded))
        mutableState.holdReply(held)

        threadStateLogic.releaseHeldReplies()

        assertEquals(listOf(loaded, held), mutableState.rawMessage.value.values.sortedBy { it.createdAt })
        assertEquals(emptyMap<String, Message>(), mutableState.heldReplies)
    }

    @Test
    fun `Given a jump page with held replies When it is left Should add the held replies and keep the loaded ones`() =
        runTest {
            val (mutableState, threadStateLogic) = threadStateLogic(backgroundScope)
            val loaded = reply(createdAt = 10)
            val held = reply(createdAt = 50)
            threadStateLogic.upsertMessages(listOf(loaded))
            mutableState.isJumpPage = true
            mutableState.holdReply(held)

            threadStateLogic.leaveJumpPage()

            assertEquals(listOf(loaded, held), mutableState.rawMessage.value.values.sortedBy { it.createdAt })
            assertFalse(mutableState.isJumpPage)
            assertEquals(emptyMap<String, Message>(), mutableState.heldReplies)
        }

    private fun reply(
        id: String = randomString(),
        createdAt: Long,
        syncStatus: SyncStatus = SyncStatus.COMPLETED,
    ): Message = randomMessage(
        id = id,
        parentId = parentId.takeUnless { id == parentId },
        type = "regular",
        createdAt = Date(createdAt),
        createdLocallyAt = null,
        updatedAt = null,
        updatedLocallyAt = null,
        deletedAt = null,
        deletedForMe = false,
        syncStatus = syncStatus,
    )

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
