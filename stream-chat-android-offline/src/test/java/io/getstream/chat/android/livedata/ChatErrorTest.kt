package io.getstream.chat.android.livedata

import androidx.test.ext.junit.runners.AndroidJUnit4
import com.google.common.truth.Truth
import io.getstream.chat.android.client.errors.ChatNetworkError
import io.getstream.chat.android.livedata.extensions.isPermanent
import org.junit.Test
import org.junit.runner.RunWith

@RunWith(AndroidJUnit4::class)
internal class ChatErrorTest {

    @Test
    fun invalidMessageInput() {
        val error = ChatNetworkError.create(4, "a message with ID thesame already exists", 400, null)
        Truth.assertThat(error.isPermanent()).isTrue()
    }

    @Test
    fun `rateLimit error should be temporary`() {
        val error = ChatNetworkError.create(9, "", 429, null)
        Truth.assertThat(error.isPermanent()).isFalse()
    }

    @Test
    fun `request timeout should be a temporary error`() {
        val error = ChatNetworkError.create(23, "", 408, null)
        Truth.assertThat(error.isPermanent()).isFalse()
    }

    @Test
    fun `broken api should be a temporary error`() {
        val error = ChatNetworkError.create(0, "", 500, null)
        Truth.assertThat(error.isPermanent()).isFalse()
    }

    @Test
    fun `cool down period error should be permanent`() {
        val error = ChatNetworkError.create(60, "", 403, null)
        Truth.assertThat(error.isPermanent()).isTrue()
    }
}
