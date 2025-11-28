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

package io.getstream.chat.android.offline.repository

import io.getstream.chat.android.client.persistance.repository.ReactionRepository
import io.getstream.chat.android.models.SyncStatus
import io.getstream.chat.android.models.User
import io.getstream.chat.android.offline.repository.domain.reaction.internal.DatabaseReactionRepository
import io.getstream.chat.android.offline.repository.domain.reaction.internal.ReactionDao
import io.getstream.chat.android.offline.repository.domain.reaction.internal.toEntity
import io.getstream.chat.android.randomReaction
import io.getstream.chat.android.randomString
import io.getstream.chat.android.randomUser
import io.getstream.chat.android.test.TestCoroutineRule
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.test.runTest
import org.amshove.kluent.coInvoking
import org.amshove.kluent.`should be equal to`
import org.amshove.kluent.shouldThrow
import org.junit.Rule
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.mockito.kotlin.argThat
import org.mockito.kotlin.mock
import org.mockito.kotlin.verify
import org.mockito.kotlin.verifyNoInteractions
import org.mockito.kotlin.whenever

@ExperimentalCoroutinesApi
internal class ReactionRepositoryTest {
    @get:Rule
    val testCoroutines: TestCoroutineRule = TestCoroutineRule()

    private lateinit var reactionDao: ReactionDao
    private lateinit var currentUser: User
    private lateinit var reactionRepo: ReactionRepository

    @BeforeEach
    fun setup() {
        runTest {
            currentUser = randomUser()
            reactionDao = mock()
            reactionRepo = DatabaseReactionRepository(testCoroutines.scope, reactionDao) { currentUser }
        }
    }

    @Test
    fun `Given valid reaction when it's saved with repo dao should store it in DB`() = runTest {
        val messgId = randomString(10)
        val reaction = randomReaction(messageId = messgId, user = currentUser, type = "love")

        reactionRepo.insertReaction(reaction)

        verify(reactionDao).insert(
            argThat {
                this.messageId == messageId && this.userId == currentUser.id
            },
        )
    }

    @Test
    fun `Given reaction with empty messageId when it's saved in repo dao shouldn't store it to DB`() = runTest {
        val reaction = randomReaction(messageId = "", user = currentUser, type = "love")

        coInvoking { reactionRepo.insertReaction(reaction) }.shouldThrow(IllegalArgumentException::class)

        verifyNoInteractions(reactionDao)
    }

    @Test
    fun `Given reaction with empty type when it's saved in repo dao shouldn't store it to DB`() = runTest {
        val reaction = randomReaction(messageId = randomString(10), user = currentUser, type = "")

        coInvoking { reactionRepo.insertReaction(reaction) }.shouldThrow(IllegalArgumentException::class)

        verifyNoInteractions(reactionDao)
    }

    @Test
    fun `Given reaction with empty userId when it's saved in repo dao shouldn't store it to DB`() = runTest {
        val reaction = randomReaction(messageId = randomString(10), userId = "", type = "love")

        coInvoking { reactionRepo.insertReaction(reaction) }.shouldThrow(IllegalArgumentException::class)

        verifyNoInteractions(reactionDao)
    }

    @Test
    fun `When dao returns reactions with syncNeeded == true they should contain current user data`() = runTest {
        whenever(reactionDao.selectSyncStatus(SyncStatus.SYNC_NEEDED))
            .thenReturn(listOf(randomReaction(syncStatus = SyncStatus.SYNC_NEEDED).toEntity()))

        reactionRepo.selectReactionsBySyncStatus(SyncStatus.SYNC_NEEDED).apply {
            this.size `should be equal to` 1
            this.first().user `should be equal to` currentUser
        }
    }
}
