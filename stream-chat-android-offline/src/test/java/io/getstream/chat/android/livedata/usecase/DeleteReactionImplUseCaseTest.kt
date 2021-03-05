package io.getstream.chat.android.livedata.usecase

import androidx.test.ext.junit.runners.AndroidJUnit4
import com.google.common.truth.Truth
import io.getstream.chat.android.client.models.Message
import io.getstream.chat.android.client.models.Reaction
import io.getstream.chat.android.client.utils.SyncStatus
import io.getstream.chat.android.livedata.mockwebserver.BaseMockWebServerIntegrationTest
import io.getstream.chat.android.livedata.mockwebserver.createSendMessageResponseJsonString
import kotlinx.coroutines.runBlocking
import okhttp3.mockwebserver.MockResponse
import okhttp3.mockwebserver.RecordedRequest
import org.junit.Test
import org.junit.runner.RunWith

@RunWith(AndroidJUnit4::class)
internal class DeleteReactionImplUseCaseTest : BaseMockWebServerIntegrationTest() {

    override fun dispatchApiRequest(request: RecordedRequest): MockResponse {
        return if (request.path?.contains("") == true) {
            MockResponse()
                .setResponseCode(200)
                .setBody(createSendMessageResponseJsonString())
        } else {
            throw IllegalStateException("Request not supported")
        }
    }

    @Test(expected = IllegalArgumentException::class)
    fun reactionWithoutInformationShouldFail() = runBlocking {
        chatDomain.useCases.deleteReaction(
            "messaging:!members-rOvo1wmwXakoJ-aRjvyoAPFvULMoEFizIR9UYpSoyEM",
            Reaction()
        ).execute()

        Truth.assertThat(chatDomain.isOnline()).isTrue()
    }

    @Test()
    fun reactionDelete() = runBlocking {
        val cid = "messaging:e87283f0-a58d-4685-bf0b-729a7b6eb84d"
        val messageId = "cebf562a-4806-4c64-a827-59d50aac42ba-80f7fe50-ec73-49f8-a389-3dd5b4304b6f"
        val message = Message(
            id = messageId,
            cid = cid,
            text = "message test"
        )
        val reaction = Reaction(
            messageId = messageId,
            type = "like",
            userId = "29e46def-88f4-4b6a-a10c-584d10c4fdc9"
        )

        val sendMessageResult = chatDomain.useCases
            .sendMessage(message)
            .execute()

        Truth.assertThat(sendMessageResult.isSuccess).isTrue()

        val result = chatDomain.useCases.deleteReaction(cid, reaction).execute()

        Truth.assertThat(result.isSuccess).isTrue()
        Truth.assertThat(reaction.syncStatus).isEqualTo(SyncStatus.COMPLETED)
    }
}
