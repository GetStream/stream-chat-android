package io.getstream.chat.android.offline.usecase

import androidx.test.ext.junit.runners.AndroidJUnit4
import com.google.common.truth.Truth
import com.nhaarman.mockitokotlin2.any
import com.nhaarman.mockitokotlin2.doReturn
import com.nhaarman.mockitokotlin2.whenever
import io.getstream.chat.android.client.api.models.WatchChannelRequest
import io.getstream.chat.android.client.models.Channel
import io.getstream.chat.android.client.models.Message
import io.getstream.chat.android.livedata.BaseDomainTest2
import io.getstream.chat.android.test.asCall
import io.getstream.chat.android.test.failedCall
import io.getstream.chat.android.test.randomString
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.test.runBlockingTest
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith

@RunWith(AndroidJUnit4::class)
@ExperimentalCoroutinesApi
internal class LoadOldMessagesTest : BaseDomainTest2() {

    @Before
    override fun setup() {
        super.setup()
    }

    @Test
    fun `when watching a channel, new messages are updated correctly for the watcher`() = runBlockingTest {
        val newMessage = data.createMessage()

        whenever(channelClientMock.sendMessage(any())) doReturn newMessage.asCall()

        val channelState = chatDomain.watchChannel(data.channel1.cid, 0).execute().data()
        val result = chatDomainImpl.loadOlderMessages(data.channel1.cid, 10).execute()

        val messages1: List<Message> = channelState.messages.value
        chatDomain.sendMessage(newMessage).execute()

        val messages2 = channelState.messages.value

        Truth.assertThat(messages2).isNotEqualTo(messages1)
        Truth.assertThat(messages2.last()).isEqualTo(newMessage)
    }

    @Test
    fun `when online request works, it is the one used instead of local cache`() = runBlockingTest {
        val desiredCid = randomString()

        whenever(channelClientMock.watch(any<WatchChannelRequest>())) doReturn Channel(cid = desiredCid).asCall()

        val result = chatDomainImpl.loadOlderMessages(data.channel1.cid, 10).execute()

        Truth.assertThat(result.isSuccess).isTrue()
        Truth.assertThat(result.data().cid).isEqualTo(desiredCid)
    }

    @Test
    fun `when online request does NOT work, local cache is used`() = runBlockingTest {
        val queryChannelCall = Channel(cid = data.channel1.cid).asCall()

        whenever(channelClientMock.watch(any<WatchChannelRequest>())) doReturn queryChannelCall

        // Load older messages using backend.
        chatDomainImpl.loadOlderMessages(data.channel1.cid, 10).execute()

        whenever(channelClientMock.watch(any<WatchChannelRequest>())) doReturn failedCall("the call failed")

        // Now backend fails, so the cache request must work for a successful result.
        val result = chatDomainImpl.loadOlderMessages(data.channel1.cid, 10).execute()

        Truth.assertThat(result.isSuccess).isTrue()
    }
}
