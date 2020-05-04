package io.getstream.chat.android.livedata

import androidx.recyclerview.widget.DiffUtil
import androidx.test.ext.junit.runners.AndroidJUnit4
import com.google.common.truth.Truth
import io.getstream.chat.android.client.models.Channel
import io.getstream.chat.android.client.models.Message
import io.getstream.chat.android.livedata.utils.ChannelDiffCallback
import io.getstream.chat.android.livedata.utils.LiveDiffCounter
import io.getstream.chat.android.livedata.utils.MessageDiffCallback
import java.lang.Thread.sleep
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.runBlocking
import org.junit.Test
import org.junit.runner.RunWith

@RunWith(AndroidJUnit4::class)
class PerformanceTest : BaseConnectedIntegrationTest() {
    @Test
    fun channels() = runBlocking(Dispatchers.IO) {
        var queryChannelResult = chatDomain.useCases.queryChannels(data.filter1, null).execute()
        assertSuccess(queryChannelResult)
        val queryChannelsController = queryChannelResult.data()

        var counter = LiveDiffCounter { old: List<Channel>, new: List<Channel> ->
            DiffUtil.calculateDiff(ChannelDiffCallback(old, new))
        }

        queryChannelsController.channels.observeForever {
            val channelIds = it.map { it.cid }
            System.out.println("Channel ids is now equal to $channelIds")
            counter.onEvent(it)
        }

        chatDomainImpl.eventHandler.handleEvent(data.newMessageEvent)
        chatDomainImpl.eventHandler.handleEvent(data.newMessageEvent2)
        chatDomainImpl.eventHandler.handleEvent(data.userStartWatchingEvent)

        Truth.assertThat(counter.counts).isEqualTo(mutableMapOf("events" to 2, "changed" to 1, "moved" to 0, "inserted" to 0, "removed" to 0))
        sleep(10000)
    }
    @Test
    fun messages() = runBlocking(Dispatchers.IO) {
        var channelController = chatDomainImpl.channel(data.channel1)


        var counter = LiveDiffCounter { old: List<Message>, new: List<Message> ->
            DiffUtil.calculateDiff(MessageDiffCallback(old, new))
        }

        channelController.messages.observeForever { messages ->
            val messageIds = messages.map { it.cid }
            System.out.println("Message ids is now equal to $messageIds")
            counter.onEvent(messages)
        }

        // should trigger 2 updates, once for offline and another for online
        channelController.watch()

        // adding a message should lead to an inserted count increase

        // updating a message should lead to a changed event

        Truth.assertThat(counter.counts).isEqualTo(mutableMapOf("events" to 2, "changed" to 1, "moved" to 0, "inserted" to 0, "removed" to 0))
        sleep(10000)
    }
}
