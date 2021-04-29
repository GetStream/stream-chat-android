package io.getstream.chat.android.offline.usecase

import androidx.test.ext.junit.runners.AndroidJUnit4
import com.google.common.truth.Truth
import io.getstream.chat.android.offline.integration.BaseConnectedIntegrationTest
import kotlinx.coroutines.runBlocking
import org.junit.Test
import org.junit.runner.RunWith

@RunWith(AndroidJUnit4::class)
internal class DeleteReactionTest : BaseConnectedIntegrationTest() {

    @Test
    fun reactionUseCase() = runBlocking {
        val channelController =
            chatDomain.watchChannel(data.channel1.cid, 10).execute().data()
        val message1 = data.createMessage()
        val result = chatDomain.sendMessage(message1).execute()
        assertSuccess(result)
        data.reaction1.messageId = result.data().id
        val result2 = chatDomain.sendReaction(data.channel1.cid, data.reaction1, false).execute()
        assertSuccess(result2)
        val result3 =
            chatDomain.deleteReaction(data.channel1.cid, data.reaction1).execute()
        assertSuccess(result3)
        val msg = channelController.getMessage(message1.id)
        Truth.assertThat(msg!!.id).isEqualTo(result.data().id)
        Truth.assertThat(msg.latestReactions.size).isEqualTo(0)
        Truth.assertThat(msg.ownReactions.size).isEqualTo(0)
    }
}
