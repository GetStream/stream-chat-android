package io.getstream.chat.android.livedata

import androidx.test.ext.junit.runners.AndroidJUnit4
import com.google.common.truth.Truth
import io.getstream.chat.android.client.errors.ChatNetworkError
import io.getstream.chat.android.client.models.Message
import io.getstream.chat.android.livedata.extensions.isPermanent
import org.junit.Test
import org.junit.runner.RunWith

@RunWith(AndroidJUnit4::class)
internal class ChatErrorTest : BaseConnectedIntegrationTest() {
    // https://getstream.io/chat/docs/api_errors_response/?language=js
    @Test
    fun invalidMessageInput() {
        val message = Message(text = "hi", id = "thesame")
        client.sendMessage("messaging", data.channel1.id, message).execute()
        // this will always fail since the id is the same
        val result2 = client.sendMessage("messaging", data.channel1.id, message).execute()
        val error = result2.error()
        Truth.assertThat(error.isPermanent()).isTrue()
    }

    @Test
    fun rateLimit() {
        val error = ChatNetworkError.create(9, "", 429, null)
        Truth.assertThat(error.isPermanent()).isFalse()
    }

    @Test
    fun `request timeout should be temporary`() {
        val error = ChatNetworkError.create(23, "", 408, null)
        Truth.assertThat(error.isPermanent()).isFalse()
    }

    @Test
    fun brokenAPI() {
        val error = ChatNetworkError.create(0, "", 500, null)
        Truth.assertThat(error.isPermanent()).isFalse()
    }

    @Test
    fun `cool down period error should be permanent`() {
        val error = ChatNetworkError.create(60, "", 403, null)
        Truth.assertThat(error.isPermanent()).isTrue()
    }
}
