package io.getstream.chat.android.livedata.usecase

import android.webkit.MimeTypeMap
import androidx.test.ext.junit.runners.AndroidJUnit4
import assertions.`should be equal to result`
import com.nhaarman.mockitokotlin2.any
import com.nhaarman.mockitokotlin2.doAnswer
import com.nhaarman.mockitokotlin2.doReturn
import com.nhaarman.mockitokotlin2.mock
import io.getstream.chat.android.client.errors.ChatError
import io.getstream.chat.android.client.models.Attachment
import io.getstream.chat.android.client.models.Message
import io.getstream.chat.android.client.utils.Result
import io.getstream.chat.android.livedata.*
import io.getstream.chat.android.livedata.controller.ChannelControllerImpl
import kotlinx.coroutines.runBlocking
import org.amshove.kluent.*
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.Shadows.shadowOf
import java.io.File
import java.security.InvalidParameterException

@RunWith(AndroidJUnit4::class)
class SendMessageWithAttachmentsImplTest {

    val channelController: ChannelControllerImpl = mock()
    val chatDomain: ChatDomainImpl = mock()
    val sendMessageWithAttachemen = SendMessageWithAttachmentsImpl(chatDomain)

    @Before
    fun setup() {
        shadowOf(MimeTypeMap.getSingleton()).addExtensionMimeTypMapping("jpg", "image/jpeg")
        runBlocking { When calling channelController.sendMessage(any()) doAnswer { Result(it.arguments[0] as Message) } }
        When calling chatDomain.channel(any<String>()) doReturn channelController
    }

    @Test
    fun `Should throw an exception if the channel cid is empty`() {
        invoking {
            sendMessageWithAttachemen("", randomString(), randomFiles())
        } `should throw` InvalidParameterException::class `with message` "cid cant be empty"
    }

    @Test
    fun `Should throw an exception if the channel cid doesn't contain a colon`() {
        invoking {
            sendMessageWithAttachemen(randomString().replace(":", ""), randomString(), randomFiles())
        } `should throw` InvalidParameterException::class`with message` "cid needs to be in the format channelType:channelId. For example messaging:123"
    }

    @Test
    fun `Should return error sending files`() {
        runBlocking {
            val badFile = randomFile()
            val files = randomFiles()
            val expectedResult = Result<Message>(badFile.toCharError())
            When calling channelController.scope doReturn this
            channelController.configureSuccessResultSendingFiles(files)
            channelController.configureFailureResultSendingFile(badFile)

            val result = sendMessageWithAttachemen(
                randomCID(),
                randomString(),
                (files + badFile).shuffled()
            ).execute()

            result `should be equal to result` expectedResult
        }
    }

    @Test
    fun `Should return error sending Images`() {
        runBlocking {
            val badImage = randomImageFile()
            val images = randomFiles { randomImageFile() }
            val expectedResult = Result<Message>(badImage.toCharError())
            When calling channelController.scope doReturn this
            channelController.configureSuccessResultSendingImages(images)
            channelController.configureFailureResultSendingImage(badImage)

            val result = sendMessageWithAttachemen(randomCID(), randomString(), (images + badImage).shuffled()).execute()

            result `should be equal to result`  expectedResult
        }
    }

    @Test
    fun `Should return message sending files`() {
        runBlocking {
            val files = randomFiles()
            val messageText = randomString()
            val cid = randomCID()
            val expectedResult = Result(
                Message(
                    cid = cid,
                    text = messageText,
                    attachments = files.map {
                        it.toAttachment(null).apply {
                            assetUrl = it.absolutePath
                        }
                    }.toMutableList()
                )
            )
            When calling channelController.scope doReturn this
            channelController.configureSuccessResultSendingFiles(files)

            val result = sendMessageWithAttachemen(cid, messageText, files).execute()

            result `should be equal to` expectedResult
        }
    }

    @Test
    fun `Should return message sending Images`() {
        runBlocking {
            val images = randomFiles { randomImageFile() }
            val messageText = randomString()
            val cid = randomCID()
            val expectedResult = Result(
                Message(
                    cid = cid,
                    text = messageText,
                    attachments = images.map {
                        it.toAttachment("image/jpeg").apply {
                            imageUrl = it.absolutePath
                        }
                    }.toMutableList()
                )
            )
            When calling channelController.scope doReturn this
            channelController.configureSuccessResultSendingImages(images)

            val result = sendMessageWithAttachemen(cid, messageText, images).execute()

            result `should be equal to result` expectedResult
        }
    }
}

private fun File.toCharError(): ChatError = ChatError(absolutePath)
private fun File.toAttachment(mimetype: String?) = Attachment(
    name = name,
    fileSize = length().toInt(),
    mimeType = mimetype,
    url = absolutePath
)

private suspend fun ChannelControllerImpl.configureSuccessResultSendingFiles(files: List<File>) {
    files.forEach { configureSuccessResultSendingFile(it) }
}

private suspend fun ChannelControllerImpl.configureSuccessResultSendingFile(file: File) {
    configureResultSendingFile(file, Result(file.absolutePath))
}

private suspend fun ChannelControllerImpl.configureFailureResultSendingFile(file: File) {
    configureResultSendingFile(file, Result(file.toCharError()))
}

private suspend fun ChannelControllerImpl.configureResultSendingFile(file: File, result: Result<String>) {
    When calling sendFile(file) doReturn result
}

private suspend fun ChannelControllerImpl.configureSuccessResultSendingImages(files: List<File>) {
    files.forEach { configureSuccessResultSendingImage(it) }
}

private suspend fun ChannelControllerImpl.configureSuccessResultSendingImage(file: File) {
    configureResultSendingImage(file, Result(file.absolutePath))
}

private suspend fun ChannelControllerImpl.configureFailureResultSendingImage(file: File) {
    configureResultSendingImage(file, Result(file.toCharError()))
}

private suspend fun ChannelControllerImpl.configureResultSendingImage(file: File, result: Result<String>) {
    When calling sendImage(file) doReturn result
}