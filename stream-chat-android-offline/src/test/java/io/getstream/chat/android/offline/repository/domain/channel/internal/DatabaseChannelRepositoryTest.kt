/*
 * Copyright (c) 2014-2023 Stream.io Inc. All rights reserved.
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

package io.getstream.chat.android.offline.repository.domain.channel.internal

import io.getstream.chat.android.client.test.randomChannel
import io.getstream.chat.android.client.test.randomMessage
import io.getstream.chat.android.client.test.randomUser
import io.getstream.chat.android.models.Message
import io.getstream.chat.android.models.User
import io.getstream.chat.android.test.randomString
import kotlinx.coroutines.test.runTest
import org.junit.jupiter.api.Test
import org.mockito.kotlin.argThat
import org.mockito.kotlin.doReturn
import org.mockito.kotlin.mock
import org.mockito.kotlin.verify
import org.mockito.kotlin.whenever
import java.util.Date

internal class DatabaseChannelRepositoryTest {

    private val channelDao: ChannelDao = mock()
    private val getUser: suspend (userId: String) -> User = { randomUser() }
    private val getMessage: suspend (messageId: String) -> Message? = { randomMessage() }

    private val channelRepository = DatabaseChannelRepository(channelDao, getUser, getMessage)

    @Test
    fun `when upserting a channel with a newer lastMessageAt, it should be updated`() = runTest {
        val id = randomString()
        val type = randomString()

        val dbLastMessageId = randomString()
        // Old last message
        val dbLastMessageAt = Date(0)

        // New last message
        val backendMessage = randomMessage(createdAt = Date())

        val backendChannel =
            randomChannel(id = id, type = type, messages = listOf(backendMessage))
        val dbChannel = randomChannel(id = id, type = type)

        whenever(channelDao.select(dbChannel.cid)) doReturn dbChannel.toEntity(dbLastMessageId, dbLastMessageAt)

        channelRepository.upsertChannel(backendChannel)

        verify(channelDao).insert(
            argThat { channelEntity ->
                channelEntity.lastMessageId == backendMessage.id
            },
        )
    }

    @Test
    fun `when upserting a channel with a older lastMessageAt, lastMessageAt should NOT be updated`() = runTest {
        val id = randomString()
        val type = randomString()

        val dbLastMessageId = randomString()
        // New last message
        val dbLastMessageAt = Date()

        // Old last message
        val backendMessage = randomMessage(createdAt = Date(0))

        val backendChannel =
            randomChannel(id = id, type = type, messages = listOf(backendMessage))
        val dbChannel = randomChannel(id = id, type = type)

        whenever(channelDao.select(dbChannel.cid)) doReturn dbChannel.toEntity(dbLastMessageId, dbLastMessageAt)

        channelRepository.upsertChannel(backendChannel)

        verify(channelDao).insert(
            argThat { channelEntity ->
                channelEntity.lastMessageId == dbLastMessageId
            },
        )
    }

    @Test
    fun `when upserting a channel with a null lastMessageAt, lastMessageAt should NOT be updated`() = runTest {
        val id = randomString()
        val type = randomString()

        val dbLastMessageId = randomString()
        // New last message
        val dbLastMessageAt = Date()

        // Null createdAt last message
        val backendMessage = randomMessage(createdAt = null)

        val backendChannel =
            randomChannel(id = id, type = type, messages = listOf(backendMessage), lastMessageAt = null)
        val dbChannel = randomChannel(id = id, type = type)

        whenever(channelDao.select(dbChannel.cid)) doReturn dbChannel.toEntity(dbLastMessageId, dbLastMessageAt)

        channelRepository.upsertChannel(backendChannel)

        verify(channelDao).insert(
            argThat { channelEntity ->
                channelEntity.lastMessageId == dbLastMessageId
            },
        )
    }
}
