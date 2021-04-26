package io.getstream.chat.android.livedata.usecase

import androidx.test.ext.junit.runners.AndroidJUnit4
import com.google.common.truth.Truth
import com.nhaarman.mockitokotlin2.any
import com.nhaarman.mockitokotlin2.doReturn
import com.nhaarman.mockitokotlin2.whenever
import io.getstream.chat.android.client.utils.Result
import io.getstream.chat.android.livedata.BaseDomainTest2
import io.getstream.chat.android.test.TestCall
import kotlinx.coroutines.runBlocking
import org.junit.Test
import org.junit.runner.RunWith

@RunWith(AndroidJUnit4::class)
internal class EditMessageImplUseCaseTest : BaseDomainTest2() {

    @Test
    fun `edit message use case full example`() = runBlocking {
        // TODO: this test is slow for unknown reasons
        val originalMessage = data.createMessage()

        whenever(channelClientMock.sendMessage(any())) doReturn TestCall(Result(originalMessage))
        val result = channelControllerImpl.sendMessage(originalMessage)
        assertSuccess(result)

        var messages = channelControllerImpl.messages.value
        val lastMessage = messages.last()
        Truth.assertThat(lastMessage.id).isEqualTo(originalMessage.id)

        // need to use result.data and not originalMessage as the created At date is different
        val updatedMessage = result.data().copy(extraData = mutableMapOf("plaid" to true))

        whenever(clientMock.updateMessage(any())) doReturn TestCall(Result(updatedMessage))
        val result2 = channelControllerImpl.editMessage(updatedMessage)

        assertSuccess(result2)
        messages = channelControllerImpl.messages.value
        val liveLastMessage = messages.last()
        Truth.assertThat(liveLastMessage.id).isEqualTo(originalMessage.id)
        Truth.assertThat(liveLastMessage.extraData).containsAtLeastEntriesIn(updatedMessage.extraData)
        Truth.assertThat(liveLastMessage.extraData["plaid"]).isEqualTo(true)
        // verify it's not the same object (since that breaks diffUtils)
        Truth.assertThat(liveLastMessage === updatedMessage).isFalse()
    }
}
