package io.getstream.chat.android.livedata.mockwebserver

import androidx.test.ext.junit.runners.AndroidJUnit4
import com.google.common.truth.Truth
import io.getstream.chat.android.client.models.Message
import io.getstream.chat.android.client.models.name
import io.getstream.chat.android.client.utils.SyncStatus
import io.getstream.chat.android.livedata.utils.isRequestBodyEqualTo
import kotlinx.coroutines.runBlocking
import okhttp3.mockwebserver.MockResponse
import okhttp3.mockwebserver.RecordedRequest
import org.junit.Test
import org.junit.runner.RunWith

@RunWith(AndroidJUnit4::class)
internal class SendMessageUseCaseTest : BaseMockWebServerIntegrationTest() {

    @Test
    fun sendMessageUseCase(): Unit = runBlocking {
        // setup mock web server routing
        serverCalls = listOf(CreateMessageServerCall())

        val sendMessageResult = chatDomain.useCases
            .sendMessage(Message(id = MESSAGE_ID, cid = CHANNEL_ID, text = MESSAGE_TEXT))
            .execute()

        assertSuccess(sendMessageResult)

        val messagesFromDatabase = chatDomainImpl.repos.selectMessages(listOf(MESSAGE_ID))

        Truth.assertThat(messagesFromDatabase.size).isEqualTo(1)
        Truth.assertThat(messagesFromDatabase[0].syncStatus).isEqualTo(SyncStatus.COMPLETED)
        Truth.assertThat(messagesFromDatabase[0].text).isEqualTo(MESSAGE_TEXT)
        Truth.assertThat(messagesFromDatabase[0].user.name).isEqualTo(USERNAME)
    }

    private class CreateMessageServerCall : MockWebServerCall {
        override fun isApplicable(request: RecordedRequest): Boolean {
            return request.isRequestBodyEqualTo(
                createSendMessageRequestJsonString(
                    messageId = MESSAGE_ID,
                    messageText = MESSAGE_TEXT,
                    channelId = CHANNEL_ID
                )
            )
        }

        override fun executeCall(): MockResponse {
            return MockResponse()
                .setResponseCode(200)
                .setBody(
                    createSendMessageResponseJsonString(
                        messageId = MESSAGE_ID,
                        messageText = MESSAGE_TEXT,
                        channelId = CHANNEL_ID,
                        username = USERNAME
                    )
                )
        }
    }

    companion object {
        private const val MESSAGE_ID = "cebf562a-4806-4c64-a827-59d50aac42ba-80f7fe50-ec73-49f8-a389-3dd5b4304b6f"
        private const val CHANNEL_ID = "messaging:e87283f0-a58d-4685-bf0b-729a7b6eb84d"
        private const val MESSAGE_TEXT = "message test"
        private const val USERNAME = "Zetra"
    }
}
