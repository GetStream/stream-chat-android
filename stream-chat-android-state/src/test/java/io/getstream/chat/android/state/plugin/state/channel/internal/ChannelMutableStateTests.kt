/*
 * Copyright (c) 2014-2024 Stream.io Inc. All rights reserved.
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

package io.getstream.chat.android.state.plugin.state.channel.internal

import io.getstream.chat.android.models.User
import io.getstream.chat.android.randomMessage
import io.getstream.chat.android.randomString
import io.getstream.chat.android.test.TestCoroutineExtension
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.test.advanceTimeBy
import kotlinx.coroutines.test.runTest
import org.amshove.kluent.shouldBeEqualTo
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.extension.RegisterExtension
import java.util.Date
import kotlin.time.Duration.Companion.hours
import kotlin.time.Duration.Companion.minutes
import kotlin.time.Duration.Companion.seconds

@ExperimentalCoroutinesApi
internal class ChannelMutableStateTests {

    private val userFlow = MutableStateFlow(currentUser)

    private lateinit var channelState: ChannelMutableState

    @BeforeEach
    fun setUp() {
        channelState = ChannelMutableState(
            channelType = CHANNEL_TYPE,
            channelId = CHANNEL_ID,
            userFlow = userFlow,
            latestUsers = MutableStateFlow(
                mapOf(currentUser.id to currentUser),
            ),
            activeLiveLocations = MutableStateFlow(
                emptyList(),
            ),
            messagesLimitFilter = { it },
            now = ChannelMutableStateTests::currentTime,
        )
    }

    @Test
    fun `Test expired pinned messages`() = runTest {
        // given
        val now = currentTime()
        val alreadyExpired = randomMessage(
            cid = CID,
            parentId = null,
            createdLocallyAt = null,
            updatedLocallyAt = null,
            createdAt = Date(now),
            updatedAt = null,
            deletedAt = null,
            pinned = true,
            pinnedAt = Date(now),
            pinExpires = Date(now + 1.minutes.inWholeMilliseconds),
        )
        val expiresIn1h = alreadyExpired.copy(
            id = randomString(),
            pinExpires = Date(now + 1.hours.inWholeMilliseconds),
        )
        val pinnedMessages = listOf(alreadyExpired, expiresIn1h)

        // when
        advanceTimeBy(10.minutes)
        channelState.setPinnedMessages(pinnedMessages)
        advanceTimeBy(10.seconds)

        // then
        channelState.assertPinnedMessagesSizeEqualsTo(size = 1)

        // when
        advanceTimeBy(2.hours)

        // then
        channelState.assertPinnedMessagesSizeEqualsTo(size = 0)
    }

    @Test
    fun `When watchers get inserted, the watchers list should be updated`() = runTest {
        // given
        val watchers = listOf(currentUser, User(id = "bob", name = "Bob"))

        // when
        channelState.upsertWatchers(watchers, watchers.size)

        // then
        channelState.watcherCount.value shouldBeEqualTo watchers.size
        channelState.watchers.value.size shouldBeEqualTo watchers.size
        channelState.watchers.value shouldBeEqualTo watchers
    }

    @Test
    fun `When a watcher gets deleted, the watchers list should be updated`() = runTest {
        // given
        val anotherUser = User(id = "bob", name = "Bob")
        val watchers = listOf(currentUser, anotherUser)
        channelState.upsertWatchers(watchers, watchers.size)
        val newWatcherCount = watchers.size - 1

        // when
        channelState.deleteWatcher(anotherUser, newWatcherCount)

        // then
        channelState.watcherCount.value shouldBeEqualTo newWatcherCount
        channelState.watchers.value.size shouldBeEqualTo newWatcherCount
        channelState.watchers.value shouldBeEqualTo listOf(currentUser)
    }

    private fun ChannelMutableState.assertPinnedMessagesSizeEqualsTo(size: Int) {
        require(pinnedMessages.value.size == size) {
            "pinnedMessages should have $size items, but was ${pinnedMessages.value.size}"
        }
        require(visiblePinnedMessages.value.size == size) {
            "visiblePinnedMessages should have $size items, but was ${visiblePinnedMessages.value.size}"
        }
        require(sortedPinnedMessages.value.size == size) {
            "sortedPinnedMessages should have $size items, but was ${sortedPinnedMessages.value.size}"
        }
        require(pinnedMessagesList.value.size == size) {
            "pinnedMessagesList should have $size items, but was ${pinnedMessagesList.value.size}"
        }
        require(rawPinnedMessages.size == size) {
            "rawPinnedMessages should have $size items, but was ${rawPinnedMessages.size}"
        }
    }

    companion object {
        @JvmField
        @RegisterExtension
        val testCoroutines = TestCoroutineExtension()

        private const val CHANNEL_TYPE = "messaging"
        private const val CHANNEL_ID = "123"
        private const val CID = "messaging:123"

        private val currentUser = User(id = "tom", name = "Tom")

        private fun currentTime() = testCoroutines.dispatcher.scheduler.currentTime
    }
}
