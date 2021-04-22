package io.getstream.chat.android.offline.repository.facade

import com.nhaarman.mockitokotlin2.doReturn
import com.nhaarman.mockitokotlin2.eq
import com.nhaarman.mockitokotlin2.mock
import com.nhaarman.mockitokotlin2.verify
import com.nhaarman.mockitokotlin2.verifyZeroInteractions
import com.nhaarman.mockitokotlin2.whenever
import io.getstream.chat.android.client.models.Channel
import io.getstream.chat.android.client.models.ChannelUserRead
import io.getstream.chat.android.client.models.Member
import io.getstream.chat.android.client.models.Message
import io.getstream.chat.android.client.models.Reaction
import io.getstream.chat.android.client.models.User
import io.getstream.chat.android.livedata.randomChannel
import io.getstream.chat.android.livedata.randomMember
import io.getstream.chat.android.livedata.randomMessage
import io.getstream.chat.android.livedata.randomReaction
import io.getstream.chat.android.livedata.randomUser
import io.getstream.chat.android.offline.model.ChannelConfig
import io.getstream.chat.android.offline.request.AnyChannelPaginationRequest
import io.getstream.chat.android.test.positiveRandomInt
import io.getstream.chat.android.test.randomBoolean
import io.getstream.chat.android.test.randomCID
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.test.runBlockingTest
import org.amshove.kluent.`should be equal to`
import org.amshove.kluent.`should contain same`
import org.amshove.kluent.shouldBeEqualTo
import org.junit.jupiter.api.Test

@ExperimentalCoroutinesApi
internal class RepositoryFacadeTests : BaseRepositoryFacadeTest() {

