package io.getstream.chat.android.livedata

import androidx.test.ext.junit.runners.AndroidJUnit4
import com.google.common.truth.Truth
import io.getstream.chat.android.client.models.Message
import io.getstream.chat.android.livedata.controller.isPermanent
import org.junit.Ignore
import org.junit.Test
import org.junit.runner.RunWith

@RunWith(AndroidJUnit4::class)
class ChatErrorTest : BaseConnectedIntegrationTest() {
    @Test
    @Ignore("error parsing in LLC is broken")
    fun checkIsPermanent() {
        // TODO: add more test cases when possible
        val message = Message(text = "hi", id = "thesame")
        val result1 = client.sendMessage("messaging", data.channel1.id, message).execute()
        assertSuccess(result1)
        // this will always fail since the id is the same
        val result2 = client.sendMessage("messaging", data.channel1.id, message).execute()
        val error = result2.error()
        Truth.assertThat(error.isPermanent()).isTrue()
    }
}
