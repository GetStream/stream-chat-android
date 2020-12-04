package io.getstream.chat.android.livedata.repository

import com.nhaarman.mockitokotlin2.any
import com.nhaarman.mockitokotlin2.doReturn
import com.nhaarman.mockitokotlin2.eq
import com.nhaarman.mockitokotlin2.mock
import io.getstream.chat.android.client.models.Channel
import io.getstream.chat.android.client.models.ChannelUserRead
import io.getstream.chat.android.client.models.Member
import io.getstream.chat.android.client.models.User
import io.getstream.chat.android.livedata.extensions.users
import io.getstream.chat.android.livedata.randomChannel
import io.getstream.chat.android.livedata.randomChannelEntity
import io.getstream.chat.android.livedata.randomChannelUserReadEntity
import io.getstream.chat.android.livedata.randomMemberEntity
import io.getstream.chat.android.livedata.randomMessage
import io.getstream.chat.android.livedata.randomMessageEntity
import io.getstream.chat.android.livedata.randomReactionEntity
import io.getstream.chat.android.livedata.randomUser
import io.getstream.chat.android.livedata.request.AnyChannelPaginationRequest
import io.getstream.chat.android.test.positiveRandomInt
import io.getstream.chat.android.test.randomCID
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.test.TestCoroutineScope
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
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test

@ExperimentalCoroutinesApi
internal class RepositoryHelperTests {

    private lateinit var users: UserRepository
    private lateinit var configs: ChannelConfigRepository
    private lateinit var channels: ChannelRepository
    private lateinit var queryChannels: QueryChannelsRepository
    private lateinit var messages: MessageRepository
    private lateinit var reactions: ReactionRepository
    private lateinit var syncState: SyncStateRepository

    private val scope = TestCoroutineScope()

    private lateinit var sut: RepositoryHelper

    @BeforeEach
    fun setUp() {
        users = mock()
        configs = mock()
        channels = mock()
        queryChannels = mock()
        messages = mock()
        reactions = mock()
        syncState = mock()
        val factory: RepositoryFactory = mock {
            on { createUserRepository() } doReturn users
            on { createChannelConfigRepository() } doReturn configs
            on { createChannelRepository() } doReturn channels
            on { createQueryChannelsRepository() } doReturn queryChannels
            on { createMessageRepository() } doReturn messages
            on { createReactionRepository() } doReturn reactions
            on { createSyncStateRepository() } doReturn syncState
        }
        sut = RepositoryHelper(factory, scope)
    }

    @Test
    fun `When calculate user Ids for list of channels should return list of user IDs according to channels`() {
        val cid = randomCID()
        val authorId1 = "authorId1"
        val authorId2 = "authorId2"
        val memberId1 = "memberId1"
        val memberId2 = "memberId2"
        val readId1 = "readId1"
        val readId2 = "readId2"
        val lastMessageUserId = "lastMessageUserId"
        val lastMessageReactionUserId = "lastMessageReactionUserId"
        val channelEntity1 = randomChannelEntity().apply {
            this.cid = cid
            this.createdByUserId = authorId1
            members.putAll(
                listOf(
                    memberId1 to randomMemberEntity(memberId1),
                    memberId2 to randomMemberEntity(memberId2)
                )
            )
            reads.putAll(
                listOf(
                    readId1 to randomChannelUserReadEntity(readId1),
                    readId2 to randomChannelUserReadEntity(readId2)
                )
            )
            lastMessage = randomMessageEntity(
                userId = lastMessageUserId,
                latestReactions = listOf(randomReactionEntity(userId = lastMessageReactionUserId))
            )
        }
        val channelEntity2 = randomChannelEntity().apply { createdByUserId = authorId2 }
        val channels = listOf(channelEntity1, channelEntity2)
        val messageUserId1 = "messageUserId1"
        val messageUserId2 = "messageUserId2"
        val reactionUserId1 = "reactionUserId1"
        val reactionUserId2 = "reactionUserId2"
        val message1 = randomMessageEntity(
            cid = cid,
            userId = messageUserId1,
            latestReactions = listOf(
                randomReactionEntity(userId = reactionUserId1),
                randomReactionEntity(userId = reactionUserId2)
            )
        )
        val message2 = randomMessageEntity(cid = cid, userId = messageUserId2)
        val messageMap = mapOf(cid to listOf(message1, message2))

        val result = sut.calculateUserIds(channels, messageMap)

        result shouldBeEqualTo setOf(
            authorId1,
            authorId2,
            memberId1,
            memberId2,
            readId1,
            readId2,
            messageUserId1,
            messageUserId2,
            reactionUserId1,
            reactionUserId2,
            lastMessageReactionUserId,
            lastMessageUserId
        )
    }

