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

package io.getstream.chat.android.client.internal.offline.repository.domain.channel.internal

import io.getstream.chat.android.client.ChatClient
import io.getstream.chat.android.client.MockChatClientBuilder
import io.getstream.chat.android.randomChannel
import io.getstream.chat.android.randomChannelUserRead
import io.getstream.chat.android.randomConfig
import io.getstream.chat.android.randomDraftMessageOrNull
import io.getstream.chat.android.randomMessage
import io.getstream.chat.android.randomUser
import io.getstream.chat.android.test.TestCoroutineRule
import kotlinx.coroutines.test.runTest
import org.junit.Rule
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.mockito.Mockito
import org.mockito.kotlin.any
import org.mockito.kotlin.argumentCaptor
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
    private val chatClient: ChatClient = MockChatClientBuilder {
        Mockito.mock<ChatClient>().also {
            whenever(it.getCurrentUser()) doReturn randomUser()
        }
    }.build()

    private val channelDao: ChannelDao = mock()
    private val channelRepository: DatabaseChannelRepository =
        DatabaseChannelRepository(
            testCoroutines.scope,
            channelDao,
            { randomUser() },
            { randomMessage() },
            { randomDraftMessageOrNull() },
        )

    @BeforeEach
    fun setup() {
        reset(channelDao)
    }

    @Test
    fun `Given channel with recent lastMessage in DB, Should NOT insert channel`() = runTest {
        reset(channelDao)

        val before = Date(1000)
        val after = Date(2000)
        val outdatedMessage = randomMessage(id = "messageId1", createdAt = before)
        val newLastMessage = randomMessage(id = "messageId2", createdAt = after)
        val channel = randomChannel(messages = listOf(newLastMessage))
        whenever(channelDao.select(cid = "cid")) doReturn channel.toEntity()

        channelRepository.updateLastMessageForChannel("cid", outdatedMessage)

        verify(channelDao, never()).insert(any())
    }

    @Test
    fun `insertChannels preserves a newer stored read for a read-events-disabled channel`() = runTest {
        val repo = repositoryWithUserEchoingIds()
        val cid = "messaging:local"
        // Stored on disk: a locally-tracked read with a recent event date and a non-zero count.
        val stored = randomChannel(
            id = "local",
            type = "messaging",
            config = randomConfig(readEventsEnabled = false),
            read = listOf(
                randomChannelUserRead(user = currentUser, unreadMessages = 5, lastReceivedEventDate = Date(2000)),
            ),
        )
        whenever(channelDao.select(cid)) doReturn stored.toEntity()
        // Server sends a stale read with count 0.
        val serverPayload = randomChannel(
            id = "local",
            type = "messaging",
            config = randomConfig(readEventsEnabled = false),
            read = listOf(
                randomChannelUserRead(user = currentUser, unreadMessages = 0, lastReceivedEventDate = Date(1000)),
            ),
        )

        repo.insertChannels(listOf(serverPayload))

        assertEquals(5, persistedUnreadCount(cid))
    }

    @Test
    fun `insertChannels lets the server read win for a read-events-enabled channel`() = runTest {
        val repo = repositoryWithUserEchoingIds()
        val cid = "messaging:regular"
        val stored = randomChannel(
            id = "regular",
            type = "messaging",
            config = randomConfig(readEventsEnabled = true),
            read = listOf(
                randomChannelUserRead(user = currentUser, unreadMessages = 5, lastReceivedEventDate = Date(2000)),
            ),
        )
        whenever(channelDao.select(cid)) doReturn stored.toEntity()
        val serverPayload = randomChannel(
            id = "regular",
            type = "messaging",
            config = randomConfig(readEventsEnabled = true),
            read = listOf(
                randomChannelUserRead(user = currentUser, unreadMessages = 0, lastReceivedEventDate = Date(1000)),
            ),
        )

        repo.insertChannels(listOf(serverPayload))

        assertEquals(0, persistedUnreadCount(cid))
    }

    private val currentUser = randomUser(id = "current-user")

    private fun repositoryWithUserEchoingIds(): DatabaseChannelRepository =
        DatabaseChannelRepository(
            testCoroutines.scope,
            channelDao,
            { userId -> randomUser(id = userId) },
            { randomMessage() },
            { randomDraftMessageOrNull() },
        )

    /** Returns the current user's unread count from the entity written to the DAO for [cid]. */
    private suspend fun persistedUnreadCount(cid: String): Int? {
        val captor = argumentCaptor<List<ChannelEntity>>()
        verify(channelDao).insertMany(captor.capture())
        return captor.allValues.flatten()
            .firstOrNull { it.cid == cid }
            ?.reads?.get(currentUser.id)?.unreadMessages
    }
}
