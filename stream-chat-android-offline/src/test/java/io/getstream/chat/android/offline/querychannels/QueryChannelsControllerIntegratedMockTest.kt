package io.getstream.chat.android.offline.querychannels

import androidx.test.ext.junit.runners.AndroidJUnit4
import io.getstream.chat.android.offline.integration.BaseConnectedMockedTest
import org.junit.Test
import org.junit.runner.RunWith

@RunWith(AndroidJUnit4::class)
internal class QueryChannelsControllerIntegratedMockTest : BaseConnectedMockedTest() {

    @Test
    fun `Given some channels When received new message event Should return the same channels with proper orderings`() {
        // Todo: Move this test to correct place
        // coroutineTest {
        //     val queryChannelsController =
        //         chatDomainImpl.queryChannels(data.filter1, QuerySort.desc(Channel::lastMessageAt))
        //     val channel1 = data.channel1.copy(lastMessageAt = Date(10000L))
        //     val channel2 = data.channel2.copy(lastMessageAt = Date(20000L))
        //     whenever(client.queryChannelsInternal(any())) doReturn TestCall(Result(listOf(channel1, channel2)))
        //
        //     // 1. Query channels and check that live data emits a proper sorted list.
        //     queryChannelsController.query()
        //
        //     val firstEmittedValue = queryChannelsController.channels.value.shouldNotBeNull()
        //     firstEmittedValue.size shouldBeEqualTo 2
        //     firstEmittedValue[0].run {
        //         cid shouldBeEqualTo channel2.cid
        //         lastMessageAt shouldBeEqualTo channel2.lastMessageAt
        //     }
        //
        //     // 2. Update the channel1 by newMessageEvent and check that second live data's value sorted properly.
        //     chatDomainImpl.eventHandler.handleEvent(
        //         data.newMessageEvent.copy(
        //             cid = channel1.cid,
        //             message = data.newMessageEvent.message.copy(
        //                 createdAt = Date(30000L)
        //             )
        //         )
        //     )
        //
        //     val secondEmittedValue = queryChannelsController.channels.value.shouldNotBeNull()
        //
        //     secondEmittedValue.size shouldBeEqualTo 2
        //     secondEmittedValue[0].run {
        //         cid shouldBeEqualTo channel1.cid
        //         lastMessageAt shouldBeEqualTo Date(30000L)
        //     }
        // }
    }
}
