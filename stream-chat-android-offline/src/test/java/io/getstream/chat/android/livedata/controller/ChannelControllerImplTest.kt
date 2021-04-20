package io.getstream.chat.android.livedata.controller

import androidx.test.ext.junit.runners.AndroidJUnit4
import com.nhaarman.mockitokotlin2.any
import com.nhaarman.mockitokotlin2.doReturn
import com.nhaarman.mockitokotlin2.eq
import com.nhaarman.mockitokotlin2.mock
import com.nhaarman.mockitokotlin2.spy
import com.nhaarman.mockitokotlin2.verify
import com.nhaarman.mockitokotlin2.whenever
import io.getstream.chat.android.client.ChatClient
import io.getstream.chat.android.client.call.Call
import io.getstream.chat.android.client.errors.ChatError
import io.getstream.chat.android.client.models.Attachment
import io.getstream.chat.android.client.models.Channel
import io.getstream.chat.android.client.models.Member
import io.getstream.chat.android.client.models.Message
import io.getstream.chat.android.client.models.User
import io.getstream.chat.android.client.utils.Result
import io.getstream.chat.android.livedata.BaseDomainTest2
import io.getstream.chat.android.livedata.randomAttachmentsWithFile
import io.getstream.chat.android.offline.channel.ChannelController
import io.getstream.chat.android.test.TestCall
import io.getstream.chat.android.test.randomString
import junit.framework.Assert.assertEquals
import kotlinx.coroutines.runBlocking
import org.amshove.kluent.`should be`
import org.amshove.kluent.shouldBeEqualTo
import org.amshove.kluent.shouldBeNull
import org.amshove.kluent.shouldNotBeNull
import org.junit.Ignore
import org.junit.Test
import org.junit.runner.RunWith
import java.io.File

@RunWith(AndroidJUnit4::class)
@Ignore("broken due to usage of spy")
internal class ChannelControllerImplTest : BaseDomainTest2() {

    private val chatClient: ChatClient = spy()
    private val channelType: String = randomString()
    private val channelId: String = randomString()
    private val call: Call<String> = mock()
    private lateinit var channelController: ChannelController

    override fun setup() {
        super.setup()
        channelController =
            ChannelController(channelType, channelId, chatClient, chatDomainImpl)
    }

    @Test
    fun `Should return successful result when sending an image`(): Unit = runBlocking {
        val file = File(randomString())
        val expectedResult = Result(randomString())
        whenever(call.execute()) doReturn expectedResult
        whenever(chatClient.sendImage(channelType, channelId, file)) doReturn call

        val result = channelController.sendImage(file)

        result `should be` expectedResult
        verify(chatClient).sendImage(
            eq(channelType),
            eq(channelId),
            eq(file)
        )
    }

    @Test
    fun `Should return successful result when sending a file`(): Unit = runBlocking {
        val file = File(randomString())
        val expectedResult = Result(randomString())
        whenever(call.execute()) doReturn expectedResult
        whenever(chatClient.sendFile(channelType, channelId, file)) doReturn call

        val result = channelController.sendFile(file)

        result `should be` expectedResult
        verify(chatClient).sendFile(
            eq(channelType),
            eq(channelId),
            eq(file)
        )
    }

    @Test
    fun `Should return failure result when sending an image`() = runBlocking {
        val file = File(randomString())
        val expectedResult = Result<String>(ChatError(randomString()))
        whenever(call.execute()) doReturn expectedResult
        whenever(chatClient.sendImage(channelType, channelId, file)) doReturn call

        val result = channelController.sendImage(file)

        result `should be` expectedResult
        verify(chatClient).sendImage(
            eq(channelType),
            eq(channelId),
            eq(file)
        )
    }

    @Test
    fun `Should return failure result when sending a file`(): Unit = runBlocking {
        val file = File(randomString())
        val expectedResult = Result<String>(ChatError(randomString()))
        whenever(call.execute()) doReturn expectedResult
        whenever(chatClient.sendFile(channelType, channelId, file)) doReturn call

        val result = channelController.sendFile(file)

        result `should be` expectedResult
        verify(chatClient).sendFile(
            eq(channelType),
            eq(channelId),
            eq(file)
        )
    }

    @Test
    fun `Should return an empty channel when data is not present`(): Unit = runBlocking {
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

    @Test
    fun `Should return attachment with properly filled data when sending file has failed`(): Unit = runBlocking {
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

    @Test
    fun `Should return attachment with properly filled data when successfully sent file`(): Unit = runBlocking {
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

    @Test
    fun `Should include hidden property in the toChannel method`(): Unit = runBlocking {
        whenever(chatClient.hideChannel(any(), any(), any())) doReturn TestCall(Result(Unit))

        channelController.toChannel().hidden shouldBeEqualTo false
    }

    private fun givenMockedFileUploads(result: Result<String>) {
        whenever(chatClient.sendImage(any(), any(), any())) doReturn TestCall(result)
        whenever(chatClient.sendFile(any(), any(), any())) doReturn TestCall(result)
    }
}
