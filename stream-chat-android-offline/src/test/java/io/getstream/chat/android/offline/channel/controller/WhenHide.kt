package io.getstream.chat.android.offline.channel.controller

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
import org.amshove.kluent.shouldBeEqualTo
import org.amshove.kluent.shouldBeTrue
import org.amshove.kluent.shouldNotBeNull
import org.junit.jupiter.api.Test
import java.util.Date

@ExperimentalCoroutinesApi
internal class WhenHide : BaseChannelControllerTests() {

    @Test
    fun `Should mark channel as hidden`() = runBlockingTest {
        val response = Result(Unit)
        whenever(channelClient.hide(any())).thenReturn(TestCall(response))

        val result = sut.hide(clearHistory = false)

        response shouldBeEqualTo result
        sut.hidden.value.shouldBeTrue()
    }

    @Test
    fun `Given failed response Should return server error`() =
        runBlockingTest {
            val response = Result<Unit>(error = ChatError())
            whenever(channelClient.hide(any())).thenReturn(TestCall(response))

            val result = sut.hide(clearHistory = false)

            response shouldBeEqualTo result
        }

    @Test
    fun `Given successful response And channel without clearing history Should update channel in database`() =
        runBlockingTest {
            val response = Result(Unit)
            whenever(channelClient.hide(any())).thenReturn(TestCall(response))

            val result = sut.hide(clearHistory = false)

            response shouldBeEqualTo result
            verify(channelClient).hide(clearHistory = false)
            verify(repos).setHiddenForChannel(cid = cid, hidden = true)
        }

    @Test
    fun `Given successful response And channel with clearing history Should hide messages sent before`() =
        runBlockingTest {
            val response = Result(Unit)
            whenever(channelClient.hide(any())).thenReturn(TestCall(response))

            val result = sut.hide(clearHistory = true)

            response shouldBeEqualTo result
            sut.hideMessagesBefore.shouldNotBeNull()
        }

    @Test
    fun `Given successful response And channel with clearing history Should update channel in database`() =
        runBlockingTest {
            val response = Result(Unit)
            whenever(channelClient.hide(any())).thenReturn(TestCall(response))

            val result = sut.hide(clearHistory = true)

            response shouldBeEqualTo result
            verify(channelClient).hide(clearHistory = true)
            sut.hideMessagesBefore.shouldNotBeNull()
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

            response shouldBeEqualTo result
            verify(channelClient).hide(clearHistory = true)
            sut.hideMessagesBefore.shouldNotBeNull()
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

            response shouldBeEqualTo result
            val messages = sut.unfilteredMessages.first()
            messages.size shouldBeEqualTo 1
            messages.any { it == messageSentAfterChannelIsCleared }.shouldBeTrue()
        }
}
