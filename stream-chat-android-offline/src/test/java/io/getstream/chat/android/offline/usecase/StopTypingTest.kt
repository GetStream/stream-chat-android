package io.getstream.chat.android.offline.usecase

import androidx.test.ext.junit.runners.AndroidJUnit4
import io.getstream.chat.android.offline.integration.BaseConnectedIntegrationTest
import kotlinx.coroutines.runBlocking
import org.amshove.kluent.shouldBeFalse
import org.amshove.kluent.shouldBeTrue
import org.junit.Test
import org.junit.runner.RunWith

@RunWith(AndroidJUnit4::class)
internal class StopTypingTest : BaseConnectedIntegrationTest() {

    @Test
    fun stopTyping(): Unit = runBlocking {
        chatDomain.watchChannel(data.channel1.cid, 10).execute()
        val result = chatDomain.keystroke(data.channel1.cid, null).execute()
        result.data().shouldBeTrue()
        val result2 = chatDomain.stopTyping(data.channel1.cid).execute()
        result2.data().shouldBeTrue()

        // this shouldnt trigger an event since nobody is typing
        val result3 = chatDomain.stopTyping(data.channel1.cid).execute()
        result3.data().shouldBeFalse()
    }
}
