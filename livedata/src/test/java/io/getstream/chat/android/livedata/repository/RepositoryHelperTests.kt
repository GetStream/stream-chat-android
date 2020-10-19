package io.getstream.chat.android.livedata.repository

import com.nhaarman.mockitokotlin2.doReturn
import com.nhaarman.mockitokotlin2.mock
import io.getstream.chat.android.client.ChatClient
import io.getstream.chat.android.livedata.ChatDatabase
import io.getstream.chat.android.livedata.randomCID
import io.getstream.chat.android.livedata.randomChannelEntity
import io.getstream.chat.android.livedata.randomChannelUserReadEntity
import io.getstream.chat.android.livedata.randomMemberEntity
import io.getstream.chat.android.livedata.randomMessageEntity
import io.getstream.chat.android.livedata.randomReactionEntity
import io.getstream.chat.android.livedata.randomUser
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.test.TestCoroutineScope
import org.amshove.kluent.shouldBeEqualTo
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test

@ExperimentalCoroutinesApi
internal class RepositoryHelperTests {

    private lateinit var chatClient: ChatClient
    private lateinit var chatDatabase: ChatDatabase
    private lateinit var sut: RepositoryHelper
    private val scope = TestCoroutineScope()

    @BeforeEach
    fun setUp() {
        chatClient = mock()
        chatDatabase = mock {
            on { queryChannelsQDao() } doReturn mock()
            on { userDao() } doReturn mock()
            on { reactionDao() } doReturn mock()
            on { messageDao() } doReturn mock()
            on { channelStateDao() } doReturn mock()
            on { channelConfigDao() } doReturn mock()
            on { syncStateDao() } doReturn mock()
        }
        sut = RepositoryHelper(chatClient, randomUser(), chatDatabase, scope)
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
}
