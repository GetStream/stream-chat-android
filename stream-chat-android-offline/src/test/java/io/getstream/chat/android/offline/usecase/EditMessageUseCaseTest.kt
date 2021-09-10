package io.getstream.chat.android.offline.usecase

import androidx.test.ext.junit.runners.AndroidJUnit4
import com.nhaarman.mockitokotlin2.any
import com.nhaarman.mockitokotlin2.doReturn
import com.nhaarman.mockitokotlin2.whenever
import io.getstream.chat.android.client.utils.Result
import io.getstream.chat.android.offline.integration.BaseDomainTest2
import io.getstream.chat.android.test.TestCall
import kotlinx.coroutines.runBlocking
import org.amshove.kluent.shouldBeEqualTo
import org.amshove.kluent.shouldContainAll
import org.amshove.kluent.shouldNotBe
import org.junit.Test
import org.junit.runner.RunWith

@RunWith(AndroidJUnit4::class)
internal class EditMessageUseCaseTest : BaseDomainTest2() {

    @Test
    fun `edit message use case full example`(): Unit = runBlocking {
        // TODO: this test is slow for unknown reasons
        val originalMessage = data.createMessage()

        whenever(channelClientMock.sendMessage(any())) doReturn TestCall(Result(originalMessage))
        val result = channelControllerImpl.sendMessage(originalMessage)
        assertSuccess(result)

        var messages = channelControllerImpl.messages.value
        val lastMessage = messages.last()
        lastMessage.id shouldBeEqualTo originalMessage.id

        // need to use result.data and not originalMessage as the created At date is different
        val updatedMessage = result.data().copy(extraData = mutableMapOf("plaid" to true))

        whenever(clientMock.updateMessage(any())) doReturn TestCall(Result(updatedMessage.copy()))
        val result2 = channelControllerImpl.editMessage(updatedMessage)

        assertSuccess(result2)
        messages = channelControllerImpl.messages.value
        val liveLastMessage = messages.last()
        liveLastMessage.id shouldBeEqualTo originalMessage.id
        liveLastMessage.extraData shouldContainAll updatedMessage.extraData
        liveLastMessage.extraData["plaid"] shouldBeEqualTo true
        // verify it's not the same object (since that breaks diffUtils)
        liveLastMessage shouldNotBe updatedMessage
    }
}
