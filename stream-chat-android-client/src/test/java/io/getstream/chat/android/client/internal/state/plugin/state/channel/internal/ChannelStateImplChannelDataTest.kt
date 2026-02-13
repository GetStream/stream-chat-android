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
import io.getstream.chat.android.models.Config
import io.getstream.chat.android.models.MessageType
import io.getstream.chat.android.models.PushPreference
import io.getstream.chat.android.models.PushPreferenceLevel
import io.getstream.chat.android.models.User
import io.getstream.chat.android.randomUser
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.test.runTest
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertFalse
import org.junit.jupiter.api.Assertions.assertNull
import org.junit.jupiter.api.Assertions.assertTrue
import org.junit.jupiter.api.Nested
import org.junit.jupiter.api.Test
import java.util.Date

@ExperimentalCoroutinesApi
internal class ChannelStateImplChannelDataTest : ChannelStateImplTestBase() {

    // region UpdateChannelData (lambda)

    @Nested
    inner class UpdateChannelDataLambda {

        @Test
        fun `updateChannelData should set initial channel data`() = runTest {
            // given
            val data = ChannelData(type = CHANNEL_TYPE, id = CHANNEL_ID, name = "Test Channel")
            // when
            channelState.updateChannelData { data }
            // then
            assertEquals("Test Channel", channelState.channelData.value.name)
        }

        @Test
        fun `updateChannelData should update existing channel data`() = runTest {
            // given
            channelState.updateChannelData {
                ChannelData(type = CHANNEL_TYPE, id = CHANNEL_ID, name = "Original")
            }
            // when
            channelState.updateChannelData { current ->
                current?.copy(name = "Updated")
            }
            // then
            assertEquals("Updated", channelState.channelData.value.name)
        }

        @Test
        fun `updateChannelData with null result should clear channel data`() = runTest {
            // given
            channelState.updateChannelData {
                ChannelData(type = CHANNEL_TYPE, id = CHANNEL_ID, name = "Test")
            }
            // when
            channelState.updateChannelData { null }
            // then - should fall back to default ChannelData
            assertEquals(CHANNEL_TYPE, channelState.channelData.value.type)
            assertEquals(CHANNEL_ID, channelState.channelData.value.id)
            assertEquals("", channelState.channelData.value.name)
        }

        @Test
        fun `updateChannelData receives current data in lambda`() = runTest {
            // given
            channelState.updateChannelData {
                ChannelData(type = CHANNEL_TYPE, id = CHANNEL_ID, memberCount = 10)
            }
            // when
            channelState.updateChannelData { current ->
                current?.copy(memberCount = current.memberCount + 5)
            }
            // then
            assertEquals(15, channelState.channelData.value.memberCount)
        }
    }

    // endregion

    // region UpdateChannelData (event)

    @Nested
    inner class UpdateChannelDataEvent {

        @Test
        fun `updateChannelData from event should merge into existing channel data`() = runTest {
            // given - set initial channel data with ownCapabilities and membership
            val membership = io.getstream.chat.android.randomMember()
            channelState.updateChannelData {
                ChannelData(
                    type = CHANNEL_TYPE,
                    id = CHANNEL_ID,
                    name = "Original",
                    ownCapabilities = setOf("send-message"),
                    membership = membership,
                )
            }
            // when - update via HasChannel event with new name and memberCount
            val eventChannel = io.getstream.chat.android.randomChannel(
                type = CHANNEL_TYPE,
                id = CHANNEL_ID,
                name = "Updated From Event",
                memberCount = 42,
            )
            val event = io.getstream.chat.android.client.events.ChannelUpdatedEvent(
                type = "channel.updated",
                createdAt = Date(),
                rawCreatedAt = "",
                cid = CID,
                channelType = CHANNEL_TYPE,
                channelId = CHANNEL_ID,
                channel = eventChannel,
                message = null,
            )
            channelState.updateChannelData(event)
            // then - name and memberCount updated from event
            val data = channelState.channelData.value
            assertEquals("Updated From Event", data.name)
            assertEquals(42, data.memberCount)
            // ownCapabilities and membership are NOT overwritten by mergeFromEvent
            assertEquals(setOf("send-message"), data.ownCapabilities)
            assertEquals(membership.getUserId(), data.membership?.getUserId())
        }
    }

