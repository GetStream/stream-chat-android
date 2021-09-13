package io.getstream.chat.android.offline.usecase

import androidx.test.ext.junit.runners.AndroidJUnit4
import io.getstream.chat.android.offline.integration.BaseConnectedIntegrationTest
import kotlinx.coroutines.runBlocking
import org.amshove.kluent.shouldBeEqualTo
import org.amshove.kluent.shouldBeTrue
import org.amshove.kluent.shouldNotBe
import org.junit.Test
import org.junit.runner.RunWith

@RunWith(AndroidJUnit4::class)
internal class SendReactionTest : BaseConnectedIntegrationTest() {

    @Test
    fun reactionUseCase(): Unit = runBlocking {
        val channelState = chatDomain.watchChannel(data.channel1.cid, 10).execute().data()
        val message1 = data.createMessage()
        val result = chatDomain.sendMessage(message1).execute()
        assertSuccess(result)
        data.reaction1.messageId = result.data().id
        // go offline, reaction should still update state
        chatDomainImpl.setOffline()
        val oldMsg = channelState.getMessage(message1.id)
        val oldReactionCounts = oldMsg!!.reactionCounts
        val result2 = chatDomain.sendReaction(data.channel1.cid, data.reaction1, false).execute()
        assertSuccess(result2)
        result2.isSuccess.shouldBeTrue()
        val msg = channelState.getMessage(message1.id)
        val newReactionCounts = msg!!.reactionCounts
        msg.id shouldBeEqualTo result.data().id
        msg.reactionCounts shouldBeEqualTo mapOf("like" to 1)
        msg.latestReactions.last() shouldBeEqualTo data.reaction1
        msg.ownReactions.last() shouldBeEqualTo data.reaction1
        // if its the same object diffutils wont notice the difference since old and new will be equal
        oldReactionCounts shouldNotBe newReactionCounts
    }
}
