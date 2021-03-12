package io.getstream.chat.android.livedata.repository

import androidx.test.ext.junit.runners.AndroidJUnit4
import com.google.common.truth.Truth
import com.nhaarman.mockitokotlin2.any
import com.nhaarman.mockitokotlin2.doReturn
import com.nhaarman.mockitokotlin2.whenever
import io.getstream.chat.android.client.models.Message
import io.getstream.chat.android.client.utils.Result
import io.getstream.chat.android.client.utils.SyncStatus
import io.getstream.chat.android.livedata.BaseDomainTest
import io.getstream.chat.android.test.TestCall
import kotlinx.coroutines.runBlocking
import org.amshove.kluent.`should be equal to`
import org.amshove.kluent.`should not be`
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith

@RunWith(AndroidJUnit4::class)
internal class ReactionRepositoryTest : BaseDomainTest() {
    private val helper by lazy { chatDomainImpl.repos }

    @Before
    fun beforeEach() {
        runBlocking {
            helper.insertMessage(data.message1)
        }
    }

    @Test
    fun testInsertAndRead() = runBlocking {
        helper.insertReaction(data.reaction1)
        val reaction = helper.selectUserReactionsToMessage(data.reaction1.messageId, data.reaction1.user!!.id).first()
        Truth.assertThat(reaction).isEqualTo(data.reaction1)
    }

    @Test
    fun testSyncNeeded() = runBlocking {
        data.reaction1.syncStatus = SyncStatus.FAILED_PERMANENTLY
        val reaction2 =
            data.reaction1.copy().apply { type = "love"; syncStatus = SyncStatus.SYNC_NEEDED }
        helper.insertReaction(data.reaction1)
        helper.insertReaction(reaction2)
        var reactions = helper.selectReactionsSyncNeeded()
        Truth.assertThat(reactions.size).isEqualTo(1)
        Truth.assertThat(reactions.first().syncStatus).isEqualTo(SyncStatus.SYNC_NEEDED)

        reactions = chatDomainImpl.retryReactions()
        Truth.assertThat(reactions.size).isEqualTo(1)
        Truth.assertThat(reactions.first().syncStatus).isEqualTo(SyncStatus.COMPLETED)

        reactions = helper.selectReactionsSyncNeeded()
        Truth.assertThat(reactions.size).isEqualTo(0)
    }

    @Test
    fun testUpdate() = runBlocking {
        val reaction1Updated =
            data.reaction1.copy().apply { extraData = mutableMapOf("theanswer" to 42.0) }
        helper.insertReaction(data.reaction1)
        helper.insertReaction(reaction1Updated)

        val reaction = helper.selectUserReactionsToMessage(data.reaction1.messageId, data.reaction1.user!!.id).first()
        Truth.assertThat(reaction).isEqualTo(reaction1Updated)
    }

    @Test
    fun `When sending and deleting a reaction while offline Then retry reaction invocation should sync the deleted reaction`(): Unit =
        runBlocking {
            chatDomainImpl.setOffline()
            channelControllerImpl.sendReaction(data.reaction1, enforceUnique = false)
            channelControllerImpl.deleteReaction(data.reaction1)
            val reaction = chatDomainImpl.repos
                .selectUserReactionsToMessage(data.message1.id, data.user1.id)
                .first()

            reaction.syncStatus `should be equal to` SyncStatus.SYNC_NEEDED
            reaction.deletedAt `should not be` null

            whenever(client.deleteReaction(any(), any())) doReturn TestCall(Result(Message()))
            val reactions = chatDomainImpl.retryReactions()

            reactions.size `should be equal to` 1
        }
}