    @Test
    fun `Given request less than last message When select channels Should return channels from DB with empty messages`() =
        runBlockingTest {
            val paginationRequest = AnyChannelPaginationRequest(0)
            val user = randomUser(id = "userId")
            whenever(users.selectUser("userId")) doReturn user
            val channel1 = randomChannel(messages = emptyList(), cid = "cid1", createdBy = user)
            val channel2 = randomChannel(messages = emptyList(), cid = "cid2", createdBy = user)
            whenever(channels.selectChannels(eq(listOf("cid1", "cid2")))) doReturn listOf(channel1, channel2)

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
            whenever(users.selectUser("userId")) doReturn user
            val message1 = randomMessage(id = "messageId1", cid = "cid1", user = user)
            val message2 = randomMessage(id = "messageId2", cid = "cid2", user = user)
            whenever(messages.selectMessagesForChannel(eq("cid1"), eq(paginationRequest))) doReturn listOf(
                message1
            )
            whenever(messages.selectMessagesForChannel(eq("cid2"), eq(paginationRequest))) doReturn listOf(
                message2
            )
            val channel1 = randomChannel(messages = emptyList(), cid = "cid1", createdBy = user)
            val channelEntity2 = randomChannel(messages = emptyList(), cid = "cid2", createdBy = user)
            whenever(channels.selectChannels(eq(listOf("cid1", "cid2")))) doReturn listOf(
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
        whenever(messages.selectMessages(eq(listOf("messageId1", "messageId2")))) doReturn listOf(message1, message2)

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

        verify(channels).insertChannel(eq(channel))
        verify(users).insertUsers(
            com.nhaarman.mockitokotlin2.check { listUser ->
                listUser.size `should be equal to` 4
                listUser `should contain same` listOf(memberUser, channelUser, userRead, messageUser)
            }
        )
    }

    @Test
    fun `When insert a message, all participant users of this message need to be stored`() = runBlockingTest {
        val messageUser = randomUser()
        val replyToUser = randomUser()
        val latestReactions = List(positiveRandomInt(10)) { randomReaction() }.toMutableList()
        val ownReactions = List(positiveRandomInt(10)) { randomReaction() }.toMutableList()
        val latestReactionUsers = latestReactions.mapNotNull(Reaction::user)
        val ownReactionUsers = ownReactions.mapNotNull(Reaction::user)
        val mentionedUsers = List(positiveRandomInt(10)) { randomUser() }.toMutableList()
        val threadParticipantsUsers = List(positiveRandomInt(10)) { randomUser() }.toMutableList()
        val expectedListOfUser = latestReactionUsers + ownReactionUsers + threadParticipantsUsers + mentionedUsers + replyToUser + messageUser
        val message = randomMessage(
            user = messageUser,
            replyTo = randomMessage(user = replyToUser),
            latestReactions = latestReactions,
            ownReactions = ownReactions,
            mentionedUsers = mentionedUsers,
            threadParticipants = threadParticipantsUsers,
        )
        val cache = randomBoolean()
        sut.insertMessage(message, cache)

        verify(messages).insertMessage(eq(message), eq(cache))
        verify(users).insertUsers(
            com.nhaarman.mockitokotlin2.check { listUser ->
                listUser `should contain same` expectedListOfUser
            }
        )
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

            verify(channels).insertChannels(eq(listOfChannels))
            verify(users).insertUsers(
                com.nhaarman.mockitokotlin2.check { listUser ->
                    listUser `should contain same` listOfUser
                }
            )
        }

    @Test
    fun `When insert a list of messages, all participant users of these messages need to be stored`() =
        runBlockingTest {
            val (listOfUser: List<User>, listOfMessages: List<Message>) =
                (0..positiveRandomInt(20)).fold((listOf<User>() to listOf<Message>())) { acc, _ ->
                    val messageUser = randomUser()
                    val replyToUser = randomUser()
                    val latestReactions = List(positiveRandomInt(10)) { randomReaction() }.toMutableList()
                    val ownReactions = List(positiveRandomInt(10)) { randomReaction() }.toMutableList()
                    val latestReactionUsers = latestReactions.mapNotNull(Reaction::user)
                    val ownReactionUsers = ownReactions.mapNotNull(Reaction::user)
                    val mentionedUsers = List(positiveRandomInt(10)) { randomUser() }.toMutableList()
                    val threadParticipantsUsers = List(positiveRandomInt(10)) { randomUser() }.toMutableList()
                    val message = randomMessage(
                        user = messageUser,
                        replyTo = randomMessage(user = replyToUser),
                        latestReactions = latestReactions,
                        ownReactions = ownReactions,
                        mentionedUsers = mentionedUsers,
                        threadParticipants = threadParticipantsUsers,
                    )
                    (acc.first + latestReactionUsers + ownReactionUsers + threadParticipantsUsers + mentionedUsers + replyToUser + messageUser) to acc.second + message
                }
            val cache = randomBoolean()

            sut.insertMessages(listOfMessages, cache)

            verify(messages).insertMessages(eq(listOfMessages), eq(cache))
            verify(users).insertUsers(
                com.nhaarman.mockitokotlin2.check { listUser ->
                    listUser `should contain same` listOfUser
                }
            )
        }

    @Test
    fun `When insert a reaction, it should have a valid users and it need to be stored`() = runBlockingTest {
        val user = randomUser()
        val reaction = randomReaction(user = user)

        sut.insertReaction(reaction)

        verify(reactions).insertReaction(eq(reaction))
        verify(users).insertUser(user)
    }

    @Test
    fun `When updating members of a channels, they need to be stored`() = runBlockingTest {
        val usersList = List(positiveRandomInt(20)) { randomUser() }
        val members = usersList.map(::randomMember)
        val cid = randomCID()

        sut.updateMembersForChannel(cid, members)

        verify(users).insertUsers(usersList)
        verify(channels).updateMembersForChannel(cid, members)
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

            verify(configs).insertChannelConfigs(configList)
            verify(users).insertUsers(userList)
            verify(channels).insertChannels(channelList)
            verify(messages).insertMessages(messageList, false)
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

            verifyZeroInteractions(configs)
            verify(users).insertUsers(userList)
            verify(channels).insertChannels(channelList)
            verify(messages).insertMessages(messageList, false)
        }
    }
}