    // endregion

    // region ChannelData StateFlow

    @Nested
    inner class ChannelDataStateFlow {

        @Test
        fun `channelData should return default when no data is set`() = runTest {
            // when & then
            val data = channelState.channelData.value
            assertEquals(CHANNEL_TYPE, data.type)
            assertEquals(CHANNEL_ID, data.id)
        }

        @Test
        fun `channelData should enrich createdBy with latest users`() = runTest {
            // given - set channel data with createdBy matching currentUser
            channelState.updateChannelData {
                ChannelData(
                    type = CHANNEL_TYPE,
                    id = CHANNEL_ID,
                    createdBy = User(id = currentUser.id, name = "Old Name"),
                )
            }
            // when - the latestUsers flow already contains currentUser with name "Tom"
            // then - createdBy should be enriched with latest user data
            assertEquals("Tom", channelState.channelData.value.createdBy.name)
        }

        @Test
        fun `channelData should preserve createdBy when user not in latestUsers`() = runTest {
            // given
            val otherUser = User(id = "unknown_user", name = "Unknown")
            channelState.updateChannelData {
                ChannelData(type = CHANNEL_TYPE, id = CHANNEL_ID, createdBy = otherUser)
            }
            // when & then - user not in latestUsers, should keep original
            assertEquals("Unknown", channelState.channelData.value.createdBy.name)
        }
    }

    // endregion

    // region SetHidden

    @Nested
    inner class SetHidden {

        @Test
        fun `setHidden should set hidden to true`() = runTest {
            // when
            channelState.setHidden(true)
            // then
            assertTrue(channelState.hidden.value)
        }

        @Test
        fun `setHidden should set hidden to false`() = runTest {
            // given
            channelState.setHidden(true)
            // when
            channelState.setHidden(false)
            // then
            assertFalse(channelState.hidden.value)
        }

        @Test
        fun `hidden should default to false`() = runTest {
            // when & then
            assertFalse(channelState.hidden.value)
        }
    }

    // endregion

    // region SetMuted

    @Nested
    inner class SetMuted {

        @Test
        fun `setMuted should set muted to true`() = runTest {
            // when
            channelState.setMuted(true)
            // then
            assertTrue(channelState.muted.value)
        }

        @Test
        fun `setMuted should set muted to false`() = runTest {
            // given
            channelState.setMuted(true)
            // when
            channelState.setMuted(false)
            // then
            assertFalse(channelState.muted.value)
        }

        @Test
        fun `muted should default to false`() = runTest {
            // when & then
            assertFalse(channelState.muted.value)
        }
    }

    // endregion

    // region SetPushPreference

    @Nested
    inner class SetPushPreference {

        @Test
        fun `setPushPreference should set the push preference`() = runTest {
            // given
            channelState.updateChannelData {
                ChannelData(type = CHANNEL_TYPE, id = CHANNEL_ID)
            }
            val preference = PushPreference(
                level = PushPreferenceLevel("all"),
                disabledUntil = null,
            )
            // when
            channelState.setPushPreference(preference)
            // then
            assertEquals(preference, channelState.channelData.value.pushPreference)
        }

        @Test
        fun `setPushPreference should update existing push preference`() = runTest {
            // given
            channelState.updateChannelData {
                ChannelData(type = CHANNEL_TYPE, id = CHANNEL_ID)
            }
            val initialPreference = PushPreference(
                level = PushPreferenceLevel("all"),
                disabledUntil = null,
            )
            channelState.setPushPreference(initialPreference)
            // when
            val updatedPreference = PushPreference(
                level = PushPreferenceLevel("muted"),
                disabledUntil = Date(System.currentTimeMillis() + 3600000),
            )
            channelState.setPushPreference(updatedPreference)
            // then
            assertEquals(updatedPreference, channelState.channelData.value.pushPreference)
        }

        @Test
        fun `setPushPreference should do nothing when no channel data exists`() = runTest {
            // given - no channel data set (internal _channelData is null)
            // when
            channelState.setPushPreference(
                PushPreference(level = PushPreferenceLevel("all"), disabledUntil = null),
            )
            // then - should not throw, channelData falls back to default with null pushPreference
            assertNull(channelState.channelData.value.pushPreference)
        }
    }

