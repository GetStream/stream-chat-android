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

package io.getstream.chat.android.client.internal.state.facade

import io.getstream.chat.android.randomChannel
import io.getstream.chat.android.randomMessage
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.test.runTest
import org.junit.jupiter.api.Test
import org.mockito.kotlin.any
import org.mockito.kotlin.doReturn
import org.mockito.kotlin.eq
import org.mockito.kotlin.never
import org.mockito.kotlin.verify
import org.mockito.kotlin.whenever
import java.util.Date

@ExperimentalCoroutinesApi
internal class WhenUpdateLastMessage : BaseRepositoryFacadeTest() {

    @Test
    fun `Given no channel in DB Should not do insert`() = runTest {
        whenever(channels.selectChannel(eq("cid"))) doReturn null

        sut.updateLastMessageForChannel("cid", randomMessage())

        verify(channels, never()).insertChannel(any())
    }

    @Test
    fun `Given channel with actual lastMessage in DB Should not insert any channel`() = runTest {
        val before = Date(1000)
        val after = Date(2000)
        val outdatedMessage = randomMessage(id = "messageId1", createdAt = before)
        val newLastMessage = randomMessage(id = "messageId2", createdAt = after)
        val channel = randomChannel(messages = listOf(newLastMessage))
        whenever(channels.selectChannel(eq("cid"))) doReturn channel

        sut.updateLastMessageForChannel("cid", outdatedMessage)

        verify(channels, never()).insertChannel(any())
    }
}
