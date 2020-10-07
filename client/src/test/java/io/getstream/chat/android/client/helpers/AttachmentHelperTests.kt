package io.getstream.chat.android.client.helpers

import io.getstream.chat.android.client.Mother
import org.amshove.kluent.shouldBeEqualTo
import org.junit.jupiter.api.Test
import org.junit.jupiter.params.ParameterizedTest
import org.junit.jupiter.params.provider.MethodSource

internal class AttachmentHelperTests {

    private val sut = AttachmentHelper()

    @Test
    fun `When has valid url If attachment has null url Should return false`() {
        val attachment = Mother.randomAttachment { url = null }

        val result = sut.hasValidUrl(attachment)

        result shouldBeEqualTo false
    }

    @ParameterizedTest
    @MethodSource("nonValidUrls")
    fun `When has valid url if attachment url is not valid Should return false`(notValidUrl: String) {
        val attachment = Mother.randomAttachment { url = notValidUrl }

        val result = sut.hasValidUrl(attachment)

        result shouldBeEqualTo false
    }

    @Test
    fun `When has valid url if attachment url is valid without Expires Should return true`() {
        val attachment = Mother.randomAttachment { url = "https://www.someDomain.com/some-resource-id1.jpg" }

        val result = sut.hasValidUrl(attachment)

        result shouldBeEqualTo true
    }

    @Test
    fun `When has valid url if attachment url is valid with Expires and failed to parse timestamp Should return false`() {
        val attachment = Mother.randomAttachment { url = "https://www.someDomain.com/some-resource-id1.jpg?Expires=xxTTee" }

        val result = sut.hasValidUrl(attachment)

        result shouldBeEqualTo false
    }

    companion object {
        @JvmStatic
        fun nonValidUrls() = listOf<String>(
                "someNotValidUrl",
                "https://????.com",
                "https://domain.com/xxIII ioi",
                "https://www.++++.---.com",
                "www.someDomainWithoutProtocol.com"
            )
    }
}
