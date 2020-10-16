package io.getstream.chat.android.livedata.usecase

import android.webkit.MimeTypeMap
import androidx.test.ext.junit.runners.AndroidJUnit4
import assertions.`should be equal to result`
import com.nhaarman.mockitokotlin2.any
import com.nhaarman.mockitokotlin2.doAnswer
import com.nhaarman.mockitokotlin2.doReturn
import com.nhaarman.mockitokotlin2.eq
import com.nhaarman.mockitokotlin2.mock
import com.nhaarman.mockitokotlin2.spy
import io.getstream.chat.android.client.errors.ChatError
import io.getstream.chat.android.client.models.Attachment
import io.getstream.chat.android.client.models.Message
import io.getstream.chat.android.client.utils.Result
import io.getstream.chat.android.livedata.ChatDomainImpl
import io.getstream.chat.android.livedata.controller.ChannelControllerImpl
import io.getstream.chat.android.livedata.randomCID
import io.getstream.chat.android.livedata.randomFile
import io.getstream.chat.android.livedata.randomFiles
import io.getstream.chat.android.livedata.randomImageFile
import io.getstream.chat.android.livedata.randomMessage
import io.getstream.chat.android.livedata.randomString
import kotlinx.coroutines.runBlocking
import org.amshove.kluent.Verify
import org.amshove.kluent.When
import org.amshove.kluent.`should be equal to`
import org.amshove.kluent.`should throw`
import org.amshove.kluent.`with message`
import org.amshove.kluent.called
import org.amshove.kluent.calling
import org.amshove.kluent.invoking
import org.amshove.kluent.on
import org.amshove.kluent.that
import org.amshove.kluent.was
import org.junit.Before
import org.junit.Ignore
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.Shadows.shadowOf
import java.io.File

@RunWith(AndroidJUnit4::class)
@Ignore
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
            sendMessageWithAttachemen("", randomMessage(), randomFiles())
        } `should throw` IllegalArgumentException::class `with message` "cid can not be empty"
    }

    @Test
    fun `Should throw an exception if the channel cid doesn't contain a colon`() {
        invoking {
            sendMessageWithAttachemen(randomString().replace(":", ""), randomMessage(), randomFiles())
        } `should throw` IllegalArgumentException::class `with message` "cid needs to be in the format channelType:channelId. For example, messaging:123"
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
                randomMessage(),
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

            val result = sendMessageWithAttachemen(randomCID(), randomMessage(), (images + badImage).shuffled()).execute()

            result `should be equal to result` expectedResult
        }
    }

    @Test
    fun `Should return message sending files`() {
        runBlocking {
            val files = randomFiles()
            val cid = randomCID()
            val message = randomMessage(cid = cid)
            val expectedResult = Result(
                message.copy(
                    cid = cid,
                    attachments = (
                        message.attachments + files.map {
                            it.toAttachment(null).apply {
                                assetUrl = it.absolutePath
                                type = "file"
                            }
                        }
                        ).toMutableList()
                )
            )
            When calling channelController.scope doReturn this
            channelController.configureSuccessResultSendingFiles(files)

            val result = sendMessageWithAttachemen(cid, message, files).execute()

            result `should be equal to` expectedResult
        }
    }

    @Test
    fun `Should return message sending Images`() {
        runBlocking {
            val images = randomFiles { randomImageFile() }
            val cid = randomCID()
            val message = randomMessage()
            val expectedResult = Result(
                message.copy(
                    cid = cid,
                    attachments = (
                        message.attachments + images.map {
                            it.toAttachment("image/jpeg").apply {
                                imageUrl = it.absolutePath
                                type = "image"
                            }
                        }
                        ).toMutableList()
                )
            )
            When calling channelController.scope doReturn this
            channelController.configureSuccessResultSendingImages(images)

            val result = sendMessageWithAttachemen(cid, message, images).execute()

            result `should be equal to result` expectedResult
        }
    }

    @Test
    fun `Should apply transformations to attachments from image files`() {
        runBlocking {
            val images = randomFiles { randomImageFile() }
            val cid = randomCID()
            val message = randomMessage()
            val extraDataKey = randomString()
            val attachmentTransformation: Attachment.(file: File) -> Unit = { file ->
                this.extraData[extraDataKey] = file.name
            }
            val transformationSpy: Attachment.(File) -> Unit = spy(attachmentTransformation)
            val expectedResult = Result(
                message.copy(
                    cid = cid,
                    attachments = (
                        message.attachments + images.map {
                            it.toAttachment("image/jpeg").apply {
                                imageUrl = it.absolutePath
                                type = "image"
                                extraData[extraDataKey] = it.name
                            }
                        }
                        ).toMutableList()
                )
            )
            When calling channelController.scope doReturn this
            channelController.configureSuccessResultSendingImages(images)

            val result = sendMessageWithAttachemen(cid, message, images, transformationSpy).execute()

            result `should be equal to result` expectedResult
            images.forEach {
                Verify on transformationSpy that transformationSpy(any(), eq(it)) was called
            }
        }
    }

    @Test
    fun `Should apply transformations to attachments from files`() {
        runBlocking {
            val files = randomFiles()
            val cid = randomCID()
            val message = randomMessage()
            val extraDataKey = randomString()
            val attachmentTransformation: Attachment.(file: File) -> Unit = { file ->
                this.extraData[extraDataKey] = file.name
            }
            val transformationSpy: Attachment.(File) -> Unit = spy(attachmentTransformation)
            val expectedResult = Result(
                message.copy(
                    cid = cid,
                    attachments = (
                        message.attachments + files.map {
                            it.toAttachment(null).apply {
                                assetUrl = it.absolutePath
                                type = "file"
                                extraData[extraDataKey] = it.name
                            }
                        }
                        ).toMutableList()
                )
            )
            When calling channelController.scope doReturn this
            channelController.configureSuccessResultSendingFiles(files)

            val result = sendMessageWithAttachemen(cid, message, files, transformationSpy).execute()

            result `should be equal to result` expectedResult
            files.forEach {
                Verify on transformationSpy that transformationSpy(any(), eq(it)) was called
            }
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
