package io.getstream.chat.android.client

import com.nhaarman.mockitokotlin2.mock
import com.nhaarman.mockitokotlin2.whenever
import io.getstream.chat.android.client.api.GsonChatApi
import io.getstream.chat.android.client.api.RetrofitAnonymousApi
import io.getstream.chat.android.client.api.RetrofitApi
import io.getstream.chat.android.client.api.models.MessageRequest
import io.getstream.chat.android.client.api.models.MessageResponse
import io.getstream.chat.android.client.models.Message
import io.getstream.chat.android.client.utils.RetroSuccess
import io.getstream.chat.android.client.utils.UuidGenerator
import io.getstream.chat.android.test.TestCoroutineExtension
import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.extension.RegisterExtension

internal class MessageIdGenerationTests {

    companion object {
        @JvmField
        @RegisterExtension
        val testCoroutines = TestCoroutineExtension()
    }

    val userId = "user-id"
    val connectionId = "connection-id"
    val messageId = "message-id"
    val channelType = "channel-type"
    val channelId = "channel-id"
    val randomUuid = "random-uuid"
    val messageText = "message-text"

    lateinit var uuidGenerator: UuidGenerator
    private lateinit var retroApi: RetrofitApi
    private lateinit var retroAnonymousApi: RetrofitAnonymousApi
    private lateinit var api: GsonChatApi

    @BeforeEach
    fun before() {
        retroApi = mock()
        retroAnonymousApi = mock()
        uuidGenerator = mock()
        api = GsonChatApi(
            retroApi,
            retroAnonymousApi,
            uuidGenerator,
            mock(),
            testCoroutines.scope,
        )
        api.setConnection(userId, connectionId)
    }

    @Test
    fun emptyMessageId() {

        val message = Message()
        message.text = messageText

        whenever(uuidGenerator.generate()).thenReturn(randomUuid)
        whenever(
            retroApi.sendMessage(
                channelType,
                channelId,
                connectionId,
                MessageRequest(message)
            )
        ).thenReturn(RetroSuccess(MessageResponse(message)).toRetrofitCall())

        api.sendMessage(channelType, channelId, message)

        assertThat(message.id).isEqualTo("$userId-$randomUuid")
    }

    @Test
    fun filledMessageId() {

        val message = Message()
        val preGeneratedId = "pre-generated-id"
        message.text = messageText
        message.id = preGeneratedId

        whenever(
            retroApi.sendMessage(
                channelType,
                channelId,
                connectionId,
                MessageRequest(message)
            )
        ).thenReturn(RetroSuccess(MessageResponse(message)).toRetrofitCall())

        api.sendMessage(channelType, channelId, message)

        assertThat(message.id).isEqualTo(preGeneratedId)
    }
}
