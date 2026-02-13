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

package io.getstream.chat.android.client.internal.state.plugin.state.channel.internal

import io.getstream.chat.android.client.events.UserStartWatchingEvent
import io.getstream.chat.android.client.events.UserStopWatchingEvent
import io.getstream.chat.android.models.User
import io.getstream.chat.android.randomUser
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.test.runTest
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertFalse
import org.junit.jupiter.api.Assertions.assertTrue
import org.junit.jupiter.api.Nested
import org.junit.jupiter.api.Test
import java.util.Date

@ExperimentalCoroutinesApi
internal class ChannelStateImplWatchersTest : ChannelStateImplTestBase() {

    @Nested
    inner class SetWatchers {

        @Test
        fun `setWatchers should set the watchers and watcher count`() = runTest {
            // given
            val watchers = createWatchers(3)
            // when
            channelState.setWatchers(watchers, watcherCount = 3)
            // then
            assertEquals(3, channelState.watchers.value.size)
            assertEquals(3, channelState.watcherCount.value)
        }

        @Test
        fun `setWatchers should replace existing watchers`() = runTest {
            // given
            val initialWatchers = createWatchers(3)
            channelState.setWatchers(initialWatchers, watcherCount = 3)
            // when
            val newWatchers = createWatchers(5, startIndex = 10)
            channelState.setWatchers(newWatchers, watcherCount = 5)
            // then
            assertEquals(5, channelState.watchers.value.size)
            assertEquals(5, channelState.watcherCount.value)
            assertEquals(
                newWatchers.map { it.id }.toSet(),
                channelState.watchers.value.map { it.id }.toSet(),
            )
        }

        @Test
        fun `setWatchers with empty list should clear watchers`() = runTest {
            // given
            val watchers = createWatchers(3)
            channelState.setWatchers(watchers, watcherCount = 3)
            // when
            channelState.setWatchers(emptyList(), watcherCount = 0)
            // then
            assertTrue(channelState.watchers.value.isEmpty())
            assertEquals(0, channelState.watcherCount.value)
        }

        @Test
        fun `setWatchers should clamp negative watcher count to zero`() = runTest {
            // when
            channelState.setWatchers(emptyList(), watcherCount = -5)
            // then
            assertEquals(0, channelState.watcherCount.value)
        }

        @Test
        fun `setWatchers should allow watcher count larger than provided watchers list`() = runTest {
            // given - server returns partial watcher list but full count
            val watchers = createWatchers(2)
            // when
            channelState.setWatchers(watchers, watcherCount = 100)
            // then
            assertEquals(2, channelState.watchers.value.size)
            assertEquals(100, channelState.watcherCount.value)
        }
    }

    @Nested
    inner class UpsertWatcherEvent {

        @Test
        fun `upsertWatcher should add watcher from UserStartWatchingEvent`() = runTest {
            // given
            val user = createWatcher(1)
            val event = createStartWatchingEvent(user, watcherCount = 1)
            // when
            channelState.upsertWatcher(event)
            // then
            assertEquals(1, channelState.watchers.value.size)
            assertEquals(user.id, channelState.watchers.value.first().id)
            assertEquals(1, channelState.watcherCount.value)
        }

        @Test
        fun `upsertWatcher should update existing watcher from event`() = runTest {
            // given
            val user = createWatcher(1)
            channelState.setWatchers(listOf(user), watcherCount = 1)
            // when
            val updatedUser = user.copy(name = "Updated Name")
            val event = createStartWatchingEvent(updatedUser, watcherCount = 1)
            channelState.upsertWatcher(event)
            // then
            assertEquals(1, channelState.watchers.value.size)
            assertEquals("Updated Name", channelState.watchers.value.first().name)
        }

        @Test
        fun `upsertWatcher should update watcher count from event`() = runTest {
            // given
            val user1 = createWatcher(1)
            channelState.setWatchers(listOf(user1), watcherCount = 1)
            // when
            val user2 = createWatcher(2)
            val event = createStartWatchingEvent(user2, watcherCount = 2)
            channelState.upsertWatcher(event)
            // then
            assertEquals(2, channelState.watchers.value.size)
            assertEquals(2, channelState.watcherCount.value)
        }
    }

