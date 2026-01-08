/*
 * Copyright (c) 2014-2026 Stream.io Inc. All rights reserved.
 *
 * Licensed under the Stream License;
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *    https://github.com/GetStream/stream-chat-android/blob/main/LICENSE
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package io.getstream.chat.android.client.attachment

import io.getstream.chat.android.models.Attachment
import io.getstream.chat.android.randomMessage
import io.getstream.chat.android.test.TestLoggingHelper
import io.getstream.result.Result
import org.amshove.kluent.`should be equal to`
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import java.io.File

internal class AttachmentsVerifierTests {

    private val verifier = AttachmentsVerifier

    @BeforeEach
    fun setUp() {
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
