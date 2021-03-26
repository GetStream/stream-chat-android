package io.getstream.chat.android.livedata.repository

import com.nhaarman.mockitokotlin2.argThat
import com.nhaarman.mockitokotlin2.mock
import com.nhaarman.mockitokotlin2.verify
import com.nhaarman.mockitokotlin2.verifyZeroInteractions
import com.nhaarman.mockitokotlin2.whenever
import io.getstream.chat.android.client.models.Reaction
import io.getstream.chat.android.client.models.User
import io.getstream.chat.android.client.utils.SyncStatus
import io.getstream.chat.android.livedata.randomReaction
import io.getstream.chat.android.livedata.randomUser
import io.getstream.chat.android.livedata.repository.domain.reaction.ReactionDao
import io.getstream.chat.android.livedata.repository.domain.reaction.ReactionRepository
import io.getstream.chat.android.livedata.repository.domain.reaction.ReactionRepositoryImpl
import io.getstream.chat.android.livedata.repository.domain.reaction.toEntity
import io.getstream.chat.android.test.randomString
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.runBlocking
import kotlinx.coroutines.test.runBlockingTest
import org.amshove.kluent.`should be equal to`
import org.junit.Before
import org.junit.Test
import org.junit.jupiter.api.assertThrows

@ExperimentalCoroutinesApi
internal class ReactionRepositoryTest {
    private lateinit var reactionDao: ReactionDao
    private lateinit var currentUser: User
    private lateinit var reactionRepo: ReactionRepository

    @Before
    fun setup() {
        runBlocking {
            currentUser = randomUser()
            reactionDao = mock()
            reactionRepo = ReactionRepositoryImpl(reactionDao) { currentUser }
        }
    }

    @Test
    fun `Reaction should be stored in Db`() = runBlockingTest {
        val messgId = randomString(10)
        val reaction = randomReaction(messageId = messgId, user = currentUser, type = "love")

        reactionRepo.insertReaction(reaction)

        verify(reactionDao).insert(
            argThat {
                this.messageId == messageId && this.userId == currentUser.id
            }
        )
    }

    @Test
    fun `Shouldn't allow storing reaction with empty messageId`() = runBlockingTest {
        val reaction = Reaction(messageId = "", user = currentUser, type = "love")

        assertThrows<IllegalArgumentException> { reactionRepo.insertReaction(reaction) }

        verifyZeroInteractions(reactionDao)
    }

    @Test
    fun `Shouldn't allow storing reaction with empty type`() = runBlockingTest {
        val reaction = Reaction(messageId = randomString(10), user = currentUser, type = "")

        assertThrows<IllegalArgumentException> { reactionRepo.insertReaction(reaction) }

        verifyZeroInteractions(reactionDao)
    }

    @Test
    fun `Shouldn't allow storing reaction with empty user`() = runBlockingTest {
        val reaction = Reaction(messageId = randomString(10), user = null, type = "love")

        assertThrows<IllegalArgumentException> { reactionRepo.insertReaction(reaction) }

        verifyZeroInteractions(reactionDao)
    }

    @Test
    fun `Should return reactions with syncNeeded==true containing user data`() = runBlockingTest {
        whenever(reactionDao.selectSyncNeeded()).thenReturn(listOf(randomReaction(syncStatus = SyncStatus.SYNC_NEEDED).toEntity()))

        reactionRepo.selectReactionsSyncNeeded().apply {
            this.size `should be equal to` 1
            this.first().user `should be equal to` currentUser
        }
    }

    @Test
    fun `Should return ReactionEntity containing user data`() = runBlockingTest {
        val messageId = randomString(10)
        whenever(reactionDao.selectUserReactionsToMessage(messageId, currentUser.id)).thenReturn(listOf(randomReaction(syncStatus = SyncStatus.SYNC_NEEDED).toEntity()))

        reactionRepo.selectUserReactionsToMessage(messageId, currentUser.id).apply {
            this.size `should be equal to` 1
            this.first().user `should be equal to` currentUser
        }
    }
}