    @Nested
    inner class UpsertWatchers {

        @Test
        fun `upsertWatchers should add multiple new watchers`() = runTest {
            // given
            val watchers = createWatchers(3)
            // when
            channelState.upsertWatchers(watchers, watcherCount = 3)
            // then
            assertEquals(3, channelState.watchers.value.size)
            assertEquals(3, channelState.watcherCount.value)
        }

        @Test
        fun `upsertWatchers should update existing watchers`() = runTest {
            // given
            val watchers = createWatchers(3)
            channelState.setWatchers(watchers, watcherCount = 3)
            // when
            val updatedWatchers = watchers.map { it.copy(name = "Updated: ${it.name}") }
            channelState.upsertWatchers(updatedWatchers, watcherCount = 3)
            // then
            assertEquals(3, channelState.watchers.value.size)
            channelState.watchers.value.forEach { watcher ->
                assertTrue(watcher.name.startsWith("Updated:"))
            }
        }

        @Test
        fun `upsertWatchers should add and update mixed watchers`() = runTest {
            // given
            val existingWatchers = createWatchers(2)
            channelState.setWatchers(existingWatchers, watcherCount = 2)
            // when
            val newWatcher = createWatcher(10)
            val updatedWatcher = existingWatchers[0].copy(name = "Updated")
            channelState.upsertWatchers(listOf(updatedWatcher, newWatcher), watcherCount = 3)
            // then
            assertEquals(3, channelState.watchers.value.size)
            assertEquals(3, channelState.watcherCount.value)
            assertEquals(
                "Updated",
                channelState.watchers.value.find { it.id == existingWatchers[0].id }?.name,
            )
        }

        @Test
        fun `upsertWatchers should sort watchers by createdAt`() = runTest {
            // given
            val watcher1 = randomUser(id = "user_1", createdAt = Date(1000))
            val watcher3 = randomUser(id = "user_3", createdAt = Date(3000))
            val watcher2 = randomUser(id = "user_2", createdAt = Date(2000))
            // when
            channelState.upsertWatchers(listOf(watcher3, watcher1, watcher2), watcherCount = 3)
            // then
            val sortedIds = channelState.watchers.value.map { it.id }
            assertEquals(listOf("user_1", "user_2", "user_3"), sortedIds)
        }

        @Test
        fun `upsertWatchers should clamp negative watcher count to zero`() = runTest {
            // when
            channelState.upsertWatchers(createWatchers(2), watcherCount = -1)
            // then
            assertEquals(0, channelState.watcherCount.value)
        }
    }

    @Nested
    inner class DeleteWatcherEvent {

        @Test
        fun `deleteWatcher should remove watcher from UserStopWatchingEvent`() = runTest {
            // given
            val watchers = createWatchers(3)
            channelState.setWatchers(watchers, watcherCount = 3)
            // when
            val event = createStopWatchingEvent(watchers[1], watcherCount = 2)
            channelState.deleteWatcher(event)
            // then
            assertEquals(2, channelState.watchers.value.size)
            assertFalse(channelState.watchers.value.any { it.id == watchers[1].id })
            assertEquals(2, channelState.watcherCount.value)
        }

        @Test
        fun `deleteWatcher should update watcher count from event`() = runTest {
            // given
            val watchers = createWatchers(3)
            channelState.setWatchers(watchers, watcherCount = 3)
            // when
            val event = createStopWatchingEvent(watchers[0], watcherCount = 2)
            channelState.deleteWatcher(event)
            // then
            assertEquals(2, channelState.watcherCount.value)
        }
    }

    @Nested
    inner class DeleteWatcherById {

        @Test
        fun `deleteWatcher by userId should remove watcher from state`() = runTest {
            // given
            val watchers = createWatchers(3)
            channelState.setWatchers(watchers, watcherCount = 3)
            // when
            channelState.deleteWatcher(watchers[1].id, watcherCount = 2)
            // then
            assertEquals(2, channelState.watchers.value.size)
            assertFalse(channelState.watchers.value.any { it.id == watchers[1].id })
            assertEquals(2, channelState.watcherCount.value)
        }

        @Test
        fun `deleteWatcher by userId should do nothing for non-existing user`() = runTest {
            // given
            val watchers = createWatchers(3)
            channelState.setWatchers(watchers, watcherCount = 3)
            // when
            channelState.deleteWatcher("non_existing_id", watcherCount = 2)
            // then
            assertEquals(3, channelState.watchers.value.size)
            assertEquals(2, channelState.watcherCount.value)
        }

        @Test
        fun `deleteWatcher should clamp negative watcher count to zero`() = runTest {
            // given
            val watchers = createWatchers(1)
            channelState.setWatchers(watchers, watcherCount = 1)
            // when
            channelState.deleteWatcher(watchers[0].id, watcherCount = -1)
            // then
            assertEquals(0, channelState.watcherCount.value)
        }
    }

    @Nested
    inner class WatchersEnrichedWithLatestUsers {

        @Test
        fun `watchers should be enriched with latest user data`() = runTest {
            // given
            val watcher = User(id = currentUser.id, name = "Old Name")
            channelState.setWatchers(listOf(watcher), watcherCount = 1)
            // when - the latestUsers flow already contains currentUser with name "Tom"
            // then - watchers flow should enrich with latest user data
            val enrichedWatcher = channelState.watchers.value.find { it.id == currentUser.id }
            assertEquals("Tom", enrichedWatcher?.name)
        }
    }

    private fun createWatcher(index: Int): User {
        return randomUser(id = "watcher_$index", name = "Watcher $index")
    }

    private fun createWatchers(count: Int, startIndex: Int = 1): List<User> {
        return (startIndex until startIndex + count).map { i ->
            createWatcher(i)
        }
    }

    private fun createStartWatchingEvent(user: User, watcherCount: Int): UserStartWatchingEvent {
        return UserStartWatchingEvent(
            type = "user.watching.start",
            createdAt = Date(),
            rawCreatedAt = "",
            cid = CID,
            watcherCount = watcherCount,
            channelType = CHANNEL_TYPE,
            channelId = CHANNEL_ID,
            user = user,
        )
    }

    private fun createStopWatchingEvent(user: User, watcherCount: Int): UserStopWatchingEvent {
        return UserStopWatchingEvent(
            type = "user.watching.stop",
            createdAt = Date(),
            rawCreatedAt = "",
            cid = CID,
            watcherCount = watcherCount,
            channelType = CHANNEL_TYPE,
            channelId = CHANNEL_ID,
            user = user,
        )
    }
}
