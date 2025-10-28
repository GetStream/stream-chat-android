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

package io.getstream.chat.android.offline.repository

import androidx.test.ext.junit.runners.AndroidJUnit4
import io.getstream.chat.android.offline.integration.BaseDomainTest2
import kotlinx.coroutines.test.runTest
import org.amshove.kluent.shouldBeEqualTo
import org.amshove.kluent.shouldBeNull
import org.junit.Test
import org.junit.runner.RunWith

@RunWith(AndroidJUnit4::class)
internal class ChannelRepositoryTest : BaseDomainTest2() {
    private val helper by lazy { repos }

    @Test
    fun `inserting a channel and reading it should be equal`(): Unit = runTest {
        helper.insertChannels(listOf(data.channel1))
        val channel = helper.selectChannel(data.channel1.cid)!!

        channel shouldBeEqualTo data.channel1
    }

    @Test
    fun `deleting a channel should work`(): Unit = runTest {
        helper.insertChannels(listOf(data.channel1))
        helper.deleteChannel(data.channel1.cid)
        val entity = helper.selectChannel(data.channel1.cid)

        entity.shouldBeNull()
    }

    @Test
    fun `updating a channel should work as intended`(): Unit = runTest {
        helper.insertChannels(listOf(data.channel1, data.channel1Updated))
        val channel = helper.selectChannel(data.channel1.cid)

        channel shouldBeEqualTo data.channel1Updated
    }

    @Test
    fun `deleteAllChannelUserMessages should remove all messages from specific user in specific channel`(): Unit = runTest {
        // Create messages from different users
        val messageFromUser1 = data.message1.copy(id = "msg1", user = data.user1, text = "Message from user1")
        val messageFromUser2 = data.message1.copy(id = "msg2", user = data.user2, text = "Message from user2")
        val anotherMessageFromUser1 =
            data.message1.copy(id = "msg3", user = data.user1, text = "Another message from user1")

        // Create channel with messages from both users
        val channelWithMessages = data.channel1.copy(
            messages = listOf(messageFromUser1, messageFromUser2, anotherMessageFromUser1),
        )

        helper.insertChannels(listOf(channelWithMessages))

        // Delete all messages from user1 in this specific channel
        helper.deleteAllChannelUserMessages(data.channel1.cid, data.user1.id)

        val updatedChannel = helper.selectChannel(data.channel1.cid)!!

        // Only message from user2 should remain
        updatedChannel.messages.size shouldBeEqualTo 1
        updatedChannel.messages.first().user.id shouldBeEqualTo data.user2.id
        updatedChannel.messages.first().text shouldBeEqualTo "Message from user2"
    }

    @Test
    fun `deleteAllChannelUserMessages should remove pinned messages from specific user in specific channel`(): Unit = runTest {
        // Create pinned messages from different users
        val pinnedMessageFromUser1 =
            data.message1.copy(id = "pinned1", user = data.user1, text = "Pinned message from user1", pinned = true)
        val pinnedMessageFromUser2 =
            data.message1.copy(id = "pinned2", user = data.user2, text = "Pinned message from user2", pinned = true)

        // Create channel with pinned messages from both users
        val channelWithPinnedMessages = data.channel1.copy(
            messages = listOf(pinnedMessageFromUser1, pinnedMessageFromUser2),
            pinnedMessages = listOf(pinnedMessageFromUser1, pinnedMessageFromUser2),
        )

        helper.insertChannels(listOf(channelWithPinnedMessages))

        // Delete all messages from user1 in this specific channel
        helper.deleteAllChannelUserMessages(data.channel1.cid, data.user1.id)

        val updatedChannel = helper.selectChannel(data.channel1.cid)!!

        // Only pinned message from user2 should remain
        updatedChannel.pinnedMessages.size shouldBeEqualTo 1
        updatedChannel.pinnedMessages.first().user.id shouldBeEqualTo data.user2.id
        updatedChannel.pinnedMessages.first().text shouldBeEqualTo "Pinned message from user2"

        // Regular messages should also be filtered
        updatedChannel.messages.size shouldBeEqualTo 1
        updatedChannel.messages.first().user.id shouldBeEqualTo data.user2.id
    }

    @Test
    fun `deleteAllChannelUserMessages with null cid should remove all messages from user across all channels`(): Unit = runTest {
        // Create messages from user1 in multiple channels
        val messageInChannel1 = data.message1.copy(id = "msg1", user = data.user1, text = "Message in channel1")
        val messageInChannel2 = data.message1.copy(
            id = "msg2",
            user = data.user1,
            cid = data.channel2.cid,
            text = "Message in channel2",
        )
        val messageFromUser2InChannel1 =
            data.message1.copy(id = "msg3", user = data.user2, text = "Message from user2 in channel1")

        val channel1WithMessages =
            data.channel1.copy(messages = listOf(messageInChannel1, messageFromUser2InChannel1))
        val channel2WithMessages = data.channel2.copy(messages = listOf(messageInChannel2))

        helper.insertChannels(listOf(channel1WithMessages, channel2WithMessages))

        // Delete all messages from user1 across all channels (null cid)
        helper.deleteAllChannelUserMessages(null, data.user1.id)

        val updatedChannel1 = helper.selectChannel(data.channel1.cid)!!
        val updatedChannel2 = helper.selectChannel(data.channel2.cid)!!

        // Channel1 should only have message from user2
        updatedChannel1.messages.size shouldBeEqualTo 1
        updatedChannel1.messages.first().user.id shouldBeEqualTo data.user2.id

        // Channel2 should have no messages (user1's message was removed)
        updatedChannel2.messages.size shouldBeEqualTo 0
    }
}
