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

import io.getstream.chat.android.models.ChannelData
import io.getstream.chat.android.models.User
import io.getstream.chat.android.randomMember
import io.getstream.chat.android.randomUser
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.test.runTest
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Nested
import org.junit.jupiter.api.Test
import java.util.Date

@ExperimentalCoroutinesApi
internal class ChannelStateImplUsersTest : ChannelStateImplTestBase() {

    @Nested
    inner class UpsertUserPresenceMembers {

        @Test
        fun `upsertUserPresence should update user in members`() = runTest {
            // given
            val user = randomUser(id = "user_1", name = "Old Name")
            val member = randomMember(user = user)
            channelState.setMembers(listOf(member))
            // when
            val updatedUser = user.copy(name = "New Name")
            channelState.upsertUserPresence(updatedUser)
            // then
            val updatedMember = channelState.members.value.find { it.getUserId() == "user_1" }
            assertEquals("New Name", updatedMember?.user?.name)
        }

        @Test
        fun `upsertUserPresence should update only the matching member`() = runTest {
            // given
            val user1 = randomUser(id = "user_1", name = "User One")
            val user2 = randomUser(id = "user_2", name = "User Two")
            channelState.setMembers(listOf(randomMember(user = user1), randomMember(user = user2)))
            // when
            val updatedUser1 = user1.copy(name = "Updated User One")
            channelState.upsertUserPresence(updatedUser1)
            // then
            assertEquals("Updated User One", channelState.members.value.find { it.getUserId() == "user_1" }?.user?.name)
            assertEquals("User Two", channelState.members.value.find { it.getUserId() == "user_2" }?.user?.name)
        }

        @Test
        fun `upsertUserPresence should do nothing for members when user not found`() = runTest {
            // given
            val user = randomUser(id = "user_1", name = "User One")
            channelState.setMembers(listOf(randomMember(user = user)))
            // when
            val unknownUser = User(id = "unknown_user", name = "Unknown")
            channelState.upsertUserPresence(unknownUser)
            // then
            assertEquals(1, channelState.members.value.size)
            assertEquals("User One", channelState.members.value.first().user.name)
        }
    }

    @Nested
    inner class UpsertUserPresenceWatchers {

        @Test
        fun `upsertUserPresence should update user in watchers`() = runTest {
            // given
            val user = randomUser(id = "user_1", name = "Old Name")
            channelState.setWatchers(listOf(user), watcherCount = 1)
            // when
            val updatedUser = user.copy(name = "New Name")
            channelState.upsertUserPresence(updatedUser)
            // then
            val updatedWatcher = channelState.watchers.value.find { it.id == "user_1" }
            assertEquals("New Name", updatedWatcher?.name)
        }

        @Test
        fun `upsertUserPresence should update only the matching watcher`() = runTest {
            // given
            val user1 = randomUser(id = "user_1", name = "User One")
            val user2 = randomUser(id = "user_2", name = "User Two")
            channelState.setWatchers(listOf(user1, user2), watcherCount = 2)
            // when
            val updatedUser1 = user1.copy(name = "Updated User One")
            channelState.upsertUserPresence(updatedUser1)
            // then
            assertEquals("Updated User One", channelState.watchers.value.find { it.id == "user_1" }?.name)
            assertEquals("User Two", channelState.watchers.value.find { it.id == "user_2" }?.name)
        }

        @Test
        fun `upsertUserPresence should do nothing for watchers when user not found`() = runTest {
            // given
            val user = randomUser(id = "user_1", name = "User One")
            channelState.setWatchers(listOf(user), watcherCount = 1)
            // when
            val unknownUser = User(id = "unknown_user", name = "Unknown")
            channelState.upsertUserPresence(unknownUser)
            // then
            assertEquals(1, channelState.watchers.value.size)
            assertEquals("User One", channelState.watchers.value.first().name)
        }
    }

