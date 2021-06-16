package io.getstream.chat.android.offline.channel.controller.attachment

import android.webkit.MimeTypeMap
import androidx.test.ext.junit.runners.AndroidJUnit4
import com.google.common.truth.Truth
import com.nhaarman.mockitokotlin2.any
import com.nhaarman.mockitokotlin2.anyOrNull
import com.nhaarman.mockitokotlin2.doReturn
import com.nhaarman.mockitokotlin2.eq
import com.nhaarman.mockitokotlin2.mock
import com.nhaarman.mockitokotlin2.same
import com.nhaarman.mockitokotlin2.whenever
import io.getstream.chat.android.client.ChatClient
import io.getstream.chat.android.client.errors.ChatError
import io.getstream.chat.android.client.models.Attachment
import io.getstream.chat.android.client.utils.Result
import io.getstream.chat.android.offline.message.attachment.AttachmentUploader
import io.getstream.chat.android.offline.randomAttachmentsWithFile
import io.getstream.chat.android.offline.randomMessage
import io.getstream.chat.android.test.TestCall
import io.getstream.chat.android.test.positiveRandomInt
import io.getstream.chat.android.test.randomString
import kotlinx.coroutines.test.runBlockingTest
import org.amshove.kluent.shouldBeEqualTo
import org.amshove.kluent.shouldBeNull
import org.amshove.kluent.shouldNotBeNull
import org.junit.Before
import org.junit.Ignore
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.Shadows
import java.io.File

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
    fun `Should return attachment with properly filled data when sending file has failed`(): Unit = runBlockingTest {
        val error = ChatError("")
        val attachment = randomAttachments(size = 1).first()

        val sut = Fixture()
            .givenMockedFileUploads(channelType, channelId, Result(error))
            .get()

        val result = sut.uploadAttachment(channelType, channelId, attachment)

        with(result.data()) {
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
    fun `Should return attachment with properly filled data when successfully sent file`() = runBlockingTest {
        val attachment = randomAttachments(size = 1).first()
        val url = "url"

        val sut = Fixture()
            .givenMockedFileUploads(channelType, channelId, Result(url))
            .get()

        val result = sut.uploadAttachment(channelType, channelId, attachment)

        with(result.data()) {
            name shouldBeEqualTo attachment.upload!!.name
            url shouldBeEqualTo url
            fileSize.shouldNotBeNull()
            type.shouldNotBeNull()
            mimeType.shouldNotBeNull()
            uploadState shouldBeEqualTo Attachment.UploadState.Success
        }
    }

    @Test
    fun `Upload attachment should have the right format`() = runBlockingTest {
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
                uploadState = Attachment.UploadState.Success
            )
            val result = sut.uploadAttachment(channelType, channelId, attachment)
            Truth.assertThat(result.isSuccess).isTrue()
            Truth.assertThat(result.data()).isEqualTo(expectedAttachment)
        }
    }

    @Test
    fun `Upload attachment should be configurable`() = runBlockingTest {
        val attachments = randomAttachments()
        val files: List<File> = attachments.map { it.upload!! }
        val extra = mutableMapOf<String, Any>("The Answer" to 42)

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
                extraData = extra,
                uploadState = Attachment.UploadState.Success
            )
            val result = sut.uploadAttachment(
                channelType,
                channelId,
                attachment = attachment,
                attachmentTransformer = { attachment, _ ->
                    attachment.copy(extraData = extra)
                }
            )

            Truth.assertThat(result.isSuccess).isTrue()
            Truth.assertThat(result.data()).isEqualTo(expectedAttachment)
        }
    }

    @Test
    fun `Should return apply the right transformation to attachments`() = runBlockingTest {
        val message = randomMessage()
        message.cid = "cid"
        message.attachments = randomAttachments()
        val extra = mutableMapOf<String, Any>("the answer" to 42)

        val files: List<File> = message.attachments.map { it.upload!! }

        val sut = Fixture()
            .givenMockedFileUploads(channelType, channelId, files)
            .get()

        val result = sut.uploadAttachment(
            channelType,
            channelId,
            message.attachments.first(),
            attachmentTransformer = { attachment, _ ->
                attachment.copy(extraData = extra)
            }
        )

        Truth.assertThat(result.isSuccess).isTrue()
        val uploadedAttachment = result.data()

        Truth.assertThat(uploadedAttachment.extraData).isEqualTo(extra)
    }

    private fun randomAttachments(size: Int = positiveRandomInt(10)): MutableList<Attachment> {
        return randomAttachmentsWithFile(size)
            .map { attachment ->
                attachment.apply { this.uploadState = Attachment.UploadState.InProgress }
            }
            .toMutableList()
    }

    private class Fixture {
        private var clientMock: ChatClient = mock()

        fun givenMockedFileUploads(channelType: String, channelId: String, result: Result<String>) = apply {
            whenever(
                clientMock.sendImage(
                    eq(channelType),
                    eq(channelId),
                    any(),
                    anyOrNull()
                )
            ) doReturn TestCall(result)
            whenever(
                clientMock.sendFile(
                    eq(channelType),
                    eq(channelId),
                    any(),
                    anyOrNull()
                )
            ) doReturn TestCall(result)
        }

        fun givenMockedFileUploads(channelType: String, channelId: String, files: List<File>) = apply {
            for (file in files) {
                val result = Result(file.absolutePath)
                whenever(
                    clientMock.sendFile(
                        eq(channelType),
                        eq(channelId),
                        same(file),
                        anyOrNull(),
                    )
                ) doReturn TestCall(result)
                whenever(
                    clientMock.sendImage(
                        eq(channelType),
                        eq(channelId),
                        same(file),
                        anyOrNull(),
                    )
                ) doReturn TestCall(result)
            }
        }

        fun get(): AttachmentUploader {
            return AttachmentUploader(clientMock)
        }
    }
}
