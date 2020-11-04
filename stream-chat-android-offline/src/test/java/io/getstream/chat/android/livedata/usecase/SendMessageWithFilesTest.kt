package io.getstream.chat.android.livedata.usecase

import android.webkit.MimeTypeMap
import androidx.test.ext.junit.runners.AndroidJUnit4
import com.google.common.truth.Truth
import com.nhaarman.mockitokotlin2.argThat
import com.nhaarman.mockitokotlin2.doReturn
import com.nhaarman.mockitokotlin2.eq
import com.nhaarman.mockitokotlin2.same
import io.getstream.chat.android.client.errors.ChatError
import io.getstream.chat.android.client.models.Attachment
import io.getstream.chat.android.client.utils.Result
import io.getstream.chat.android.livedata.BaseConnectedMockedTest
import io.getstream.chat.android.livedata.randomAttachmentsWithFile
import io.getstream.chat.android.livedata.randomMessage
import io.getstream.chat.android.livedata.utils.TestCall
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.runBlocking
import org.amshove.kluent.When
import org.amshove.kluent.`should throw`
import org.amshove.kluent.`with message`
import org.amshove.kluent.calling
import org.amshove.kluent.invoking
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.Shadows.shadowOf
import java.io.File

@RunWith(AndroidJUnit4::class)
internal class SendMessageWithFilesTest : BaseConnectedMockedTest() {

    val sendMessageWithFile: SendMessage by lazy { chatDomain.useCases.sendMessage }

    @Before
    override fun setup() {
        super.setup()
        shadowOf(MimeTypeMap.getSingleton()).addExtensionMimeTypMapping("jpg", "image/jpeg")
    }

    private fun mockFileUploads(files: List<File>) {
        for (file in files) {
            val result = Result(file.absolutePath)
            When calling client.sendFile(eq(channelControllerImpl.channelType), eq(channelControllerImpl.channelId), same(file)) doReturn TestCall(result)
            When calling client.sendImage(eq(channelControllerImpl.channelType), eq(channelControllerImpl.channelId), same(file)) doReturn TestCall(result)
        }
    }

    private fun mockFileUploadsFailure(files: List<File>) {

        for (file in files) {
            val path: String? = null
            val result = Result(path, file.toChatError())
            When calling client.sendFile(eq(channelControllerImpl.channelType), eq(channelControllerImpl.channelId), same(file)) doReturn TestCall(result)
            When calling client.sendImage(eq(channelControllerImpl.channelType), eq(channelControllerImpl.channelId), same(file)) doReturn TestCall(result)
        }
    }

    @Test
    fun `Should return message sending files`() {
        runBlocking(Dispatchers.IO) {
            val message = randomMessage()
            message.cid = channelControllerImpl.cid
            message.attachments = randomAttachmentsWithFile().toMutableList()

            val expectedAttachments = message.attachments.map { it.copy(assetUrl = it.upload!!.absolutePath, upload = null, type = "file") }

            val expectedResult = Result(
                message.copy(
                    attachments = expectedAttachments.toMutableList()
                )
            )
            val files: List<File> = message.attachments.map { it.upload!! }

            When calling channelMock.sendMessage(argThat { id === message.id }) doReturn TestCall(expectedResult)

            mockFileUploads(files)

            val result = sendMessageWithFile(message).execute()
            assertSuccess(result)

            Truth.assertThat(result).isEqualTo(expectedResult)
        }
    }

    @Test
    fun `Upload attachment should have the right format`() {
        runBlocking(Dispatchers.IO) {
            val attachments = randomAttachmentsWithFile().toMutableList()
            val files: List<File> = attachments.map { it.upload!! }

            mockFileUploads(files)

            for (attachment in attachments) {
                val url = attachment.upload!!.absolutePath
                val expectedAttachment = attachment.copy(assetUrl = url, url = url, type = "file", mimeType = "", name = attachment.upload!!.name, uploadState = Attachment.UploadState.Success)
                val result = channelControllerImpl.uploadAttachment(attachment = attachment)
                assertSuccess(result)
                Truth.assertThat(result.data()).isEqualTo(expectedAttachment)
            }
        }
    }

