package io.getstream.chat.android.offline.channel.controller

import androidx.test.ext.junit.runners.AndroidJUnit4
import com.nhaarman.mockitokotlin2.any
import com.nhaarman.mockitokotlin2.doReturn
import com.nhaarman.mockitokotlin2.eq
import com.nhaarman.mockitokotlin2.verify
import com.nhaarman.mockitokotlin2.whenever
import io.getstream.chat.android.client.errors.ChatError
import io.getstream.chat.android.client.models.Channel
import io.getstream.chat.android.client.models.Member
import io.getstream.chat.android.client.models.Message
import io.getstream.chat.android.client.models.User
import io.getstream.chat.android.client.utils.Result
import io.getstream.chat.android.core.ExperimentalStreamChatApi
import io.getstream.chat.android.offline.channel.ChannelController
import io.getstream.chat.android.offline.experimental.channel.logic.ChannelLogic
import io.getstream.chat.android.offline.experimental.channel.state.ChannelMutableState
import io.getstream.chat.android.offline.integration.BaseDomainTest2
import io.getstream.chat.android.test.TestCall
import io.getstream.chat.android.test.randomString
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.runBlocking
import org.amshove.kluent.`should be`
import org.amshove.kluent.shouldBeEqualTo
import org.junit.Test
import org.junit.runner.RunWith
import java.io.File

@OptIn(ExperimentalStreamChatApi::class)
@RunWith(AndroidJUnit4::class)
internal class ChannelControllerImplTest : BaseDomainTest2() {

    private val channelType: String = randomString()
    private val channelId: String = randomString()
    private lateinit var channelController: ChannelController

    override fun setup() {
        super.setup()
        val mutableState = ChannelMutableState(
            channelType,
            channelId,
            chatDomainImpl.scope,
            chatDomainImpl.user,
            MutableStateFlow(emptyMap())
        )
        channelController =
            ChannelController(
                mutableState,
                ChannelLogic(mutableState, chatDomainImpl),
                clientMock,
                chatDomainImpl
            )
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

        result.id shouldBeEqualTo expectedResult.id
        result.type shouldBeEqualTo expectedResult.type
        result.cid shouldBeEqualTo expectedResult.cid

        emptyList<Message>() shouldBeEqualTo result.messages
        emptyList<User>() shouldBeEqualTo result.watchers
        emptyList<Member>() shouldBeEqualTo result.members
        emptyList<Message>() shouldBeEqualTo result.messages
    }

    @Test
    fun `Should include hidden property in the toChannel method`(): Unit = runBlocking {
        whenever(clientMock.hideChannel(any(), any(), any())) doReturn TestCall(Result(Unit))

        channelController.toChannel().hidden shouldBeEqualTo false
    }
}
