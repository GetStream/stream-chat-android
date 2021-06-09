package io.getstream.chat.android.offline.channel.controller

import androidx.test.ext.junit.runners.AndroidJUnit4
import com.nhaarman.mockitokotlin2.any
import com.nhaarman.mockitokotlin2.doReturn
import com.nhaarman.mockitokotlin2.eq
import com.nhaarman.mockitokotlin2.mock
import com.nhaarman.mockitokotlin2.verify
import com.nhaarman.mockitokotlin2.whenever
import io.getstream.chat.android.client.errors.ChatError
import io.getstream.chat.android.client.models.Channel
import io.getstream.chat.android.client.models.Member
import io.getstream.chat.android.client.models.Message
import io.getstream.chat.android.client.models.User
import io.getstream.chat.android.client.utils.Result
import io.getstream.chat.android.offline.channel.ChannelController
import io.getstream.chat.android.offline.integration.BaseDomainTest2
import io.getstream.chat.android.test.TestCall
import io.getstream.chat.android.test.randomString
import junit.framework.Assert.assertEquals
import kotlinx.coroutines.runBlocking
import org.amshove.kluent.`should be`
import org.amshove.kluent.shouldBeEqualTo
import org.junit.Test
import org.junit.runner.RunWith
import java.io.File

@RunWith(AndroidJUnit4::class)
internal class ChannelControllerImplTest : BaseDomainTest2() {

    private val channelType: String = randomString()
    private val channelId: String = randomString()
    private lateinit var channelController: ChannelController

    override fun setup() {
        super.setup()
        channelController =
            ChannelController(channelType, channelId, clientMock, chatDomainImpl, messageSendingService = mock())
    }

    @Test
    fun `Should return successful result when sending an image`(): Unit = runBlocking {
        val file = File(randomString())
        val expectedResult = Result(randomString())
        whenever(clientMock.sendImage(channelType, channelId, file, null)) doReturn TestCall(expectedResult)

        val result = channelController.sendImage(file)

        result `should be` expectedResult
        verify(clientMock).sendImage(
            eq(channelType),
            eq(channelId),
            eq(file),
            eq(null)
        )
    }

    @Test
    fun `Should return successful result when sending a file`(): Unit = runBlocking {
        val file = File(randomString())
        val expectedResult = Result(randomString())
        whenever(clientMock.sendFile(channelType, channelId, file, null)) doReturn TestCall(expectedResult)

        val result = channelController.sendFile(file)

        result `should be` expectedResult
        verify(clientMock).sendFile(
            eq(channelType),
            eq(channelId),
            eq(file),
            eq(null)
        )
    }

    @Test
    fun `Should return failure result when sending an image`(): Unit = runBlocking {
        val file = File(randomString())
        val expectedResult = Result<String>(ChatError(randomString()))
        whenever(clientMock.sendImage(channelType, channelId, file, null)) doReturn TestCall(expectedResult)

        val result = channelController.sendImage(file)

        result `should be` expectedResult
        verify(clientMock).sendImage(
            eq(channelType),
            eq(channelId),
            eq(file),
            eq(null)
        )
    }

    @Test
    fun `Should return failure result when sending a file`(): Unit = runBlocking {
        val file = File(randomString())
        val expectedResult = Result<String>(ChatError(randomString()))
        whenever(clientMock.sendFile(channelType, channelId, file, null)) doReturn TestCall(expectedResult)

        val result = channelController.sendFile(file)

        result `should be` expectedResult
        verify(clientMock).sendFile(
            eq(channelType),
            eq(channelId),
            eq(file),
            eq(null)
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
    fun `Should include hidden property in the toChannel method`(): Unit = runBlocking {
        whenever(clientMock.hideChannel(any(), any(), any())) doReturn TestCall(Result(Unit))

        channelController.toChannel().hidden shouldBeEqualTo false
    }
}
