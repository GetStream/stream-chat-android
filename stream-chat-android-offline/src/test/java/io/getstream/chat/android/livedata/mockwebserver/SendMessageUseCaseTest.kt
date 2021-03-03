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

    override fun dispatchApiRequest(request: RecordedRequest): MockResponse {
        return if (request.isRequestBodyEqualTo(createSendMessageRequestJsonString())) {
            MockResponse()
                .setResponseCode(200)
                .setBody(createSendMessageResponseJsonString())
        } else {
            throw IllegalStateException("Request not supported")
        }
    }

    @Test
    fun sendMessageUseCase(): Unit = runBlocking {
        val messageId = "cebf562a-4806-4c64-a827-59d50aac42ba-80f7fe50-ec73-49f8-a389-3dd5b4304b6f"
        val message = Message(
            id = messageId,
            cid = "messaging:e87283f0-a58d-4685-bf0b-729a7b6eb84d",
            text = "message test"
        )

        val sendMessageResult = chatDomain.useCases
            .sendMessage(message)
            .execute()

        assertSuccess(sendMessageResult)

        val messagesFromDatabase = chatDomainImpl.repos.selectMessages(listOf(messageId))

        Truth.assertThat(messagesFromDatabase.size).isEqualTo(1)
        Truth.assertThat(messagesFromDatabase[0].syncStatus).isEqualTo(SyncStatus.COMPLETED)
        Truth.assertThat(messagesFromDatabase[0].text).isEqualTo("message test")
        Truth.assertThat(messagesFromDatabase[0].user.name).isEqualTo("Zetra")
    }
}
