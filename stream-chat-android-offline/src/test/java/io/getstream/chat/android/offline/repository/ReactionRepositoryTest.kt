package io.getstream.chat.android.offline.repository

import io.getstream.chat.android.client.models.User
import io.getstream.chat.android.client.utils.SyncStatus
import io.getstream.chat.android.offline.randomReaction
import io.getstream.chat.android.offline.randomUser
import io.getstream.chat.android.offline.repository.domain.reaction.internal.ReactionDao
import io.getstream.chat.android.offline.repository.domain.reaction.internal.ReactionRepository
import io.getstream.chat.android.offline.repository.domain.reaction.internal.ReactionRepositoryImpl
import io.getstream.chat.android.offline.repository.domain.reaction.internal.toEntity
import io.getstream.chat.android.test.randomString
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.runBlocking
import kotlinx.coroutines.test.runBlockingTest
import org.amshove.kluent.coInvoking
import org.amshove.kluent.`should be equal to`
import org.amshove.kluent.shouldThrow
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.mockito.kotlin.argThat
import org.mockito.kotlin.mock
import org.mockito.kotlin.verify
import org.mockito.kotlin.verifyNoInteractions
import org.mockito.kotlin.whenever

@ExperimentalCoroutinesApi
internal class ReactionRepositoryTest {
    private lateinit var reactionDao: ReactionDao
    private lateinit var currentUser: User
    private lateinit var reactionRepo: ReactionRepository

    @BeforeEach
    fun setup() {
        runBlocking {
            currentUser = randomUser()
            reactionDao = mock()
            reactionRepo = ReactionRepositoryImpl(reactionDao) { currentUser }
        }
    }

    @Test
    fun `Given valid reaction when it's saved with repo dao should store it in DB`() = runBlockingTest {
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
    fun `Given reaction with empty messageId when it's saved in repo dao shouldn't store it to DB`() = runBlockingTest {
        val reaction = randomReaction(messageId = "", user = currentUser, type = "love")

        coInvoking { reactionRepo.insertReaction(reaction) }.shouldThrow(IllegalArgumentException::class)

        verifyNoInteractions(reactionDao)
    }

    @Test
    fun `Given reaction with empty type when it's saved in repo dao shouldn't store it to DB`() = runBlockingTest {
        val reaction = randomReaction(messageId = randomString(10), user = currentUser, type = "")

        coInvoking { reactionRepo.insertReaction(reaction) }.shouldThrow(IllegalArgumentException::class)

        verifyNoInteractions(reactionDao)
    }

    @Test
    fun `Given reaction with empty userId when it's saved in repo dao shouldn't store it to DB`() = runBlockingTest {
        val reaction = randomReaction(messageId = randomString(10), userId = "", type = "love")

        coInvoking { reactionRepo.insertReaction(reaction) }.shouldThrow(IllegalArgumentException::class)

        verifyNoInteractions(reactionDao)
    }

    @Test
    fun `When dao returns reactions with syncNeeded == true they should contain current user data`() = runBlockingTest {
        whenever(reactionDao.selectSyncStatus(SyncStatus.SYNC_NEEDED))
            .thenReturn(listOf(randomReaction(syncStatus = SyncStatus.SYNC_NEEDED).toEntity()))

        reactionRepo.selectReactionsBySyncStatus(SyncStatus.SYNC_NEEDED).apply {
            this.size `should be equal to` 1
            this.first().user `should be equal to` currentUser
        }
    }
}
