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

package io.getstream.chat.android.client.internal.offline.repository

import androidx.test.ext.junit.runners.AndroidJUnit4
import io.getstream.chat.android.client.internal.offline.integration.BaseDomainTest2
import io.getstream.chat.android.models.Attachment
import io.getstream.chat.android.randomCID
import io.getstream.chat.android.randomDraftMessage
import io.getstream.chat.android.randomString
import io.getstream.chat.android.randomUser
import kotlinx.coroutines.test.runTest
import org.amshove.kluent.shouldBeEqualTo
import org.amshove.kluent.shouldBeNull
import org.amshove.kluent.shouldContainSame
import org.junit.Test
import org.junit.runner.RunWith

@RunWith(AndroidJUnit4::class)
internal class DraftMessageRepositoryIntegrationTest : BaseDomainTest2() {

    @Test
    fun `inserting a channel draft and reading it back by cid should be equal`(): Unit = runTest {
        val draft = randomDraftMessage(parentId = null, replyMessage = null)

        repos.insertDraftMessage(draft)

        repos.selectDraftMessagesByCid(draft.cid) shouldBeEqualTo draft
    }

    @Test
    fun `inserting a draft with attachments and reading it back should keep the attachments`(): Unit = runTest {
        val attachments = listOf(
            Attachment(type = "image", name = randomString(), imageUrl = randomString()),
            Attachment(type = "file", name = randomString(), assetUrl = randomString(), extraData = mapOf("key" to "value")),
        )
        val draft = randomDraftMessage(parentId = null, replyMessage = null, attachments = attachments)

        repos.insertDraftMessage(draft)

        repos.selectDraftMessagesByCid(draft.cid) shouldBeEqualTo draft
    }

    @Test
    fun `inserting a draft with mentioned users and reading it back should keep the users`(): Unit = runTest {
        val draft = randomDraftMessage(parentId = null, replyMessage = null, mentionedUsers = listOf(randomUser()))

        repos.insertDraftMessage(draft)

        repos.selectDraftMessagesByCid(draft.cid) shouldBeEqualTo draft
    }

    @Test
    fun `inserting a channel draft with a new id should keep the thread drafts of the channel`(): Unit = runTest {
        val threadDraft = randomDraftMessage(parentId = randomString(), replyMessage = null)
        val older = randomDraftMessage(cid = threadDraft.cid, parentId = null, replyMessage = null)
        val newer = randomDraftMessage(cid = threadDraft.cid, parentId = null, replyMessage = null)
        repos.insertDraftMessage(threadDraft)
        repos.insertDraftMessage(older)

        repos.insertDraftMessage(newer)

        repos.selectDraftMessages() shouldContainSame listOf(threadDraft, newer)
    }

    @Test
    fun `inserting a channel draft with a new id should replace the older draft of the channel`(): Unit = runTest {
        val older = randomDraftMessage(parentId = null, replyMessage = null)
        val newer = randomDraftMessage(cid = older.cid, parentId = null, replyMessage = null)
        repos.insertDraftMessage(older)

        repos.insertDraftMessage(newer)

        repos.selectDraftMessages() shouldContainSame listOf(newer)
    }

    @Test
    fun `inserting a thread draft with a new id should replace only the older draft of the thread`(): Unit = runTest {
        val channelDraft = randomDraftMessage(parentId = null, replyMessage = null)
        val older = randomDraftMessage(cid = channelDraft.cid, parentId = randomString(), replyMessage = null)
        val newer = randomDraftMessage(cid = channelDraft.cid, parentId = older.parentId, replyMessage = null)
        repos.insertDraftMessage(channelDraft)
        repos.insertDraftMessage(older)

        repos.insertDraftMessage(newer)

        repos.selectDraftMessages() shouldContainSame listOf(channelDraft, newer)
    }

    @Test
    fun `inserting a thread draft and reading it back by parent id should be equal`(): Unit = runTest {
        val parentId = randomString()
        val draft = randomDraftMessage(parentId = parentId, replyMessage = null)

        repos.insertDraftMessage(draft)

        repos.selectDraftMessageByParentId(parentId) shouldBeEqualTo draft
    }

    @Test
    fun `deleting a draft by cid should remove it`(): Unit = runTest {
        val draft = randomDraftMessage(parentId = null, replyMessage = null)
        repos.insertDraftMessage(draft)

        repos.deleteDraftMessage(cid = draft.cid, parentId = null)

        repos.selectDraftMessagesByCid(draft.cid).shouldBeNull()
    }

    @Test
    fun `deleting a draft by parent id should remove it`(): Unit = runTest {
        val parentId = randomString()
        val draft = randomDraftMessage(parentId = parentId, replyMessage = null)
        repos.insertDraftMessage(draft)

        repos.deleteDraftMessage(cid = draft.cid, parentId = parentId)

        repos.selectDraftMessageByParentId(parentId).shouldBeNull()
    }

    @Test
    fun `deleting a channel draft by cid should leave thread drafts of the same channel untouched`(): Unit = runTest {
        val cid = randomCID()
        val parentId = randomString()
        val channelDraft = randomDraftMessage(cid = cid, parentId = null, replyMessage = null)
        val threadDraft = randomDraftMessage(cid = cid, parentId = parentId, replyMessage = null)
        repos.insertDraftMessage(channelDraft)
        repos.insertDraftMessage(threadDraft)

        repos.deleteDraftMessage(cid = cid, parentId = null)

        repos.selectDraftMessagesByCid(cid).shouldBeNull()
        repos.selectDraftMessageByParentId(parentId) shouldBeEqualTo threadDraft
    }

    @Test
    fun `deleting a thread draft should leave the channel draft of the same channel untouched`(): Unit = runTest {
        val cid = randomCID()
        val parentId = randomString()
        val channelDraft = randomDraftMessage(cid = cid, parentId = null, replyMessage = null)
        val threadDraft = randomDraftMessage(cid = cid, parentId = parentId, replyMessage = null)
        repos.insertDraftMessage(channelDraft)
        repos.insertDraftMessage(threadDraft)

        repos.deleteDraftMessage(cid = cid, parentId = parentId)

        repos.selectDraftMessageByParentId(parentId).shouldBeNull()
        repos.selectDraftMessagesByCid(cid) shouldBeEqualTo channelDraft
    }

    @Test
    fun `selecting all drafts should return every stored draft`(): Unit = runTest {
        val channelDraft = randomDraftMessage(parentId = null, replyMessage = null)
        val threadDraft = randomDraftMessage(parentId = randomString(), replyMessage = null)
        repos.insertDraftMessage(channelDraft)
        repos.insertDraftMessage(threadDraft)

        repos.selectDraftMessages() shouldContainSame listOf(channelDraft, threadDraft)
    }

    @Test
    fun `deleting a draft by its id should remove it`(): Unit = runTest {
        val draft = randomDraftMessage(parentId = null, replyMessage = null)
        repos.insertDraftMessage(draft)

        repos.deleteDraftMessage(draft)

        repos.selectDraftMessagesByCid(draft.cid).shouldBeNull()
    }
}
