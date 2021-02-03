package io.getstream.chat.android.livedata.repository.helper

import com.nhaarman.mockitokotlin2.any
import com.nhaarman.mockitokotlin2.doReturn
import com.nhaarman.mockitokotlin2.eq
import com.nhaarman.mockitokotlin2.mock
import io.getstream.chat.android.client.models.Channel
import io.getstream.chat.android.client.models.ChannelUserRead
import io.getstream.chat.android.client.models.Config
import io.getstream.chat.android.client.models.Member
import io.getstream.chat.android.client.models.User
import io.getstream.chat.android.livedata.model.ChannelConfig
import io.getstream.chat.android.livedata.randomChannel
import io.getstream.chat.android.livedata.randomMessage
import io.getstream.chat.android.livedata.randomUser
import io.getstream.chat.android.livedata.request.AnyChannelPaginationRequest
import io.getstream.chat.android.test.positiveRandomInt
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.test.runBlockingTest
import org.amshove.kluent.Verify
import org.amshove.kluent.When
import org.amshove.kluent.`should be equal to`
import org.amshove.kluent.`should contain same`
import org.amshove.kluent.called
import org.amshove.kluent.calling
import org.amshove.kluent.on
import org.amshove.kluent.shouldBeEqualTo
import org.amshove.kluent.that
import org.amshove.kluent.was
import org.junit.jupiter.api.Test

@ExperimentalCoroutinesApi
internal class RepositoryHelperTests : BaseRepositoryHelperTest() {

    @Test
    fun `Given request less than last message When select channels Should return channels from DB with empty messages`() =
        runBlockingTest {
            val paginationRequest = AnyChannelPaginationRequest(0)
            val user = randomUser(id = "userId")
            When calling users.select("userId") doReturn user
            val channel1 = randomChannel(messages = emptyList(), cid = "cid1", createdBy = user)
            val channel2 = randomChannel(messages = emptyList(), cid = "cid2", createdBy = user)
            When calling channels.select(eq(listOf("cid1", "cid2")), any(), any()) doReturn listOf(channel1, channel2)

            val result = sut.selectChannels(listOf("cid1", "cid2"), mock(), paginationRequest)

            result.size shouldBeEqualTo 2
            result.any { it.cid == "cid1" && it.messages.isEmpty() } shouldBeEqualTo true
            result.any { it.cid == "cid2" && it.messages.isEmpty() } shouldBeEqualTo true
        }

    @Test
    fun `Given request more than last message When select channels Should return channels from DB with messages`() =
        runBlockingTest {
            val paginationRequest = AnyChannelPaginationRequest(100)
            val user = randomUser(id = "userId")
            When calling users.select("userId") doReturn user
            val message1 = randomMessage(id = "messageId1", cid = "cid1", user = user)
            val message2 = randomMessage(id = "messageId2", cid = "cid2", user = user)
            When calling messages.selectMessagesForChannel(eq("cid1"), eq(paginationRequest), any()) doReturn listOf(
                message1
            )
            When calling messages.selectMessagesForChannel(eq("cid2"), eq(paginationRequest), any()) doReturn listOf(
                message2
            )
            val channel1 = randomChannel(messages = emptyList(), cid = "cid1", createdBy = user)
            val channelEntity2 = randomChannel(messages = emptyList(), cid = "cid2", createdBy = user)
            When calling channels.select(eq(listOf("cid1", "cid2")), any(), any()) doReturn listOf(
                channel1,
                channelEntity2
            )

            val result = sut.selectChannels(listOf("cid1", "cid2"), mock(), paginationRequest)

            result.size shouldBeEqualTo 2
            result.any { it.cid == "cid1" && it.messages.size == 1 && it.messages.first().id == "messageId1" } shouldBeEqualTo true
            result.any { it.cid == "cid2" && it.messages.size == 1 && it.messages.first().id == "messageId2" } shouldBeEqualTo true
        }

    @Test
    fun `Given Db contains all required data When select messages Should return message list`() = runBlockingTest {
        val message1 = randomMessage()
        val message2 = randomMessage()
        When calling messages.select(eq(listOf("messageId1", "messageId2")), any()) doReturn listOf(message1, message2)

        val result = sut.selectMessages(listOf("messageId1", "messageId2"))

        result.size shouldBeEqualTo 2
    }

    @Test
    fun `When insert a channel, all participant users of this channel need to be stored`() = runBlockingTest {
        val memberUser = randomUser()
        val channelUser = randomUser()
        val userRead = randomUser()
        val messageUser = randomUser()
        val channel = randomChannel(
            createdBy = channelUser,
            members = listOf(Member(memberUser)),
            read = listOf(ChannelUserRead(userRead)),
            messages = listOf(randomMessage(user = messageUser))
        )

        sut.insertChannel(channel)

        Verify on channels that channels.insertChannels(eq(listOf(channel))) was called
        Verify on users that users.insert(
            com.nhaarman.mockitokotlin2.check { listUser ->
                listUser.size `should be equal to` 4
                listUser `should contain same` listOf(memberUser, channelUser, userRead, messageUser)
            }
        ) was called
    }

    @Test
    fun `When insert a list of channels, all participant users of these channels need to be stored`() =
        runBlockingTest {
            val (listOfUser: List<User>, listOfChannels: List<Channel>) =
                (0..positiveRandomInt(20)).fold((listOf<User>() to listOf<Channel>())) { acc, _ ->
                    val memberUser = randomUser()
                    val channelUser = randomUser()
                    val userRead = randomUser()
                    val messageUser = randomUser()
                    val channel = randomChannel(
                        createdBy = channelUser,
                        members = listOf(Member(memberUser)),
                        read = listOf(ChannelUserRead(userRead)),
                        messages = listOf(randomMessage(user = messageUser))
                    )
                    acc.first + listOf(memberUser, channelUser, userRead, messageUser) to acc.second + channel
                }

            sut.insertChannels(listOfChannels)

            Verify on channels that channels.insertChannels(eq(listOfChannels)) was called
            Verify on users that users.insert(
                com.nhaarman.mockitokotlin2.check { listUser ->
                    listUser `should contain same` listOfUser
                }
            ) was called
        }

    @Test
    fun `When enrich channel If there is channel config in repo Should update channel by config from repo`() {
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
    fun `When enrich channel If there is not channel config in repo Should update channel by default config`() {
        sut.run {
            val channel = randomChannel(type = "channelType")
            val defaultConfig = Config(name = "default")
            When calling configs.select("channelType") doReturn null

            channel.enrichChannel(emptyMap(), defaultConfig)

            channel.config `should be equal to` defaultConfig
        }
    }

    @Test
    fun `When enrich channel If there are messages for channel in the map And channel messages are empty Should update channel with these messages`() {
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
    fun `When enrich channel If there are messages for channel in the map And channel messages are not empty Should update channel with distinct set of messages`() {
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
    fun `When enrich channel If there are no messages for channel in the map Should not update channel messages`() {
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
