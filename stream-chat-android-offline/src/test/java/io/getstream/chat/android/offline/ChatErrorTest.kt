package io.getstream.chat.android.offline

import io.getstream.chat.android.client.errors.ChatNetworkError
import io.getstream.chat.android.offline.extensions.isPermanent
import org.amshove.kluent.shouldBeFalse
import org.amshove.kluent.shouldBeTrue
import org.junit.Test

internal class ChatErrorTest {

    @Test
    fun `error for messages with the same ID should be permanent`() {
        val error = ChatNetworkError.create(4, "a message with ID the same id already exists", 400, null)
        error.isPermanent().shouldBeTrue()
    }

    @Test
    fun `rateLimit error should be temporary`() {
        val error = ChatNetworkError.create(9, "", 429, null)
        error.isPermanent().shouldBeFalse()
    }

    @Test
    fun `request timeout should be a temporary error`() {
        val error = ChatNetworkError.create(23, "", 408, null)
        error.isPermanent().shouldBeFalse()
    }

    @Test
    fun `broken api should be a temporary error`() {
        val error = ChatNetworkError.create(0, "", 500, null)
        error.isPermanent().shouldBeFalse()
    }

    @Test
    fun `cool down period error should be permanent`() {
        val error = ChatNetworkError.create(60, "", 403, null)
        error.isPermanent().shouldBeTrue()
    }
}
