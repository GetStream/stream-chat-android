/*
 * Copyright (c) 2014-2022 Stream.io Inc. All rights reserved.
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

import android.webkit.MimeTypeMap
import androidx.test.ext.junit.runners.AndroidJUnit4
import io.getstream.chat.android.client.ChatClient
import io.getstream.chat.android.client.errors.ChatError
import io.getstream.chat.android.client.extensions.uploadId
import io.getstream.chat.android.client.models.Attachment
import io.getstream.chat.android.client.utils.Result
import io.getstream.chat.android.models.UploadedFile
import io.getstream.chat.android.models.UploadedImage
import io.getstream.chat.android.test.TestCall
import io.getstream.chat.android.test.positiveRandomInt
import io.getstream.chat.android.test.randomFile
import io.getstream.chat.android.test.randomString
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.test.runTest
import org.amshove.kluent.shouldBeEqualTo
import org.amshove.kluent.shouldBeInstanceOf
import org.amshove.kluent.shouldBeNull
import org.amshove.kluent.shouldNotBeNull
import org.junit.Before
import org.junit.Ignore
import org.junit.Test
import org.junit.runner.RunWith
import org.mockito.kotlin.any
import org.mockito.kotlin.anyOrNull
import org.mockito.kotlin.doReturn
import org.mockito.kotlin.eq
import org.mockito.kotlin.mock
import org.mockito.kotlin.same
import org.mockito.kotlin.whenever
import org.robolectric.Shadows
import java.io.File
import java.util.UUID

@ExperimentalCoroutinesApi
@RunWith(AndroidJUnit4::class)
internal class AttachmentUploaderTests {
    private val channelType: String = randomString()
    private val channelId: String = randomString()

    @Before
    fun setup() {
        Shadows.shadowOf(MimeTypeMap.getSingleton())
            .addExtensionMimeTypMapping("jpg", "image/jpeg")
    }

    @Test
    @Ignore("Current logic doesn't work so. Need to rewrite test")
    fun `Should return attachment with properly filled data when sending file has failed`(): Unit = runTest {
        val error = ChatError.GenericError(message = "")
        val attachment = randomAttachments(size = 1).first()

        val sut = Fixture()
            .givenMockedFileUploads(channelType, channelId, Result.Failure(error))
            .get()

        val result = sut.uploadAttachment(channelType, channelId, attachment) as Result.Success

        with(result.value) {
            name shouldBeEqualTo attachment.upload!!.name
            fileSize.shouldNotBeNull()
            type.shouldNotBeNull()
            mimeType.shouldNotBeNull()
            uploadState shouldBeEqualTo Attachment.UploadState.Failed(error)
            url.shouldBeNull()
            imageUrl.shouldBeNull()
            assetUrl.shouldBeNull()
        }
    }

    @Test
    fun `Should return attachment with properly filled data when successfully sent file`() = runTest {
        val attachment = randomAttachments(size = 1).first()
        val url = "url"

        val sut = Fixture()
            .givenMockedFileUploads(channelType, channelId, Result.Success(UploadedFile(file = url)))
            .get()

        val result = sut.uploadAttachment(channelType, channelId, attachment) as Result.Success

        with(result.value) {
            name shouldBeEqualTo attachment.upload!!.name
            url shouldBeEqualTo url
            fileSize.shouldNotBeNull()
            type.shouldNotBeNull()
            mimeType.shouldNotBeNull()
            uploadState shouldBeEqualTo Attachment.UploadState.Success
        }
    }

    @Test
    fun `Should return attachment with proper data including thumb and image url when successfully sent video file`() =
        runTest {
            val attachment = randomAttachments(size = 1)
                .first()
                .apply { mimeType = "video/mp4" }

            val url = "url"
            val thumbUrl = "thumbUrl"

            val sut = Fixture()
                .givenMockedFileUploads(channelType, channelId, Result.Success(UploadedFile(file = url, thumbUrl = thumbUrl)))
                .get()

            val result = sut.uploadAttachment(channelType, channelId, attachment) as Result.Success

            with(result.value) {
                name shouldBeEqualTo attachment.upload!!.name
                url shouldBeEqualTo url
                thumbUrl shouldBeEqualTo thumbUrl
                imageUrl shouldBeEqualTo thumbUrl
                fileSize.shouldNotBeNull()
                type.shouldNotBeNull()
                mimeType.shouldNotBeNull()
                uploadState shouldBeEqualTo Attachment.UploadState.Success
            }
        }

    @Test
    fun `Upload attachment should have the right format`() = runTest {
        val attachments = randomAttachments()
        val files: List<File> = attachments.map { it.upload!! }

        val sut = Fixture()
            .givenMockedFileUploads(channelType, channelId, files)
            .get()

        for (attachment in attachments) {
            val url = attachment.upload!!.absolutePath
            val expectedAttachment = attachment.copy(
                assetUrl = url,
                url = url,
                type = "file",
                mimeType = "",
                name = attachment.upload!!.name,
                title = attachment.upload!!.name,
                uploadState = Attachment.UploadState.Success
            )
            val result = sut.uploadAttachment(channelType, channelId, attachment)
            result.shouldBeInstanceOf(Result.Success::class)
            (result as Result.Success).value shouldBeEqualTo expectedAttachment
        }
    }

    @Test
    fun `Upload attachment should be configurable`() = runTest {
        val attachments = randomAttachments()
        val files: List<File> = attachments.map { it.upload!! }

        val sut = Fixture()
            .givenMockedFileUploads(channelType, channelId, files)
            .get()

        for (attachment in attachments) {
            val url = attachment.upload!!.absolutePath
            val expectedAttachment = attachment.copy(
                assetUrl = url,
                url = url,
                type = "file",
                mimeType = "",
                name = attachment.upload!!.name,
                title = attachment.upload!!.name,
                uploadState = Attachment.UploadState.Success
            )
            val result = sut.uploadAttachment(
                channelType,
                channelId,
                attachment = attachment,
            )

            result.shouldBeInstanceOf(Result.Success::class)
            (result as Result.Success).value shouldBeEqualTo expectedAttachment
        }
    }

    private fun randomAttachments(size: Int = positiveRandomInt(10)): MutableList<Attachment> {
        return randomAttachmentsWithFile(size)
            .map { attachment ->
                attachment.apply { this.uploadState = Attachment.UploadState.Idle }
            }
            .toMutableList()
    }

    private class Fixture {
        private var clientMock: ChatClient = mock()

        fun givenMockedFileUploads(channelType: String, channelId: String, result: Result<UploadedFile>) = apply {
            whenever(
                clientMock.sendFile(
                    eq(channelType),
                    eq(channelId),
                    any(),
                    anyOrNull()
                )
            ) doReturn TestCall(result)
        }

        fun givenMockedImageUploads(channelType: String, channelId: String, result: Result<UploadedImage>) {
            whenever(
                clientMock.sendImage(
                    eq(channelType),
                    eq(channelId),
                    any(),
                    anyOrNull()
                )
            ) doReturn TestCall(result)
        }

        fun givenMockedFileUploads(channelType: String, channelId: String, files: List<File>) = apply {
            for (file in files) {
                val imageResult = Result.Success(UploadedImage(file.absolutePath))
                val fileResult = Result.Success(UploadedFile(file = file.absolutePath))

                whenever(
                    clientMock.sendFile(
                        eq(channelType),
                        eq(channelId),
                        same(file),
                        anyOrNull(),
                    )
                ) doReturn TestCall(fileResult)
                whenever(
                    clientMock.sendImage(
                        eq(channelType),
                        eq(channelId),
                        same(file),
                        anyOrNull(),
                    )
                ) doReturn TestCall(imageResult)
            }
        }

        fun get(): AttachmentUploader {
            return AttachmentUploader(clientMock)
        }
    }
}

internal fun randomAttachmentsWithFile(
    size: Int = positiveRandomInt(10),
    creationFunction: (Int) -> Attachment = {
        Attachment(upload = randomFile()).apply {
            uploadId = generateUploadId()
        }
    },
): List<Attachment> = (1..size).map(creationFunction)

internal fun generateUploadId(): String {
    return "upload_id_${UUID.randomUUID()}"
}
