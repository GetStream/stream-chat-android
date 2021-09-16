package io.getstream.chat.android.offline.usecase

import androidx.test.ext.junit.runners.AndroidJUnit4
import io.getstream.chat.android.offline.integration.BaseConnectedIntegrationTest
import kotlinx.coroutines.runBlocking
import org.amshove.kluent.shouldBeEqualTo
import org.amshove.kluent.shouldNotBeNull
import org.junit.Test
import org.junit.runner.RunWith

@RunWith(AndroidJUnit4::class)
internal class DeleteReactionTest : BaseConnectedIntegrationTest() {

    @Test
    fun reactionUseCase(): Unit = runBlocking {
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
        msg.shouldNotBeNull()
        msg.id shouldBeEqualTo result.data().id
        msg.latestReactions.size shouldBeEqualTo 0
        msg.ownReactions.size shouldBeEqualTo 0
    }
}
