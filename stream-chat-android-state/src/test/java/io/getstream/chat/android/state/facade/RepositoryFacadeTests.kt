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
import io.getstream.chat.android.client.test.randomChannel
import io.getstream.chat.android.client.test.randomMember
import io.getstream.chat.android.client.test.randomMessage
import io.getstream.chat.android.client.test.randomReaction
import io.getstream.chat.android.client.test.randomUser
import io.getstream.chat.android.models.Channel
import io.getstream.chat.android.models.ChannelConfig
import io.getstream.chat.android.models.ChannelUserRead
import io.getstream.chat.android.models.Member
import io.getstream.chat.android.models.Message
import io.getstream.chat.android.models.Reaction
import io.getstream.chat.android.models.User
import io.getstream.chat.android.test.positiveRandomInt
import io.getstream.chat.android.test.randomBoolean
import io.getstream.chat.android.test.randomCID
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.test.runTest
import org.amshove.kluent.`should be equal to`
import org.amshove.kluent.`should contain same`
import org.amshove.kluent.shouldBeEqualTo
import org.junit.jupiter.api.Test
import org.mockito.kotlin.any
import org.mockito.kotlin.check
import org.mockito.kotlin.doReturn
import org.mockito.kotlin.eq
import org.mockito.kotlin.mock
import org.mockito.kotlin.verify
import org.mockito.kotlin.verifyNoInteractions
import org.mockito.kotlin.whenever

@ExperimentalCoroutinesApi
internal class RepositoryFacadeTests : BaseRepositoryFacadeTest() {

    @Test
    fun `Given request less than last message When select channels Should return channels from DB with empty messages`() =
        runTest {
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
    fun `Given request more than last message When select channels Should return channels from DB with messages`(): Unit =
        runTest {
            val paginationRequest = AnyChannelPaginationRequest(100)
            val user = randomUser(id = "userId")
            whenever(users.selectUser("userId")) doReturn user
            val message1 = randomMessage(id = "messageId1", cid = "type:id1", user = user)
            val message2 = randomMessage(id = "messageId2", cid = "type:id2", user = user)
            whenever(messages.selectMessagesForChannel(eq("type:id1"), eq(paginationRequest))) doReturn listOf(
                message1
            )
            whenever(messages.selectMessagesForChannel(eq("type:id2"), eq(paginationRequest))) doReturn listOf(
                message2
            )
            val channel1 = randomChannel(messages = emptyList(), id = "id1", type = "type", createdBy = user)
            val channelEntity2 = randomChannel(messages = emptyList(), id = "id2", type = "type", createdBy = user)
            whenever(channels.selectChannels(eq(listOf("type:id1", "type:id2")))) doReturn listOf(
                channel1,
                channelEntity2
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
        whenever(messages.selectMessages(eq(listOf("messageId1", "messageId2")), any())) doReturn listOf(
            message1,
            message2
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
            read = listOf(ChannelUserRead(userRead)),
            messages = listOf(randomMessage(user = messageUser, pinnedBy = pinnedByUser)),
        )

        sut.upsertChannel(channel)

        verify(channels).upsertChannel(eq(channel))
        verify(users).insertUsers(
            check { listUser ->
                listUser.size `should be equal to` 5
                listUser `should contain same` listOf(memberUser, channelUser, userRead, messageUser, pinnedByUser)
            }
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
        val cache = randomBoolean()
        sut.insertMessage(message, cache)

        verify(messages).insertMessage(eq(message), eq(cache))
        verify(users).insertUsers(
            check { listUser ->
                listUser `should contain same` expectedListOfUser
            }
        )
    }

    @Test
    fun `When insert a list of channels, all participant users of these channels need to be stored`() =
        runTest {
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
                        read = listOf(ChannelUserRead(userRead)),
                        messages = listOf(randomMessage(user = messageUser, pinnedBy = pinnedByUser)),
                    )
                    acc.first + listOf(
                        memberUser,
                        channelUser,
                        userRead,
                        messageUser,
                        pinnedByUser
                    ) to acc.second + channel
                }

            sut.upsertChannels(listOfChannels)

            verify(channels).upsertChannels(eq(listOfChannels))
            verify(users).insertUsers(
                check { listUser ->
                    listUser `should contain same` listOfUser
                }
            )
        }

    @Test
    fun `When insert a list of messages, all participant users of these messages need to be stored`() =
        runTest {
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
            val cache = randomBoolean()

            sut.insertMessages(listOfMessages, cache)

            verify(messages).insertMessages(eq(listOfMessages), eq(cache))
            verify(users).insertUsers(
                check { listUser ->
                    listUser `should contain same` listOfUser
                }
            )
        }

    @Test
    fun `When insert a reaction, it should have a valid users and it need to be stored`() = runTest {
        val user = randomUser()
        val reaction = randomReaction(user = user)

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
    fun `Proves that correct methods are called in storeStateForChannels`() {
        runTest {
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
            verify(channels).upsertChannels(channelList)
            verify(messages).insertMessages(messageList, false)
        }
    }

    @Test
    fun `Proves that configs are not change if null is passed`() = runTest {
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

        verifyNoInteractions(configs)
        verify(users).insertUsers(userList)
        verify(channels).upsertChannels(channelList)
        verify(messages).insertMessages(messageList, false)
    }
}
