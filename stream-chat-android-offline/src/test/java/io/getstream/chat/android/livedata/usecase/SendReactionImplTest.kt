package io.getstream.chat.android.livedata.usecase

import androidx.test.ext.junit.runners.AndroidJUnit4
import com.google.common.truth.Truth
import io.getstream.chat.android.livedata.BaseConnectedIntegrationTest
import kotlinx.coroutines.runBlocking
import org.junit.Test
import org.junit.runner.RunWith

@RunWith(AndroidJUnit4::class)
internal class SendReactionImplTest : BaseConnectedIntegrationTest() {

    @Test
    fun reactionUseCase() = runBlocking {
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
        Truth.assertThat(result2.isSuccess).isTrue()
        val msg = channelState.getMessage(message1.id)
        val newReactionCounts = msg!!.reactionCounts
        Truth.assertThat(msg.id).isEqualTo(result.data().id)
        Truth.assertThat(msg.reactionCounts).isEqualTo(mapOf("like" to 1))
        Truth.assertThat(msg.latestReactions.last()).isEqualTo(data.reaction1)
        Truth.assertThat(msg.ownReactions.last()).isEqualTo(data.reaction1)
        // if its the same object diffutils wont notice the difference since old and new will be equal
        Truth.assertThat(oldReactionCounts === newReactionCounts).isFalse()
    }
}
