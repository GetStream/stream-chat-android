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

package io.getstream.chat.android.state.facade

import io.getstream.chat.android.client.query.pagination.AnyChannelPaginationRequest
import io.getstream.chat.android.models.Channel
import io.getstream.chat.android.models.ChannelConfig
import io.getstream.chat.android.models.Member
import io.getstream.chat.android.models.Message
import io.getstream.chat.android.models.Reaction
import io.getstream.chat.android.models.User
import io.getstream.chat.android.positiveRandomInt
import io.getstream.chat.android.randomCID
import io.getstream.chat.android.randomChannel
import io.getstream.chat.android.randomChannelUserRead
import io.getstream.chat.android.randomConfig
import io.getstream.chat.android.randomMember
import io.getstream.chat.android.randomMessage
import io.getstream.chat.android.randomReaction
import io.getstream.chat.android.randomUser
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.test.runTest
import org.amshove.kluent.`should be equal to`
import org.amshove.kluent.`should contain same`
import org.amshove.kluent.shouldBeEqualTo
import org.junit.jupiter.api.Test
import org.mockito.kotlin.check
import org.mockito.kotlin.doReturn
import org.mockito.kotlin.eq
import org.mockito.kotlin.verify
import org.mockito.kotlin.whenever

@ExperimentalCoroutinesApi
internal class RepositoryFacadeTests : BaseRepositoryFacadeTest() {

    @Test
    fun `Given request less than last message When select channels Should return channels from DB with empty messages`() = runTest {
        val paginationRequest = AnyChannelPaginationRequest(0)
        val user = randomUser(id = "userId")
        whenever(users.selectUser("userId")) doReturn user
        val channel1 = randomChannel(messages = emptyList(), id = "id1", type = "type", createdBy = user)
        val channel2 = randomChannel(messages = emptyList(), id = "id2", type = "type", createdBy = user)
        whenever(channels.selectChannels(eq(listOf("type:id1", "type:id2")))) doReturn listOf(channel1, channel2)

        val result = sut.selectChannels(listOf("type:id1", "type:id2"), paginationRequest)

        result.size shouldBeEqualTo 2
        result.any { it.cid == "type:id1" && it.messages.isEmpty() } shouldBeEqualTo true
        result.any { it.cid == "type:id2" && it.messages.isEmpty() } shouldBeEqualTo true
    }

    @Test
    fun `Given request more than last message When select channels Should return channels from DB with messages`(): Unit = runTest {
        val paginationRequest = AnyChannelPaginationRequest(100)
        val user = randomUser(id = "userId")
        whenever(users.selectUser("userId")) doReturn user
        val message1 = randomMessage(id = "messageId1", cid = "type:id1", user = user)
        val message2 = randomMessage(id = "messageId2", cid = "type:id2", user = user)
        whenever(messages.selectMessagesForChannel(eq("type:id1"), eq(paginationRequest))) doReturn listOf(
            message1,
        )
        whenever(messages.selectMessagesForChannel(eq("type:id2"), eq(paginationRequest))) doReturn listOf(
            message2,
        )
        val channel1 = randomChannel(messages = emptyList(), id = "id1", type = "type", createdBy = user)
        val channelEntity2 = randomChannel(messages = emptyList(), id = "id2", type = "type", createdBy = user)
        whenever(channels.selectChannels(eq(listOf("type:id1", "type:id2")))) doReturn listOf(
            channel1,
            channelEntity2,
        )

        val result = sut.selectChannels(listOf("type:id1", "type:id2"), paginationRequest)

        result.size shouldBeEqualTo 2
        result.any { it.cid == "type:id1" && it.messages.size == 1 && it.messages.first().id == "messageId1" } shouldBeEqualTo true
        result.any { it.cid == "type:id2" && it.messages.size == 1 && it.messages.first().id == "messageId2" } shouldBeEqualTo true
    }

    @Test
    fun `Given Db contains all required data When select messages Should return message list`() = runTest {
        val message1 = randomMessage()
        val message2 = randomMessage()
        whenever(messages.selectMessages(eq(listOf("messageId1", "messageId2")))) doReturn listOf(
            message1,
            message2,
        )

        val result = sut.selectMessages(listOf("messageId1", "messageId2"))

        result.size shouldBeEqualTo 2
    }

    @Test
    fun `When insert a channel, all participant users of this channel need to be stored`() = runTest {
        val memberUser = randomUser()
        val channelUser = randomUser()
        val userRead = randomUser()
        val messageUser = randomUser()
        val pinnedByUser = randomUser()
        val channel = randomChannel(
            createdBy = channelUser,
            members = listOf(Member(memberUser)),
            read = listOf(randomChannelUserRead(user = userRead)),
            messages = listOf(randomMessage(user = messageUser, pinnedBy = pinnedByUser)),
        )

        sut.insertChannel(channel)

        verify(channels).insertChannel(eq(channel))
        verify(users).insertUsers(
            check { listUser ->
                listUser.size `should be equal to` 5
                listUser `should contain same` listOf(memberUser, channelUser, userRead, messageUser, pinnedByUser)
            },
        )
    }

