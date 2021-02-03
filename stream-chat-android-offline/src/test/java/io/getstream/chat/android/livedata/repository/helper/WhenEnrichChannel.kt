package io.getstream.chat.android.livedata.repository.helper

import com.nhaarman.mockitokotlin2.doReturn
import io.getstream.chat.android.client.models.Config
import io.getstream.chat.android.livedata.model.ChannelConfig
import io.getstream.chat.android.livedata.randomChannel
import io.getstream.chat.android.livedata.randomMessage
import kotlinx.coroutines.ExperimentalCoroutinesApi
import org.amshove.kluent.When
import org.amshove.kluent.`should be equal to`
import org.amshove.kluent.calling
import org.junit.jupiter.api.Test

@ExperimentalCoroutinesApi
internal class WhenEnrichChannel : BaseRepositoryHelperTest() {

    @Test
    fun `Given a channel config in repo Should update channel by config from repo`() {
        sut.run {
            val channel = randomChannel(type = "channelType")
            val defaultConfig = Config(name = "default")
            val config = Config(name = "forChannel")
            When calling configs.select("channelType") doReturn ChannelConfig("channelType", config)

            channel.enrichChannel(emptyMap(), defaultConfig)

            channel.config `should be equal to` config
        }
    }

    @Test
    fun `Given no channel config in repo Should update channel by default config`() {
        sut.run {
            val channel = randomChannel(type = "channelType")
            val defaultConfig = Config(name = "default")
            When calling configs.select("channelType") doReturn null

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
