package io.getstream.chat.android.client.helpers

import com.flextrade.kfixture.KFixture
import io.getstream.chat.android.client.models.Attachment
import org.amshove.kluent.shouldBeEqualTo
import org.junit.jupiter.api.Test

internal class AttachmentHelperTests {

    private val sut = AttachmentHelper()

    @Test
    fun `When has valid Url If attachment has null url Should return false`() {
        val attachment = KFixture {
            sameInstance(
                Attachment.UploadState::class.java,
                Attachment.UploadState.Success
            )
        }.invoke<Attachment>().copy(url = null)

        val result = sut.hasValidUrl(attachment)

        result shouldBeEqualTo false
    }
}
