package io.getstream.chat.android.offline.querychannels

import androidx.test.ext.junit.runners.AndroidJUnit4
import com.nhaarman.mockitokotlin2.any
import com.nhaarman.mockitokotlin2.doReturn
import com.nhaarman.mockitokotlin2.whenever
import io.getstream.chat.android.client.api.models.QuerySort
import io.getstream.chat.android.client.models.Channel
import io.getstream.chat.android.client.utils.Result
import io.getstream.chat.android.livedata.BaseConnectedMockedTest
import io.getstream.chat.android.test.TestCall
import kotlinx.coroutines.runBlocking
import org.amshove.kluent.shouldBeEqualTo
import org.amshove.kluent.shouldNotBeNull
import org.junit.Test
import org.junit.runner.RunWith
import java.util.Date

@RunWith(AndroidJUnit4::class)
internal class QueryChannelsControllerIntegratedMockTest : BaseConnectedMockedTest() {

    @Test
    fun `Given some channels When received new message event Should return the same channels with proper orderings`() {
        runBlocking {
            val queryChannelsController =
                chatDomainImpl.queryChannels(data.filter1, QuerySort.desc(Channel::lastMessageAt))
            val channel1 = data.channel1.copy(lastMessageAt = Date(10000L))
            val channel2 = data.channel2.copy(lastMessageAt = Date(20000L))
            whenever(client.queryChannels(any())) doReturn TestCall(Result(listOf(channel1, channel2)))

            // 1. Query channels and check that live data emits a proper sorted list.
            queryChannelsController.query()

            val firstEmittedValue = queryChannelsController.channels.value.shouldNotBeNull()
            firstEmittedValue.size shouldBeEqualTo 2
            firstEmittedValue[0].run {
                cid shouldBeEqualTo channel2.cid
                lastMessageAt shouldBeEqualTo channel2.lastMessageAt
            }

            // 2. Update the channel1 by newMessageEvent and check that second live data's value sorted properly.
            chatDomainImpl.eventHandler.handleEvent(
                data.newMessageEvent.copy(
                    cid = channel1.cid,
                    message = data.newMessageEvent.message.copy(
                        createdAt = Date(30000L)
                    )
                )
            )

            val secondEmittedValue = queryChannelsController.channels.value.shouldNotBeNull()

            secondEmittedValue.size shouldBeEqualTo 2
            secondEmittedValue[0].run {
                cid shouldBeEqualTo channel1.cid
                lastMessageAt shouldBeEqualTo Date(30000L)
            }
        }
    }
}