    // endregion

    // region SetMessageCount

    @Nested
    inner class SetMessageCount {

        @Test
        fun `setMessageCount should set the message count`() = runTest {
            // given
            channelState.updateChannelData {
                ChannelData(type = CHANNEL_TYPE, id = CHANNEL_ID)
            }
            // when
            channelState.setMessageCount(42)
            // then
            assertEquals(42, channelState.messageCount.value)
        }

        @Test
        fun `setMessageCount should update existing message count`() = runTest {
            // given
            channelState.updateChannelData {
                ChannelData(type = CHANNEL_TYPE, id = CHANNEL_ID, messageCount = 10)
            }
            // when
            channelState.setMessageCount(20)
            // then
            assertEquals(20, channelState.messageCount.value)
        }

        @Test
        fun `setMessageCount should do nothing when no channel data exists`() = runTest {
            // given - no channel data set
            // when
            channelState.setMessageCount(10)
            // then - messageCount should remain null (default fallback)
            assertNull(channelState.messageCount.value)
        }

        @Test
        fun `messageCount should reflect channelData messageCount`() = runTest {
            // given
            channelState.updateChannelData {
                ChannelData(type = CHANNEL_TYPE, id = CHANNEL_ID, messageCount = 99)
            }
            // then
            assertEquals(99, channelState.messageCount.value)
        }
    }

    // endregion

    // region UpdateLastMessageAt

    @Nested
    inner class UpdateLastMessageAt {

        @Test
        fun `updateLastMessageAt should set lastMessageAt from message`() = runTest {
            // given
            channelState.updateChannelData {
                ChannelData(type = CHANNEL_TYPE, id = CHANNEL_ID, lastMessageAt = null)
            }
            val message = createMessage(1, timestamp = 5000)
            // when
            channelState.updateLastMessageAt(message)
            // then
            assertEquals(Date(5000), channelState.channelData.value.lastMessageAt)
        }

        @Test
        fun `updateLastMessageAt should update to newer message date`() = runTest {
            // given
            channelState.updateChannelData {
                ChannelData(type = CHANNEL_TYPE, id = CHANNEL_ID, lastMessageAt = Date(3000))
            }
            val newerMessage = createMessage(1, timestamp = 5000)
            // when
            channelState.updateLastMessageAt(newerMessage)
            // then
            assertEquals(Date(5000), channelState.channelData.value.lastMessageAt)
        }

        @Test
        fun `updateLastMessageAt should not update to older message date`() = runTest {
            // given
            channelState.updateChannelData {
                ChannelData(type = CHANNEL_TYPE, id = CHANNEL_ID, lastMessageAt = Date(5000))
            }
            val olderMessage = createMessage(1, timestamp = 3000)
            // when
            channelState.updateLastMessageAt(olderMessage)
            // then
            assertEquals(Date(5000), channelState.channelData.value.lastMessageAt)
        }

        @Test
        fun `updateLastMessageAt should skip shadowed messages`() = runTest {
            // given
            channelState.updateChannelData {
                ChannelData(type = CHANNEL_TYPE, id = CHANNEL_ID, lastMessageAt = Date(1000))
            }
            val otherUser = randomUser(id = "other_user")
            val shadowedMessage = createMessage(1, timestamp = 5000, user = otherUser, shadowed = true)
            // when
            channelState.updateLastMessageAt(shadowedMessage)
            // then
            assertEquals(Date(1000), channelState.channelData.value.lastMessageAt)
        }

        @Test
        fun `updateLastMessageAt should skip thread replies not shown in channel`() = runTest {
            // given
            channelState.updateChannelData {
                ChannelData(type = CHANNEL_TYPE, id = CHANNEL_ID, lastMessageAt = Date(1000))
            }
            val threadReply = createMessage(1, timestamp = 5000, parentId = "parent_1", showInChannel = false)
            // when
            channelState.updateLastMessageAt(threadReply)
            // then
            assertEquals(Date(1000), channelState.channelData.value.lastMessageAt)
        }

        @Test
        fun `updateLastMessageAt should skip system messages when config says so`() = runTest {
            // given
            channelState.setChannelConfig(Config(skipLastMsgUpdateForSystemMsgs = true))
            channelState.updateChannelData {
                ChannelData(type = CHANNEL_TYPE, id = CHANNEL_ID, lastMessageAt = Date(1000))
            }
            val systemMessage = createMessage(1, timestamp = 5000).copy(type = MessageType.SYSTEM)
            // when
            channelState.updateLastMessageAt(systemMessage)
            // then
            assertEquals(Date(1000), channelState.channelData.value.lastMessageAt)
        }

        @Test
        fun `updateLastMessageAt should not skip system messages when config allows them`() = runTest {
            // given
            channelState.setChannelConfig(Config(skipLastMsgUpdateForSystemMsgs = false))
            channelState.updateChannelData {
                ChannelData(type = CHANNEL_TYPE, id = CHANNEL_ID, lastMessageAt = Date(1000))
            }
            val systemMessage = createMessage(1, timestamp = 5000).copy(type = MessageType.SYSTEM)
            // when
            channelState.updateLastMessageAt(systemMessage)
            // then
            assertEquals(Date(5000), channelState.channelData.value.lastMessageAt)
        }
    }

