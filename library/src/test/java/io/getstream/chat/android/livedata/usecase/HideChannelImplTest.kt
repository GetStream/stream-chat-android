package io.getstream.chat.android.livedata.usecase

import androidx.test.ext.junit.runners.AndroidJUnit4
import com.google.common.truth.Truth
import io.getstream.chat.android.client.utils.Result
import io.getstream.chat.android.livedata.BaseConnectedIntegrationTest
import io.getstream.chat.android.livedata.utils.getOrAwaitValue
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.runBlocking
import org.junit.Test
import org.junit.runner.RunWith

@RunWith(AndroidJUnit4::class)
class HideChannelImplTest : BaseConnectedIntegrationTest() {

    @Test
    fun hide() = runBlocking(Dispatchers.IO) {
        var channelController = chatDomain.useCases.watchChannel(data.channel1.cid, 10).execute().data()
        var channelControllerImpl = chatDomainImpl.channel(data.channel1.cid)
        val result = chatDomain.useCases.hideChannel(data.channel1.cid, true).execute()
        // verify it's now hidden
        Truth.assertThat(channelController.hidden.getOrAwaitValue()).isTrue()
        // verify that it's no longer showing up in query channels
        // TODO

        // verify that receiving a new message unhides it
        channelControllerImpl.handleEvent(data.newMessageEventNotification)
        Truth.assertThat(channelController.hidden.getOrAwaitValue()).isFalse()
    }

    @Test
    fun show() = runBlocking(Dispatchers.IO) {
        var channelController = chatDomain.useCases.watchChannel(data.channel1.cid, 10).execute().data()
        var channelControllerImpl = chatDomainImpl.channel(data.channel1.cid)
        chatDomain.useCases.hideChannel(data.channel1.cid, true).execute()
        chatDomain.useCases.showChannel(data.channel1.cid).execute()

        Truth.assertThat(channelController.hidden.getOrAwaitValue()).isFalse()
    }

    @Test
    fun keepHistory() = runBlocking(Dispatchers.IO) {
        var channelController = chatDomain.useCases.watchChannel(data.channel1.cid, 10).execute().data()
        var channelControllerImpl = chatDomainImpl.channel(data.channel1.cid)
        // add a message that should no longer be visible afterwards
        chatDomainImpl.repos.messages.insertMessage(data.message2Older)
        channelControllerImpl.handleEvent(data.newMessageEvent2)
        // keep history = false, so messages should go bye bye
        val result = chatDomain.useCases.hideChannel(data.channel1.cid, false).execute()
        assertSuccess(result as Result<Any>)
        // verify it's now hidden
        Truth.assertThat(channelController.hidden.getOrAwaitValue()).isTrue()
        // verify that old messages are gone...
        val oldMessage = channelControllerImpl.getMessage(data.message2Older.id)
        Truth.assertThat(oldMessage).isNull()
    }
}
