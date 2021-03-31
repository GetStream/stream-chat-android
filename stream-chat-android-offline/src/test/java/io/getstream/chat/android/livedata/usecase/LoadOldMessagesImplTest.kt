package io.getstream.chat.android.livedata.usecase

import androidx.test.ext.junit.runners.AndroidJUnit4
import com.google.common.truth.Truth
import com.nhaarman.mockitokotlin2.any
import com.nhaarman.mockitokotlin2.doReturn
import com.nhaarman.mockitokotlin2.whenever
import io.getstream.chat.android.client.api.models.WatchChannelRequest
import io.getstream.chat.android.client.call.Call
import io.getstream.chat.android.client.errors.ChatError
import io.getstream.chat.android.client.models.Channel
import io.getstream.chat.android.client.models.Message
import io.getstream.chat.android.client.utils.Result
import io.getstream.chat.android.livedata.BaseDomainTest2
import io.getstream.chat.android.test.getOrAwaitValue
import io.getstream.chat.android.test.randomString
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.test.runBlockingTest
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith

@RunWith(AndroidJUnit4::class)
@ExperimentalCoroutinesApi
internal class LoadOldMessagesImplTest : BaseDomainTest2() {

    @Before
    override fun setup() {
        super.setup()
    }

    @Test
    fun `when watching a channel, new messages are updated correctly for the watcher`() = runBlockingTest {
        val newMessage = data.createMessage()
        val sendMessageCall = createCall(newMessage)

        whenever(channelClientMock.sendMessage(any())) doReturn sendMessageCall

        val channelState = chatDomain.useCases.watchChannel(data.channel1.cid, 0).execute().data()
        val result = chatDomainImpl.useCases.loadOlderMessages(data.channel1.cid, 10).execute()

        val messages1: List<Message> = channelState.messages.getOrAwaitValue()
        chatDomain.useCases.sendMessage(newMessage).execute()

        val messages2 = channelState.messages.getOrAwaitValue()

        Truth.assertThat(messages2).isNotEqualTo(messages1)
        Truth.assertThat(messages2.last()).isEqualTo(newMessage)
    }

    @Test
    fun `when online request works, it is the one used instead of local cache`() = runBlockingTest {
        val desiredCid = randomString()
        val queryChannelCall = createCall(Channel(cid = desiredCid))

        whenever(channelClientMock.watch(any<WatchChannelRequest>())) doReturn queryChannelCall

        val result = chatDomainImpl.useCases.loadOlderMessages(data.channel1.cid, 10).execute()

        Truth.assertThat(result.isSuccess).isTrue()
        Truth.assertThat(result.data().cid).isEqualTo(desiredCid)
    }

    @Test
    fun `when online request does NOT work, local cache is used`() = runBlockingTest {
        val queryChannelCall = createCall(Channel(cid = data.channel1.cid))

        whenever(channelClientMock.watch(any<WatchChannelRequest>())) doReturn queryChannelCall

        //Load older messages using backend.
        chatDomainImpl.useCases.loadOlderMessages(data.channel1.cid, 10).execute()

        whenever(channelClientMock.watch(any<WatchChannelRequest>())) doReturn createFailedCall()

        // Now backend fails, so the cache request must work for a successful result.
        val result = chatDomainImpl.useCases.loadOlderMessages(data.channel1.cid, 10).execute()

        Truth.assertThat(result.isSuccess).isTrue()
    }

    private fun <T: Any> createCall(data: T) : Call<T> {
        return object : Call<T> {
            override fun execute(): Result<T> = Result(data)

            override fun enqueue(callback: Call.Callback<T>) {
                TODO("Not yet implemented")
            }

            override fun cancel() {
                TODO("Not yet implemented")
            }
        }
    }

    private inline fun <reified T: Any> createFailedCall() : Call<T> {
        return object : Call<T> {
            override fun execute(): Result<T> = Result(ChatError("this call failed"))

            override fun enqueue(callback: Call.Callback<T>) {
                TODO("Not yet implemented")
            }

            override fun cancel() {
                TODO("Not yet implemented")
            }
        }
    }
}
