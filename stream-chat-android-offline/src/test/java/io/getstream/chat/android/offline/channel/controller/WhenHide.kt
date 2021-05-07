package io.getstream.chat.android.offline.channel.controller

import com.google.common.truth.Truth
import com.nhaarman.mockitokotlin2.any
import com.nhaarman.mockitokotlin2.verify
import com.nhaarman.mockitokotlin2.whenever
import io.getstream.chat.android.client.errors.ChatError
import io.getstream.chat.android.client.utils.Result
import io.getstream.chat.android.offline.randomMessage
import io.getstream.chat.android.test.TestCall
import io.getstream.chat.android.test.randomDateBefore
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.test.runBlockingTest
import org.junit.jupiter.api.Test
import java.util.Date

@ExperimentalCoroutinesApi
internal class WhenHide : BaseChannelControllerTests() {

    @Test
    fun `Should mark channel as hidden`() = runBlockingTest {
        val response = Result(Unit)
        whenever(channelClient.hide(any())).thenReturn(TestCall(response))

        val result = sut.hide(clearHistory = false)

        Truth.assertThat(response).isEqualTo(result)
        Truth.assertThat(sut.hidden.value).isTrue()
    }

    @Test
    fun `Given failed response Should return server error`() =
        runBlockingTest {
            val response = Result<Unit>(error = ChatError())
            whenever(channelClient.hide(any())).thenReturn(TestCall(response))

            val result = sut.hide(clearHistory = false)

            Truth.assertThat(response).isEqualTo(result)
        }

    @Test
    fun `Given successful response And channel without clearing history Should update channel in database`() =
        runBlockingTest {
            val response = Result(Unit)
            whenever(channelClient.hide(any())).thenReturn(TestCall(response))

            val result = sut.hide(clearHistory = false)

            Truth.assertThat(response).isEqualTo(result)
            verify(channelClient).hide(clearHistory = false)
            verify(repos).setHiddenForChannel(cid = cid, hidden = true)
        }

    @Test
    fun `Given successful response And channel with clearing history Should hide messages sent before`() =
        runBlockingTest {
            val response = Result(Unit)
            whenever(channelClient.hide(any())).thenReturn(TestCall(response))

            val result = sut.hide(clearHistory = true)

            Truth.assertThat(response).isEqualTo(result)
            Truth.assertThat(sut.hideMessagesBefore).isNotNull()
        }

    @Test
    fun `Given successful response And channel with clearing history Should update channel in database`() =
        runBlockingTest {
            val response = Result(Unit)
            whenever(channelClient.hide(any())).thenReturn(TestCall(response))

            val result = sut.hide(clearHistory = true)

            Truth.assertThat(response).isEqualTo(result)
            verify(channelClient).hide(clearHistory = true)
            Truth.assertThat(sut.hideMessagesBefore).isNotNull()
            verify(repos).setHiddenForChannel(
                cid = cid,
                hidden = true,
                hideMessagesBefore = sut.hideMessagesBefore!!,
            )
        }

    @Test
    fun `Given successful response And channel with clearing history Should delete hidden messages from database`() =
        runBlockingTest {
            val response = Result(Unit)
            whenever(channelClient.hide(any())).thenReturn(TestCall(response))

            val result = sut.hide(clearHistory = true)

            Truth.assertThat(response).isEqualTo(result)
            verify(channelClient).hide(clearHistory = true)
            Truth.assertThat(sut.hideMessagesBefore).isNotNull()
            verify(repos).deleteChannelMessagesBefore(
                cid = cid,
                hideMessagesBefore = sut.hideMessagesBefore!!,
            )
        }

    @Test
    fun `Given successful response And channel with clearing history Should clear messages sent before channel was hidden`() =
        runBlockingTest {
            val now = Date().time
            val response = Result(Unit)
            val messageSentAfterChannelIsCleared = randomMessage(createdAt = Date(now + 1000))
            whenever(channelClient.hide(any())).thenReturn(TestCall(response))
            sut.apply {
                upsertMessage(randomMessage(createdAt = randomDateBefore(Date().time)))
                upsertMessage(randomMessage(createdAt = randomDateBefore(Date().time)))
                upsertMessage(randomMessage(createdAt = randomDateBefore(Date().time)))
                upsertMessage(randomMessage(createdAt = randomDateBefore(Date().time)))
                upsertMessage(messageSentAfterChannelIsCleared)
            }

            val result = sut.hide(clearHistory = true)

            Truth.assertThat(response).isEqualTo(result)
            val messages = sut.unfilteredMessages.first()
            Truth.assertThat(messages.size).isEqualTo(1)
            Truth.assertThat(messages.any { it == messageSentAfterChannelIsCleared }).isTrue()
        }
}
