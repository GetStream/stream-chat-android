package io.getstream.chat.android.client.attachment

import io.getstream.chat.android.client.test.randomMessage
import io.getstream.chat.android.models.Attachment
import io.getstream.chat.android.test.TestLoggingHelper
import io.getstream.result.Result
import org.amshove.kluent.`should be equal to`
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import java.io.File

internal class AttachmentsVerifierTests {

    private lateinit var verifier: AttachmentsVerifier

    @BeforeEach
    fun setUp() {
        verifier = AttachmentsVerifier()

        TestLoggingHelper.initialize()
    }

    @Test
    fun `when verify message with non-File attachment, result must be Successful`() {
        /* Given */
        val locationAttachment = Attachment(
            type = "location",
            extraData = mutableMapOf("latitude" to 1.0, "longitude" to 2.0),
            uploadState = Attachment.UploadState.Success,
        )
        val message = randomMessage(attachments = arrayListOf(locationAttachment))
        val result = Result.Success(message)

        /* When */
        val verified = verifier.verifyAttachments(result)

        /* Then */
        verified.isSuccess `should be equal to` true
    }

    @Test
    fun `when verify message with File attachment, result must be Failure if no CDN urls`() {
        /* Given */
        val fileAttachment = Attachment(
            type = "audio",
            mimeType = "audio/mp3",
            upload = File("./some.mp3"),
            assetUrl = null,
            imageUrl = null,
            uploadState = Attachment.UploadState.Success,
        )
        val message = randomMessage(attachments = arrayListOf(fileAttachment))
        val result = Result.Success(message)

        /* When */
        val verified = verifier.verifyAttachments(result)

        /* Then */
        verified.isFailure `should be equal to` true
    }

}