    @Test
    fun `Given request less than last message When select channels Should return channels from DB with empty messages`() =
        runBlockingTest {
            val paginationRequest = AnyChannelPaginationRequest(0)
            When calling users.selectUserMap(any()) doReturn mapOf("userId" to randomUser(id = "userId"))
            val channelEntity1 = randomChannelEntity().apply {
                cid = "cid1"
                createdByUserId = "userId"
            }
            val channelEntity2 = randomChannelEntity().apply {
                cid = "cid2"
                createdByUserId = "userId"
            }
            When calling channels.select(listOf("cid1", "cid2")) doReturn listOf(channelEntity1, channelEntity2)

            val result = sut.selectChannels(listOf("cid1", "cid2"), mock(), paginationRequest)

            result.size shouldBeEqualTo 2
            result.any { it.cid == "cid1" && it.messages.isEmpty() } shouldBeEqualTo true
            result.any { it.cid == "cid2" && it.messages.isEmpty() } shouldBeEqualTo true
        }

    @Test
    fun `Given request more than last message When select channels Should return channels from DB with messages`() =
        runBlockingTest {
            val paginationRequest = AnyChannelPaginationRequest(100)
            When calling users.selectUserMap(any()) doReturn mapOf("userId" to randomUser(id = "userId"))
            val messageEntity1 = randomMessageEntity(id = "messageId1", cid = "cid1", userId = "userId")
            val messageEntity2 = randomMessageEntity(id = "messageId2", cid = "cid2", userId = "userId")
            When calling messages.selectMessagesEntitiesForChannel("cid1", paginationRequest) doReturn listOf(
                messageEntity1
            )
            When calling messages.selectMessagesEntitiesForChannel("cid2", paginationRequest) doReturn listOf(
                messageEntity2
            )
            val channelEntity1 = randomChannelEntity().apply {
                cid = "cid1"
                createdByUserId = "userId"
            }
            val channelEntity2 = randomChannelEntity().apply {
                cid = "cid2"
                createdByUserId = "userId"
            }
            When calling channels.select(listOf("cid1", "cid2")) doReturn listOf(channelEntity1, channelEntity2)

            val result = sut.selectChannels(listOf("cid1", "cid2"), mock(), paginationRequest)

            result.size shouldBeEqualTo 2
            result.any { it.cid == "cid1" && it.messages.size == 1 && it.messages.first().id == "messageId1" } shouldBeEqualTo true
            result.any { it.cid == "cid2" && it.messages.size == 1 && it.messages.first().id == "messageId2" } shouldBeEqualTo true
        }

    @Test
    fun `Given Db contains all required data When select messages Should return message list`() = runBlockingTest {
        val reaction1 = randomReactionEntity(userId = "reactionUserId1")
        val reaction2 = randomReactionEntity(userId = "reactionUserId2")
        val message1 = randomMessageEntity(
            id = "messageId1",
            userId = "messageUserId1",
            latestReactions = listOf(reaction1, reaction2)
        )
        val message2 = randomMessageEntity(id = "messageId2", userId = "messageUserId2")
        When calling messages.selectEntities(listOf("messageId1", "messageId2")) doReturn listOf(message1, message2)
        When calling users.selectUserMap(
            listOf(
                "reactionUserId1",
                "reactionUserId2",
                "messageUserId1",
                "messageUserId2"
            )
        ) doReturn listOf(
            randomUser(id = "reactionUserId1"),
            randomUser(id = "reactionUserId2"),
            randomUser(id = "messageUserId1"),
            randomUser(id = "messageUserId2")
        ).associateBy(User::id)

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
    fun `When insert a list of channels, all participant users of these channels need to be stored`() = runBlockingTest {
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
}