    // endregion

    // region DeleteChannel

    @Nested
    inner class DeleteChannel {

        @Test
        fun `deleteChannel should set deletedAt timestamp`() = runTest {
            // given
            channelState.updateChannelData {
                ChannelData(type = CHANNEL_TYPE, id = CHANNEL_ID)
            }
            val deletedAt = Date(5000)
            // when
            channelState.deleteChannel(deletedAt)
            // then
            assertEquals(deletedAt, channelState.channelData.value.deletedAt)
        }

        @Test
        fun `deleteChannel should preserve other channel data fields`() = runTest {
            // given
            channelState.updateChannelData {
                ChannelData(type = CHANNEL_TYPE, id = CHANNEL_ID, name = "My Channel", memberCount = 5)
            }
            val deletedAt = Date(5000)
            // when
            channelState.deleteChannel(deletedAt)
            // then
            val data = channelState.channelData.value
            assertEquals("My Channel", data.name)
            assertEquals(5, data.memberCount)
            assertEquals(deletedAt, data.deletedAt)
        }

        @Test
        fun `deleteChannel should do nothing when no channel data exists`() = runTest {
            // given - no channel data set
            // when
            channelState.deleteChannel(Date(5000))
            // then - should not throw, channelData falls back to default with null deletedAt
            assertNull(channelState.channelData.value.deletedAt)
        }
    }

    // endregion

    // region SetChannelConfig

    @Nested
    inner class SetChannelConfig {

        @Test
        fun `setChannelConfig should set the config`() = runTest {
            // given
            val config = Config(readEventsEnabled = false)
            // when
            channelState.setChannelConfig(config)
            // then
            assertFalse(channelState.channelConfig.value.readEventsEnabled)
        }

        @Test
        fun `setChannelConfig should replace existing config`() = runTest {
            // given
            channelState.setChannelConfig(Config(readEventsEnabled = false))
            // when
            channelState.setChannelConfig(Config(readEventsEnabled = true))
            // then
            assertTrue(channelState.channelConfig.value.readEventsEnabled)
        }

        @Test
        fun `channelConfig should default to Config with defaults`() = runTest {
            // when & then
            assertTrue(channelState.channelConfig.value.readEventsEnabled)
        }
    }

    // endregion
}
