package io.getstream.chat.android.livedata.repository.helper

import com.nhaarman.mockitokotlin2.doReturn
import com.nhaarman.mockitokotlin2.eq
import com.nhaarman.mockitokotlin2.mock
import io.getstream.chat.android.client.models.Channel
import io.getstream.chat.android.client.models.ChannelUserRead
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
import org.amshove.kluent.VerifyNoInteractions
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
            When calling users.selectUser("userId") doReturn user
            val channel1 = randomChannel(messages = emptyList(), cid = "cid1", createdBy = user)
            val channel2 = randomChannel(messages = emptyList(), cid = "cid2", createdBy = user)
            When calling channels.selectChannels(eq(listOf("cid1", "cid2"))) doReturn listOf(channel1, channel2)

            val result = sut.selectChannels(listOf("cid1", "cid2"), paginationRequest)

            result.size shouldBeEqualTo 2
            result.any { it.cid == "cid1" && it.messages.isEmpty() } shouldBeEqualTo true
            result.any { it.cid == "cid2" && it.messages.isEmpty() } shouldBeEqualTo true
        }

    @Test
    fun `Given request more than last message When select channels Should return channels from DB with messages`() =
        runBlockingTest {
            val paginationRequest = AnyChannelPaginationRequest(100)
            val user = randomUser(id = "userId")
            When calling users.selectUser("userId") doReturn user
            val message1 = randomMessage(id = "messageId1", cid = "cid1", user = user)
            val message2 = randomMessage(id = "messageId2", cid = "cid2", user = user)
            When calling messages.selectMessagesForChannel(eq("cid1"), eq(paginationRequest)) doReturn listOf(
                message1
            )
            When calling messages.selectMessagesForChannel(eq("cid2"), eq(paginationRequest)) doReturn listOf(
                message2
            )
            val channel1 = randomChannel(messages = emptyList(), cid = "cid1", createdBy = user)
            val channelEntity2 = randomChannel(messages = emptyList(), cid = "cid2", createdBy = user)
            When calling channels.selectChannels(eq(listOf("cid1", "cid2"))) doReturn listOf(
                channel1,
                channelEntity2
            )

            val result = sut.selectChannels(listOf("cid1", "cid2"), paginationRequest)

            result.size shouldBeEqualTo 2
            result.any { it.cid == "cid1" && it.messages.size == 1 && it.messages.first().id == "messageId1" } shouldBeEqualTo true
            result.any { it.cid == "cid2" && it.messages.size == 1 && it.messages.first().id == "messageId2" } shouldBeEqualTo true
        }

    @Test
    fun `Given Db contains all required data When select messages Should return message list`() = runBlockingTest {
        val message1 = randomMessage()
        val message2 = randomMessage()
        When calling messages.selectMessages(eq(listOf("messageId1", "messageId2"))) doReturn listOf(message1, message2)

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

        Verify on channels that channels.insertChannel(eq(channel)) was called
        Verify on users that users.insertUsers(
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
            Verify on users that users.insertUsers(
                com.nhaarman.mockitokotlin2.check { listUser ->
                    listUser `should contain same` listOfUser
                }
            ) was called
        }

    @Test
    fun `Proves that correct methods are called in storeStateForChannels`() {
        runBlockingTest {
            val configList = listOf<ChannelConfig>(mock())
            val userList = listOf(randomUser())
            val channelList = listOf(randomChannel())
            val messageList = listOf(randomMessage())

            sut.storeStateForChannels(
                configs = configList,
                users = userList,
                channels = channelList,
                messages = messageList,
                cacheForMessages = false
            )

            Verify on configs that configs.insert(configList) was called
            Verify on users that users.insertUsers(userList) was called
            Verify on channels that channels.insertChannels(channelList) was called
            Verify on messages that messages.insertMessages(messageList, false) was called
        }
    }

    @Test
    fun `Proves that configs are not change if null is passed`() {
        runBlockingTest {
            val userList = listOf(randomUser())
            val channelList = listOf(randomChannel())
            val messageList = listOf(randomMessage())

            sut.storeStateForChannels(
                configs = null,
                users = userList,
                channels = channelList,
                messages = messageList,
                cacheForMessages = false
            )

            VerifyNoInteractions on configs
            Verify on users that users.insertUsers(userList) was called
            Verify on channels that channels.insertChannels(channelList) was called
            Verify on messages that messages.insertMessages(messageList, false) was called
        }
    }
}
