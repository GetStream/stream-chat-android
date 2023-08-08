/*
 * Copyright (c) 2014-2022 Stream.io Inc. All rights reserved.
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

@file:OptIn(ExperimentalCoroutinesApi::class)

package io.getstream.chat.android.offline.repository.domain.channel.internal

import io.getstream.chat.android.randomChannel
import io.getstream.chat.android.randomMessage
import io.getstream.chat.android.randomUser
import io.getstream.chat.android.test.TestCoroutineRule
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.test.runTest
import org.junit.Rule
import org.junit.jupiter.api.Test
import org.mockito.kotlin.any
import org.mockito.kotlin.argThat
import org.mockito.kotlin.doReturn
import org.mockito.kotlin.mock
import org.mockito.kotlin.never
import org.mockito.kotlin.reset
import org.mockito.kotlin.verify
import org.mockito.kotlin.whenever
import java.util.Date

internal class ChannelRepositoryImplTest {
    @get:Rule
    val testCoroutines: TestCoroutineRule = TestCoroutineRule()

    private val channelDao: ChannelDao = mock()
    private val channelRepository: DatabaseChannelRepository =
        DatabaseChannelRepository(
            channelDao,
            { randomUser() },
            { randomMessage() },
        )

    @Test
    fun `Given channel without messages in DB, Should insert channel with updated last message`() = runTest {
        val channel = randomChannel(messages = emptyList())
        val lastMessage = randomMessage(createdAt = Date())
        whenever(channelDao.select(listOf("cid"))) doReturn listOf(channel.toEntity(null, null))

        channelRepository.updateLastMessageForChannel("cid", lastMessage)

        verify(channelDao).insert(
            argThat { channelEntity ->
                channelEntity.lastMessageAt == lastMessage.createdAt &&
                    channelEntity.lastMessageId == lastMessage.id
            },
        )
    }

    @Test
    fun `Given channel with outdated lastMessage in DB, Should insert channel with updated last message`() = runTest {
        val before = Date(1000)
        val after = Date(2000)
        val outdatedMessage = randomMessage(id = "messageId1", createdAt = before)
        val newLastMessage = randomMessage(id = "messageId2", createdAt = after)
        val channel = randomChannel(messages = listOf(outdatedMessage), lastMessageAt = before)
        whenever(channelDao.select(cid = "cid")) doReturn channel.toEntity(null, null)

        channelRepository.updateLastMessageForChannel("cid", newLastMessage)

        verify(channelDao).insert(
            argThat { channelEntity ->
                channelEntity.lastMessageAt == after &&
                    channelEntity.lastMessageId == newLastMessage.id
            },
        )
    }

    @Test
    fun `Given channel with recent lastMessage in DB, Should NOT insert channel`() = runTest {
        reset(channelDao)

        val before = Date(1000)
        val after = Date(2000)
        val outdatedMessage = randomMessage(id = "messageId1", createdAt = before)
        val newLastMessage = randomMessage(id = "messageId2", createdAt = after)
        val channel = randomChannel(messages = listOf(newLastMessage), lastMessageAt = after)
        whenever(channelDao.select(cid = "cid")) doReturn channel.toEntity(newLastMessage.id, newLastMessage.createdAt)

        channelRepository.updateLastMessageForChannel("cid", outdatedMessage)

        verify(channelDao, never()).insert(any())
    }
}
