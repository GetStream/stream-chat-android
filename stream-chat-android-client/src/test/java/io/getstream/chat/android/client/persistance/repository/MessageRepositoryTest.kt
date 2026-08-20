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

package io.getstream.chat.android.client.persistance.repository

import io.getstream.chat.android.models.DraftMessage
import io.getstream.chat.android.randomCID
import io.getstream.chat.android.randomDraftMessage
import io.getstream.chat.android.randomString
import kotlinx.coroutines.test.runTest
import org.junit.jupiter.api.Test
import org.mockito.kotlin.any
import org.mockito.kotlin.anyOrNull
import org.mockito.kotlin.doCallRealMethod
import org.mockito.kotlin.doReturn
import org.mockito.kotlin.mock
import org.mockito.kotlin.never
import org.mockito.kotlin.verify
import org.mockito.kotlin.whenever

internal class MessageRepositoryTest {

    @Test
    fun `the default deleteDraftMessage should look up the channel draft and delete it`() = runTest {
        val cid = randomCID()
        val draftMessage = randomDraftMessage(cid = cid, parentId = null)
        val repository = repositoryWithDefaultDelete()
        whenever(repository.selectDraftMessagesByCid(cid)) doReturn draftMessage

        repository.deleteDraftMessage(cid = cid, parentId = null)

        verify(repository).deleteDraftMessage(draftMessage)
    }

    @Test
    fun `the default deleteDraftMessage should look up the thread draft and delete it`() = runTest {
        val parentId = randomString()
        val draftMessage = randomDraftMessage(parentId = parentId)
        val repository = repositoryWithDefaultDelete()
        whenever(repository.selectDraftMessageByParentId(parentId)) doReturn draftMessage

        repository.deleteDraftMessage(cid = draftMessage.cid, parentId = parentId)

        verify(repository).deleteDraftMessage(draftMessage)
    }

    @Test
    fun `the default deleteDraftMessage should do nothing when there is no stored draft`() = runTest {
        val cid = randomCID()
        val repository = repositoryWithDefaultDelete()
        whenever(repository.selectDraftMessagesByCid(cid)) doReturn null

        repository.deleteDraftMessage(cid = cid, parentId = null)

        verify(repository, never()).deleteDraftMessage(any<DraftMessage>())
    }

    private suspend fun repositoryWithDefaultDelete(): MessageRepository = mock<MessageRepository>().also {
        doCallRealMethod().whenever(it).deleteDraftMessage(any<String>(), anyOrNull())
    }
}
