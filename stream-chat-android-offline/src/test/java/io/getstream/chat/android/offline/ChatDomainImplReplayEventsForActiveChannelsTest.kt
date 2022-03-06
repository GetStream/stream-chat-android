package io.getstream.chat.android.offline

//Todo: Move these tests to proper class
// @ExperimentalCoroutinesApi
// internal class ChatDomainImplReplayEventsForActiveChannelsTest {
//
//     companion object {
//         @JvmField
//         @RegisterExtension
//         val testCoroutines = TestCoroutineExtension()
//     }
//
//     @Test
//     fun `when replaying events for active channels should add channel to active channels`() =
//         testCoroutines.scope.runBlockingTest {
//             val cid = "ChannelType:ChannelId"
//             val sut = Fixture(testCoroutines.scope).givenSyncHistoryResult(Result(emptyList())).get()
//
//             sut.replayEvents(cid)
//
//             sut.isActiveChannel(cid).shouldBeTrue()
//         }
//
//     @Test
//     fun `when replaying events for active channels should get sync history for active channels`() =
//         testCoroutines.scope.runBlockingTest {
//             val cid = "ChannelType:ChannelId"
//             val chatClient: ChatClient = mock {
//                 on(it.channel(any())) doReturn mock()
//             }
//             val sut = Fixture(testCoroutines.scope)
//                 .givenChatClient(chatClient)
//                 .givenSyncHistoryResult(Result(emptyList()))
//                 .get()
//
//             sut.replayEvents(cid)
//
//             verify(chatClient).getSyncHistory(argThat { contains(cid) }, any())
//         }
//
//     @Test
//     fun `when replaying events for active channels should update offline storage from events`() =
//         testCoroutines.scope.runBlockingTest {
//             val cid = "ChannelType:ChannelId"
//             val events = listOf<ChatEvent>(
//                 randomChannelUpdatedEvent(),
//                 randomNewMessageEvent(),
//                 randomNewMessageEvent(),
//                 randomChannelUpdatedEvent(),
//                 randomNewMessageEvent(),
//             )
//             val eventHandlerImpl: EventHandlerImpl = mock()
//             val sut = Fixture(testCoroutines.scope)
//                 .givenEventHandlerImpl(eventHandlerImpl)
//                 .givenSyncHistoryResult(Result(events))
//                 .get()
//
//             sut.replayEvents(cid)
//
//             verify(eventHandlerImpl).handleEventsInternal(events, isFromSync = true)
//         }
//
//     private class Fixture(private val coroutineScope: CoroutineScope) {
//         private val context: Context = mock()
//         private var chatClient: ChatClient = mock {
//             on(it.channel(any())) doReturn mock()
//         }
//         private var eventHandlerImpl: EventHandlerImpl = mock()
//
//         fun givenChatClient(chatClient: ChatClient): Fixture {
//             this.chatClient = chatClient
//
//             return this
//         }
//
//         fun givenSyncHistoryResult(result: Result<List<ChatEvent>>): Fixture {
//             whenever(chatClient.getSyncHistory(any(), any())).doAnswer {
//                 TestCall(result)
//             }
//             return this
//         }
//
//         fun givenEventHandlerImpl(eventHandlerImpl: EventHandlerImpl): Fixture {
//             this.eventHandlerImpl = eventHandlerImpl
//             return this
//         }
//
//         fun get(): ChatDomainImpl {
//             return ChatDomain.Builder(context, chatClient).build()
//                 .let { it as ChatDomainImpl }
//                 .apply {
//                     repos = RepositoryFacade.create(RepositoryFactory(mockDb(), randomUser()), mock(), mock())
//                     scope = coroutineScope
//                     eventHandler = eventHandlerImpl
//                 }
//         }
//     }
// }
