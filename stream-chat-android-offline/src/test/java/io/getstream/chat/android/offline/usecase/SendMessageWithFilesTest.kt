package io.getstream.chat.android.offline.usecase

import android.webkit.MimeTypeMap
import androidx.test.ext.junit.runners.AndroidJUnit4
import com.google.common.truth.Truth
import com.nhaarman.mockitokotlin2.anyOrNull
import com.nhaarman.mockitokotlin2.argThat
import com.nhaarman.mockitokotlin2.doReturn
import com.nhaarman.mockitokotlin2.eq
import com.nhaarman.mockitokotlin2.same
import com.nhaarman.mockitokotlin2.whenever
import io.getstream.chat.android.client.errors.ChatError
import io.getstream.chat.android.client.models.Attachment
import io.getstream.chat.android.client.utils.Result
import io.getstream.chat.android.offline.integration.BaseDomainTest2
import io.getstream.chat.android.offline.randomAttachmentsWithFile
import io.getstream.chat.android.offline.randomMessage
import io.getstream.chat.android.test.TestCall
import kotlinx.coroutines.test.runBlockingTest
import org.amshove.kluent.`should throw`
import org.amshove.kluent.`with message`
import org.amshove.kluent.invoking
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.Shadows.shadowOf
import java.io.File

@RunWith(AndroidJUnit4::class)
internal class SendMessageWithFilesTest : BaseDomainTest2() {

    @Before
    override fun setup() {
        super.setup()
        shadowOf(MimeTypeMap.getSingleton()).addExtensionMimeTypMapping("jpg", "image/jpeg")
    }

    private fun mockFileUploads(files: List<File>) {
        for (file in files) {
            val result = Result(file.absolutePath)
            whenever(
                clientMock.sendFile(
                    eq(channelControllerImpl.channelType),
                    eq(channelControllerImpl.channelId),
                    same(file),
                    anyOrNull(),
                )
            ) doReturn TestCall(result)
            whenever(
                clientMock.sendImage(
                    eq(channelControllerImpl.channelType),
                    eq(channelControllerImpl.channelId),
                    same(file),
                    anyOrNull(),
                )
            ) doReturn TestCall(result)
        }
    }

    private fun mockFileUploadsFailure(files: List<File>) {
        for (file in files) {
            val result = Result<String>(file.toChatError())
            whenever(
                clientMock.sendFile(
                    eq(channelControllerImpl.channelType),
                    eq(channelControllerImpl.channelId),
                    same(file),
                    anyOrNull(),
                )
            ) doReturn TestCall(result)
            whenever(
                clientMock.sendImage(
                    eq(channelControllerImpl.channelType),
                    eq(channelControllerImpl.channelId),
                    same(file),
                    anyOrNull(),
                )
            ) doReturn TestCall(result)
        }
    }

    @Test
    fun `Should return message sending files`() = testCoroutines.scope.runBlockingTest {
        val message = randomMessage()
        message.cid = channelControllerImpl.cid
        message.attachments = defaultAttachments()

        val expectedAttachments =
            message.attachments.map { it.copy(assetUrl = it.upload!!.absolutePath, upload = null, type = "file") }

        val expectedResult = Result(
            message.copy(
                attachments = expectedAttachments.toMutableList()
            )
        )
        val files: List<File> = message.attachments.map { it.upload!! }

        whenever(channelClientMock.sendMessage(argThat { id === message.id })) doReturn TestCall(expectedResult)

        mockFileUploads(files)

        val result = chatDomain.sendMessage(message).execute()
        assertSuccess(result)

        Truth.assertThat(result).isEqualTo(expectedResult)
    }

    @Test
    fun `Errors should still return the attachments`() = testCoroutines.scope.runBlockingTest {
        val message = randomMessage()
        message.cid = channelControllerImpl.cid
        message.attachments = defaultAttachments()

        val expectedAttachments =
            message.attachments.map { it.copy(uploadState = Attachment.UploadState.Failed(it.upload!!.toChatError())) }

        val expectedResult = Result(
            message.copy(attachments = expectedAttachments.toMutableList())
        )
        val files: List<File> = message.attachments.map { it.upload!! }

        whenever(channelClientMock.sendMessage(argThat { id === message.id })) doReturn TestCall(expectedResult)

        mockFileUploadsFailure(files)

        val result = chatDomain.sendMessage(message).execute()

        Truth.assertThat(result).isEqualTo(expectedResult)
    }

    @Test
    fun `Should throw an exception if the channel cid is empty`() = testCoroutines.scope.runBlockingTest {
        val message = randomMessage()
        message.attachments = defaultAttachments()
        message.cid = ""

        invoking {
            chatDomain.sendMessage(message)
        } `should throw` IllegalArgumentException::class `with message` "cid can not be empty"
    }

    @Test
    fun `Should throw an exception if the channel cid doesn't contain a colon`() =
        testCoroutines.scope.runBlockingTest {
            val message = randomMessage()
            message.attachments = defaultAttachments()
            message.cid = "abc"

            invoking {
                chatDomain.sendMessage(message)
            } `should throw` IllegalArgumentException::class `with message` "cid needs to be in the format channelType:channelId. For example, messaging:123"
        }

    private fun defaultAttachments() =
        randomAttachmentsWithFile()
            .map { attachment ->
                attachment.apply { this.uploadState = Attachment.UploadState.InProgress }
            }
            .toMutableList()

    private fun File.toChatError(): ChatError = ChatError(absolutePath)
}
