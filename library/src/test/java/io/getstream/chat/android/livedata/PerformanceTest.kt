package io.getstream.chat.android.livedata

import androidx.recyclerview.widget.DiffUtil
import androidx.test.ext.junit.runners.AndroidJUnit4
import com.google.common.truth.Truth
import io.getstream.chat.android.client.models.Channel
import io.getstream.chat.android.client.models.Message
import io.getstream.chat.android.livedata.utils.ChannelDiffCallback
import io.getstream.chat.android.livedata.utils.MessageDiffCallback

import io.getstream.chat.android.livedata.utils.LiveDiffCounter
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.runBlocking
import org.junit.Test
import org.junit.runner.RunWith
import java.lang.Thread.sleep




@RunWith(AndroidJUnit4::class)
class PerformanceTest: BaseConnectedIntegrationTest() {
    @Test
    fun channels() = runBlocking(Dispatchers.IO) {
        var queryChannelResult = chatDomain.useCases.queryChannels(data.filter1, null).execute()
        assertSuccess(queryChannelResult)
        val queryChannelsController = queryChannelResult.data()
        var old: List<Channel>? = null
        var new: List<Channel>? = null

        var counter = LiveDiffCounter(MessageDiffCallback)

        queryChannelsController.channels.observeForever {
            val channelIds = it.map { it.cid }
            System.out.println("Channel ids is now equal to $channelIds")
            new = it
            counter.onEvent()
            if (old != null && new != null) {
                val result = DiffUtil.calculateDiff(ChannelDiffCallback(old!!, new!!))
                result.dispatchUpdatesTo(counter)
            }
            old = new
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

        var counter = LiveDiffCounter<Message>(MessageDiffCallback)

        channelController.messages.observeForever {
            val channelIds = it.map { it.cid }
            System.out.println("Channel ids is now equal to $channelIds")
            counter.onEvent(it)
        }

        // should trigger 2 updates, once for offline and another for online
        channelController.watch()

        // adding a message should lead to an inserted count increase

        // updating a message should lead to a changed event

        Truth.assertThat(counter.counts).isEqualTo(mutableMapOf("events" to 2, "changed" to 1, "moved" to 0, "inserted" to 0, "removed" to 0))
        sleep(10000)
    }
}