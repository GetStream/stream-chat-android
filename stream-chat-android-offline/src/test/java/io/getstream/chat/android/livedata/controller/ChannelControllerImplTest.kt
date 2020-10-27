package io.getstream.chat.android.livedata.controller

import androidx.test.ext.junit.runners.AndroidJUnit4
import com.nhaarman.mockitokotlin2.any
import com.nhaarman.mockitokotlin2.doReturn
import com.nhaarman.mockitokotlin2.eq
import com.nhaarman.mockitokotlin2.mock
import com.nhaarman.mockitokotlin2.spy
import io.getstream.chat.android.client.call.Call
import io.getstream.chat.android.client.errors.ChatError
import io.getstream.chat.android.client.models.Attachment
import io.getstream.chat.android.client.models.Channel
import io.getstream.chat.android.client.models.Member
import io.getstream.chat.android.client.models.Message
import io.getstream.chat.android.client.models.User
import io.getstream.chat.android.client.utils.Result
import io.getstream.chat.android.livedata.BaseDomainTest
import io.getstream.chat.android.livedata.TestResultCall
import io.getstream.chat.android.livedata.randomAttachmentsWithFile
import io.getstream.chat.android.livedata.randomString
import junit.framework.Assert.assertEquals
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.runBlocking
import org.amshove.kluent.Verify
import org.amshove.kluent.When
import org.amshove.kluent.`should be`
import org.amshove.kluent.called
import org.amshove.kluent.calling
import org.amshove.kluent.on
import org.amshove.kluent.shouldBeEqualTo
import org.amshove.kluent.shouldBeNull
import org.amshove.kluent.shouldNotBeNull
import org.junit.Test
import org.junit.runner.RunWith
import java.io.File

@RunWith(AndroidJUnit4::class)
internal class ChannelControllerImplTest : BaseDomainTest() {

    private val chatClient: ChatClient = spy()
    private val channelType: String = randomString()
    private val channelId: String = randomString()
    private val call: Call<String> = mock()
    private lateinit var channelController: ChannelControllerImpl

    override fun setup() {
        super.setup()
        channelController =
            ChannelControllerImpl(channelType, channelId, chatClient, chatDomainImpl)
    }

    @Test
    fun `Should return successful result when sending an image`() {
        runBlocking(Dispatchers.IO) {
            val file = File(randomString())
            val expectedResult = Result(randomString())
            When calling call.execute() doReturn expectedResult
            When calling chatClient.sendImage(channelType, channelId, file) doReturn call

            val result = channelController.sendImage(file)

            result `should be` expectedResult
            Verify on chatClient that chatClient.sendImage(
                eq(channelType),
                eq(channelId),
                eq(file)
            ) was called
        }
    }

    @Test
    fun `Should return successful result when sending a file`() {
        runBlocking(Dispatchers.IO) {
            val file = File(randomString())
            val expectedResult = Result(randomString())
            When calling call.execute() doReturn expectedResult
            When calling chatClient.sendFile(channelType, channelId, file) doReturn call

            val result = channelController.sendFile(file)

            result `should be` expectedResult
            Verify on chatClient that chatClient.sendFile(
                eq(channelType),
                eq(channelId),
                eq(file)
            ) was called
        }
    }

    @Test
    fun `Should return failure result when sending an image`() {
        runBlocking(Dispatchers.IO) {
            val file = File(randomString())
            val expectedResult = Result<String>(ChatError(randomString()))
            When calling call.execute() doReturn expectedResult
            When calling chatClient.sendImage(channelType, channelId, file) doReturn call

            val result = channelController.sendImage(file)

            result `should be` expectedResult
            Verify on chatClient that chatClient.sendImage(
                eq(channelType),
                eq(channelId),
                eq(file)
            ) was called
        }
    }

    @Test
    fun `Should return failure result when sending a file`() {
        runBlocking(Dispatchers.IO) {
            val file = File(randomString())
            val expectedResult = Result<String>(ChatError(randomString()))
            When calling call.execute() doReturn expectedResult
            When calling chatClient.sendFile(channelType, channelId, file) doReturn call

            val result = channelController.sendFile(file)

            result `should be` expectedResult
            Verify on chatClient that chatClient.sendFile(
                eq(channelType),
                eq(channelId),
                eq(file)
            ) was called
        }
    }

    @Test
    fun `Should return an empty channel when data is not present`() {
        runBlocking(Dispatchers.IO) {
            val expectedResult = Channel().apply {
                id = channelId
                type = channelType
                cid = channelController.cid
            }

            val result = channelController.toChannel()

            assertEquals(expectedResult.id, result.id)
            assertEquals(expectedResult.type, result.type)
            assertEquals(expectedResult.cid, result.cid)

            assertEquals(result.messages, emptyList<Message>())
            assertEquals(result.watchers, emptyList<User>())
            assertEquals(result.members, emptyList<Member>())
            assertEquals(result.messages, emptyList<Message>())
        }
    }

    @Test
    fun `Should return attachment with properly filled data when sending file has failed`() {
        runBlocking(Dispatchers.IO) {
            val error = ChatError("")
            val attachment = randomAttachmentsWithFile(size = 1).first()
            givenMockedFileUploads(Result(error))

            val result = channelController.uploadAttachment(attachment)

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
    }

    @Test
    fun `Should return attachment with properly filled data when successfully sent file`() {
        runBlocking(Dispatchers.IO) {
            val attachment = randomAttachmentsWithFile(size = 1).first()
            val url = "url"
            givenMockedFileUploads(Result(url))

            val result = channelController.uploadAttachment(attachment)

            with(result.data()) {
                name shouldBeEqualTo attachment.upload!!.name
                url shouldBeEqualTo url
                fileSize.shouldNotBeNull()
                type.shouldNotBeNull()
                mimeType.shouldNotBeNull()
                uploadState shouldBeEqualTo Attachment.UploadState.Success
            }
        }
    }

    @Test
    fun `Should include hidden property in the toChannel method`() {
        runBlocking(Dispatchers.IO) {
            When calling chatClient.hideChannel(any(), any(), any()) doReturn TestResultCall(Result(Unit))

            channelController.toChannel().hidden shouldBeEqualTo false
        }
    }

    private fun givenMockedFileUploads(result: Result<String>) {
        When calling chatClient.sendImage(any(), any(), any()) doReturn TestResultCall(result)
        When calling chatClient.sendFile(any(), any(), any()) doReturn TestResultCall(result)
    }
}
