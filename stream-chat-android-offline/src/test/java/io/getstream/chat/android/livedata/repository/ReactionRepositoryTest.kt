package io.getstream.chat.android.livedata.repository

import androidx.test.ext.junit.runners.AndroidJUnit4
import com.google.common.truth.Truth
import io.getstream.chat.android.client.utils.SyncStatus
import io.getstream.chat.android.livedata.BaseDomainTest
import kotlinx.coroutines.runBlocking
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith

@RunWith(AndroidJUnit4::class)
internal class ReactionRepositoryTest : BaseDomainTest() {
    private val helper by lazy { chatDomainImpl.repos }
    private val repo by lazy { helper.reactions }

    @Before
    fun beforeEach() {
        runBlocking {
            helper.messages.insert(data.message1)
        }
    }

    @Test
    fun testInsertAndRead() = runBlocking {
        repo.insert(data.reaction1)
        val reaction =
            helper.selectUserReactionsToMessageByType(data.reaction1.messageId, data.reaction1.user!!.id, data.reaction1.type)
        Truth.assertThat(reaction).isEqualTo(data.reaction1)
    }

    @Test
    fun testSyncNeeded() = runBlocking {
        data.reaction1.syncStatus = SyncStatus.FAILED_PERMANENTLY
        val reaction2 =
            data.reaction1.copy().apply { type = "love"; syncStatus = SyncStatus.SYNC_NEEDED }
        repo.insert(listOf(data.reaction1, reaction2))
        var reactions = helper.selectReactionSyncNeeded()
        Truth.assertThat(reactions.size).isEqualTo(1)
        Truth.assertThat(reactions.first().syncStatus).isEqualTo(SyncStatus.SYNC_NEEDED)

        reactions = chatDomainImpl.retryReactions()
        Truth.assertThat(reactions.size).isEqualTo(1)
        Truth.assertThat(reactions.first().syncStatus).isEqualTo(SyncStatus.COMPLETED)

        reactions = helper.selectReactionSyncNeeded()
        Truth.assertThat(reactions.size).isEqualTo(0)
    }

    @Test
    fun testUpdate() = runBlocking {
        val reaction1Updated =
            data.reaction1.copy().apply { extraData = mutableMapOf("theanswer" to 42.0) }
        repo.insert(data.reaction1)
        repo.insert(reaction1Updated)

        val reaction =
            helper.selectUserReactionsToMessageByType(data.reaction1.messageId, data.reaction1.user!!.id, data.reaction1.type)
        Truth.assertThat(reaction).isEqualTo(reaction1Updated)
    }
}
