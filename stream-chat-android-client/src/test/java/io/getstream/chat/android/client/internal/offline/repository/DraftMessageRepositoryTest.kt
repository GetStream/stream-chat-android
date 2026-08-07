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

import io.getstream.chat.android.client.internal.offline.repository.domain.message.internal.DatabaseMessageRepository
import io.getstream.chat.android.client.internal.offline.repository.domain.message.internal.MessageDao
import io.getstream.chat.android.client.persistance.repository.MessageRepository
import io.getstream.chat.android.randomCID
import io.getstream.chat.android.randomDraftMessage
import io.getstream.chat.android.randomString
import io.getstream.chat.android.randomUser
import io.getstream.chat.android.test.TestCoroutineRule
import kotlinx.coroutines.test.runTest
import org.junit.Before
import org.junit.Rule
import org.junit.Test
import org.mockito.kotlin.any
import org.mockito.kotlin.mock
import org.mockito.kotlin.never
import org.mockito.kotlin.verify

internal class DraftMessageRepositoryTest {

    @get:Rule
    val testCoroutines: TestCoroutineRule = TestCoroutineRule()

    private lateinit var messageDao: MessageDao
    private lateinit var sut: MessageRepository

    @Before
    fun before() {
        messageDao = mock()
        sut = DatabaseMessageRepository(
            scope = testCoroutines.scope,
            messageDao = messageDao,
            replyMessageDao = mock(),
            pollDao = mock(),
            getUser = { randomUser() },
            currentUser = randomUser(),
            ignoredChannelTypes = emptySet(),
        )
    }

    @Test
    fun `When deleting a draft message Should delete it by its id`() = runTest {
        val draftMessage = randomDraftMessage()

        sut.deleteDraftMessage(draftMessage)

        verify(messageDao).deleteDraftMessage(draftMessage.id)
    }

    @Test
    fun `When deleting a channel draft message Should delete it by cid`() = runTest {
        val cid = randomCID()

        sut.deleteDraftMessage(cid = cid, parentId = null)

        verify(messageDao).deleteDraftMessageByCid(cid)
        verify(messageDao, never()).deleteDraftMessageByParentId(any())
    }

    @Test
    fun `When deleting a thread draft message Should delete it by parent id`() = runTest {
        val cid = randomCID()
        val parentId = randomString()

        sut.deleteDraftMessage(cid = cid, parentId = parentId)

        verify(messageDao).deleteDraftMessageByParentId(parentId)
        verify(messageDao, never()).deleteDraftMessageByCid(any())
    }

    @Test
    fun `When inserting a draft message Should store it in the database`() = runTest {
        val draftMessage = randomDraftMessage()

        sut.insertDraftMessage(draftMessage)

        verify(messageDao).insertDraftMessages(any())
    }
}
