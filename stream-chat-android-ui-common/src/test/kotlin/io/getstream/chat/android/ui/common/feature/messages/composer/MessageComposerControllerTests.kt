package io.getstream.chat.android.ui.common.feature.messages.composer

import kotlinx.coroutines.ExperimentalCoroutinesApi
import org.amshove.kluent.`should be equal to`
import org.junit.jupiter.api.Test

@ExperimentalCoroutinesApi
internal class MessageComposerControllerTests {

    @Test
    fun `test valid URLs with LinkPattern`() {
        val pattern = MessageComposerController.LinkPattern
        val validUrls = listOf(
            "https://www.example.com",
            "http://www.example.com",
            "www.example.com",
            "example.com",
            "https://subdomain.example.com",
            "http://example.com/path/to/page?name=parameter&another=value",
            "example.co.uk"
        )
        validUrls.forEach { url ->
            pattern.matches(url) `should be equal to` true
        }
    }

    @Test
    fun `test invalid URLs with LinkPattern`() {
        val pattern = MessageComposerController.LinkPattern
        val invalidUrls = listOf(
            "http//www.example.com",
            "htp://example.com",
            "://example.com",
            "example",
            "http://example..com",
            "http://-example.com",
            "http://example"
        )
        invalidUrls.forEach { url ->
            pattern.matches(url) `should be equal to` false
        }
    }
}