package io.getstream.chat.android.offline.querychannels

import androidx.test.ext.junit.runners.AndroidJUnit4
import io.getstream.chat.android.offline.integration.BaseConnectedMockedTest
import org.junit.runner.RunWith

// @RunWith(AndroidJUnit4::class)
//Todo: move to correct class
// internal class QueryChannelsControllerIntegrationTest : BaseConnectedMockedTest() {
    //
    // @Test
    // fun `Given initialized SDK When request channels Should not crash`(): Unit = runBlocking {
    //     val queryChannelsController =
    //         Fixture(chatDomainImpl, data.filter1).givenChannelsInOfflineStorage(data.channel1).get()
    //
    //     val queryChannelsResult = queryChannelsController.query()
    //
    //     queryChannelsResult.isSuccess shouldBe true
    // }
    //
    // @Test
    // fun `Given the same channels in cache and BE When observing channels Should receive the correct number of events with channels`(): Unit =
    //     coroutineTest {
    //         val counter = DiffUtilOperationCounter { old: List<Channel>, new: List<Channel> ->
    //             DiffUtil.calculateDiff(ChannelDiffCallback(old, new), true)
    //         }
    //         val sut = Fixture(chatDomainImpl, data.filter1)
    //             .givenChannelEventsHandler { event, _, _ ->
    //                 when (event) {
    //                     is NotificationAddedToChannelEvent -> EventHandlingResult.Add(event.channel)
    //                     else -> EventHandlingResult.Skip
    //                 }
    //             }
    //             .givenChannelsInOfflineStorage(data.channel1)
    //             .givenMessageInOfflineStorage(data.message1)
    //             .withCounter(counter)
    //             .get()
    //
    //         // querying channels, should trigger 2 events (once for offline and another for online),
    //         // but there should be only 1 insert since the channels in our case are the same
    //         sut.query()
    //         counter.counts shouldBeEqualTo UpdateOperationCounts(events = 2, changed = 0, inserted = 1)
    //
    //         // adding a new channel, should trigger 1 "insert" operation, 1 "update" caused by refresh flow values
    //         chatDomainImpl.eventHandler.handleEvent(data.notificationAddedToChannel2Event)
    //         counter.counts shouldBeEqualTo UpdateOperationCounts(events = 4, changed = 1, inserted = 2)
    //
    //         // adding a new message, should trigger 1 "changed" operation
    //         chatDomainImpl.eventHandler.handleEvent(data.newMessageFromUser2)
    //         counter.counts shouldBeEqualTo UpdateOperationCounts(events = 5, changed = 2, inserted = 2)
    //
    //         // updating the last message, should should trigger 1 "changed" operation
    //         chatDomainImpl.eventHandler.handleEvent(data.messageUpdatedEvent)
    //         counter.counts shouldBeEqualTo UpdateOperationCounts(events = 6, changed = 3, inserted = 2)
    //     }
    //
    // @Test
    // fun `Given channel events handler When handle channel events Should invoke channel events handler`(): Unit =
    //     runBlocking {
    //         val chatEventHandler = object : ChatEventHandler {
    //             var didHandle = false
    //             override fun handleChatEvent(event: ChatEvent, filter: FilterObject, cachedChannel: Channel?): EventHandlingResult {
    //                 didHandle = true
    //                 return EventHandlingResult.Skip
    //             }
    //         }
    //         val sut = Fixture(chatDomainImpl, data.filter1).givenChannelsInOfflineStorage(data.channel1).get()
    //         sut.chatEventHandler = chatEventHandler
    //
    //         sut.handleEvent(
    //             ChannelUpdatedByUserEvent(
    //                 type = randomString(),
    //                 createdAt = randomDate(),
    //                 cid = randomCID(),
    //                 channelType = randomString(),
    //                 channelId = randomString(),
    //                 user = mock(),
    //                 message = mock(),
    //                 channel = mock()
    //             )
    //         )
    //
    //         chatEventHandler.didHandle shouldBe true
    //     }
    //
    // @Test
    // fun `Given three channels in query When handle ChannelHiddenEvent Should remove channel`(): Unit = coroutineTest {
    //     val sut = Fixture(chatDomainImpl, data.filter1)
    //         .givenChannelsInOfflineStorage(data.channel1, data.channel2, data.channel3)
    //         .get()
    //     sut.query()
    //     sut.channels.value shouldBeEqualTo listOf(data.channel1, data.channel2, data.channel3)
    //
    //     sut.handleEvent(
    //         mock<ChannelHiddenEvent> {
    //             on { it.cid } doReturn data.channel3.cid
    //         }
    //     )
    //
    //     sut.channels.value shouldBeEqualTo listOf(data.channel1, data.channel2)
    // }
    //
    // @Test
    // fun `Given hidden channel and channel events handler that add channel on new message When handle NotificationMessageNewEvent Should add channel to list`(): Unit =
    //     coroutineTest {
    //         val sut = Fixture(chatDomainImpl, data.filter1)
    //             .givenChannelsInOfflineStorage(data.channel1, data.channel2, data.channel3)
    //             .givenChannelEventsHandler { event, _, _ ->
    //                 if (event is NotificationMessageNewEvent) {
    //                     EventHandlingResult.Add(event.channel)
    //                 } else {
    //                     EventHandlingResult.Skip
    //                 }
    //             }
    //             .get()
    //         sut.query()
    //         sut.handleEvent(
    //             mock<ChannelHiddenEvent> {
    //                 on { it.cid } doReturn data.channel3.cid
    //             }
    //         )
    //
    //         sut.handleEvent(
    //             mock<NotificationMessageNewEvent> {
    //                 on { it.channel } doReturn data.channel3
    //                 on { it.cid } doReturn data.channel3.cid
    //             }
    //         )
    //
    //         sut.channels.value shouldBeEqualTo listOf(data.channel1, data.channel2, data.channel3)
    //     }
    //
    // private class Fixture(
    //     private val chatDomainImpl: ChatDomainImpl,
    //     private val filter: FilterObject,
    // ) {
    //     private val queryChannelsControllerImpl = chatDomainImpl.queryChannels(filter, QuerySort())
    //
    //     fun givenChannelsInOfflineStorage(vararg channels: Channel): Fixture {
    //         runBlocking {
    //             val channelList = channels.toList()
    //             val query = QueryChannelsSpec(filter, QuerySort()).apply { cids = channelList.map(Channel::cid).toSet() }
    //             chatDomainImpl.repos.insertChannels(channelList)
    //             chatDomainImpl.repos.insertQueryChannels(query)
    //             whenever(chatDomainImpl.client.queryChannelsInternal(any())) doReturn channelList.asCall()
    //         }
    //         return this
    //     }
    //
    //     fun givenMessageInOfflineStorage(message: Message): Fixture {
    //         runBlocking {
    //             chatDomainImpl.repos.insertMessage(message)
    //         }
    //         return this
    //     }
    //
    //     suspend fun withCounter(counter: DiffUtilOperationCounter<Channel>): Fixture {
    //         chatDomainImpl.scope.launch {
    //             queryChannelsControllerImpl.channels.collect { channels ->
    //                 val ids = channels.map { it.cid }
    //                 println("Channel ids is now equal to $ids")
    //                 counter.onEvent(channels)
    //             }
    //         }
    //         return this
    //     }
    //
    //     fun givenChannelEventsHandler(eventHandler: ChatEventHandler) = apply {
    //         queryChannelsControllerImpl.chatEventHandler = eventHandler
    //     }
    //
    //     fun get(): QueryChannelsController = queryChannelsControllerImpl
    // }
// }
