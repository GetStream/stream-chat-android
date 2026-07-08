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

package io.getstream.chat.android.models

import io.getstream.chat.android.randomAttachment
import io.getstream.chat.android.randomBoolean
import io.getstream.chat.android.randomCID
import io.getstream.chat.android.randomDraftMessage
import io.getstream.chat.android.randomMessage
import io.getstream.chat.android.randomString
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertNotEquals
import org.junit.jupiter.api.Assertions.assertNull
import org.junit.jupiter.api.Assertions.assertTrue
import org.junit.jupiter.api.Test

internal class DraftMessageTest {

    @Test
    fun `builder should set every field`() {
        val expected = DraftMessage(
            id = randomString(),
            cid = randomCID(),
            text = randomString(),
            parentId = randomString(),
            attachments = listOf(randomAttachment()),
            mentionedUsersIds = listOf(randomString()),
            extraData = mapOf(randomString() to randomString()),
            silent = randomBoolean(),
            showInChannel = randomBoolean(),
            replyMessage = randomMessage(),
            command = randomString(),
            args = randomString(),
        )

        val built = DraftMessage.Builder()
            .withId(expected.id)
            .withCid(expected.cid)
            .withText(expected.text)
            .withParentId(expected.parentId)
            .withAttachments(expected.attachments)
            .withMentionedUsersIds(expected.mentionedUsersIds)
            .withExtraData(expected.extraData)
            .withSilent(expected.silent)
            .withShowInChannel(expected.showInChannel)
            .withReplyMessage(expected.replyMessage)
            .withCommand(expected.command)
            .withArgs(expected.args)
            .build()

        assertEquals(expected, built)
    }

    @Test
    fun `builder copy constructor should copy every field`() {
        val draft = randomDraftMessage(
            attachments = listOf(randomAttachment()),
            extraData = mapOf(randomString() to randomString()),
            command = randomString(),
            args = randomString(),
        )

        val built = DraftMessage.Builder(draft).build()

        assertEquals(draft, built)
    }

    @Test
    fun `identifierHash should be based on the message id when there is no parent`() {
        val draft = randomDraftMessage(id = "draft1", parentId = null)
        assertEquals("draft1".hashCode().toLong(), draft.identifierHash())
    }

    @Test
    fun `identifierHash should change when the parent id changes`() {
        val draft = randomDraftMessage(id = "draft1", parentId = null)
        val threadReply1 = draft.copy(parentId = "parent1")
        val threadReply2 = draft.copy(parentId = "parent2")
        assertNotEquals(draft.identifierHash(), threadReply1.identifierHash())
        assertNotEquals(threadReply1.identifierHash(), threadReply2.identifierHash())
    }

    @Test
    fun `getComparableField should return direct fields`() {
        val draft = randomDraftMessage()
        assertEquals(draft.id, draft.getComparableField("id"))
        assertEquals(draft.cid, draft.getComparableField("cid"))
        assertEquals(draft.text, draft.getComparableField("text"))
        assertEquals(draft.parentId, draft.getComparableField("parent_id"))
        assertEquals(draft.parentId, draft.getComparableField("parentId"))
        assertEquals(draft.silent, draft.getComparableField("silent"))
    }

    @Test
    fun `getComparableField should return extraData value for custom field`() {
        val draft = randomDraftMessage(extraData = mapOf("customField" to "customValue"))
        assertEquals("customValue", draft.getComparableField("customField"))
    }

    @Test
    fun `getComparableField should return null for unknown field`() {
        val draft = randomDraftMessage(extraData = emptyMap())
        assertNull(draft.getComparableField("unknownField"))
    }

    @Test
    fun `toString should contain the core draft fields`() {
        val draft = randomDraftMessage(id = "draft1", text = "Hello", command = "giphy")
        val string = draft.toString()
        assertTrue(string.contains("id=\"draft1\""))
        assertTrue(string.contains("text=\"Hello\""))
        assertTrue(string.contains("command=\"giphy\""))
    }
}
