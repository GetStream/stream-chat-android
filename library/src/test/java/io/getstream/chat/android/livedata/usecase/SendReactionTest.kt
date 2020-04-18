package io.getstream.chat.android.livedata.usecase

import androidx.test.ext.junit.runners.AndroidJUnit4
import com.google.common.truth.Truth
import io.getstream.chat.android.livedata.BaseIntegrationTest
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.runBlocking
import org.junit.Test
import org.junit.runner.RunWith
import java.util.*

@RunWith(AndroidJUnit4::class)
class SendReactionTest: BaseIntegrationTest() {

    @Test
    fun reactionUseCase() = runBlocking(Dispatchers.IO) {
        var channelState = chatDomain.useCases.watchChannel(data.channel1.cid, 10).execute().data()
        val message1 = data.message1.apply { id=""; createdAt= Date() }
        var result = chatDomain.useCases.sendMessage(message1).execute()
        Truth.assertThat(result.isSuccess).isTrue()
        data.reaction1.messageId = result.data().id
        val result2 = chatDomain.useCases.sendReaction(data.channel1.cid, data.reaction1).execute()
        Truth.assertThat(result2.isSuccess).isTrue()
        val msg = channelState.getMessage(data.message1.id)
        Truth.assertThat(msg!!.id).isEqualTo(result.data().id)
        Truth.assertThat(msg!!.latestReactions.last()).isEqualTo(data.reaction1)
        Truth.assertThat(msg!!.ownReactions.last()).isEqualTo(data.reaction1)
    }
}