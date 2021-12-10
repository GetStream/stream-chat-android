package io.getstream.chat.android.offline.usecase

import androidx.test.ext.junit.runners.AndroidJUnit4
import com.nhaarman.mockitokotlin2.any
import com.nhaarman.mockitokotlin2.doReturn
import com.nhaarman.mockitokotlin2.eq
import com.nhaarman.mockitokotlin2.whenever
import io.getstream.chat.android.client.api.models.WatchChannelRequest
import io.getstream.chat.android.client.models.Channel
import io.getstream.chat.android.client.models.Message
import io.getstream.chat.android.offline.extensions.loadOlderMessages
import io.getstream.chat.android.offline.integration.BaseDomainTest2
import io.getstream.chat.android.test.asCall
import io.getstream.chat.android.test.failedCall
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.test.runBlockingTest
import org.amshove.kluent.shouldBeEqualTo
import org.amshove.kluent.shouldBeTrue
import org.amshove.kluent.shouldNotBeEqualTo
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
    fun `when watching a channel, new messages are updated correctly for the watcher`() = coroutineTest {
        val newMessage = data.createMessage()

        whenever(channelClientMock.sendMessage(any())) doReturn newMessage.asCall()

        val channelController = chatDomain.watchChannel(data.channel1.cid, 0).execute().data()
        clientMock.loadOlderMessages(data.channel1.cid, 10).execute()

        val messages1: Collection<Message> = channelController.messages.value
        chatDomain.sendMessage(newMessage).execute()

        val messages2 = channelController.messages.value

        messages2 shouldNotBeEqualTo messages1
        messages2.last() shouldBeEqualTo newMessage
    }

    @Test
    fun `when online request works, it is the one used instead of local cache`() = runBlockingTest {
        val type = "channelType"
        val id = "channelId"
        val desiredCid = "$type:$id"

        whenever(clientMock.queryChannelInternal(eq(type), eq(id), any())) doReturn Channel(cid = desiredCid).asCall()

        val result = clientMock.loadOlderMessages(desiredCid, 10).execute()

        result.isSuccess.shouldBeTrue()
        result.data().cid shouldBeEqualTo desiredCid
    }

    @Test
    fun `when online request does NOT work, local cache is used`() = runBlockingTest {
        val queryChannelCall = Channel(cid = data.channel1.cid).asCall()

        whenever(channelClientMock.watch(any<WatchChannelRequest>())) doReturn queryChannelCall

        // Load older messages using backend.
        clientMock.loadOlderMessages(data.channel1.cid, 10).execute()

        whenever(channelClientMock.watch(any<WatchChannelRequest>())) doReturn failedCall("the call failed")

        // Now backend fails, so the cache request must work for a successful result.
        val result = chatDomainImpl.loadOlderMessages(data.channel1.cid, 10).execute()

        result.isSuccess.shouldBeTrue()
    }
}
