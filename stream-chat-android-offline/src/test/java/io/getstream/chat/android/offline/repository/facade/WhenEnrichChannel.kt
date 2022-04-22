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

package io.getstream.chat.android.offline.repository.facade

import io.getstream.chat.android.client.models.ChannelConfig
import io.getstream.chat.android.client.models.Config
import io.getstream.chat.android.offline.randomChannel
import io.getstream.chat.android.offline.randomMessage
import kotlinx.coroutines.ExperimentalCoroutinesApi
import org.amshove.kluent.`should be equal to`
import org.junit.jupiter.api.Test
import org.mockito.kotlin.doReturn
import org.mockito.kotlin.whenever

@ExperimentalCoroutinesApi
internal class WhenEnrichChannel : BaseRepositoryFacadeTest() {

    @Test
    fun `Given a channel config in repo Should update channel by config from repo`() {
        sut.run {
            val channel = randomChannel(type = "channelType")
            val defaultConfig = Config(name = "default")
            val config = Config(name = "forChannel")
            whenever(configs.selectChannelConfig("channelType")) doReturn ChannelConfig("channelType", config)

            channel.enrichChannel(emptyMap(), defaultConfig)

            channel.config `should be equal to` config
        }
    }

    @Test
    fun `Given no channel config in repo Should update channel by default config`() {
        sut.run {
            val channel = randomChannel(type = "channelType")
            val defaultConfig = Config(name = "default")
            whenever(configs.selectChannelConfig("channelType")) doReturn null

            channel.enrichChannel(emptyMap(), defaultConfig)

            channel.config `should be equal to` defaultConfig
        }
    }

    @Test
    fun `Given messages for channel in the map And channel messages are empty Should update channel with these messages`() {
        sut.run {
            val message1 = randomMessage()
            val message2 = randomMessage()
            val channel = randomChannel(cid = "cid1")
            val messageMap = mapOf("cid1" to listOf(message1, message2))

            channel.enrichChannel(messageMap, Config())

            val channelMessages = channel.messages
            channelMessages.size `should be equal to` 2
            channelMessages[0] `should be equal to` message1
            channelMessages[1] `should be equal to` message2
        }
    }

    @Test
    fun `Given messages for channel in the map And channel messages are not empty Should update channel with distinct set of messages`() {
        sut.run {
            val message1 = randomMessage()
            val message2 = randomMessage()
            val message3 = randomMessage()
            val channel = randomChannel(cid = "cid1", messages = listOf(message1, message3))
            val messageMap = mapOf("cid1" to listOf(message1, message2))

            channel.enrichChannel(messageMap, Config())

            val channelMessages = channel.messages
            channelMessages.size `should be equal to` 3
            channelMessages[0] `should be equal to` message1
            channelMessages[1] `should be equal to` message2
            channelMessages[2] `should be equal to` message3
        }
    }

    @Test
    fun `Given messages for channel in the map And channel messages are not empty And contain message with the same id but different data Should update channel with distinct set of messages`() {
        sut.run {
            val commonMessageId = "commonMessage"
            val commonMessage = randomMessage(id = commonMessageId)
            val message2 = randomMessage()
            val message3 = randomMessage()
            val channel = randomChannel(cid = "cid1", messages = listOf(randomMessage(id = commonMessageId), message3))
            val messageMap = mapOf("cid1" to listOf(commonMessage, message2))

            channel.enrichChannel(messageMap, Config())

            val channelMessages = channel.messages
            channelMessages.size `should be equal to` 3
            channelMessages[0] `should be equal to` commonMessage
            channelMessages[1] `should be equal to` message2
            channelMessages[2] `should be equal to` message3
        }
    }

    @Test
    fun `Given no messages for channel in the map Should not update channel messages`() {
        sut.run {
            val message1 = randomMessage()
            val message2 = randomMessage()
            val message3 = randomMessage()
            val channel = randomChannel(cid = "cid1", messages = listOf(message1))
            val messageMap = mapOf("cid2" to listOf(message2, message3))

            channel.enrichChannel(messageMap, Config())

            val channelMessages = channel.messages
            channelMessages.size `should be equal to` 1
            channelMessages[0] `should be equal to` message1
        }
    }
}