    @Nested
    inner class UpsertUserPresenceChannelData {

        @Test
        fun `upsertUserPresence should update createdBy in channel data`() = runTest {
            // given
            val creator = randomUser(id = "creator_1", name = "Old Creator")
            channelState.updateChannelData {
                ChannelData(type = CHANNEL_TYPE, id = CHANNEL_ID, createdBy = creator)
            }
            // when
            val updatedCreator = creator.copy(name = "New Creator")
            channelState.upsertUserPresence(updatedCreator)
            // then
            assertEquals("New Creator", channelState.channelData.value.createdBy.name)
        }

        @Test
        fun `upsertUserPresence should not update createdBy when user does not match`() = runTest {
            // given
            val creator = randomUser(id = "creator_1", name = "Creator")
            channelState.updateChannelData {
                ChannelData(type = CHANNEL_TYPE, id = CHANNEL_ID, createdBy = creator)
            }
            // when
            val otherUser = User(id = "other_user", name = "Other")
            channelState.upsertUserPresence(otherUser)
            // then
            assertEquals("Creator", channelState.channelData.value.createdBy.name)
        }

        @Test
        fun `upsertUserPresence should not update createdBy when no channel data exists`() = runTest {
            // given - no channel data set
            // when
            val user = User(id = "some_user", name = "Some User")
            channelState.upsertUserPresence(user)
            // then - should not throw, channelData falls back to default
            assertEquals("", channelState.channelData.value.createdBy.name)
        }
    }

    @Nested
    inner class UpsertUserPresenceMessages {

        @Test
        fun `upsertUserPresence should update user in main messages`() = runTest {
            // given
            val user = randomUser(id = "user_1", name = "Old Name")
            val message = createMessage(1, user = user)
            channelState.setMessages(listOf(message))
            // when
            val updatedUser = user.copy(name = "New Name")
            channelState.upsertUserPresence(updatedUser)
            // then
            assertEquals("New Name", channelState.messages.value.first().user.name)
        }

        @Test
        fun `upsertUserPresence should update user in cached messages`() = runTest {
            // given
            val user = randomUser(id = "user_1", name = "Old Name")
            val message = createMessage(1, user = user)
            channelState.setMessages(listOf(message))
            channelState.cacheLatestMessages()
            channelState.setMessages(emptyList()) // Clear main messages to test cached
            // when
            val updatedUser = user.copy(name = "New Name")
            channelState.upsertUserPresence(updatedUser)
            // then
            val cachedMessages = channelState.toChannel().cachedLatestMessages
            assertEquals("New Name", cachedMessages.first().user.name)
        }

        @Test
        fun `upsertUserPresence should update user in pinned messages`() = runTest {
            // given
            val user = randomUser(id = "user_1", name = "Old Name")
            val pinnedMessage = createMessage(1, user = user, pinned = true, pinnedAt = Date())
            channelState.addPinnedMessage(pinnedMessage)
            // when
            val updatedUser = user.copy(name = "New Name")
            channelState.upsertUserPresence(updatedUser)
            // then
            assertEquals("New Name", channelState.pinnedMessages.value.first().user.name)
        }

        @Test
        fun `upsertUserPresence should only update messages from the matching user`() = runTest {
            // given
            val user1 = randomUser(id = "user_1", name = "User One")
            val user2 = randomUser(id = "user_2", name = "User Two")
            val message1 = createMessage(1, user = user1)
            val message2 = createMessage(2, user = user2)
            channelState.setMessages(listOf(message1, message2))
            // when
            val updatedUser1 = user1.copy(name = "Updated User One")
            channelState.upsertUserPresence(updatedUser1)
            // then
            assertEquals("Updated User One", channelState.messages.value.find { it.id == message1.id }?.user?.name)
            assertEquals("User Two", channelState.messages.value.find { it.id == message2.id }?.user?.name)
        }

        @Test
        fun `upsertUserPresence should do nothing for messages when user not found`() = runTest {
            // given
            val user = randomUser(id = "user_1", name = "User One")
            val message = createMessage(1, user = user)
            channelState.setMessages(listOf(message))
            // when
            val unknownUser = User(id = "unknown_user", name = "Unknown")
            channelState.upsertUserPresence(unknownUser)
            // then
            assertEquals("User One", channelState.messages.value.first().user.name)
        }
    }
}
