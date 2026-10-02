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

package io.getstream.chat.android.offline.repository.domain.channel.internal

import io.getstream.chat.android.client.extensions.syncUnreadCountWithReads
import io.getstream.chat.android.models.Location
import io.getstream.chat.android.offline.MockChatClientBuilder
import io.getstream.chat.android.randomChannel
import io.getstream.chat.android.randomLocation
import io.getstream.chat.android.randomMessage
import io.getstream.chat.android.randomUser
import kotlinx.coroutines.test.TestScope
import kotlinx.coroutines.test.advanceUntilIdle
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Test
import org.junit.runner.RunWith
import org.mockito.kotlin.any
import org.mockito.kotlin.doReturn
import org.mockito.kotlin.mock
import org.mockito.kotlin.times
import org.mockito.kotlin.verify
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config

// Robolectric provides a working android.util.LruCache, which backs the channel cache.
@RunWith(RobolectricTestRunner::class)
@Config(sdk = [33])
internal class DatabaseChannelRepositoryCacheTest {

    private val testScope = TestScope()

    init {
        // Merging with a cached channel syncs unread counts against the current user.
        MockChatClientBuilder().build()
    }
    private val channelDao: ChannelDao = mock {
        onBlocking { select(any<List<String>>()) } doReturn emptyList()
    }
    private val sut = DatabaseChannelRepository(
        testScope,
        channelDao,
        { randomUser(id = it) },
        { randomMessage(id = it) },
        { null },
    )

    @Test
    fun `selectChannel does not return the live locations of an inserted channel`() = testScope.runTest {
        val channel = randomChannel(activeLiveLocations = listOf(randomLocation()))

        sut.insertChannel(channel)

        assertEquals(emptyList<Location>(), sut.selectChannel(channel.cid)?.activeLiveLocations)
    }

    @Test
    fun `inserting an unchanged channel with live locations again does not write it again`() = testScope.runTest {
        // Shaped like a server channel (no messages, unread count synced), so merging with the cached copy keeps it equal.
        val channel = randomChannel(
            messages = emptyList(),
            read = emptyList(),
            activeLiveLocations = listOf(randomLocation()),
        ).syncUnreadCountWithReads()

        sut.insertChannel(channel)
        advanceUntilIdle()
        sut.insertChannel(channel)
        advanceUntilIdle()

        verify(channelDao, times(1)).insertMany(any())
    }

    @Test
    fun `selectChannels does not return the live locations of inserted channels`() = testScope.runTest {
        val channels = listOf(
            randomChannel(activeLiveLocations = listOf(randomLocation())),
            randomChannel(activeLiveLocations = listOf(randomLocation())),
        )

        sut.insertChannels(channels)

        assertEquals(
            listOf(emptyList<Location>(), emptyList()),
            sut.selectChannels(channels.map { it.cid }).map { it.activeLiveLocations },
        )
    }
}
