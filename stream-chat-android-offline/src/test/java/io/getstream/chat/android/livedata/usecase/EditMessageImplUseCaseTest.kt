package io.getstream.chat.android.livedata.usecase

import androidx.test.ext.junit.runners.AndroidJUnit4
import com.google.common.truth.Truth
import io.getstream.chat.android.livedata.BaseConnectedIntegrationTest
import io.getstream.chat.android.livedata.utils.getOrAwaitValue
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.runBlocking
import org.junit.Test
import org.junit.runner.RunWith

@RunWith(AndroidJUnit4::class)
internal class EditMessageImplUseCaseTest : BaseConnectedIntegrationTest() {

    @Test
    fun editMessageUseCase() = runBlocking(Dispatchers.IO) {
        val originalMessage = data.createMessage()
        val result = channelControllerImpl.sendMessage(originalMessage)
        assertSuccess(result)

        var messages = channelControllerImpl.messages.getOrAwaitValue()
        val lastMessage = messages.last()
        Truth.assertThat(lastMessage.id).isEqualTo(originalMessage.id)

        // need to use result.data and not originalMessage as the created At date is different
        val updatedMessage = result.data().copy(extraData = mutableMapOf("plaid" to true))

        val result2 = channelControllerImpl.editMessage(updatedMessage)
        assertSuccess(result2)
        messages = channelControllerImpl.messages.getOrAwaitValue()
        val liveLastMessage = messages.last()
        Truth.assertThat(liveLastMessage.id).isEqualTo(originalMessage.id)
        Truth.assertThat(liveLastMessage.extraData).containsAtLeastEntriesIn(updatedMessage.extraData)
        Truth.assertThat(liveLastMessage.extraData["plaid"]).isEqualTo(true)
        // verify it's not the same object (since that breaks diffUtils)
        Truth.assertThat(liveLastMessage === updatedMessage).isFalse()
    }
}
