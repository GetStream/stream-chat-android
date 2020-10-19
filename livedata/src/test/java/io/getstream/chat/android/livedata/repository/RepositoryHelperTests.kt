package io.getstream.chat.android.livedata.repository

import com.nhaarman.mockitokotlin2.any
import com.nhaarman.mockitokotlin2.doReturn
import com.nhaarman.mockitokotlin2.mock
import io.getstream.chat.android.livedata.randomCID
import io.getstream.chat.android.livedata.randomChannelEntity
import io.getstream.chat.android.livedata.randomChannelUserReadEntity
import io.getstream.chat.android.livedata.randomMemberEntity
import io.getstream.chat.android.livedata.randomMessageEntity
import io.getstream.chat.android.livedata.randomReactionEntity
import io.getstream.chat.android.livedata.randomUser
import io.getstream.chat.android.livedata.request.AnyChannelPaginationRequest
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.test.TestCoroutineScope
import kotlinx.coroutines.test.runBlockingTest
import org.amshove.kluent.When
import org.amshove.kluent.calling
import org.amshove.kluent.shouldBeEqualTo
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
            reactionUserId2
        )
    }

    @Test
    fun `Given request less than last message When select channels Should return channels from DB with empty messages`() = runBlockingTest {
        val paginationRequest = mock<AnyChannelPaginationRequest>()
        When calling paginationRequest.memberLimit doReturn 0
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

        val result = sut.selectChannels(listOf("cid1", "cid2"), paginationRequest, mock())

        result.size shouldBeEqualTo 2
        result.any { it.cid == "cid1" && it.messages.isEmpty() } shouldBeEqualTo true
        result.any { it.cid == "cid2" && it.messages.isEmpty() } shouldBeEqualTo true
    }
}
