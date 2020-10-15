package io.getstream.chat.android.livedata.repository

import com.nhaarman.mockitokotlin2.doReturn
import com.nhaarman.mockitokotlin2.eq
import com.nhaarman.mockitokotlin2.mock
import io.getstream.chat.android.livedata.dao.MessageDao
import io.getstream.chat.android.livedata.randomMessageEntity
import io.getstream.chat.android.livedata.randomReactionEntity
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.test.runBlockingTest
import org.amshove.kluent.When
import org.amshove.kluent.any
import org.amshove.kluent.calling
import org.amshove.kluent.shouldBeEqualTo
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test

@ExperimentalCoroutinesApi
internal class MessageRepositoryTests {

    private lateinit var messageDao: MessageDao

    private lateinit var sut: MessageRepository

    @BeforeEach
    fun setup() {
        messageDao = mock()
        sut = MessageRepository(messageDao)
    }

    @Test
    fun `Should select user ids from messages by channel ids`() = runBlockingTest {
        val channelId = "channelId"
        val userId1 = "userId1"
        val userId2 = "userId2"
        val reactionUserId1 = "reactionUserId1"
        val reactionUserId2 = "reactionUserId2"
        val reactionUserId3 = "reactionUserId3"
        val messageEntity1 = randomMessageEntity(userId = userId1, latestReactions = listOf(randomReactionEntity(userId = reactionUserId1), randomReactionEntity(userId = reactionUserId2)))
        val messageEntity2 = randomMessageEntity(userId = userId2, latestReactions = listOf(randomReactionEntity(userId = reactionUserId3)))
        When calling messageDao.messagesForChannel(eq(channelId), any()) doReturn listOf(messageEntity1, messageEntity2)

        val result = sut.selectUserIdsFromMessagesByChannelsIds(listOf(channelId), mock())

        result shouldBeEqualTo setOf(userId1, userId2, reactionUserId1, reactionUserId2, reactionUserId3)
    }
}