    @Test
    fun `Upload attachment should be configurable`() {
        runBlocking(Dispatchers.IO) {
            val attachments = randomAttachmentsWithFile().toMutableList()
            val files: List<File> = attachments.map { it.upload!! }

            mockFileUploads(files)
            val extra = mutableMapOf<String, Any>("The Answer" to 42)

            for (attachment in attachments) {
                val url = attachment.upload!!.absolutePath
                val expectedAttachment = attachment.copy(assetUrl = url, url = url, type = "file", mimeType = "", name = attachment.upload!!.name, extraData = extra, uploadState = Attachment.UploadState.Success)
                val result = channelControllerImpl.uploadAttachment(attachment = attachment) {
                    attachment, _ ->
                    attachment.copy(extraData = extra)
                }
                assertSuccess(result)
                Truth.assertThat(result.data()).isEqualTo(expectedAttachment)
            }
        }
    }

    @Test
    fun `Upload attachment with error should be configurable`() {
        runBlocking(Dispatchers.IO) {
            val attachments = randomAttachmentsWithFile().toMutableList()
            val files: List<File> = attachments.map { it.upload!! }

            mockFileUploadsFailure(files)
            val extra = mutableMapOf<String, Any>("The Answer" to 42)

            for (attachment in attachments) {
                val error = attachment.upload!!.toChatError()
                val result = channelControllerImpl.uploadAttachment(attachment = attachment) {
                    attachment, _ ->
                    attachment.copy(extraData = extra)
                }
                assertFailure(result)
                Truth.assertThat(result.data().uploadState).isInstanceOf(Attachment.UploadState.Failed::class.java)
                Truth.assertThat(result.data().extraData).isEqualTo(extra)
            }
        }
    }

    @Test
    fun `Upload attachment should with errors should have the right format`() {
        runBlocking(Dispatchers.IO) {
            val attachments = randomAttachmentsWithFile().toMutableList()
            val files: List<File> = attachments.map { it.upload!! }

            mockFileUploadsFailure(files)

            for (attachment in attachments) {
                val error = attachment.upload!!.toChatError()
                val result = channelControllerImpl.uploadAttachment(attachment = attachment)
                assertFailure(result)
                Truth.assertThat(result.data().uploadState).isInstanceOf(Attachment.UploadState.Failed::class.java)
            }
        }
    }

    @Test
    fun `Errors should still return the attachments`() {
        runBlocking(Dispatchers.IO) {

            val message = randomMessage()
            message.cid = channelControllerImpl.cid
            message.attachments = randomAttachmentsWithFile().toMutableList()

            val expectedAttachments = message.attachments.map { it.copy(uploadState = Attachment.UploadState.Failed(it.upload!!.toChatError())) }

            val expectedResult = Result(
                message.copy(attachments = expectedAttachments.toMutableList())
            )
            val files: List<File> = message.attachments.map { it.upload!! }

            When calling channelMock.sendMessage(argThat { id === message.id }) doReturn TestCall(expectedResult)

            mockFileUploadsFailure(files)

            val result = sendMessageWithFile(message).execute()

            Truth.assertThat(result).isEqualTo(expectedResult)
        }
    }

    @Test
    fun `Should return apply the right transformation to attachments`() {
        runBlocking(Dispatchers.IO) {
            val message = randomMessage()
            message.cid = channelControllerImpl.cid
            message.attachments = randomAttachmentsWithFile().toMutableList()
            val extra = mutableMapOf<String, Any>("the answer" to 42)

            val files: List<File> = message.attachments.map { it.upload!! }

            mockFileUploads(files)

            val result = channelControllerImpl.uploadAttachment(message.attachments.first()) {
                attachment, _ ->
                attachment.copy(extraData = extra)
            }
            assertSuccess(result)
            val uploadedAttachment = result.data()

            Truth.assertThat(uploadedAttachment.extraData).isEqualTo(extra)
        }
    }

    @Test
    fun `Should throw an exception if the channel cid is empty`() {
        val message = randomMessage()
        message.attachments = randomAttachmentsWithFile().toMutableList()
        message.cid = ""

        invoking {
            sendMessageWithFile(message)
        } `should throw` IllegalArgumentException::class `with message` "cid can not be empty"
    }

    @Test
    fun `Should throw an exception if the channel cid doesn't contain a colon`() {
        val message = randomMessage()
        message.attachments = randomAttachmentsWithFile().toMutableList()
        message.cid = "abc"

        invoking {
            sendMessageWithFile(message)
        } `should throw` IllegalArgumentException::class `with message` "cid needs to be in the format channelType:channelId. For example, messaging:123"
    }
}

private fun File.toChatError(): ChatError = ChatError(absolutePath)
