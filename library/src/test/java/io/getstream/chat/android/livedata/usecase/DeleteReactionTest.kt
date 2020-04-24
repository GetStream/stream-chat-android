package io.getstream.chat.android.livedata.usecase

import androidx.test.ext.junit.runners.AndroidJUnit4
import com.google.common.truth.Truth
import io.getstream.chat.android.client.utils.Result
import io.getstream.chat.android.livedata.BaseConnectedIntegrationTest
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.runBlocking
import org.junit.Test
import org.junit.runner.RunWith

@RunWith(AndroidJUnit4::class)
class DeleteReactionTest : BaseConnectedIntegrationTest() {

    @Test
    fun reactionUseCase() = runBlocking(Dispatchers.IO) {
        var channelState = chatDomain.useCases.watchChannel(data.channel1.cid, 10).execute().data()
        val message1 = data.createMessage()
        var result = chatDomain.useCases.sendMessage(message1).execute()
        assertSuccess(result as Result<Any>)
        data.reaction1.messageId = result.data().id
        val result2 = chatDomain.useCases.sendReaction(data.channel1.cid, data.reaction1).execute()
        assertSuccess(result2 as Result<Any>)
        val result3 =
            chatDomain.useCases.deleteReaction(data.channel1.cid, data.reaction1).execute()
        assertSuccess(result3 as Result<Any>)
        val msg = channelState.getMessage(message1.id)
        Truth.assertThat(msg!!.id).isEqualTo(result.data().id)
        Truth.assertThat(msg.latestReactions.size).isEqualTo(0)
        Truth.assertThat(msg.ownReactions.size).isEqualTo(0)
    }
}