    @Test
    fun `When insert a message, all participant users of this message need to be stored`() = runTest {
        val messageUser = randomUser()
        val replyToUser = randomUser()
        val latestReactions = List(positiveRandomInt(10)) { randomReaction() }.toMutableList()
        val ownReactions = List(positiveRandomInt(10)) { randomReaction() }.toMutableList()
        val latestReactionUsers = latestReactions.mapNotNull(Reaction::user)
        val ownReactionUsers = ownReactions.mapNotNull(Reaction::user)
        val mentionedUsers = List(positiveRandomInt(10)) { randomUser() }.toMutableList()
        val threadParticipantsUsers = List(positiveRandomInt(10)) { randomUser() }.toMutableList()
        val pinnedByUser = randomUser()
        val expectedListOfUser =
            latestReactionUsers + ownReactionUsers + threadParticipantsUsers + mentionedUsers + replyToUser + messageUser + pinnedByUser
        val message = randomMessage(
            user = messageUser,
            replyTo = randomMessage(user = replyToUser, pinnedBy = null),
            latestReactions = latestReactions,
            ownReactions = ownReactions,
            mentionedUsers = mentionedUsers,
            threadParticipants = threadParticipantsUsers,
            pinnedBy = pinnedByUser,
        )
        sut.insertMessage(message)

        verify(messages).insertMessage(eq(message))
        verify(users).insertUsers(
            check { listUser ->
                listUser `should contain same` expectedListOfUser
            },
        )
    }

    @Test
    fun `When insert a list of channels, all participant users of these channels need to be stored`() = runTest {
        val (listOfUser: List<User>, listOfChannels: List<Channel>) =
            (0..positiveRandomInt(20)).fold((listOf<User>() to listOf<Channel>())) { acc, _ ->
                val memberUser = randomUser()
                val channelUser = randomUser()
                val userRead = randomUser()
                val messageUser = randomUser()
                val pinnedByUser = randomUser()
                val channel = randomChannel(
                    createdBy = channelUser,
                    members = listOf(Member(memberUser)),
                    read = listOf(randomChannelUserRead(user = userRead)),
                    messages = listOf(randomMessage(user = messageUser, pinnedBy = pinnedByUser)),
                )
                acc.first + listOf(
                    memberUser,
                    channelUser,
                    userRead,
                    messageUser,
                    pinnedByUser,
                ) to acc.second + channel
            }

        sut.insertChannels(listOfChannels)

        verify(channels).insertChannels(eq(listOfChannels))
        verify(users).insertUsers(
            check { listUser ->
                listUser `should contain same` listOfUser
            },
        )
    }

    @Test
    fun `When insert a list of messages, all participant users of these messages need to be stored`() = runTest {
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
                val pinnedByUser = randomUser()
                val message = randomMessage(
                    user = messageUser,
                    replyTo = randomMessage(user = replyToUser, pinnedBy = null),
                    latestReactions = latestReactions,
                    ownReactions = ownReactions,
                    mentionedUsers = mentionedUsers,
                    threadParticipants = threadParticipantsUsers,
                    pinnedBy = pinnedByUser,
                )
                (acc.first + latestReactionUsers + ownReactionUsers + threadParticipantsUsers + mentionedUsers + replyToUser + messageUser + pinnedByUser) to acc.second + message
            }

        sut.insertMessages(listOfMessages)

        verify(messages).insertMessages(eq(listOfMessages))
        verify(users).insertUsers(
            check { listUser ->
                listUser `should contain same` listOfUser
            },
        )
    }

    @Test
    fun `When insert a reaction, it should have a valid users and it need to be stored`() = runTest {
        val user = randomUser()
        val message = randomMessage(user = user)
        val reaction = randomReaction(user = user, messageId = message.id)
        whenever(messages.selectMessage(message.id)) doReturn message

        sut.insertReaction(reaction)

        verify(reactions).insertReaction(eq(reaction))
        verify(users).insertUser(user)
    }

    @Test
    fun `When updating members of a channels, they need to be stored`() = runTest {
        val usersList = List(positiveRandomInt(20)) { randomUser() }
        val members = usersList.map(::randomMember)
        val cid = randomCID()

        sut.updateMembersForChannel(cid, members)

        verify(users).insertUsers(usersList)
        verify(channels).updateMembersForChannel(cid, members)
    }

    @Test
    fun `When storing state for a channel, messages and config should be stored as well`() = runTest {
        val channel = randomChannel(
            config = randomConfig(),

        ).let { channel ->
            channel.copy(
                messages = (0..positiveRandomInt(20)).map { randomMessage(cid = channel.cid) },
            )
        }
        val expectedChannelsConfig = listOf(ChannelConfig(channel.type, channel.config))
        val expectedChannels = listOf(channel)
        val expectedMessages = channel.messages

        sut.storeStateForChannel(channel)

        verify(configs).insertChannelConfigs(expectedChannelsConfig)
        verify(channels).insertChannels(expectedChannels)
        verify(messages).insertMessages(expectedMessages)
    }

    @Test
    fun `When storing state for channels, messages and config should be stored as well`() = runTest {
        val channelsToBeInserted = (0..positiveRandomInt(20)).map {
            randomChannel(
                config = randomConfig(),
            ).let { channel ->
                channel.copy(
                    messages = (0..positiveRandomInt(20)).map { randomMessage(cid = channel.cid) },
                )
            }
        }
        val expectedChannelsConfig = channelsToBeInserted.map { ChannelConfig(it.type, it.config) }
        val expectedChannels = channelsToBeInserted
        val expectedMessages = channelsToBeInserted.flatMap { it.messages }

        sut.storeStateForChannels(channelsToBeInserted)

        verify(configs).insertChannelConfigs(expectedChannelsConfig)
        verify(channels).insertChannels(expectedChannels)
        verify(messages).insertMessages(expectedMessages)
    }
}
