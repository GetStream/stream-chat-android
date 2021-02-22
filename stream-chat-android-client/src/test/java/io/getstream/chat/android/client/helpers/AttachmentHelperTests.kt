package io.getstream.chat.android.client.helpers

import com.nhaarman.mockitokotlin2.doReturn
import com.nhaarman.mockitokotlin2.mock
import com.nhaarman.mockitokotlin2.whenever
import io.getstream.chat.android.client.Mother
import io.getstream.chat.android.client.utils.SystemTimeProvider
import org.amshove.kluent.shouldBeEqualTo
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.junit.jupiter.params.ParameterizedTest
import org.junit.jupiter.params.provider.MethodSource

internal class AttachmentHelperTests {

    private lateinit var timeProvider: SystemTimeProvider
    private lateinit var sut: AttachmentHelper

    @BeforeEach
    fun setup() {
        timeProvider = mock()
        sut = AttachmentHelper(timeProvider)
    }

    @Test
    fun `When has valid image url If attachment has null url Should return false`() {
        val attachment = Mother.randomAttachment { imageUrl = null }

        val result = sut.hasValidImageUrl(attachment)

        result shouldBeEqualTo false
    }

    /** [nonValidUrls] */
    @ParameterizedTest
    @MethodSource("nonValidUrls")
    fun `When has valid image url if attachment url is not valid Should return false`(notValidUrl: String) {
        val attachment = Mother.randomAttachment { imageUrl = notValidUrl }

        val result = sut.hasValidImageUrl(attachment)

        result shouldBeEqualTo false
    }

    @Test
    fun `When has valid image url if attachment url is valid without Expires Should return true`() {
        val attachment = Mother.randomAttachment { imageUrl = "https://www.someDomain.com/some-resource-id1.jpg" }

        val result = sut.hasValidImageUrl(attachment)

        result shouldBeEqualTo true
    }

    @Test
    fun `When has valid image url if attachment url is valid with Expires and failed to parse timestamp Should return false`() {
        val attachment =
            Mother.randomAttachment { imageUrl = "https://www.someDomain.com/some-resource-id1.jpg?Expires=xxTTee" }

        val result = sut.hasValidImageUrl(attachment)

        result shouldBeEqualTo false
    }

    @Test
    fun `When has valid image url if attachment url is valid with Expires and timestamp is greater than current time Should return true`() {
        val currentTime = 1000L
        val timeStamp = currentTime + 100L
        whenever(timeProvider.provideCurrentTimeInSeconds()) doReturn currentTime
        val attachment =
            Mother.randomAttachment { imageUrl = "https://www.someDomain.com/some-resource-id1.jpg?Expires=$timeStamp" }

        val result = sut.hasValidImageUrl(attachment)

        result shouldBeEqualTo true
    }

    @Test
    fun `When has valid image url if attachment url is valid with Expires and timestamp is less than current time Should return false`() {
        val currentTime = 1000L
        val timeStamp = currentTime - 100L
        whenever(timeProvider.provideCurrentTimeInSeconds()) doReturn currentTime
        val attachment =
            Mother.randomAttachment { imageUrl = "https://www.someDomain.com/some-resource-id1.jpg?Expires=$timeStamp" }

        val result = sut.hasValidImageUrl(attachment)

        result shouldBeEqualTo false
    }

    companion object {
        @JvmStatic
        fun nonValidUrls() = listOf(
            "someNotValidUrl",
            "https://????.com",
            "www.someDomainWithoutProtocol.com"
        )
    }
}
