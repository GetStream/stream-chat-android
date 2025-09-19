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
    fun `deleting multiple messages from single channel should work`(): Unit = runTest {
        // Given: A channel with multiple messages
        val message1 = data.message1.copy(id = "msg1", cid = data.channel1.cid)
        val message2 = data.message1.copy(id = "msg2", cid = data.channel1.cid, text = "second message")
        val message3 = data.message1.copy(id = "msg3", cid = data.channel1.cid, text = "third message")
        val channelWithMessages = data.channel1.copy(
            messages = listOf(message1, message2, message3),
        )

        helper.insertChannels(listOf(channelWithMessages))

        // When: Deleting multiple messages
        val messagesToDelete = listOf(message1, message3)
        helper.deleteMessages(messagesToDelete)

        // Then: Only the non-deleted message should remain
        val updatedChannel = helper.selectChannel(data.channel1.cid)!!
        updatedChannel.messages.size shouldBeEqualTo 1
        updatedChannel.messages.first().id shouldBeEqualTo "msg2"
        updatedChannel.messages.first().text shouldBeEqualTo "second message"
    }

    @Test
    fun `deleting messages from multiple channels should work`(): Unit = runTest {
        // Given: Two channels with messages
        val message1Channel1 = data.message1.copy(id = "msg1", cid = data.channel1.cid)
        val message2Channel1 = data.message1.copy(id = "msg2", cid = data.channel1.cid, text = "second message")
        val message1Channel2 = data.message1.copy(id = "msg3", cid = data.channel2.cid, text = "channel2 message")
        val message2Channel2 =
            data.message1.copy(id = "msg4", cid = data.channel2.cid, text = "another channel2 message")

        val channel1WithMessages = data.channel1.copy(
            messages = listOf(message1Channel1, message2Channel1),
        )
        val channel2WithMessages = data.channel2.copy(
            messages = listOf(message1Channel2, message2Channel2),
        )

        helper.insertChannels(listOf(channel1WithMessages, channel2WithMessages))

        // When: Deleting messages from both channels
        val messagesToDelete = listOf(message1Channel1, message1Channel2)
        helper.deleteMessages(messagesToDelete)

        // Then: Each channel should have only its non-deleted message
        val updatedChannel1 = helper.selectChannel(data.channel1.cid)!!
        updatedChannel1.messages.size shouldBeEqualTo 1
        updatedChannel1.messages.first().id shouldBeEqualTo "msg2"

        val updatedChannel2 = helper.selectChannel(data.channel2.cid)!!
        updatedChannel2.messages.size shouldBeEqualTo 1
        updatedChannel2.messages.first().id shouldBeEqualTo "msg4"
    }

    @Test
    fun `deleting pinned messages should remove them from pinnedMessages list`(): Unit = runTest {
        // Given: A channel with both regular and pinned messages
        val regularMessage = data.message1.copy(id = "regular", cid = data.channel1.cid)
        val pinnedMessage = data.message1.copy(id = "pinned", cid = data.channel1.cid, text = "pinned message")
        val channelWithMessages = data.channel1.copy(
            messages = listOf(regularMessage, pinnedMessage),
            pinnedMessages = listOf(pinnedMessage),
        )

        helper.insertChannels(listOf(channelWithMessages))

        // When: Deleting the pinned message
        helper.deleteMessages(listOf(pinnedMessage))

        // Then: Message should be removed from both messages and pinnedMessages
        val updatedChannel = helper.selectChannel(data.channel1.cid)!!
        updatedChannel.messages.size shouldBeEqualTo 1
        updatedChannel.messages.first().id shouldBeEqualTo "regular"
        updatedChannel.pinnedMessages.size shouldBeEqualTo 0
    }
}
